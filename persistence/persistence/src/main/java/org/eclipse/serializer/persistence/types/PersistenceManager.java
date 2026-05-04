package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
 * %%
 * Copyright (C) 2023 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import org.eclipse.serializer.collections.Set_long;
import org.eclipse.serializer.util.BufferSizeProviderIncremental;
import org.eclipse.serializer.util.X;

import java.nio.ByteOrder;
import java.time.Duration;
import java.util.function.Consumer;

import static org.eclipse.serializer.util.X.mayNull;
import static org.eclipse.serializer.util.X.notNull;


/**
 * Central runtime façade of the persistence layer. Combines every per-operation responsibility &mdash;
 * loading ({@link PersistenceRetrieving}), storing ({@link Persister}), id allocation
 * ({@link PersistenceObjectManager}), source/target wiring ({@link PersistenceSourceSupplier} +
 * {@link #target()}), and byte-order targeting ({@link ByteOrderTargeting}) &mdash; into a single,
 * application-facing handle that fronts a single backing storage.
 * <p>
 * Per logical operation, the manager hands out fresh {@link PersistenceLoader}s,
 * {@link PersistenceStorer}s, and {@link PersistenceRegisterer}s built from the configured creators; the
 * implicit storers used by the {@link Persister#store(Object)} family share an internal monitor so concurrent
 * callers serialize automatically. Long-running setups should usually create explicit storers via
 * {@link #createStorer()} (and friends) and manage their lifecycle themselves.
 * <p>
 * Maintains a set of {@link PersistenceLocalObjectIdRegistry} instances by delegating
 * {@link #registerLocalRegistry(PersistenceLocalObjectIdRegistry)} and
 * {@link #mergeEntries(PersistenceLocalObjectIdRegistry)} to the underlying object manager, so storers see
 * each other's pending id assignments before commit.
 *
 * @param <D> the persistence data type passed through to the loader/storer/source/target layer.
 *
 * @see Persister
 * @see PersistenceRetrieving
 * @see PersistenceObjectManager
 * @see PersistenceFoundation
 */
