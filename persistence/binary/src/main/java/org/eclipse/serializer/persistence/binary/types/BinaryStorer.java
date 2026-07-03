package org.eclipse.serializer.persistence.binary.types;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
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

import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.hashing.XHashing;
import org.eclipse.serializer.math.XMath;
import org.eclipse.serializer.persistence.types.*;
import org.eclipse.serializer.reference.ObjectSwizzling;
import org.eclipse.serializer.reference.Swizzling;
import org.eclipse.serializer.util.BufferSizeProviderIncremental;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.System.identityHashCode;
import static org.eclipse.serializer.chars.XChars.systemString;
import static org.eclipse.serializer.persistence.types.PersistenceLogging.STORER_CONTEXT;
import static org.eclipse.serializer.util.X.mayNull;
import static org.eclipse.serializer.util.X.notNull;
import static org.eclipse.serializer.util.logging.Logging.LazyArg;
import static org.eclipse.serializer.util.logging.Logging.LazyArgInContext;


/**
 * Binary-specific specialization of {@link PersistenceStorer}: collects entities into per-channel
 * {@link ChunksBuffer}s, assigns object ids via the surrounding {@link PersistenceObjectManager}, and
 * flushes the assembled data to a {@link PersistenceTarget} on {@code commit()}. Three flavors are
 * shipped:
 * <ul>
 *   <li>{@link Default} &mdash; lazy: child references are stored only when not yet known to the
 *       persistence context.</li>
 *   <li>{@link Eager} &mdash; force-stores every reference reachable from a stored root.</li>
 *   <li>{@link Batching} &mdash; lazy with explicit-root re-storage and time/size-driven background
 *       flushing for write-heavy workloads.</li>
 * </ul>
 * For finer-grained control, {@link PersistenceEagerStoringFieldEvaluator} can decide eagerness on a
 * per-field basis.
 *
 * @see PersistenceStorer
 * @see BinaryLoader
 * @see Creator
 */
public interface BinaryStorer extends PersistenceStorer, PersistenceStoringCallback
{
	@Override
	public PersistenceStorer reinitialize();

	@Override
	public PersistenceStorer reinitialize(long initialCapacity);

	@Override
	public PersistenceStorer ensureCapacity(long desiredCapacity);

	@Override
	public long currentCapacity();

	@Override
	public long maximumCapacity();
	
	/**
	 * Default implementation that stores referenced instances only if required (i.e. if they have no OID assigned yet,
	 * therefore have not been stored yet, therefore require to be stored). It can be seen as a "lazy" or "on demand"
	 * storer as opposed to{@link Eager}.<br>
	 * For a more differentiated solution between the two simple, but extreme strategies,
	 * see {@link PersistenceEagerStoringFieldEvaluator}.
	 *
	 */
	public class Default
	implements BinaryStorer, PersistenceStoreHandler<Binary>, PersistenceLocalObjectIdRegistry<Binary>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		protected final static Logger logger = Logging.getLogger(BinaryStorer.class);
		
		
		protected static int defaultSlotSize()
		{
			// why permanently occupy additional memory with fields and instances for constant values?
			return 1024; // anything below 1024 doesn't pay of
		}

		

		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final boolean                               switchByteOrder;
		private final PersistenceObjectManager<Binary>      objectManager  ;
		private final ObjectSwizzling                       objectRetriever;
		private final PersistenceTypeHandlerManager<Binary> typeManager    ;
		private final PersistenceTarget<Binary>             target         ;
		private final Persister                             persister      ;
		
		// channel hashing fields
		private final BufferSizeProviderIncremental bufferSizeProvider;
		private final int                           chunksHashRange   ;
		
		// cannot be final since every commit needs to pass an independent instance.
		private ChunksBuffer[] chunks;
		
		/*
		 * Concurrency / thread-safety concept:
		 * - The storer uses the parent ObjectManager's registry monitor (objectRegistryMonitor)
		 *   as its single internal lock. Using the registry as the storer's own lock collapses the
		 *   former two-level hierarchy (objectRegistry -> this.head) into one, which makes the lock
		 *   order structural instead of convention-based: any storer state mutation is performed
		 *   inside the registry monitor, so peer threads in synchCheckLocalRegistries (already
		 *   holding the registry) cannot face a lock-order inversion when reading a foreign
		 *   storer's state. Java synchronization is reentrant, so the recursion through
		 *   ensureObjectId during typeHandler.store(...) does not self-deadlock.
		 * - The head Item below is no longer used as a monitor; it is just the sentinel start of
		 *   the linked item chain.
		 * - A storer instance is never meant to be used in a mutating fashion by more than one
		 *   thread at any given moment. The locking is only there to synchronize reading accesses
		 *   from other storer instances of other threads.
		 */
		final Object objectRegistryMonitor;

		final   Item   head = new Item(null, 0L, null, null);
		private Item   tail;
		private Item[] hashSlots;
		private int    hashRange;
		private long   itemCount;
		
		private final BulkList<PersistenceCommitListener>  commitListeners = BulkList.New(0);
		
		private final BulkList<PersistenceObjectRegistrationListener> persistenceObjectRegistrationListener = BulkList.New(0);
		
		/*
		 * Calling store again while the loop in #storeGraph is already being executed (e.g. in a type handler's #store method)
		 * would result in the loop being executed again, causing instances to be persisted multiple times redundantly.
		 * So the state of already being processed must be recognized.
		 * The easiest way to do that is by using a trivial flag, since head-tail comparison logic gets tricky with #storeAll.
		 */
		protected boolean isProcessingItems;

