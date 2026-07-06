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

import static org.eclipse.serializer.util.X.notNull;

import java.lang.ref.WeakReference;

import org.eclipse.serializer.util.X;
import org.eclipse.serializer.chars.XChars;
import org.eclipse.serializer.collections.XArrays;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.reference.Swizzling;
import org.eclipse.serializer.util.Cloneable;

/**
 * Central object-id allocator: combines a {@link PersistenceObjectRegistry} with a
 * {@link PersistenceObjectIdProvider} and orchestrates the multi-storer assignment protocol that lets
 * concurrent storers agree on the id of a freshly encountered instance without committing it to the
 * registry until the surrounding store actually succeeds.
 * <p>
 * Each in-flight storer attaches its own {@link PersistenceLocalObjectIdRegistry} via
 * {@link #registerLocalRegistry(PersistenceLocalObjectIdRegistry)}. {@link #ensureObjectId} then walks the
 * three-step lookup &mdash; global registry, peer local registries, fresh allocation &mdash; under the
 * registry's monitor, so concurrent storers see consistent ids. On commit, the storer hands its accumulated
 * entries back via {@link #mergeEntries(PersistenceLocalObjectIdRegistry)} and they are validated and
 * folded into the global registry.
 * <p>
 * The {@link #Clone()} default produces a fully independent manager (cloning both the registry and the id
 * provider), used by {@link PersistenceContextDispatcher.LocalObjectRegistration} to give each channel its
 * own private id-assignment context.
 *
 * @param <D> the persistence data type passed through to the {@link PersistenceTypeHandler}.
 *
 * @see PersistenceObjectRegistry
 * @see PersistenceObjectIdProvider
 * @see PersistenceLocalObjectIdRegistry
 * @see PersistenceTypeManager
 */
