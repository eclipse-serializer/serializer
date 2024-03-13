package org.eclipse.serializer;

import static org.eclipse.serializer.util.X.mayNull;
import static org.eclipse.serializer.util.X.notNull;

import java.nio.ByteBuffer;
import java.util.function.Function;

/*-
 * #%L
 * Eclipse Serializer
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

import org.eclipse.serializer.collections.HashTable;
import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.hashing.XHashing;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.binary.types.BinaryStorer;
import org.eclipse.serializer.persistence.binary.types.ChunksBuffer;
import org.eclipse.serializer.persistence.binary.types.ChunksBufferByteReversing;
import org.eclipse.serializer.persistence.binary.types.ChunksWrapper;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTransfer;
import org.eclipse.serializer.persistence.types.PersistenceIdSet;
import org.eclipse.serializer.persistence.types.PersistenceManager;
import org.eclipse.serializer.persistence.types.PersistenceObjectIdRequestor;
import org.eclipse.serializer.persistence.types.PersistenceObjectManager;
import org.eclipse.serializer.persistence.types.PersistenceSource;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.persistence.types.PersistenceStorer;
import org.eclipse.serializer.persistence.types.PersistenceTarget;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandler;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandlerManager;
import org.eclipse.serializer.persistence.types.Persister;
import org.eclipse.serializer.persistence.types.Storer;
import org.eclipse.serializer.reference.Lazy;
import org.eclipse.serializer.reference.ObjectSwizzling;
import org.eclipse.serializer.reference.Swizzling;
import org.eclipse.serializer.util.BufferSizeProviderIncremental;
import org.eclipse.serializer.util.X;

/**
 * Convenient API layer to use the binary persistence functionality for a simple serializer.
 * <p>
 * It is based on a {@link org.eclipse.serializer.SerializerFoundation}, which can be configured to various needs.
 * <p>
 * Per default {@link Binary} and <code>byte[]</code> are supported as medium types.
 *
 * @param <M> the medium type
 */
public interface Serializer<M> extends AutoCloseable
{
	/**
	 * Serializes the given object graph into the medium type.
	 * @param object the graph's root
	 * @return the binary format
	 */
	public M serialize(Object object);
	
	/**
	 * Recreates an object graph based on the given data.
	 * @param <T> the object's type
	 * @param medium the medium to read from
	 * @return the deserialized object graph
	 */
	public <T> T deserialize(M medium);
	
	public static Serializer<byte[]> Bytes()
	{
		return Bytes(SerializerFoundation.New());
	}
	
	public static Serializer<byte[]> Bytes(final SerializerFoundation<?> foundation)
	{
		return New(
			foundation      ,
			Static::toBytes ,
			Static::toBinary
		);
	}
	
	public static Serializer<Binary> Binary()
	{
		return Binary(SerializerFoundation.New());
	}
	
	public static Serializer<Binary> Binary(final SerializerFoundation<?> foundation)
	{
		return New(
			foundation         ,
			Function.identity(),
			Function.identity()
		);
	}
	
	public static <M> Serializer<M> New(
		final Function<Binary, M> toMedium,
		final Function<M, Binary> toBinary
	)
	{
		return New(
			SerializerFoundation.New(),
			toMedium                  ,
			toBinary
		);
	}
		
	public static <M> Serializer<M> New(
		final SerializerFoundation<?> foundation,
		final Function<Binary, M>     toMedium  ,
		final Function<M, Binary>     toBinary
	)
	{
		return new Default<>(
			notNull(foundation),
			notNull(toMedium  ),
			notNull(toBinary  )
		);
	}
	
	
	public final static class Static
	{
		public static byte[] toBytes(final Binary binary)
		{
			return XMemory.toArray(binary.buffers());
		}
		
		public static Binary toBinary(final byte[] bytes)
		{
			final ByteBuffer buffer = XMemory.allocateDirectNative(bytes.length);
			buffer.put(bytes);
			buffer.flip();
			return ChunksWrapper.New(buffer);
		}
		
		/**
		 * Dummy constructor to prevent instantiation of this static-only utility class.
		 *
		 * @throws UnsupportedOperationException when called
		 */
		private Static()
		{
			// static only
			throw new UnsupportedOperationException();
		}
	}
	
	
	public static interface Source extends PersistenceSource<Binary>
	{
		@Override
		default XGettingCollection<? extends Binary> readByObjectIds(final PersistenceIdSet[] oids)
			throws PersistenceExceptionTransfer
		{
			return null;
		}
	}
	
	
	public static interface Target extends PersistenceTarget<Binary>
	{
		@Override
		default boolean isWritable()
		{
			return true;
		}
	}
	
	
	public static class Default<M> implements Serializer<M>
	{
		private final SerializerFoundation<?> foundation        ;
		private final Function<Binary, M>     toMedium          ;
		private final Function<M, Binary>     toBinary          ;
		private PersistenceManager<Binary>    persistenceManager;
		private Storer                        storer            ;
		private Binary                        input             ;
		private Binary                        output            ;
				
