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

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.serializer.collections.HashMapIdObject;
import org.eclipse.serializer.collections.MiniMap;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionConsistency;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTypeHandlerConsistencyConflictedType;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTypeHandlerConsistencyConflictedTypeId;
import org.eclipse.serializer.reflect.XReflect;

/**
 * Mutable registry of {@link PersistenceTypeHandler}s, keyed by both Java {@link Class} and typeId, plus
 * by typeId for {@link PersistenceLegacyTypeHandler}s.
 * <p>
 * Combines:
 * <ul>
 * <li>{@link PersistenceTypeHandlerLookup} for read-only by-instance / by-class / by-typeId lookup,</li>
 * <li>{@link PersistenceTypeRegistry} for the underlying type-{@literal <-->}-typeId mapping,</li>
 * <li>{@link PersistenceTypeHandlerIterable} for iterating all current and legacy handlers.</li>
 * </ul>
 * <p>
 * Registration enforces consistency &mdash; attempting to register a different handler for an already
 * mapped type or typeId raises a {@link PersistenceExceptionTypeHandlerConsistencyConflictedType} or
 * {@link PersistenceExceptionTypeHandlerConsistencyConflictedTypeId}.
 *
 * @param <D> the data target type.
 */
public interface PersistenceTypeHandlerRegistry<D>
extends PersistenceTypeHandlerLookup<D>, PersistenceTypeRegistry, PersistenceTypeHandlerIterable<D>
{
	/**
	 * Registers the passed handler under its own {@link PersistenceTypeHandler#type()} and
	 * {@link PersistenceTypeHandler#typeId()}.
	 *
	 * @param <T>         the handled type.
	 * @param typeHandler the handler to register.
	 *
	 * @return {@code true} if the registration changed registry state.
	 *
	 * @throws PersistenceExceptionConsistency if a different handler is already registered for the
	 *                                         same type or typeId.
	 */
	public <T> boolean registerTypeHandler(PersistenceTypeHandler<D, T> typeHandler);

	/**
	 * Bulk-registers the passed handlers; equivalent to invoking
	 * {@link #registerTypeHandler(PersistenceTypeHandler)} for each entry.
	 *
	 * @param typeHandlers the handlers to register.
	 *
	 * @return the number of handlers that changed registry state.
	 */
	public long registerTypeHandlers(Iterable<? extends PersistenceTypeHandler<D, ?>> typeHandlers);

	/**
	 * Registers the passed handler under the explicitly supplied {@code type}, which may be a sub-type
	 * of the handler's own {@link PersistenceTypeHandler#type()} (e.g. for handlers that handle
	 * multiple sub-types via the same logic).
	 *
	 * @param <T>         the type to register the handler under.
	 * @param type        the registration key.
	 * @param typeHandler the handler.
	 *
	 * @return {@code true} if the registration changed registry state.
	 */
	public <T> boolean registerTypeHandler(Class<T> type, PersistenceTypeHandler<D, ? super T> typeHandler);

	/**
	 * Registers a {@link PersistenceLegacyTypeHandler} for reading older persisted data whose layout
	 * no longer matches the current handler.
	 *
	 * @param legacyTypeHandler the legacy handler.
	 *
	 * @return {@code true} if the registration changed registry state.
	 */
	public boolean registerLegacyTypeHandler(PersistenceLegacyTypeHandler<D, ?> legacyTypeHandler);



	/**
	 * Creates a new {@link PersistenceTypeHandlerRegistry} that delegates type-{@literal <-->}-typeId
	 * lookups to the passed {@link PersistenceTypeRegistry}.
	 *
	 * @param <D>          the data target type.
	 * @param typeRegistry the underlying type registry; must not be {@code null}.
	 *
	 * @return a new registry.
	 */
	public static <D> PersistenceTypeHandlerRegistry.Default<D> New(
		final PersistenceTypeRegistry typeRegistry
	)
	{
		return new PersistenceTypeHandlerRegistry.Default<>(
			notNull(typeRegistry)
		);
	}

	public final class Default<D> implements PersistenceTypeHandlerRegistry<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceTypeRegistry typeRegistry;

		private final MiniMap<Class<?>, PersistenceTypeHandler<D, ?>> handlersByType   = new MiniMap<>();
		private final HashMapIdObject<PersistenceTypeHandler<D, ?>>   handlersByTypeId = HashMapIdObject.New();



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final PersistenceTypeRegistry typeRegistry)
		{
			super();
			this.typeRegistry = typeRegistry;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public long lookupTypeId(final Class<?> type)
		{
			return this.typeRegistry.lookupTypeId(type);
		}

		@Override
		public <T> Class<T> lookupType(final long typeId)
		{
			return this.typeRegistry.lookupType(typeId);
		}

		@SuppressWarnings("unchecked") // cast type safety guaranteed by management logic
		@Override
		public <T> PersistenceTypeHandler<D, T> lookupTypeHandler(final Class<T> type)
		{
			return (PersistenceTypeHandler<D, T>)this.handlersByType.get(type);
		}

		@Override
		public PersistenceTypeHandler<D, ?> lookupTypeHandler(final long typeId)
		{
			return this.handlersByTypeId.get(typeId);
		}

		@Override
		public <T> PersistenceTypeHandler<D, T> lookupTypeHandler(final T instance)
		{
			// standard registry does not consider actual objects
			return this.lookupTypeHandler(XReflect.getClass(instance));
		}
		
		@Override
		public boolean validateTypeMapping(final long typeId, final Class<?> type) throws PersistenceExceptionConsistency
		{
			return this.typeRegistry.validateTypeMapping(typeId, type);
		}
		
		@Override
		public boolean validateTypeMappings(final Iterable<? extends PersistenceTypeLink> mappings)
			throws PersistenceExceptionConsistency
		{
			return this.typeRegistry.validateTypeMappings(mappings);
		}

		@Override
		public boolean registerType(final long tid, final Class<?> type) throws PersistenceExceptionConsistency
		{
			return this.typeRegistry.registerType(tid, type);
		}
		
		@Override
		public boolean registerTypes(final Iterable<? extends PersistenceTypeLink> types)
			throws PersistenceExceptionConsistency
		{
			return this.typeRegistry.registerTypes(types);
		}
		

		@Override
		public <T> boolean registerTypeHandler(
			final Class<T>                             type       ,
			final PersistenceTypeHandler<D, ? super T> typeHandler
		)
		{
			synchronized(this.handlersByType)
			{
				if(this.synchValidateAlreadyRegisteredTypeHandler(type, typeHandler))
				{
					return true;
				}
				
				this.registerTypeHandler(typeHandler);
				
				this.synchPutTypeMapping(type, typeHandler);
				
				// when does this method ever return false? Not registerable case is handled via exception
				return true;
			}
		}
		
		private <T> boolean synchValidateAlreadyRegisteredTypeHandler(
			final Class<T>                             type       ,
			final PersistenceTypeHandler<D, ? super T> typeHandler
		)
		{
			PersistenceTypeHandler<D, ?> actualHandler;
			if((actualHandler = this.handlersByType.get(type)) == null)
			{
				return false;
			}
			
			if(actualHandler == typeHandler)
			{
				return true;
			}

			throw new PersistenceExceptionTypeHandlerConsistencyConflictedType(type, actualHandler, typeHandler);
		}

		@Override
		public <T> boolean registerTypeHandler(final PersistenceTypeHandler<D, T> typeHandler)
		{
			synchronized(this.handlersByType)
			{
				final Class<T> type = typeHandler.type();
				final long     tid  = typeHandler.typeId();
				this.typeRegistry.registerType(tid, type); // first ensure consistency of tid<->type combination

				// check if handler is already registered for type
				this.synchValidateAlreadyRegisteredTypeHandler(type, typeHandler);

				// else: handler is not registered yet, proceed with tid check

				// check if a handler is already registered for the same tid
				if(this.synchCheckByTypeId(typeHandler))
				{
					// redundant registering attempt, abort.
					return false;
				}
				// else: handler, tid, type combination is neither registered nor inconsistent, so register handler.

				// register new bidirectional assignment
				// note: basic type<->tid registration already happened above if necessary
				this.synchPutFullMapping(typeHandler);
				
				return true;
			}
		}

		@Override
		public long registerTypeHandlers(
			final Iterable<? extends PersistenceTypeHandler<D, ?>> typeHandlers
		)
		{
			synchronized(this.handlersByType)
			{
				long registeredCount = 0;
				for(final PersistenceTypeHandler<D, ?> handler : typeHandlers)
				{
					if(this.registerTypeHandler(handler))
					{
						registeredCount++;
					}
				}
				
				return registeredCount;
			}
		}

		private <T> void synchPutTypeMapping(
			final Class<T>                             type       ,
			final PersistenceTypeHandler<D, ? super T> typeHandler
		)
		{
			this.handlersByType.put(type, typeHandler);
		}

		private <T> void synchPutFullMapping(final PersistenceTypeHandler<D, T> typeHandler)
		{
			this.synchPutTypeMapping(typeHandler.type(), typeHandler);
			this.handlersByTypeId.put(typeHandler.typeId(), typeHandler);
		}
		
		private boolean synchCheckByTypeId(final PersistenceTypeHandler<D, ?> typeHandler)
		{
			final PersistenceTypeHandler<D, ?> actualHandler;
			if((actualHandler = this.handlersByTypeId.get(typeHandler.typeId())) != null)
			{
				if(actualHandler != typeHandler)
				{
					throw new PersistenceExceptionTypeHandlerConsistencyConflictedTypeId(
						typeHandler.typeId(),
						actualHandler,
						typeHandler
					);
				}
				// else: handler is already consistently registered.
				return true;
			}
			
			return false;
		}
		

		@Override
		public boolean registerLegacyTypeHandler(final PersistenceLegacyTypeHandler<D, ?> legacyTypeHandler)
		{
			synchronized(this.handlersByType)
			{
				// check if a handler is already registered for the same tid
				if(this.synchCheckByTypeId(legacyTypeHandler))
				{
					// redundant registering attempt, abort.
					return false;
				}
				
				// no registration by type, just by typeId. This is a one-way translation helper for lookups by TID.
				this.handlersByTypeId.put(legacyTypeHandler.typeId(), legacyTypeHandler);
				
				return true;
			}
		}

		public void clear()
		{
			synchronized(this.handlersByType)
			{
				this.handlersByType.clear();
				this.handlersByTypeId.clear();
			}
		}

		@Override
		public <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateTypeHandlers(
			final C iterator
		)
		{
			synchronized(this.handlersByType)
			{
				this.handlersByType.iterateValues(iterator);
			}
			return iterator;
		}
		
		@Override
		public <C extends Consumer<? super PersistenceLegacyTypeHandler<D, ?>>> C iterateLegacyTypeHandlers(
			final C iterator
		)
		{
			synchronized(this.handlersByType)
			{
				this.handlersByTypeId.iterateValues(th ->
				{
					if(th instanceof PersistenceLegacyTypeHandler)
					{
						iterator.accept((PersistenceLegacyTypeHandler<D, ?>)th);
					}
				});
			}
			
			return iterator;
		}
		
		@Override
		public <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateAllTypeHandlers(final C iterator)
		{
			synchronized(this.handlersByType)
			{
				this.handlersByTypeId.iterateValues(iterator);
			}
			
			return iterator;
		}

		@Override
		public void iteratePerIds(final BiConsumer<Long, ? super Class<?>> consumer)
		{
			this.typeRegistry.iteratePerIds(consumer);
		}
		
	}

}