public interface PersistenceObjectManager<D>
extends PersistenceSwizzlingLookup, PersistenceObjectIdHolder, Cloneable<PersistenceObjectManager<D>>
{
	/**
	 * Returns the object id associated with {@code object}, allocating a new one if the instance is not
	 * yet known. Convenience overload that uses a {@link PersistenceObjectIdRequestor#NoOp() no-op}
	 * requestor and no preferred type handler.
	 *
	 * @param object the instance to look up or register.
	 *
	 * @return the (possibly newly assigned) object id.
	 */
	public long ensureObjectId(Object object);

	/**
	 * Lazy-registration variant of {@link #ensureObjectId(Object)}: dispatches the
	 * {@link PersistenceObjectIdRequestor#registerLazyOptional registerLazyOptional} hook only when the
	 * instance is newly known, and the eager hook unconditionally. Use this from storers that should not
	 * re-process already-stored instances.
	 *
	 * @param <T>               the instance type.
	 * @param object            the instance to look up or register.
	 * @param objectIdRequestor the requestor receiving the registration hooks.
	 * @param optionalHandler   the type handler responsible for {@code object}, or {@code null} if not yet
	 *                          known.
	 *
	 * @return the (possibly newly assigned) object id.
	 */
	public <T> long ensureObjectId(
		T                               object           ,
		PersistenceObjectIdRequestor<D> objectIdRequestor,
		PersistenceTypeHandler<D, T>    optionalHandler
	);

	/**
	 * Eager-registration variant of {@link #ensureObjectId(Object)}: dispatches the
	 * {@link PersistenceObjectIdRequestor#registerGuaranteed registerGuaranteed} hook unconditionally,
	 * even when the instance is already globally known. Used by storers that must process every visited
	 * instance regardless.
	 *
	 * @param <T>               the instance type.
	 * @param object            the instance to look up or register.
	 * @param objectIdRequestor the requestor receiving the registration hook.
	 * @param optionalHandler   the type handler responsible for {@code object}, or {@code null} if not yet
	 *                          known.
	 *
	 * @return the (possibly newly assigned) object id.
	 */
	public <T> long ensureObjectIdGuaranteedRegister(
		T                               object           ,
		PersistenceObjectIdRequestor<D> objectIdRequestor,
		PersistenceTypeHandler<D, T>    optionalHandler
	);

	/**
	 * Triggers consolidation of the underlying object registry &mdash; e.g. removing orphan entries whose
	 * weak reference has been cleared. Delegates to {@link PersistenceObjectRegistry#consolidate()} under
	 * the registry's monitor.
	 */
	public void consolidate();

	@Override
	public long currentObjectId();

	@Override
	public PersistenceObjectManager<D> updateCurrentObjectId(long currentObjectId);

	/**
	 * Useful for {@link PersistenceContextDispatcher}.
	 * @return A Clone of this instance as described in {@link Cloneable}.
	 */
	@Override
	public default PersistenceObjectManager<D> Clone()
	{
		return Cloneable.super.Clone();
	}

	/**
	 * Registers a per-storer {@link PersistenceLocalObjectIdRegistry} so subsequent
	 * {@link #ensureObjectId} calls from peer storers can see (and reuse) the local id assignments.
	 *
	 * @param localRegistry the local registry to attach.
	 *
	 * @return {@code true} if the registry was newly attached, {@code false} if it was already attached.
	 *
	 * @throws PersistenceException if {@code localRegistry} does not declare this manager as its parent.
	 */
	public boolean registerLocalRegistry(PersistenceLocalObjectIdRegistry<D> localRegistry);

	/**
	 * Folds the entries of {@code localRegistry} into the global registry on storer commit. Entries are
	 * first validated against the registry and then registered.
	 *
	 * @param localRegistry the local registry whose entries to merge.
	 *
	 * @throws PersistenceException if {@code localRegistry} is not currently attached.
	 */
	public void mergeEntries(PersistenceLocalObjectIdRegistry<D> localRegistry);

	/**
	 * Returns the monitor used by this object manager for synchronization on the object registry.
	 * <p>
	 * Storer implementations may use this monitor as their own internal lock to make the canonical
	 * lock order ({@code objectRegistry -> storer-state}) structural rather than convention-based:
	 * any storer state mutation is always performed inside the registry monitor, so peer threads in
	 * {@code synchCheckLocalRegistries} (which already hold the registry) cannot face a lock-order
	 * inversion when reading a foreign storer's state.
	 * <p>
	 * Java synchronization is reentrant, so nested calls into {@link #ensureObjectId(Object)} and
	 * related methods do not self-deadlock.
	 */
	public Object objectRegistryMonitor();



	/**
	 * Creates a new {@link Default} manager combining {@code objectRegistry} and {@code oidProvider}.
	 *
	 * @param <D>            the persistence data type.
	 * @param objectRegistry the object registry; must not be {@code null}.
	 * @param oidProvider    the object id provider; must not be {@code null}.
	 *
	 * @return the newly created manager.
	 */
	public static <D> PersistenceObjectManager.Default<D> New(
		final PersistenceObjectRegistry   objectRegistry,
		final PersistenceObjectIdProvider oidProvider
	)
	{
		return new PersistenceObjectManager.Default<>(
			notNull(objectRegistry),
			notNull(oidProvider)
		);
	}

	/**
	 * Default {@link PersistenceObjectManager}. Synchronizes every public method on the underlying
	 * {@link PersistenceObjectRegistry} so concurrent storers serialize their id assignments consistently;
	 * tracks attached {@link PersistenceLocalObjectIdRegistry} instances through weak references so a
	 * forgotten storer does not pin its registry.
	 *
	 * @param <D> the persistence data type.
	 */
	public final class Default<D> implements PersistenceObjectManager<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceObjectRegistry   objectRegistry;
		private final PersistenceObjectIdProvider oidProvider   ;
		
		private WeakReference<PersistenceLocalObjectIdRegistry<D>>[] localRegistries = X.WeakReferences(1);
		
		private final PersistenceObjectIdRequestor<D> noOp = PersistenceObjectIdRequestor.NoOp();

		

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceObjectRegistry   objectRegistry,
			final PersistenceObjectIdProvider oidProvider
		)
		{
			super();
			this.objectRegistry = objectRegistry;
			this.oidProvider    = oidProvider   ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public PersistenceObjectManager.Default<D> Clone()
		{
			/*
			 * This basically turns the globally connected manager instance into a standalone clone.
			 * The oidProvider must support cloning, e.g. be transient instead of persisting into a
			 * single target location.
			 */
			synchronized(this.objectRegistry)
			{
				return new PersistenceObjectManager.Default<>(
					this.objectRegistry.Clone(),
					this.oidProvider.Clone()
				);
			}
		}
		
		@Override
		public void consolidate()
		{
			synchronized(this.objectRegistry)
			{
				this.objectRegistry.consolidate();
			}
		}

		@Override
		public Object objectRegistryMonitor()
		{
			return this.objectRegistry;
		}

		@Override
		public long lookupObjectId(final Object object)
		{
			synchronized(this.objectRegistry)
			{
				return this.objectRegistry.lookupObjectId(object);
			}
		}

		@Override
		public Object lookupObject(final long objectId)
		{
//			XDebug.debugln(XChars.systemString(this) + " looking up \n" + objectId
//				+ " -> " + XChars.systemString(this.objectRegistry.lookupObject(objectId))
//			);
			synchronized(this.objectRegistry)
			{
				return this.objectRegistry.lookupObject(objectId);
			}
		}

		@Override
		public final long ensureObjectId(final Object object)
		{
			return this.ensureObjectId(object, this.noOp, null);
		}
		
		@Override
		public <T> long ensureObjectId(
			final T                               object           ,
			final PersistenceObjectIdRequestor<D> objectIdRequestor,
			final PersistenceTypeHandler<D, T>    optionalHandler
		)
		{
			/*
			 * Three steps to determine an object's objectId which must be executed in exactely that order
			 * and under the protection of a lock on the global registry to enqueue all concurrent storers.
			 * 
			 * 1.) check if already globally known.
			 * 2.) check if already locally known in on of the other storers (= "local registries)"
			 * 3.) otherwise, provide and assign a new ObjectId.
			 */
			synchronized(this.objectRegistry)
			{
				long objectId;
				if(Swizzling.isNotProperId(objectId = this.objectRegistry.lookupObjectId(object)))
				{
					if(Swizzling.isNotProperId(objectId = this.synchCheckLocalRegistries(objectIdRequestor, object, optionalHandler)))
					{
						// see below about not globally registering the newly assigned objectId
						objectId = this.oidProvider.provideNextObjectId();
					}

					// lazy logic means only apply if not yet globally known (= something new / "store required").
					objectIdRequestor.registerLazyOptional(objectId, object, optionalHandler);
				}
				else
				{
					/*
					 * Already globally known means lazy storing logic skips the instance: only its objectId
					 * gets referenced in the store, the instance itself is not serialized. The requestor
					 * (storer) must get the chance to retain the skipped instance, otherwise the application
					 * can drop its last strong reference between store and commit, the registry's weak entry
					 * gets reaped and the storage GC deletes the entity while the chunk referencing it is
					 * not yet committed - the commit would then persist a dangling reference.
					 */
					objectIdRequestor.registerSkippedOptional(objectId, object, optionalHandler);
				}

				// eager logic means ALWAYS apply, even if already globally known (= "store full").
				objectIdRequestor.registerEagerOptional(objectId, object, optionalHandler);

				/* (06.12.2019 TM)NOTE:
				 * A new object<->id association may NOT be registered right away, since the storing (writing) logic
				 * afterwards might fail, which would leave an inconsistency (unstored entry that the next storer
				 * would assume to have already been stored) in the registry.
				 * The associations are kept locally in the storers and are merged into the registry in the commit
				 * upon success.
				 * In the exception case, the objectId is "lost", but that is not a problem since it is no different
				 * from a deleted entity. Unused objectIds can be "recycled" by a (future) objectId condensing utility
				 * functionality.
				 * And there's the type analysis exception, anyway which stops the whole process.
				 * See PersistenceTypeHandler#guaranteeInstanceViablity.
				 */
				
				return objectId;
			}
		}
		
		/**
		 * Variant of {@link #ensureObjectId(Object)} with guaranteed registering (effectively override-eager-logic)
		 * 
		 */
		@Override
		public <T> long ensureObjectIdGuaranteedRegister(
			final T                               object           ,
			final PersistenceObjectIdRequestor<D> objectIdRequestor,
			final PersistenceTypeHandler<D, T>    optionalHandler
			
		)
		{
			// see #ensureObjectId for explaining comments
			synchronized(this.objectRegistry)
			{
				long objectId;
				if(Swizzling.isNotProperId(objectId = this.objectRegistry.lookupObjectId(object)))
				{
					if(Swizzling.isNotProperId(objectId = this.synchCheckLocalRegistries(objectIdRequestor, object, optionalHandler)))
					{
						objectId = this.oidProvider.provideNextObjectId();
					}
				}
				
				// overriding "guaranteed registering" logic
				objectIdRequestor.registerGuaranteed(objectId, object, optionalHandler);
				
				return objectId;
			}
		}
		
		private <T> long synchCheckLocalRegistries(
			final PersistenceObjectIdRequestor<D> objectIdRequestor,
			final T                               instance         ,
			final PersistenceTypeHandler<D, T>    optionalHandler
		)
		{
			for(final WeakReference<PersistenceLocalObjectIdRegistry<D>> localRegistryEntry : this.localRegistries)
			{
				if(localRegistryEntry == null)
				{
					continue;
				}
				
				final PersistenceLocalObjectIdRegistry<D> localRegistry = localRegistryEntry.get();
				if(localRegistry == null || localRegistry == objectIdRequestor)
				{
					continue;
				}
				
				final long objectId;
				if(Swizzling.isProperId(objectId = localRegistry.lookupObjectId(instance, objectIdRequestor, optionalHandler)))
				{
					return objectId;
				}
			}
			
			return Swizzling.notFoundId();
		}
		
		private void synchInternalMergeEntries(final PersistenceLocalObjectIdRegistry<D> localRegistry)
		{
			localRegistry.iterateMergeableEntries(this.objectRegistry::validate);
			localRegistry.iterateMergeableEntries(this.objectRegistry::registerObject);
		}
		
		@Override
		public boolean registerLocalRegistry(final PersistenceLocalObjectIdRegistry<D> localRegistry)
		{
			if(localRegistry.parentObjectManager() != this)
			{
				throw new PersistenceException(
					PersistenceLocalObjectIdRegistry.class.getSimpleName()
					+ " " + XChars.systemString(localRegistry)
					+ " does not belong to this "
					+ PersistenceObjectManager.class.getSimpleName()
					+ " " + XChars.systemString(this)
				);
			}
			
			synchronized(this.objectRegistry)
			{
				final WeakReference<PersistenceLocalObjectIdRegistry<D>>[] localRegistries = this.localRegistries;
				if(isAlreadyRegistered(localRegistry, localRegistries))
				{
					return false;
				}

				for(int i = 0; i < localRegistries.length; i++)
				{
					if(localRegistries[i] == null || localRegistries[i].get() == null)
					{
						localRegistries[i] = X.WeakReference(localRegistry);
						return true;
					}
				}
				
				// very conservative enlargement since there should never be many registered localRegistries at once.
				this.localRegistries = XArrays.enlarge(localRegistries, localRegistries.length + 1);
				this.localRegistries[localRegistries.length] = X.WeakReference(localRegistry);
				
				return true;
			}
		}
		
		private static <D> boolean isAlreadyRegistered(
			final PersistenceLocalObjectIdRegistry<D>                  localRegistry ,
			final WeakReference<PersistenceLocalObjectIdRegistry<D>>[] localRegistries
		)
		{
			// no hash set required since there should never be a lot of entries, anyway.
			for(int i = 0; i < localRegistries.length; i++)
			{
				if(localRegistries[i] == null)
				{
					continue;
				}
				
				final PersistenceLocalObjectIdRegistry<D> registeredLocalRegistry = localRegistries[i].get();
				if(registeredLocalRegistry == null)
				{
					// some cleanup along the way
					localRegistries[i] = null;
					continue;
				}
				
				if(registeredLocalRegistry == localRegistry)
				{
					return true;
				}
			}
			
			return false;
		}

		@Override
		public void mergeEntries(final PersistenceLocalObjectIdRegistry<D> localRegistry)
		{
			synchronized(this.objectRegistry)
			{
				int emptySlotCount = 0;
				for(int i = 0; i < this.localRegistries.length; i++)
				{
					if(this.localRegistries[i] == null)
					{
						emptySlotCount++;
						continue;
					}
					
					final PersistenceLocalObjectIdRegistry<D> registeredLocalRegistry = this.localRegistries[i].get();
					if(registeredLocalRegistry == null)
					{
						// some cleanup along the way
						this.localRegistries[i] = null;
						emptySlotCount++;
						continue;
					}
					
					if(registeredLocalRegistry == localRegistry)
					{
						this.synchInternalMergeEntries(localRegistry);
						
						if(emptySlotCount > 2)
						{
							this.localRegistries = X.consolidateWeakReferences(this.localRegistries);
						}
						
						this.objectRegistry.cleanUp();
						// local registry cannot be removed here as it might be reused. Must be weakly-managed.
						return;
					}
				}
			}
			
			throw new PersistenceException(
				PersistenceLocalObjectIdRegistry.class.getSimpleName()
				+ " " + XChars.systemString(localRegistry)
				+ " not registered at this "
				+ PersistenceObjectManager.class.getSimpleName()
				+ " " + XChars.systemString(this)
			);
		}
		
		@Override
		public final long currentObjectId()
		{
			synchronized(this.objectRegistry)
			{
				return this.oidProvider.currentObjectId();
			}
		}

		@Override
		public PersistenceObjectManager<D> updateCurrentObjectId(final long currentObjectId)
		{
			synchronized(this.objectRegistry)
			{
				if(this.oidProvider.currentObjectId() >= currentObjectId)
				{
					return this;
				}
				this.oidProvider.updateCurrentObjectId(currentObjectId);
			}
			
			return this;
		}

	}

}
