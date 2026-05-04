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

import java.util.function.Consumer;

import org.eclipse.serializer.collections.HashEnum;
import org.eclipse.serializer.collections.HashTable;
import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

/**
 * Registry of user- and platform-supplied {@link PersistenceTypeHandler}s and
 * {@link PersistenceLegacyTypeHandler}s. These are the handlers consulted before the generic reflection-
 * based handler discovery kicks in: a custom registration wins over the auto-derived handler for the same
 * type, allowing callers to override or replace the default serialization for selected types.
 * <p>
 * Live (current) handlers are stored by exact runtime {@link Class}; legacy handlers are stored as a flat
 * list because the same type may have multiple historical structures, only one of which matches a given
 * dictionary entry.
 *
 * @param <D> the persistence data type passed through to the handlers.
 *
 * @see PersistenceTypeHandler
 * @see PersistenceLegacyTypeHandler
 * @see PersistenceTypeHandlerIterable
 */
public interface PersistenceCustomTypeHandlerRegistry<D> extends PersistenceTypeHandlerIterable<D>
{
	/**
	 * Registers the passed handler under its own {@link PersistenceTypeHandler#type()}. Existing
	 * registrations for the same type are replaced (returning {@code false}); fresh registrations return
	 * {@code true}.
	 *
	 * @param <T>         the handler's type.
	 * @param typeHandler the handler to register.
	 *
	 * @return {@code true} if the registration is new, {@code false} if it replaced an existing one.
	 */
	public <T> boolean registerTypeHandler(PersistenceTypeHandler<D, T> typeHandler);

	/**
	 * Registers the passed handler under {@code type}, validating that the handler's declared entity type
	 * is compatible with {@code type}. Useful when registering a handler bound to a super-type as the
	 * handler for a more specific type.
	 *
	 * @param <T>         the registration type.
	 * @param type        the type to register under.
	 * @param typeHandler the handler to register.
	 *
	 * @return {@code true} if the registration is new, {@code false} if it replaced an existing one.
	 */
	public <T> boolean registerTypeHandler(Class<T> type, PersistenceTypeHandler<D, ? super T> typeHandler);

	/**
	 * Registers the passed legacy handler. Legacy handlers are kept as a flat list because the same type
	 * may have multiple historical structures.
	 *
	 * @param <T>               the handler's type.
	 * @param legacyTypeHandler the legacy handler to register.
	 *
	 * @return {@code true} if the handler is a new entry.
	 */
	public <T> boolean registerLegacyTypeHandler(PersistenceLegacyTypeHandler<D, T> legacyTypeHandler);

	/**
	 * Bulk variant of {@link #registerLegacyTypeHandler(PersistenceLegacyTypeHandler)}.
	 *
	 * @param legacyTypeHandlers the legacy handlers to register.
	 *
	 * @return this registry, for fluent chaining.
	 */
	public PersistenceCustomTypeHandlerRegistry<D> registerLegacyTypeHandlers(
		XGettingCollection<? extends PersistenceLegacyTypeHandler<D, ?>> legacyTypeHandlers
	);

	/**
	 * Bulk variant of {@link #registerTypeHandler(PersistenceTypeHandler)}.
	 *
	 * @param typeHandlers the handlers to register.
	 *
	 * @return this registry, for fluent chaining.
	 */
	public PersistenceCustomTypeHandlerRegistry<D> registerTypeHandlers(
		XGettingCollection<? extends PersistenceTypeHandler<D, ?>> typeHandlers
	);

	/**
	 * Looks up the live handler registered for the exact runtime type {@code type}, returning {@code null}
	 * if none is registered. Does not walk super-types &mdash; for super-type fallback see
	 * {@link PersistenceAbstractTypeHandlerSearcher}.
	 *
	 * @param <T>  the searched type.
	 * @param type the runtime type.
	 *
	 * @return the registered handler, or {@code null} if none.
	 */
	public <T> PersistenceTypeHandler<D, ? super T> lookupTypeHandler(Class<T> type);

	/**
	 * The flat list of registered legacy handlers, in registration order.
	 *
	 * @return the registered legacy handlers.
	 */
	public XGettingEnum<PersistenceLegacyTypeHandler<D, ?>> legacyTypeHandlers();

	/**
	 * Whether a live handler is registered for the exact runtime type {@code type}.
	 *
	 * @param type the runtime type.
	 *
	 * @return {@code true} if a handler is registered for {@code type}.
	 */
	public boolean knowsType(Class<?> type);



	/**
	 * Creates a new empty {@link Default} registry.
	 *
	 * @param <D> the persistence data type.
	 *
	 * @return the newly created registry.
	 */
	public static <D> PersistenceCustomTypeHandlerRegistry.Default<D> New()
	{
		return new PersistenceCustomTypeHandlerRegistry.Default<>();
	}