public interface PersistenceManager<D>
extends
PersistenceObjectManager<D>,
PersistenceRetrieving,
Persister,
PersistenceSourceSupplier<D>,
ByteOrderTargeting<PersistenceManager<D>>
{
	@Override
	public PersistenceStorer createLazyStorer();

	@Override
	public PersistenceStorer createStorer();

	@Override
	public PersistenceStorer createEagerStorer();

	/**
	 * Creates a fresh {@link PersistenceStorer} using the passed creator instead of the manager's
	 * configured one. The new storer is wired against this manager's components and reported to the
	 * registered {@link PersistenceStorer.CreationObserver}.
	 *
	 * @param storerCreator the creator to use.
	 *
	 * @return the newly created storer.
	 */
	public PersistenceStorer createStorer(PersistenceStorer.Creator<D> storerCreator);

	// manager methods //

	/**
	 * Creates a fresh {@link PersistenceLoader} for one logical load operation, wired against this
	 * manager's components.
	 *
	 * @return the newly created loader.
	 */
	public PersistenceLoader createLoader();

	/**
	 * Creates a fresh {@link PersistenceRegisterer} for one ahead-of-time id-assignment walk, wired against
	 * this manager's object manager and type handler manager.
	 *
	 * @return the newly created registerer.
	 */
	public PersistenceRegisterer createRegisterer();

	/**
	 * Updates the manager's metadata: applies the passed type dictionary, advances the highest assigned
	 * type id, and advances the highest assigned object id. Used at startup or whenever persistent
	 * metadata has been re-read from disk.
	 *
	 * @param typeDictionary  the type dictionary to install.
	 * @param highestTypeId   the new highest assigned type id.
	 * @param highestObjectId the new highest assigned object id.
	 */
	public void updateMetadata(PersistenceTypeDictionary typeDictionary, long highestTypeId, long highestObjectId);

	/**
	 * Convenience overload of {@link #updateMetadata(PersistenceTypeDictionary, long, long)} that leaves
	 * both id watermarks untouched (passed as {@code 0}, treated as "do not advance").
	 *
	 * @param typeDictionary the type dictionary to install.
	 */
	public default void updateMetadata(final PersistenceTypeDictionary typeDictionary)
	{
		this.updateMetadata(typeDictionary, 0, 0);
	}

	/**
	 * The underlying {@link PersistenceObjectRegistry}.
	 *
	 * @return the object registry.
	 */
	public PersistenceObjectRegistry objectRegistry();

	/**
	 * The currently installed {@link PersistenceTypeDictionary} as exposed by the type handler manager.
	 *
	 * @return the type dictionary.
	 */
	public PersistenceTypeDictionary typeDictionary();

	/**
	 * Read-only view on the persistent root set as currently known to the type handler manager.
	 *
	 * @return the roots view.
	 */
	public PersistenceRootsView viewRoots();

	@Override
	public long currentObjectId();

	@Override
	public PersistenceManager<D> updateCurrentObjectId(long currentObjectId);

	@Override
	public PersistenceSource<D> source();

	/**
	 * The {@link PersistenceTarget} this manager writes to.
	 *
	 * @return the target.
	 */
	public PersistenceTarget<D> target();

	/**
	 * Closes all ties to outside resources, if applicable. Typically used on shutdown to release the
	 * underlying source and target (e.g. file channels, network connections).
	 */
	public void close();



	/**
	 * Creates a fully wired {@link Default} manager. None of the arguments may be {@code null} except
	 * {@code persister}, which falls back to the manager itself when omitted.
	 *
	 * @param <D>                the persistence data type.
	 * @param objectRegistering  the object registry.
	 * @param objectManager      the object manager.
	 * @param typeHandlerManager the type handler manager.
	 * @param contextDispatcher  the context dispatcher.
	 * @param storerCreator      the storer creator.
	 * @param loaderCreator      the loader creator.
	 * @param registererCreator  the registerer creator.
	 * @param persister          the persister facade to expose to created storers and loaders, or
	 *                           {@code null} to expose the manager itself.
	 * @param target             the persistence target.
	 * @param source             the persistence source.
	 * @param storerObserver     the observer notified of every created storer.
	 * @param bufferSizeProvider the buffer size provider passed to created storers.
	 * @param targetByteOrder    the byte order produced and consumed by the manager.
	 *
	 * @return the newly created manager.
	 */
	public static <D> PersistenceManager<D> New(
		final PersistenceObjectRegistry          objectRegistering ,
		final PersistenceObjectManager<D>        objectManager     ,
		final PersistenceTypeHandlerManager<D>   typeHandlerManager,
		final PersistenceContextDispatcher<D>    contextDispatcher ,
		final PersistenceStorer.Creator<D>       storerCreator     ,
		final PersistenceLoader.Creator<D>       loaderCreator     ,
		final PersistenceRegisterer.Creator      registererCreator ,
		final Persister                          persister         ,
		final PersistenceTarget<D>               target            ,
		final PersistenceSource<D>               source            ,
		final PersistenceStorer.CreationObserver storerObserver ,
		final BufferSizeProviderIncremental      bufferSizeProvider,
		final ByteOrder                          targetByteOrder
	)
	{
		return new PersistenceManager.Default<>(
			notNull(objectRegistering) ,
			notNull(objectManager)     ,
			notNull(typeHandlerManager),
			notNull(contextDispatcher) ,
			notNull(storerCreator)     ,
			notNull(loaderCreator)     ,
			notNull(registererCreator) ,
			mayNull(persister)         , // non-null reference ensured by getEffectivePersister
			notNull(target)            ,
			notNull(source)            ,
			notNull(storerObserver)    ,
			notNull(bufferSizeProvider),
			notNull(targetByteOrder)
		);
	}

	/**
	 * Default {@link PersistenceManager}. Wires the configured object/type/source/target components together
	 * and serializes the implicit storers used by the {@link Persister#store(Object)} family on an internal
	 * monitor &mdash; explicit storers obtained via {@link #createStorer()} are caller-managed.
	 * <p>
	 * Implements {@link Unpersistable} because the manager itself must never be written to the persistent
	 * graph (it would re-create the entire persistence layer on read).
	 *
	 * @param <D> the persistence data type.
	 */
	public final class Default<D> implements PersistenceManager<D>, Unpersistable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// instance registration components //
		private final PersistenceObjectRegistry     objectRegistry   ;
		private final PersistenceObjectManager<D>   objectManager    ;
		private final PersistenceRegisterer.Creator registererCreator;

		// instance handling components //
		private final PersistenceTypeHandlerManager<D>   typeHandlerManager;
		private final PersistenceContextDispatcher<D>    contextDispatcher ;
		private final PersistenceStorer.Creator<D>       storerCreator     ;
		private final PersistenceLoader.Creator<D>       loaderCreator     ;
		private final PersistenceStorer.CreationObserver storerObserver    ;
		private final BufferSizeProviderIncremental      bufferSizeProvider;
		
		// callback linking components //
		private final Persister persister;

		// source and target //
		private final PersistenceSource<D> source;
		private final PersistenceTarget<D> target;
		
		private final ByteOrder targetByteOrder;
		
		/*
		 * To avoid race conditions in the implicitely created storer instances,
		 * their usage is serialized via mutex locking. If the suppressed parallelism
		 * is needed, explicitely created storers can be used, but must then be
		 * concurrency-managed by the using logic.
		 * Iterating the object graph and committing (i.e. I/O-flushing the collected
		 * bytes) is handled by different locks since the iteration is the concurrent-
		 * critical part but committing takes the vast majority of time (costly I/O).
		 */
		private final Object storeMutex = new Object();



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceObjectRegistry          objectRegistering ,
			final PersistenceObjectManager<D>        objectManager     ,
			final PersistenceTypeHandlerManager<D>   typeHandlerManager,
			final PersistenceContextDispatcher<D>    contextDispatcher ,
			final PersistenceStorer.Creator<D>       storerCreator     ,
			final PersistenceLoader.Creator<D>       loaderCreator     ,
			final PersistenceRegisterer.Creator      registererCreator ,
			final Persister                          persister         ,
			final PersistenceTarget<D>               target            ,
			final PersistenceSource<D>               source            ,
			final PersistenceStorer.CreationObserver storerObserver    ,
			final BufferSizeProviderIncremental      bufferSizeProvider,
			final ByteOrder                          targetByteOrder
		)
		{
			super();
			this.objectRegistry     = objectRegistering ;
			this.objectManager      = objectManager     ;
			this.typeHandlerManager = typeHandlerManager;
			this.contextDispatcher  = contextDispatcher ;
			this.storerCreator      = storerCreator     ;
			this.loaderCreator      = loaderCreator     ;
			this.registererCreator  = registererCreator ;
			this.persister          = persister         ;
			this.target             = target            ;
			this.source             = source            ;
			this.storerObserver     = storerObserver    ;
			this.bufferSizeProvider = bufferSizeProvider;
			this.targetByteOrder    = targetByteOrder   ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final ByteOrder getTargetByteOrder()
		{
			return this.targetByteOrder;
		}
						
		@Override
		public final PersistenceObjectRegistry objectRegistry()
		{
			return this.objectRegistry;
		}
		
		@Override
		public final PersistenceTypeDictionary typeDictionary()
		{
			return this.typeHandlerManager.typeDictionary();
		}

		@Override
		public final void consolidate()
		{
			this.objectRegistry.consolidate();
		}
		
		private final Persister getEffectivePersister()
		{
			return X.coalesce(this.persister, this);
		}
		
		private <S extends PersistenceStorer> S registerStorer(final S storer)
		{
			this.storerObserver.observeCreatedStorer(storer);
			return storer;
		}
				
		@Override
		public final PersistenceStorer createLazyStorer()
		{
			return this.registerStorer(this.storerCreator.createLazyStorer(
				this.contextDispatcher.dispatchTypeHandlerManager(this.typeHandlerManager),
				this.contextDispatcher.dispatchObjectManager(this.objectManager),
				this.getEffectivePersister(),
				this.target,
				this.bufferSizeProvider,
				this.persister
			));
		}
		
		@Override
		public final PersistenceStorer createStorer()
		{
			return this.registerStorer(this.storerCreator.createStorer(
				this.contextDispatcher.dispatchTypeHandlerManager(this.typeHandlerManager),
				this.contextDispatcher.dispatchObjectManager(this.objectManager),
				this.getEffectivePersister(),
				this.target,
				this.bufferSizeProvider,
				this.persister
			));
		}

		@Override
		public final PersistenceStorer createEagerStorer()
		{
			return this.registerStorer(this.storerCreator.createEagerStorer(
				this.contextDispatcher.dispatchTypeHandlerManager(this.typeHandlerManager),
				this.contextDispatcher.dispatchObjectManager(this.objectManager),
				this.getEffectivePersister(),
				this.target,
				this.bufferSizeProvider,
				this.persister
			));
		}

		@Override
		public final BatchStorer createBatchStorer(
			final BatchStorer.Controller controller   ,
			final Duration               checkInterval
		)
		{
			return this.registerStorer(this.storerCreator.createBatchStorer(
				this.contextDispatcher.dispatchTypeHandlerManager(this.typeHandlerManager),
				this.contextDispatcher.dispatchObjectManager(this.objectManager),
				this.getEffectivePersister(),
				this.target,
				this.bufferSizeProvider,
				this.persister,
				controller,
				checkInterval
			));
		}

		@Override
		public final PersistenceStorer createStorer(final PersistenceStorer.Creator<D> storerCreator)
		{
			return this.registerStorer(storerCreator.createStorer(
				this.contextDispatcher.dispatchTypeHandlerManager(this.typeHandlerManager),
				this.contextDispatcher.dispatchObjectManager(this.objectManager),
				this.getEffectivePersister(),
				this.target,
				this.bufferSizeProvider,
				this.persister
			));
		}

		@Override
		public final PersistenceRegisterer createRegisterer()
		{
			// undispatched (for now)
			return this.registererCreator.createRegisterer(
				this.objectManager,
				this.typeHandlerManager
			);
		}

		@Override
		public final long store(final Object object)
		{
			final long objectId;
			final PersistenceStorer storer = this.createStorer();
			
			synchronized(this.storeMutex)
			{
				objectId = storer.store(object);
				storer.commit();
			}

			return objectId;
		}
		
		@Override
		public final long[] storeAll(final Object... instances)
		{
			final long[] objectIds;
			final PersistenceStorer storer = this.createStorer();
			
			synchronized(this.storeMutex)
			{
				objectIds = storer.storeAll(instances);
				storer.commit();
			}

			return objectIds;
		}
		
		@Override
		public void storeAll(final Iterable<?> instances)
		{
			final PersistenceStorer storer = this.createStorer();
			
			synchronized(this.storeMutex)
			{
				storer.storeAll(instances);
				storer.commit();
			}
		}
		
		@Override
		public final long ensureObjectId(final Object object)
		{
			this.typeHandlerManager.ensureTypeHandler(object.getClass());
			return this.objectManager.ensureObjectId(object);
		}
		
		@Override
		public final <T> long ensureObjectId(
			final T                               object           ,
			final PersistenceObjectIdRequestor<D> objectIdRequestor,
			final PersistenceTypeHandler<D, T>    optionalHandler
		)
		{
			this.typeHandlerManager.ensureTypeHandler(object.getClass());
			return this.objectManager.ensureObjectId(object, objectIdRequestor, optionalHandler);
		}
		
		@Override
		public final <T> long ensureObjectIdGuaranteedRegister(
			final T                               object           ,
			final PersistenceObjectIdRequestor<D> objectIdRequestor,
			final PersistenceTypeHandler<D, T>    optionalHandler
		)
		{
			this.typeHandlerManager.ensureTypeHandler(object.getClass());
			return this.objectManager.ensureObjectIdGuaranteedRegister(object, objectIdRequestor, optionalHandler);
		}

		@Override
		public long currentObjectId()
		{
			return this.objectManager.currentObjectId();
		}

		@Override
		public final long lookupObjectId(final Object object)
		{
			return this.objectRegistry.lookupObjectId(object);
		}

		@Override
		public final Object lookupObject(final long objectId)
		{
			return this.objectRegistry.lookupObject(objectId);
		}
		
		@Override
		public final boolean registerLocalRegistry(final PersistenceLocalObjectIdRegistry<D> localRegistry)
		{
			return this.objectManager.registerLocalRegistry(localRegistry);
		}
		
		@Override
		public final void mergeEntries(final PersistenceLocalObjectIdRegistry<D> localRegistry)
		{
			this.objectManager.mergeEntries(localRegistry);
		}

		@Override
		public final Object get()
		{
			return this.createLoader().get();
		}

		@Override
		public final <C extends Consumer<Object>> C collect(final C collector, final long... objectIds)
		{
			return this.createLoader().collect(collector, objectIds);
		}

        @Override
        public final <C extends Consumer<Object>> C collect(final C collector, final Set_long objectIds)
        {
            return this.createLoader().collect(collector, objectIds);
        }

        @Override
		public final Object getObject(final long objectId)
		{
			final Object cachedInstance;
			if((cachedInstance = this.objectManager.lookupObject(objectId)) != null)
			{
				return cachedInstance;
			}
			return this.createLoader().getObject(objectId);
		}

		@Override
		public final PersistenceLoader createLoader()
		{
			return this.loaderCreator.createLoader(
				this.contextDispatcher.dispatchTypeHandlerLookup(this.typeHandlerManager),
				this.contextDispatcher.dispatchObjectRegistry(this.objectRegistry),
				this.getEffectivePersister(),
				this
			);
		}

		@Override
		public final PersistenceSource<D> source()
		{
			return this.source;
		}
		
		@Override
		public final PersistenceTarget<D> target()
		{
			return this.target;
		}
		
		@Override
		public synchronized void close()
		{
			this.target.closeTarget();
			this.source.closeSource();
		}

		@Override
		public synchronized PersistenceManager.Default<D> updateCurrentObjectId(
			final long currentObjectId
		)
		{
			this.objectManager.updateCurrentObjectId(currentObjectId);
			return this;
		}

		@Override
		public synchronized void updateMetadata(
			final PersistenceTypeDictionary typeDictionary ,
			final long                      highestTypeId  ,
			final long                      highestObjectId
		)
		{
			this.typeHandlerManager.update(typeDictionary, highestTypeId);
			this.updateCurrentObjectId(highestObjectId);
		}
		
		@Override
		public PersistenceRootsView viewRoots()
		{
			return this.typeHandlerManager.viewRoots();
		}

	}

}