		/*
		 * item hashing structures get initialized lazily for the following reasons:
		 * - the storer instance can commit (be cleared) and be reinitialized multiple times.
		 * - the storer instance can be explicitly initialized to a certain capacity.
		 * - clearing after committing can simply replace the array, easing garbage collection.
		 */

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final PersistenceObjectManager<Binary>      objectManager     ,
			final ObjectSwizzling                       objectRetriever   ,
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProviderIncremental         bufferSizeProvider,
			final int                                   channelCount      ,
			final boolean                               switchByteOrder   ,
			final Persister                             persister
		)
		{
			super();
			this.objectManager         = notNull(objectManager)               ;
			this.objectRegistryMonitor = objectManager.objectRegistryMonitor();
			this.objectRetriever       = notNull(objectRetriever)             ;
			this.typeManager           = notNull(typeManager)                 ;
			this.target                = notNull(target)                      ;
			this.bufferSizeProvider    = notNull(bufferSizeProvider)          ;
			this.chunksHashRange       =         channelCount - 1             ;
			this.switchByteOrder       =         switchByteOrder              ;
			this.persister             = mayNull(persister)                   ;

			this.defaultInitialize();
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final PersistenceObjectManager<Binary> parentObjectManager()
		{
			return this.objectManager;
		}

		@Override
		public final ObjectSwizzling getObjectRetriever()
		{
			return this.objectRetriever;
		}

		@Override
		public final long maximumCapacity()
		{
			return Long.MAX_VALUE;
		}

		@Override
		public final long currentCapacity()
		{
			synchronized(this.objectRegistryMonitor)
			{
				return this.hashSlots.length;
			}
		}

		@Override
		public final long size()
		{
			synchronized(this.objectRegistryMonitor)
			{
				return this.itemCount;
			}
		}
		
		@Override
		public Persister getPersister()
		{
			return this.persister;
		}

		protected ChunksBuffer synchLookupChunk(final long objectId)
		{
			return this.chunks[(int)(objectId & this.chunksHashRange)];
		}

		protected Binary synchComplete()
		{
			for(final ChunksBuffer chunk : this.chunks)
			{
				chunk.complete();
			}

			// all chunks know the array internally, so passing one means passing all. And there is always at least one.
			return this.chunks[0];
		}

		/**
		 * Returns the maximum accumulated byte count across all channels,
		 * including data in each channel's current (incomplete) buffer.
		 */
		protected long maxChannelByteCount()
		{
			long max = 0L;
			for(final ChunksBuffer chunk : this.chunks)
			{
				final long len = chunk.currentTotalLength();
				if(len > max)
				{
					max = len;
				}
			}
			return max;
		}

		@Override
		public PersistenceStorer reinitialize()
		{
			// does locking internally
			this.defaultInitialize();
			
			return this;
		}

		@Override
		public PersistenceStorer reinitialize(final long initialCapacity)
		{
			// does locking internally
			this.internalInitialize(XHashing.padHashLength(initialCapacity));
			
			return this;
		}

		@Override
		public void clear()
		{
			// clearing means just to reinitialize (with default values).
			this.reinitialize();
		}

		private void defaultInitialize()
		{
			// does locking internally
			this.internalInitialize(defaultSlotSize());
		}

		protected void internalInitialize(final int hashLength)
		{
			synchronized(this.objectRegistryMonitor)
			{
				this.itemCount = 0;
				this.hashSlots = new Item[hashLength];
				this.hashRange = hashLength - 1;

				// initializing/clearing item chain
				(this.tail = this.head).next = null;

				this.synchCreateStoringChunksBuffers();

				// must be clear instead of just reset to avoid memory leaks
				this.commitListeners.clear();
				this.persistenceObjectRegistrationListener.clear();
			}
		}
		
		protected boolean deduplicateChunkEntities()
		{
			return false;
		}

		private void synchCreateStoringChunksBuffers()
		{
			/* Note:
			 * May explicitly NOT clear (deallocate) the current (old/previous) chunks
			 * because in use with embedded (in-process) storage the chunks
			 * might still be used by the storage worker threads to update their entity caches.
			 * The released chunks must be handled by those threads if existing
			 * or ultimately by the garbage collector (or by some tailored additional logic)
			 */

			final boolean        dedup  = this.deduplicateChunkEntities();
			final ChunksBuffer[] chunks = this.chunks = new ChunksBuffer[this.chunksHashRange + 1];
			for(int i = 0; i < chunks.length; i++)
			{
				chunks[i] = this.switchByteOrder
					? ChunksBufferByteReversing.New(chunks, this.bufferSizeProvider, dedup)
					: ChunksBuffer.New(chunks, this.bufferSizeProvider, dedup)
				;
			}
		}

		@Override
		public PersistenceStorer ensureCapacity(final long desiredCapacity)
		{
			synchronized(this.objectRegistryMonitor)
			{
				if(this.currentCapacity() >= desiredCapacity)
				{
					return this;
				}
				this.synchRebuildStoreItems(XHashing.padHashLength(desiredCapacity));
			}

			return this;
		}
		
		@Override
		public <T> long apply(final T instance)
		{
			// concurrency: lookupOidLazyApplicable() and ensureObjectId() lock internally, the rest is thread-local

			if(instance == null)
			{
				return Swizzling.nullId();
			}

			final long objectIdLocal;
			if(Swizzling.isFoundId(objectIdLocal = this.lookupOidLazyApplicable(instance)))
			{
				// returning 0 is a valid case: an instance registered to be skipped by using the null-OID.
				return objectIdLocal;
			}

			return this.register(instance);
		}

		@Override
		public <T> long apply(final T instance, final PersistenceTypeHandler<Binary, T> localTypeHandler)
		{
			// concurrency: lookupOidLazyApplicable() and ensureObjectId() lock internally, the rest is thread-local

			if(instance == null)
			{
				return Swizzling.nullId();
			}

			final long objectIdLocal;
			if(Swizzling.isFoundId(objectIdLocal = this.lookupOidLazyApplicable(instance)))
			{
				// returning 0 is a valid case: an instance registered to be skipped by using the null-OID.
				return objectIdLocal;
			}

			return this.objectManager.ensureObjectId(instance, this, localTypeHandler);
		}
		
		@Override
		public final <T> long applyEager(final T instance)
		{
			// concurrency: lookupOid() and ensureObjectId() lock internally, the rest is thread-local
			
			if(instance == null)
			{
				return Swizzling.nullId();
			}
			
			/*
			 * "Eager" must still mean that if this storer has already stored the passed instance,
			 * it may not store it again. That would not only be data-wise redundant and unnecessary,
			 * but would also create infinite storing loops and overflows.
			 * So "eager" can only mean to not check the global registry, but it must still mean to check
			 * the local registry.
			 */
			final long objectIdLocal;
			if(Swizzling.isFoundId(objectIdLocal = this.lookupOid(instance)))
			{
				// returning 0 is a valid case: an instance registered to be skipped by using the null-OID.
				return objectIdLocal;
			}
			
			return this.registerGuaranteed(instance);
		}
		
		@Override
		public <T> long applyEager(final T instance, final PersistenceTypeHandler<Binary, T> localTypeHandler)
		{
			// concurrency: lookupOid() and ensureObjectId() lock internally, the rest is thread-local
			
			if(instance == null)
			{
				return Swizzling.nullId();
			}
			
			/*
			 * "Eager" must still mean that if this storer has already stored the passed instance,
			 * it may not store it again. That would not only be data-wise redundant and unnecessary,
			 * but would also create infinite storing loops and overflows.
			 * So "eager" can only mean to not check the global registry, but it must still mean to check
			 * the local registry.
			 */
			final long objectIdLocal;
			if(Swizzling.isFoundId(objectIdLocal = this.lookupOid(instance)))
			{
				// returning 0 is a valid case: an instance registered to be skipped by using the null-OID.
				return objectIdLocal;
			}
			
			return this.objectManager.ensureObjectIdGuaranteedRegister(instance, this, localTypeHandler);
		}
		
		/**
		 * Stores the passed instance (always) and interprets it as the root of a graph to be traversed and
		 * have its instances stored recursively if deemed necessary by the logic until all instance
		 * that can be reached by that logic have been handled.
		 * 
		 * @param root the root object of the graph
		 * @return the root's object id
		 */
		protected long internalStore(final Object root)
		{
			logger.debug(
				"Store request: {}({})",
				LazyArg(() -> systemString(root)),
				LazyArgInContext(STORER_CONTEXT, root)
			);
			
			/* (03.12.2019 TM)NOTE:
			 * Special case logic to handle explicitly passed instances:
			 * - if already handled by this storer, don't handle again.
			 * Apart from that:
			 * - register to be handled in any case, even if already registered in the object registry.
			 * - handle all registered graph objects recursively (but transformed to an iteration).
			 * Note that this is NOT the same as apply, which does NOT store if the instance is already registry-known.
			 */
			long rootOid;
			if(Swizzling.isFoundId(rootOid = this.lookupOid(root)))
			{
				return rootOid;
			}
			
			// initial registration. After that, storing adds via recursion the graph and processing items iteratively.
			rootOid = this.registerGuaranteed(notNull(root));

			// repeatedly calling #store to add an instance to the item chain is fine, but processing may only happen once.
			if(!this.isProcessingItems)
			{
				try
				{
					this.isProcessingItems = true;
					this.processItems();
				}
				finally
				{
					this.isProcessingItems = false;
				}
			}

			return rootOid;
		}
		
		protected void processItems()
		{
			// process and collect required instances in item chain (graph recursion transformed to iteration)
			for(Item item = this.tail; item != null; item = item.next)
			{
				// locks internally. May not lock the whole loop or other storers can't look up concurrently.
				this.storeItem(item);
			}
		}
		
		protected final void storeItem(final Item item)
		{
			logger.debug(
				"Storing     {}: {}({})",
				item.oid,
				LazyArg(() -> systemString(item.instance)),
				LazyArgInContext(STORER_CONTEXT, item.instance)
			);

			/*
			 * Look up the chunk under the storer monitor (== objectRegistry) because chunks[]
			 * is replaced atomically by internalInitialize(). The type handler call is done
			 * outside the monitor on purpose: typeHandler.store recurses through apply() ->
			 * register() -> objectManager.ensureObjectId, which re-acquires the same monitor.
			 * Holding it across the recursive walk would needlessly serialize the entire graph
			 * traversal of every storer; reentrant acquisitions are cheap, contended ones are not.
			 * ChunksBuffer is per-storer and only accessed by the owning thread, so no lock is
			 * needed for the actual write.
			 */
			final ChunksBuffer chunk;
			synchronized(this.objectRegistryMonitor)
			{
				chunk = this.synchLookupChunk(item.oid);
			}
			item.typeHandler.store(chunk, item.instance, item.oid, this);
		}

		@Override
		public final long store(final Object root)
		{
			return this.internalStore(root);
		}

		@Override
		public final long[] storeAll(final Object... instances)
		{
			final long[] oids = new long[instances.length];
			for(int i = 0; i < instances.length; i++)
			{
				oids[i] = this.internalStore(instances[i]);
			}
			return oids;
		}
		
		@Override
		public void storeAll(final Iterable<?> instances)
		{
			for(final Object instance : instances)
			{
				this.internalStore(instance);
			}
		}
		
		@Override
		public void iterateMergeableEntries(final PersistenceAcceptor iterator)
		{
			synchronized(this.objectRegistryMonitor)
			{
				for(Item e = this.head; (e = e.next) != null;)
				{
					// skip items are local only and not valid for being visible to (i.e. merged into) global context
					if(isSkipItem(e))
					{
						continue;
					}

					// mergeable entry
					iterator.accept(e.oid, e.instance);
				}
			}
		}
		
		@Override
		public void registerCommitListener(final PersistenceCommitListener listener)
		{
			this.commitListeners.add(listener);
		}

		@Override
		public void registerRegistrationListener(final PersistenceObjectRegistrationListener listener)
		{
			this.persistenceObjectRegistrationListener.add(listener);
		}
		
		protected void notifyCommitListeners()
		{
			this.commitListeners.iterate(PersistenceCommitListener::onAfterCommit);
		}

		@Override
		public Object commit()
		{
			logger.debug(
				"Committing {} object(s)",
				LazyArg(this::size)   // use lazy here, #size() locks
			);

			// isEmpty locks internally
			if(!this.isEmpty())
			{
				// must validate here, too, in case the WriteController disabled writing during the storer's existence.
				this.target.validateIsStoringEnabled();

				final Binary writeData;
				synchronized(this.objectRegistryMonitor)
				{
					this.typeManager.checkForPendingRootInstances();
					this.typeManager.checkForPendingRootsStoring(this);
					writeData = this.synchComplete();
				}

				// very costly IO-operation does not need to occupy the lock
				this.target.write(writeData);

				/*
				 * mergeEntries acquires the object registry, which is the same monitor we use as
				 * the storer's own lock. iterateMergeableEntries (called inside) reacquires it
				 * recursively — fine, Java synchronization is reentrant. clearStorePendingRoots
				 * is a plain field write on the type manager, called only from this thread.
				 */
				this.typeManager.clearStorePendingRoots();
				this.objectManager.mergeEntries(this);
			}
			this.notifyCommitListeners();
			this.clear();

			logger.debug("Commit finished successfully");

			// not used (yet?)
			return null;
		}
		
		public final long lookupOid(final Object object)
		{
			synchronized(this.objectRegistryMonitor)
			{
				for(Item e = this.hashSlots[identityHashCode(object) & this.hashRange]; e != null; e = e.link)
				{
					/*
					 * Pin items are pure GC-protection entries for skipped referents and must be invisible
					 * here: a lookup hit means "this storer already handled the instance", which would make
					 * explicit stores (internalStore) and eager applies silently no-ops for merely
					 * referenced instances.
					 */
					if(e.instance == object && !isPinItem(e))
					{
						return e.oid;
					}
				}

				// returning 0 is a valid case: an instance registered to be skipped by using the null-OID.
				return Swizzling.notFoundId();
			}
		}

		/**
		 * Variant of {@link #lookupOid(Object)} for lazy apply logic ONLY: pin items are considered
		 * a valid local hit here, since a pin records the instance's globally registered objectId -
		 * exactly the value {@code ensureObjectId} would return for it. This turns the pin into a
		 * local cache and saves the global registry round trip for repeated references to already
		 * stored instances.
		 * <p>
		 * Must NEVER be used by explicit stores ({@code internalStore}) or eager applies: for those,
		 * a local hit means "already handled by this storer" and a pin hit would silently suppress
		 * the required (re-)serialization (see {@link PinItem} and {@link #lookupOid(Object)}).
		 */
		private long lookupOidLazyApplicable(final Object object)
		{
			synchronized(this.objectRegistryMonitor)
			{
				for(Item e = this.hashSlots[identityHashCode(object) & this.hashRange]; e != null; e = e.link)
				{
					if(e.instance == object)
					{
						return e.oid;
					}
				}

				// returning 0 is a valid case: an instance registered to be skipped by using the null-OID.
				return Swizzling.notFoundId();
			}
		}

		private static boolean isSkipItem(final Item item)
		{
			return item.typeHandler == null;
		}

		private static boolean isPinItem(final Item item)
		{
			return item instanceof PinItem;
		}
		
		@Override
		public final <T> long lookupObjectId(
			final T                                    object           ,
			final PersistenceObjectIdRequestor<Binary> objectIdRequestor,
			final PersistenceTypeHandler<Binary, T>    optionalHandler
		)
		{
			synchronized(this.objectRegistryMonitor)
			{
				for(Item e = this.hashSlots[identityHashCode(object) & this.hashRange]; e != null; e = e.link)
				{
					if(e.instance == object)
					{
						if(isSkipItem(e))
						{
							// skip-entry for this storer, so it can offer nothing to the receiver.
							break;
						}
						
						// found a local entry in the current storer, transfer object<->id association to the receiver.
						objectIdRequestor.registerGuaranteed(e.oid, object, optionalHandler);
						return e.oid;
					}
				}
				
				return Swizzling.notFoundId();
			}
		}
		

		@Override
		public final <T> void registerGuaranteed(
			final long                              objectId       ,
			final T                                 instance       ,
			final PersistenceTypeHandler<Binary, T> optionalHandler
		)
		{
			logger.debug(
				"Registering {}: {}({})",
				objectId,
				LazyArg(() -> systemString(instance)),
				LazyArgInContext(STORER_CONTEXT, instance)
			);
			
			this.persistenceObjectRegistrationListener.forEach(c -> c.onObjectRegistration(objectId, instance));

			synchronized(this.objectRegistryMonitor)
			{
				// ensure handler (or fail if type is not persistable) before ensuring an OID.
				final PersistenceTypeHandler<Binary, ? super T> typeHandler = optionalHandler != null
					? optionalHandler
					: this.typeManager.ensureTypeHandler(instance)
				;
				final Item item = this.synchRegisterObjectId(instance, typeHandler, objectId);
				this.tail = this.tail.next = item;
			}
		}
		
		@Override
		public <T> void registerLazyOptional(
			final long                              objectId       ,
			final T                                 instance       ,
			final PersistenceTypeHandler<Binary, T> optionalHandler
		)
		{
			// default is lazy logic.
			this.registerGuaranteed(objectId, instance, optionalHandler);
		}
		
		@Override
		public <T> void registerEagerOptional(
			final long                              objectId       ,
			final T                                 instance       ,
			final PersistenceTypeHandler<Binary, T> optionalHandler
		)
		{
			// default is lazy logic, so no-op
		}

		@Override
		public <T> void registerSkippedOptional(
			final long                              objectId       ,
			final T                                 instance       ,
			final PersistenceTypeHandler<Binary, T> optionalHandler
		)
		{
			/*
			 * Pin the skipped referent: its objectId gets written into this storer's chunk, but since the
			 * instance is already globally known, no Item would be created and this storer would hold no
			 * strong reference to it. If the application drops its last strong reference between store and
			 * commit, the object registry's weak entry gets reaped and the storage GC deletes the entity
			 * while the chunk referencing it is not yet committed - the commit would then persist a
			 * dangling reference (missing entity on later loads).
			 * A PinItem holds the instance strongly until commit()/clear(), keeping its object registry
			 * entry populated so the GC's live-objectId safety net protects the entity. Pin items are
			 * never part of the item chain (not serialized, not merged) and are invisible to lookupOid,
			 * so explicit stores and eager applies of the instance still get processed normally.
			 */
			synchronized(this.objectRegistryMonitor)
			{
				// any existing local entry (regular, skip or pin) already holds the instance strongly
				for(Item e = this.hashSlots[identityHashCode(instance) & this.hashRange]; e != null; e = e.link)
				{
					if(e.instance == instance)
					{
						return;
					}
				}

				if(++this.itemCount >= this.hashRange)
				{
					this.synchRebuildStoreItems();
				}

				this.hashSlots[identityHashCode(instance) & this.hashRange] =
					new PinItem(
						instance,
						objectId,
						this.hashSlots[identityHashCode(instance) & this.hashRange]
					)
				;
			}
		}

		protected final long register(final Object instance)
		{
			/* Note:
			 * - ensureObjectId may never be called under a storer lock or a deadlock might happen!
			 * - depending on implementation lazy or eager callback, the other variant is a no-op respectively
			 */
			return this.objectManager.ensureObjectId(instance, this, null);
		}
		
		protected final long registerGuaranteed(final Object instance)
		{
			/* Note:
			 * - ensureObjectId may never be called under a storer lock or a deadlock might happen!
			 * - calls back to #register(long, Object), guaranteeing the registration
			 */
			return this.objectManager.ensureObjectIdGuaranteedRegister(instance, this, null);
		}
		
		
		@Override
		public final boolean skipMapped(final Object instance, final long objectId)
		{
			return this.internalSkip(instance, objectId);
		}

		@Override
		public final boolean skip(final Object instance)
		{
			final long foundObjectId = this.objectManager.lookupObjectId(instance);
			
			// not found means store as null. Lookup will never return 0
			if(Swizzling.isNotFoundId(foundObjectId))
			{
				return this.skipNulled(instance);
			}
			
			return this.internalSkip(instance, foundObjectId);
		}
		
		@Override
		public final boolean skipNulled(final Object instance)
		{
			return this.internalSkip(instance, Swizzling.nullId());
		}
		
		final boolean internalSkip(final Object instance, final long objectId)
		{
			synchronized(this.objectRegistryMonitor)
			{
				// lookup returns -1 on failure, so 0 is a valid lookup result. Main reason for -1 vs. 0 distinction!
				if(Swizzling.isNotFoundId(this.lookupOid(instance)))
				{
					// only register if not found locally, of course
					this.synchRegisterObjectId(instance, null, objectId);
					return true;
				}

				// already locally present (found), do nothing.
				return false;
			}
		}
		
		@SuppressWarnings("unchecked")
		public final <T> Item synchRegisterObjectId(
			final T                                         instance   ,
			final PersistenceTypeHandler<Binary, ? super T> typeHandler,
			final long                                      objectId
		)
		{
			if(++this.itemCount >= this.hashRange)
			{
				this.synchRebuildStoreItems();
			}

			return this.hashSlots[identityHashCode(instance) & this.hashRange] =
				new Item(
					instance,
					objectId,
					(PersistenceTypeHandler<Binary, Object>)typeHandler,
					this.hashSlots[identityHashCode(instance) & this.hashRange]
				)
			;
		}

		public final void synchRebuildStoreItems()
		{
			this.synchRebuildStoreItems(this.hashSlots.length * 2);
		}

		public final void synchRebuildStoreItems(final int newLength)
		{
			// more or less academic check for more than 1 billion entries
			if(this.hashSlots.length >= XMath.highestPowerOf2_int())
			{
				return; // note that aborting rebuild does not ruin anything.
			}

			final int newRange;
			final Item[] newSlots = new Item[(newRange = newLength - 1) + 1];
			for(Item entry : this.hashSlots)
			{
				for(Item next; entry != null; entry = next)
				{
					next = entry.link;
					entry.link = newSlots[identityHashCode(entry.instance) & newRange];
					newSlots[identityHashCode(entry.instance) & newRange] = entry;
				}
			}
			this.hashSlots = newSlots;
			this.hashRange = newRange;
		}



		@Override
		public long store(final Object instance, final long objectId)
		{
			return this.internalStore(instance, objectId);
		}
		
		protected final long internalStore(final Object root, final long objectId)
		{
			logger.debug(
				"Store request: {}({}) with ID {}",
				LazyArg(() -> systemString(root)),
				LazyArgInContext(STORER_CONTEXT, root),
				objectId
			);
			
			/* (03.12.2019 TM)NOTE:
			 * Special case logic to handle explicitly passed instances:
			 * - if already handled by this storer, don't handle again.
			 * Apart from that:
			 * - register to be handled in any case, even if already registered in the object registry.
			 * - handle all registered graph objects recursively (but transformed to an iteration).
			 * Note that this is NOT the same as apply, which does NOT store if the instance is already registry-known.
			 */
			long rootOid;
			if(Swizzling.isFoundId(rootOid = this.lookupOid(root)))
			{
				return rootOid;
			}
			
			// initial registration. After that, storing adds via recursion the graph and processing items iteratively.
			//rootOid = this.registerGuaranteed(notNull(root));
			
			this.registerGuaranteed(objectId, root, null);

			// repeatedly calling #store to add an instance to the item chain is fine, but processing may only happen once.
			if(!this.isProcessingItems)
			{
				try
				{
					this.isProcessingItems = true;
					this.processItems();
				}
				finally
				{
					this.isProcessingItems = false;
				}
			}

			return objectId;
		}



		@Override
		public void forceRootStore(final PersistenceRoots pendingStoreRoot) {
			
			logger.debug("Storing updated root");
			
			long rootOid;
			if(Swizzling.isFoundId(rootOid = this.lookupOid(pendingStoreRoot)))
			{
				this.registerGuaranteed(rootOid, pendingStoreRoot, null);
			}
			else
			{
				rootOid = this.registerGuaranteed(notNull(pendingStoreRoot));
			}
									
			if(!this.isProcessingItems)
			{
				try
				{
					this.isProcessingItems = true;
					this.processItems();
				}
				finally
				{
					this.isProcessingItems = false;
				}
			}
		}
						
	}
	
	/**
	 * Identical to {@link Default}, but stores every referenced instance eagerly.<br>
	 * For a more differentiated solution between the two simple, but extreme strategies,
	 * see {@link PersistenceEagerStoringFieldEvaluator}.<br>
	 * 
	 * 
	 */
	public class Eager extends Default
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Eager(
			final PersistenceObjectManager<Binary>      objectManager     ,
			final ObjectSwizzling                       objectRetriever   ,
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProviderIncremental         bufferSizeProvider,
			final int                                   channelCount      ,
			final boolean                               switchByteOrder   ,
			final Persister                             persister
		)
		{
			super(
				objectManager     ,
				objectRetriever   ,
				typeManager       ,
				target            ,
				bufferSizeProvider,
				channelCount      ,
				switchByteOrder   ,
				persister
			);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final <T> long apply(final T instance)
		{
			// for a "full" graph storing strategy, the logic is simply to store everything forced.
			return this.applyEager(instance);
		}
		
		@Override
		public <T> void registerLazyOptional(
			final long                              objectId       ,
			final T                                 instance       ,
			final PersistenceTypeHandler<Binary, T> optionalHandler
		)
		{
			// default is eager logic, so no-op
		}
		
		@Override
		public <T> void registerEagerOptional(
			final long                              objectId       ,
			final T                                 instance       ,
			final PersistenceTypeHandler<Binary, T> optionalHandler
		)
		{
			// default is eager logic.
			this.registerGuaranteed(objectId, instance, optionalHandler);
		}
		
	}

	/**
	 * A lazy storer with batching support designed for write-heavy operations.
	 * <p>
	 * Explicitly passed root instances are always re-serialized even if already registered,
	 * ensuring mutable objects (e.g. collections) capture their current state. Child objects
	 * use lazy semantics and are only stored if not yet known to the persistence context.
	 * <p>
	 * Accumulated store operations are committed in batches controlled by a
	 * {@link BatchStorer.Controller}. A background daemon thread periodically checks
	 * for pending flushes.
	 */
	public final class Batching extends Default implements BatchStorer
	{
		/**
		 * Safety threshold: ~1.75 GB. Leaves 256 MB headroom below the 2^31 hard limit
		 * to account for data written by the current store() call completing after the check.
		 */
		private static final long MAX_CHANNEL_BYTES_BEFORE_FLUSH =
			Integer.MAX_VALUE - (256L * 1024 * 1024);

		private final BatchStorer.Controller   controller        ;
		private final ScheduledExecutorService scheduler         ;
		private long                           pendingSinceNanos ;
		private volatile boolean               closed            ;

		/*
		 * commitLock serialises concurrent commit() calls (background flush thread vs. explicit
		 * user flush/commit) and the storeItem-into-chunks vs. commit's synchComplete/clear pair.
		 * Without it, a background flush could complete & clear chunks while a store thread is
		 * still writing items into them.
		 *
		 * Lock order: commitLock -> objectRegistryMonitor (the storer's only inner lock).
		 * Nothing acquires objectRegistryMonitor first and then waits on commitLock, so the order
		 * is one-way and cannot cycle.
		 */
		private final Object commitLock = new Object();

		Batching(
			final PersistenceObjectManager<Binary>      objectManager     ,
			final ObjectSwizzling                       objectRetriever   ,
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProviderIncremental         bufferSizeProvider,
			final int                                   channelCount      ,
			final boolean                               switchByteOrder   ,
			final Persister                             persister         ,
			final BatchStorer.Controller                controller        ,
			final Duration                              checkInterval
		)
		{
			super(
				objectManager     ,
				objectRetriever   ,
				typeManager       ,
				target            ,
				bufferSizeProvider,
				channelCount      ,
				switchByteOrder   ,
				persister
			);
			this.controller = notNull(controller);

			final long millis = notNull(checkInterval).toMillis();
			if(millis <= 0)
			{
				throw new IllegalArgumentException(
					"checkInterval must be > 0ms, was " + checkInterval
				);
			}

			// Validate before creating the executor so a bad checkInterval does not
			// leak a daemon thread (the constructor would throw after start()).
			this.scheduler = Executors.newSingleThreadScheduledExecutor(r ->
			{
				final Thread t = new Thread(r, "batch-storer-flush");
				t.setDaemon(true);
				return t;
			});
			this.scheduler.scheduleWithFixedDelay(
				this::backgroundFlush,
				millis,
				millis,
				TimeUnit.MILLISECONDS
			);
		}

		@Override
		protected boolean deduplicateChunkEntities()
		{
			return true;
		}

		@Override
		public long store(final Object instance, final long objectId)
		{
			logger.debug(
				"Store request: {}({}) with ID {}",
				LazyArg(() -> systemString(instance)),
				LazyArgInContext(STORER_CONTEXT, instance),
				objectId
			);

			synchronized(this.commitLock)
			{
				if(this.closed)
				{
					throw new IllegalStateException("BatchStorer is already closed.");
				}
				this.registerGuaranteed(objectId, instance, null);

				if(!this.isProcessingItems)
				{
					try
					{
						this.isProcessingItems = true;
						super.processItems();
					}
					finally
					{
						this.isProcessingItems = false;
					}
				}
			}

			this.optFlush();

			return objectId;
		}

		@Override
		public void storeAll(final Iterable<?> instances)
		{
			// No outer lock: each internalStore() call handles its own commitLock acquisition
			// and ends with optFlush() outside the lock.
			super.storeAll(instances);
		}

		@Override
		public void forceRootStore(final PersistenceRoots pendingStoreRoot)
		{
			synchronized(this.commitLock)
			{
				if(this.closed)
				{
					throw new IllegalStateException("BatchStorer is already closed.");
				}
				super.forceRootStore(pendingStoreRoot);
			}
			this.optFlush();
		}

		@Override
		public void registerCommitListener(final PersistenceCommitListener listener)
		{
			// super stores into a non-thread-safe BulkList; serialise the add against concurrent
			// listener registrations and against commit's notifyCommitListeners iteration.
			synchronized(this.objectRegistryMonitor)
			{
				super.registerCommitListener(listener);
			}
		}

		@Override
		public void registerRegistrationListener(final PersistenceObjectRegistrationListener listener)
		{
			synchronized(this.objectRegistryMonitor)
			{
				super.registerRegistrationListener(listener);
			}
		}

		@Override
		protected long internalStore(final Object root)
		{
			logger.debug(
				"Store request: {}({})",
				LazyArg(() -> systemString(root)),
				LazyArgInContext(STORER_CONTEXT, root)
			);

			/*
			 * commitLock serialises registration + item processing against commit/clear so that
			 * a background flush cannot drain & clear chunks while items are still being written
			 * into them. Inside, lookups and registrations briefly acquire objectRegistryMonitor
			 * (the storer's only inner lock); recursion through ensureObjectId reacquires it.
			 *
			 * Lock order: commitLock -> objectRegistryMonitor.
			 * optFlush() runs after the lock is released so internalFlush()'s commit can take
			 * commitLock without holding it across the optFlush decision.
			 */
			final long rootOid;
			synchronized(this.commitLock)
			{
				if(this.closed)
				{
					throw new IllegalStateException("BatchStorer is already closed.");
				}

				final long localOid = this.lookupOid(root);

				/*
				 * Always re-register root to capture current state (BatchStorer contract).
				 */
				if(Swizzling.isFoundId(localOid))
				{
					this.registerGuaranteed(localOid, root, null);
					rootOid = localOid;
				}
				else
				{
					rootOid = this.registerGuaranteed(notNull(root));
				}

				if(!this.isProcessingItems)
				{
					try
					{
						this.isProcessingItems = true;
						super.processItems();
					}
					finally
					{
						this.isProcessingItems = false;
					}
				}
			}

			this.optFlush();

			return rootOid;
		}

		@Override
		protected void processItems()
		{
			/*
			 * commitLock guarantees mutual exclusion between processItems (which writes to chunks
			 * via storeItem) and commit (which calls synchComplete/clear on the same chunks).
			 * Without this, the background flush thread could commit/clear while a store thread
			 * is still writing items, corrupting chunk state.
			 *
			 * The internalStore / store(Object,long) / forceRootStore overrides above already
			 * call this method while holding commitLock; the recursive acquisition here is harmless
			 * and keeps the invariant explicit for any other call site that may go through
			 * super.forceRootStore -> this.processItems.
			 */
			synchronized(this.commitLock)
			{
				super.processItems();
			}
		}

		@Override
		public void clear()
		{
			synchronized(this.objectRegistryMonitor)
			{
				super.clear();
				this.pendingSinceNanos = 0L;
			}
		}

		@Override
		public Object commit()
		{
			/*
			 * commitLock serialises concurrent commits (background flush thread vs. explicit
			 * user call) and excludes any in-flight processItems on the same storer. super.commit()
			 * acquires the storer's inner monitor (objectRegistryMonitor) only for the brief
			 * synchComplete and the mergeEntries calls; the IO write happens between them with
			 * no lock held.
			 */
			synchronized(this.commitLock)
			{
				return super.commit();
			}
		}

		@Override
		public void flush()
		{
			this.internalFlush();
		}

		@Override
		public boolean hasPendingData()
		{
			synchronized(this.objectRegistryMonitor)
			{
				return !this.isEmpty();
			}
		}

		@Override
		public void close()
		{
			synchronized(this.objectRegistryMonitor)
			{
				if(this.closed)
				{
					return;
				}
				this.closed = true;
			}

			this.scheduler.shutdown();
			try
			{
				if(!this.scheduler.awaitTermination(5, TimeUnit.SECONDS))
				{
					this.scheduler.shutdownNow();
					if(!this.scheduler.awaitTermination(5, TimeUnit.SECONDS))
					{
						logger.warn("Background flush thread did not terminate within timeout");
					}
				}
			}
			catch(final InterruptedException e)
			{
				this.scheduler.shutdownNow();
				Thread.currentThread().interrupt();
			}

			// Final flush takes commitLock internally via commit(); must not be held here.
			if(!this.isEmpty())
			{
				this.internalFlush();
			}
		}

		private void backgroundFlush()
		{
			try
			{
				this.optFlush();
			}
			catch(final Exception e)
			{
				logger.error("Background flush failed", e);
			}
		}

		private void optFlush()
		{
			/*
			 * Decide whether to flush under the storer's inner monitor (to read size and
			 * byte-count atomically), then perform the actual flush outside the lock.
			 * internalFlush() takes commitLock internally; not holding any lock here keeps
			 * the lock order single-direction (commitLock -> objectRegistryMonitor).
			 */
			final boolean doFlush;
			synchronized(this.objectRegistryMonitor)
			{
				if(this.closed || this.isEmpty())
				{
					return;
				}

				final long nowNanos = System.nanoTime();
				if(this.pendingSinceNanos == 0L)
				{
					this.pendingSinceNanos = nowNanos;
				}

				final long maxBytes = this.maxChannelByteCount();
				if(maxBytes >= MAX_CHANNEL_BYTES_BEFORE_FLUSH)
				{
					logger.debug(
						"Forcing flush: channel byte count {} exceeds safety threshold {}",
						maxBytes,
						MAX_CHANNEL_BYTES_BEFORE_FLUSH
					);
					doFlush = true;
				}
				else
				{
					doFlush = this.controller.shouldFlush(
						this.size(),
						TimeUnit.NANOSECONDS.toMillis(nowNanos - this.pendingSinceNanos)
					);
				}
			}

			if(doFlush)
			{
				this.internalFlush();
			}
		}

		private void internalFlush()
		{
			logger.debug("Flushing batch storer with size = {}", this.size());
			// pendingSinceNanos is reset inside commit() -> Batching.clear(); no reset needed here.
			this.commit();
		}
	}

	static class Item
	{
		final PersistenceTypeHandler<Binary, Object> typeHandler;
		final Object                                 instance   ;
		final long                                   oid        ;
		      Item                                   link, next ;

		Item(
			final Object                                 instance   ,
			final long                                   oid        ,
			final PersistenceTypeHandler<Binary, Object> typeHandler,
			final Item                                   link
		)
		{
			super();
			this.instance    = instance   ;
			this.oid         = oid        ;
			this.typeHandler = typeHandler;
			this.link        = link       ;
		}

	}

	/**
	 * Pure pin entry for a skipped referent (see {@code Default#registerSkippedOptional}): holds the
	 * instance strongly until commit/clear so the storage GC cannot delete the referenced entity while
	 * the chunk referencing it is not yet committed. Never part of the item chain (not serialized, not
	 * merged) and invisible to {@code lookupOid}, so explicit stores and eager applies still process
	 * the instance normally.
	 */
	static final class PinItem extends Item
	{
		PinItem(final Object instance, final long oid, final Item link)
		{
			super(instance, oid, null, link);
		}

	}
		
	/**
	 * Creates a new default {@link BinaryStorer.Creator}.
	 *
	 * @param channelCountProvider supplies the number of channels each created storer will partition its
	 *                             chunk buffers across.
	 * @param switchByteOrder      whether persisted values should use a non-native byte order.
	 *
	 * @return the newly created storer creator.
	 */
	public static BinaryStorer.Creator Creator(
		final BinaryChannelCountProvider channelCountProvider,
		final boolean                    switchByteOrder
	)
	{
		return new BinaryStorer.Creator.Default(
			notNull(channelCountProvider),
			        switchByteOrder
		);
	}

	/**
	 * Pluggable factory for {@link BinaryStorer} instances. Stored on the foundation so each persistence
	 * binding can wire its own storer creation strategy. Default implementations are provided for the
	 * lazy, eager, and batching flavors.
	 */
	public interface Creator extends PersistenceStorer.Creator<Binary>
	{
		@Override
		public BinaryStorer createLazyStorer(
			PersistenceTypeHandlerManager<Binary> typeManager       ,
			PersistenceObjectManager<Binary>      objectManager     ,
			ObjectSwizzling                       objectRetriever   ,
			PersistenceTarget<Binary>             target            ,
			BufferSizeProviderIncremental         bufferSizeProvider,
			Persister                             persister
		);
		
		@Override
		public default BinaryStorer createStorer(
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceObjectManager<Binary>      objectManager     ,
			final ObjectSwizzling                       objectRetriever   ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProviderIncremental         bufferSizeProvider,
			final Persister                             persister
		)
		{
			return this.createLazyStorer(typeManager, objectManager, objectRetriever, target, bufferSizeProvider, persister);
		}
		
		@Override
		public BinaryStorer createEagerStorer(
			PersistenceTypeHandlerManager<Binary> typeManager       ,
			PersistenceObjectManager<Binary>      objectManager     ,
			ObjectSwizzling                       objectRetriever   ,
			PersistenceTarget<Binary>             target            ,
			BufferSizeProviderIncremental         bufferSizeProvider,
			Persister                             persister
		);

		/**
		 * Skeletal {@link Creator} base holding the channel-count provider and byte-order flag shared by
		 * all created storers. Subclasses override the per-flavor {@code createXxxStorer} methods.
		 */
		public abstract class Abstract implements BinaryStorer.Creator
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////


			private final BinaryChannelCountProvider channelCountProvider;
			private final boolean                    switchByteOrder     ;



			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////

			protected Abstract(
				final BinaryChannelCountProvider channelCountProvider,
				final boolean                    switchByteOrder
			)
			{
				super();
				this.channelCountProvider = channelCountProvider;
				this.switchByteOrder      = switchByteOrder     ;
			}

			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			protected int channelCount()
			{
				return this.channelCountProvider.getChannelCount();
			}
			
			protected boolean switchByteOrder()
			{
				return this.switchByteOrder;
			}

		}
		
		/**
		 * Default {@link Creator} implementation. Wires the channel count and byte-order flag through to
		 * a {@link BinaryStorer.Default}, {@link BinaryStorer.Eager}, or {@link BinaryStorer.Batching}
		 * instance and registers each created storer as a local registry on its
		 * {@link PersistenceObjectManager}.
		 */
		public final class Default extends Abstract
		{
			Default(
				final BinaryChannelCountProvider channelCountProvider,
				final boolean                    switchByteOrder
			)
			{
				super(channelCountProvider, switchByteOrder);
			}

			@Override
			public final BinaryStorer createLazyStorer(
				final PersistenceTypeHandlerManager<Binary> typeManager       ,
				final PersistenceObjectManager<Binary>      objectManager     ,
				final ObjectSwizzling                       objectRetriever   ,
				final PersistenceTarget<Binary>             target            ,
				final BufferSizeProviderIncremental         bufferSizeProvider,
				final Persister                             persister
			)
			{
				this.validateIsStoring(target);
				
				final BinaryStorer.Default storer = new BinaryStorer.Default(
					objectManager         ,
					objectRetriever       ,
					typeManager           ,
					target                ,
					bufferSizeProvider    ,
					this.channelCount()   ,
					this.switchByteOrder(),
					persister
				);
				objectManager.registerLocalRegistry(storer);
				
				return storer;
			}
			@Override
			public BinaryStorer createEagerStorer(
				final PersistenceTypeHandlerManager<Binary> typeManager       ,
				final PersistenceObjectManager<Binary>      objectManager     ,
				final ObjectSwizzling                       objectRetriever   ,
				final PersistenceTarget<Binary>             target            ,
				final BufferSizeProviderIncremental         bufferSizeProvider,
				final Persister                             persister
			)
			{
				this.validateIsStoring(target);
				
				final BinaryStorer.Eager storer = new BinaryStorer.Eager(
					objectManager         ,
					objectRetriever       ,
					typeManager           ,
					target                ,
					bufferSizeProvider    ,
					this.channelCount()   ,
					this.switchByteOrder(),
					persister
				);
				objectManager.registerLocalRegistry(storer);
				
				return storer;
			}
			
			@Override
			public BatchStorer createBatchStorer(
				final PersistenceTypeHandlerManager<Binary> typeManager       ,
				final PersistenceObjectManager<Binary>      objectManager     ,
				final ObjectSwizzling                       objectRetriever   ,
				final PersistenceTarget<Binary>             target            ,
				final BufferSizeProviderIncremental         bufferSizeProvider,
				final Persister                             persister         ,
				final BatchStorer.Controller                controller        ,
				final Duration                              checkInterval
			)
			{
				this.validateIsStoring(target);

				final BinaryStorer.Batching storer = new BinaryStorer.Batching(
					objectManager         ,
					objectRetriever       ,
					typeManager           ,
					target                ,
					bufferSizeProvider    ,
					this.channelCount()   ,
					this.switchByteOrder(),
					persister             ,
					controller            ,
					checkInterval
				);
				objectManager.registerLocalRegistry(storer);

				return storer;
			}

			protected void validateIsStoring(final PersistenceTarget<Binary> target)
			{
				// (06.08.2020 TM)TODO: validation should actually be done by a StorerProvider that uses the Creator
				target.validateIsStoringEnabled();
			}

		}
				
	}

}