	/**
	 * Default {@link PersistenceCustomTypeHandlerRegistry}: live handlers in a class-keyed hash table,
	 * legacy handlers in a flat hash enum (instance equality, since legacy handlers may differ only by
	 * the structural definition they carry). All public methods synchronize on the registry instance.
	 *
	 * @param <D> the persistence data type.
	 */
	public final class Default<D> implements PersistenceCustomTypeHandlerRegistry<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		final static Logger logger = Logging.getLogger(PersistenceCustomTypeHandlerRegistry.class);

		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final HashTable<Class<?>, PersistenceTypeHandler<D, ?>> liveTypeHandlers = HashTable.New();
		
		/*
		 * Really instance equality since:
		 * - TypeId might not be present, yet.
		 * - Live type cannot be used for LTHs.
		 * - This is just a collection of "potentially structure-compatible" handlers that get sorted out later.
		 */
		private final HashEnum<PersistenceLegacyTypeHandler<D, ?>> legacyTypeHandlers = HashEnum.New();

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		


		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public synchronized boolean knowsType(final Class<?> type)
		{
			return this.liveTypeHandlers.keys().contains(type);
		}

		@Override
		public final <T> boolean registerTypeHandler(
			final Class<T>                             type                  ,
			final PersistenceTypeHandler<D, ? super T> typeHandlerInitializer
		)
		{
			// validate before mutating internal state by public API
			typeHandlerInitializer.validateEntityType(type);
			
			return this.internalRegisterTypeHandler(type, typeHandlerInitializer);
		}

		@Override
		public <T> boolean registerTypeHandler(
			final PersistenceTypeHandler<D, T> typeHandlerInitializer
		)
		{
			// no need to validate a type handler's own type reference.
			return this.internalRegisterTypeHandler(
				typeHandlerInitializer.type(),
				typeHandlerInitializer
			);
		}
		
		final synchronized <T> boolean internalRegisterTypeHandler(
			final Class<T>                             type                  ,
			final PersistenceTypeHandler<D, ? super T> typeHandlerInitializer
		)
		{
			// put instead of add to allow custom-tailored replacements for native handlers (e.g. divergent TID or logic)
			
			logger.debug("Registering type handler {} for type {}", typeHandlerInitializer.getClass(), type);
			
			return this.liveTypeHandlers.put(
				notNull(type),
				notNull(typeHandlerInitializer)
			);
		}

		@Override
		public synchronized PersistenceCustomTypeHandlerRegistry.Default<D> registerTypeHandlers(
			final XGettingCollection<? extends PersistenceTypeHandler<D, ?>> typeHandlerInitializers
		)
		{
			for(final PersistenceTypeHandler<D, ?> th : typeHandlerInitializers)
			{
				this.registerTypeHandler(th);
			}
			
			return this;
		}
		
		@Override
		public synchronized <T> boolean registerLegacyTypeHandler(
			final PersistenceLegacyTypeHandler<D, T> legacyTypeHandler
		)
		{
			logger.debug("Registering legacy type handler {} for type {}", legacyTypeHandler.getClass(), legacyTypeHandler.typeName());
			
			return this.legacyTypeHandlers.add(legacyTypeHandler);
		}
		
		@Override
		public synchronized PersistenceCustomTypeHandlerRegistry<D> registerLegacyTypeHandlers(
			final XGettingCollection<? extends PersistenceLegacyTypeHandler<D, ?>> legacyTypeHandlers
		)
		{
			for(final PersistenceLegacyTypeHandler<D, ?> lth : legacyTypeHandlers)
			{
				this.registerLegacyTypeHandler(lth);
			}
			
			return this;
		}

		@SuppressWarnings("unchecked") // cast type safety guaranteed by management logic
		private <T> PersistenceTypeHandler<D, T> internalLookupTypeHandler(final Class<T> type)
		{
			return (PersistenceTypeHandler<D, T>)this.liveTypeHandlers.get(type);
		}

		@Override
		public <T> PersistenceTypeHandler<D, T> lookupTypeHandler(final Class<T> type)
		{
			return this.internalLookupTypeHandler(type);
		}
		
		@Override
		public <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateTypeHandlers(final C iterator)
		{
			this.liveTypeHandlers.values().iterate(iterator);
			return iterator;
		}
		
		@Override
		public <C extends Consumer<? super PersistenceLegacyTypeHandler<D, ?>>> C iterateLegacyTypeHandlers(final C iterator)
		{
			return this.legacyTypeHandlers().iterate(iterator);
		}
		
		@Override
		public final XGettingEnum<PersistenceLegacyTypeHandler<D, ?>> legacyTypeHandlers()
		{
			return this.legacyTypeHandlers;
		}

	}

}