		Default(
			final SerializerFoundation<?> foundation,
			final Function<Binary, M>     toMedium  ,
			final Function<M, Binary>     toBinary
		)
		{
			super();
			this.foundation = foundation;
			this.toMedium   = toMedium  ;
			this.toBinary   = toBinary  ;
			this.lazyInit();
		}
		
		@Override
		public synchronized M serialize(final Object object)
		{
			this.storer.store(object);
			this.storer.commit();
			return this.toMedium.apply(this.output);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public synchronized <T> T deserialize(final M data)
		{
			this.input = this.toBinary.apply(data);
			return (T)this.persistenceManager.get();
		}
		
		@Override
		public synchronized void close()
		{
			if(this.persistenceManager != null)
			{
				this.persistenceManager.objectRegistry().truncateAll();
				this.persistenceManager.close();
				this.persistenceManager = null;
				this.input              = null;
				this.output             = null;
			}
		}
		
		private void lazyInit()
		{
			if(this.persistenceManager == null)
			{
				final Source source = ()   -> X.Constant(this.input);
				final Target target = data -> this.output = data    ;
								
				this.persistenceManager = this.foundation
					.setPersistenceSource(source)
					.setPersistenceTarget(target)
					.createPersistenceManager()
				;
				this.storer             = this.persistenceManager.createStorer(
					new SerializerStorer.Creator(this.foundation.isByteOrderMismatch())
				);
			}
			else
			{
				this.persistenceManager.objectRegistry().truncateAll();
			}
		}
		
		
		static class SerializerStorer
		implements BinaryStorer, PersistenceStoreHandler<Binary>, PersistenceObjectIdRequestor<Binary>
		{
			static class Creator implements PersistenceStorer.Creator<Binary>
			{
				private final boolean switchByteOrder;
				
				Creator(final boolean switchByteOrder)
				{
					super();
					this.switchByteOrder = switchByteOrder;
				}

				@Override
				public PersistenceStorer createLazyStorer(
					final PersistenceTypeHandlerManager<Binary> typeManager       ,
					final PersistenceObjectManager<Binary>      objectManager     ,
					final ObjectSwizzling                       objectRetriever   ,
					final PersistenceTarget<Binary>             target            ,
					final BufferSizeProviderIncremental         bufferSizeProvider,
					final Persister                             persister
				)
				{
					return this.createEagerStorer(
						typeManager       ,
						objectManager     ,
						objectRetriever   ,
						target            ,
						bufferSizeProvider,
						persister
					);
				}

				@Override
				public PersistenceStorer createEagerStorer(
					final PersistenceTypeHandlerManager<Binary> typeManager       ,
					final PersistenceObjectManager<Binary>      objectManager     ,
					final ObjectSwizzling                       objectRetriever   ,
					final PersistenceTarget<Binary>             target            ,
					final BufferSizeProviderIncremental         bufferSizeProvider,
					final Persister                             persister
				)
				{
					final SerializerStorer storer = new SerializerStorer(
						objectManager       ,
						objectRetriever     ,
						typeManager         ,
						target              ,
						bufferSizeProvider  ,
						this.switchByteOrder,
						persister
					);
					return storer;
				}
				
			}
			
			
			
			protected static int defaultSlotSize()
			{
				return 1024;
			}


			private final boolean                               switchByteOrder;
			private final PersistenceObjectManager<Binary>      objectManager  ;
			private final ObjectSwizzling                       objectRetriever;
			private final PersistenceTypeHandlerManager<Binary> typeManager    ;
			private final PersistenceTarget<Binary>             target         ;
			private final Persister                             persister      ;
			
			private final BufferSizeProviderIncremental bufferSizeProvider;
			
			private ChunksBuffer[] chunks;

			final   Item                    head = new Item(null, 0L, null, null);
			private Item                    tail;
			private HashTable<Object, Item> hashSlots;

			public SerializerStorer(
				final PersistenceObjectManager<Binary>      objectManager     ,
				final ObjectSwizzling                       objectRetriever   ,
				final PersistenceTypeHandlerManager<Binary> typeManager       ,
				final PersistenceTarget<Binary>             target            ,
				final BufferSizeProviderIncremental         bufferSizeProvider,
				final boolean                               switchByteOrder   ,
				final Persister                             persister
			)
			{
				super();
				this.objectManager      = notNull(objectManager)     ;
				this.objectRetriever    = notNull(objectRetriever)   ;
				this.typeManager        = notNull(typeManager)       ;
				this.target             = notNull(target)            ;
				this.bufferSizeProvider = notNull(bufferSizeProvider);
				this.switchByteOrder    =         switchByteOrder    ;
				this.persister          = mayNull(persister)         ;
				
				this.defaultInitialize();
			}



			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////

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
				return this.hashSlots.size();
			}

			@Override
			public final long size()
			{
				return this.hashSlots.size();
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
				this.hashSlots = HashTable.NewCustom(hashLength);
				
				// initializing/clearing item chain
				(this.tail = this.head).next = null;

				final ChunksBuffer[] chunks = this.chunks = new ChunksBuffer[1];
				chunks[0] = this.switchByteOrder
					? ChunksBufferByteReversing.New(chunks, this.bufferSizeProvider)
					: ChunksBuffer.New(chunks, this.bufferSizeProvider)
				;
			}

			@Override
			public PersistenceStorer ensureCapacity(final long desiredCapacity)
			{
				//no-op; done by Hashtable
				return this;
			}
			
			@Override
			public <T> long apply(final T instance)
			{
				return this.applyEager(instance);
			}
			
			@Override
			public <T> long apply(final T instance, final PersistenceTypeHandler<Binary, T> localTypeHandler)
			{
				return this.applyEager(instance, localTypeHandler);
			}
			
			@Override
			public final <T> long applyEager(final T instance)
			{
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
			
			protected void checkSerializationSupport(final Object instance)
			{
				if(instance instanceof Lazy)
				{
					throw new UnsupportedOperationException("Lazy references cannot be serialized");
				}
			}
			
			/**
			 * Stores the passed instance (always) and interprets it as the root of a graph to be traversed and
			 * have its instances stored recursively if deemed necessary by the logic until all instance
			 * that can be reached by that logic have been handled.
			 * 
			 * @param root the root object of the graph
			 * @return the root's object id
			 */
			protected final long storeGraph(final Object root)
			{
				// initial registration. After that, storing adds via recursing the graph and processing items iteratively.
				final long rootOid = this.registerGuaranteed(notNull(root));

				// process and collect required instances uniquely in item chain (graph recursion transformed to iteration)
				for(Item item = this.tail; item != null; item = item.next)
				{
					item.typeHandler.store(this.chunks[0], item.instance, item.oid, this);
				}

				return rootOid;
			}

			@Override
			public final long store(final Object root)
			{
				return this.storeGraph(root);
			}

			@Override
			public final long[] storeAll(final Object... instances)
			{
				final long[] oids = new long[instances.length];
				for(int i = 0; i < instances.length; i++)
				{
					oids[i] = this.storeGraph(instances[i]);
				}
				return oids;
			}
			
			@Override
			public void storeAll(final Iterable<?> instances)
			{
				for(final Object instance : instances)
				{
					this.storeGraph(instance);
				}
			}
			
			@Override
			public final Object commit()
			{
				// isEmpty locks internally
				if(!this.isEmpty())
				{
					// must validate here, too, in case the WriteController disabled writing during the storer's existence.
					this.target.validateIsStoringEnabled();
					this.target.write(this.chunks[0].complete());
				}
				
				this.clear();
				
				// not used (yet?)
				return null;
			}
			
			public final long lookupOid(final Object object)
			{
				final Item item = this.hashSlots.get(object);
			
				if(item != null)
				{
					return item.oid;
				}

				// returning 0 is a valid case: an instance registered to be skipped by using the null-OID.
				return Swizzling.notFoundId();
			}

			@Override
			public final <T> void registerGuaranteed(
				final long                              objectId       ,
				final T                                 instance       ,
				final PersistenceTypeHandler<Binary, T> optionalHandler
			)
			{
				this.checkSerializationSupport(instance);
				
				// ensure handler (or fail if type is not persistable) before ensuring an OID.
				final PersistenceTypeHandler<Binary, ? super T> typeHandler = optionalHandler != null
					? optionalHandler
					: this.typeManager.ensureTypeHandler(instance)
				;
				this.tail = this.tail.next = this.registerObjectId(instance, typeHandler, objectId);
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
				// lookup returns -1 on failure, so 0 is a valid lookup result. Main reason for -1 vs. 0 distinction!
				if(Swizzling.isNotFoundId(this.lookupOid(instance)))
				{
					// only register if not found locally, of course
					this.registerObjectId(instance, null, objectId);
					return true;
				}
				
				// already locally present (found), do nothing.
				return false;
			}
			
			@SuppressWarnings("unchecked")
			public final <T> Item registerObjectId(
				final T                                         instance   ,
				final PersistenceTypeHandler<Binary, ? super T> typeHandler,
				final long                                      objectId
			)
			{

				final Item item = new Item(
					instance,
					objectId,
					(PersistenceTypeHandler<Binary, Object>)typeHandler,
					null);

				this.hashSlots.put(instance, item);
				return item;
			}

			@Override
			public Persister getPersister()
			{
				return this.persister;
			}

			
			static final class Item
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
			
		}
		
	}
	
}
