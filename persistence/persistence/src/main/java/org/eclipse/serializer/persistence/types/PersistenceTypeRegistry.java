package org.eclipse.serializer.persistence.types;

import java.util.function.BiConsumer;

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

import org.eclipse.serializer.collections.HashMapIdObject;
import org.eclipse.serializer.collections.HashMapObjectId;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionConsistency;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionConsistencyWrongType;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionConsistencyWrongTypeId;
import org.eclipse.serializer.reference.Swizzling;

/**
 * Writable {@link PersistenceTypeLookup}: stores biunique {@code (typeId, Class<?>)} mappings and accepts new
 * registrations. Used as the runtime backing of {@link PersistenceTypeManager}, which adds id allocation on
 * top of a registry instance.
 * <p>
 * Registrations must be consistent &mdash; once a type id is bound to a class, it cannot later be bound to a
 * different class, and vice versa. The mutating methods report consistency violations through
 * {@link PersistenceExceptionConsistency} subclasses.
 *
 * @see PersistenceTypeLookup
 * @see PersistenceTypeManager
 */
public interface PersistenceTypeRegistry extends PersistenceTypeLookup
{
	/**
	 * Registers the passed mapping. Returns {@code false} if the mapping was already present (a no-op),
	 * {@code true} if it was newly added.
	 *
	 * @param typeId the type id.
	 * @param type   the class to bind to {@code typeId}.
	 *
	 * @return {@code true} if the mapping was newly registered, {@code false} if it was already present.
	 *
	 * @throws PersistenceExceptionConsistency if either side of the mapping is already bound to a different
	 *                                         counterpart.
	 */
	public boolean registerType(long typeId, Class<?> type) throws PersistenceExceptionConsistency;

	/**
	 * Bulk variant of {@link #registerType(long, Class)}: validates the entire batch first and only then
	 * registers any new mappings, so a conflict in any one entry leaves the registry untouched.
	 *
	 * @param types the mappings to register.
	 *
	 * @return {@code true} if all passed mappings were already registered (no work was done), {@code false}
	 *         if at least one was newly added.
	 *
	 * @throws PersistenceExceptionConsistency if any mapping conflicts with an existing registration.
	 */
	public boolean registerTypes(final Iterable<? extends PersistenceTypeLink> types)
		throws PersistenceExceptionConsistency;

	/**
	 * Iterates every registered mapping in the order the registry chooses, invoking the consumer with
	 * {@code (typeId, type)}.
	 *
	 * @param consumer the consumer to invoke for each entry.
	 */
	public void iteratePerIds(final BiConsumer<Long, ? super Class<?>> consumer);

	/**
	 * Creates a new empty {@link Default} registry backed by an in-memory hash map.
	 *
	 * @return the newly created registry.
	 */
	public static PersistenceTypeRegistry.Default New()
	{
		return new PersistenceTypeRegistry.Default();
	}

	/**
	 * Default in-memory {@link PersistenceTypeRegistry}. Maintains two synchronized hash maps for the
	 * forward and inverse direction; all public methods synchronize on the registry instance for thread
	 * safety.
	 */
	public final class Default implements PersistenceTypeRegistry
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final HashMapIdObject<Class<?>> typesPerIds = HashMapIdObject.New();
		private final HashMapObjectId<Class<?>> idsPerTypes = HashMapObjectId.New();
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final synchronized long lookupTypeId(final Class<?> type)
		{
			return this.idsPerTypes.get(type, Swizzling.notFoundId());
		}

		@SuppressWarnings("unchecked") // cast safety ensured by registration logic
		@Override
		public final synchronized <T> Class<T> lookupType(final long typeId)
		{
			return (Class<T>)this.typesPerIds.get(typeId);
		}

		@Override
		public final synchronized boolean validateTypeMapping(
			final long     typeId,
			final Class<?> type
		)
			throws PersistenceExceptionConsistency
		{
			if(Swizzling.isNotProperId(typeId))
			{
				throw new PersistenceException("Not a proper TypeId: " + typeId + " for type " + type);
			}
			
			final Class<?> registeredType   = this.typesPerIds.get(typeId);
			final long     registeredTypeId = this.idsPerTypes.get(type, Swizzling.notFoundId());
			
			if(registeredType == null)
			{
				if(Swizzling.isNotFoundId(registeredTypeId))
				{
					return false;
				}
				
				throw new PersistenceExceptionConsistencyWrongTypeId(type, registeredTypeId, typeId);
			}
			
			if(registeredType == type)
			{
				if(registeredTypeId == typeId)
				{
					return true;
				}

				throw new PersistenceExceptionConsistencyWrongTypeId(type, registeredTypeId, typeId);
			}

			throw new PersistenceExceptionConsistencyWrongType(typeId, registeredType, type);
		}

		@Override
		public final synchronized boolean validateTypeMappings(
			final Iterable<? extends PersistenceTypeLink> mappings
		)
			throws PersistenceExceptionConsistency
		{
			// the initial assumption is that all pairs are already contained/registered
			boolean containsAll = true;
			
			for(final PersistenceTypeLink type : mappings)
			{
				if(!this.validateTypeMapping(type.typeId(), type.type()))
				{
					// if only one pair is not registered yet, the return false is flipped to false.
					containsAll = false;
				}
			}
			
			return containsAll;
		}
		
		private void synchRegisterType(
			final long     typeId,
			final Class<?> type
		)
		{
			this.typesPerIds.add(typeId, type);
			this.idsPerTypes.add(type, typeId);
		}

		@Override
		public final synchronized boolean registerType(
			final long     typeId,
			final Class<?> type
		)
			throws PersistenceExceptionConsistency
		{
			if(this.validateTypeMapping(typeId, type))
			{
				return false;
			}
			this.synchRegisterType(typeId, type);
			
			return true;
		}
		
		@Override
		public final synchronized boolean registerTypes(final Iterable<? extends PersistenceTypeLink> types)
			throws PersistenceExceptionConsistency
		{
			// validate all type mappings before registering anything
			if(this.validateTypeMappings(types))
			{
				return false;
			}
			
			// itereate all valid types and register each one
			for(final PersistenceTypeLink type : types)
			{
				// already registered types are ignored, inconsistent types are impossible at this point
				this.synchRegisterType(type.typeId(), type.type());
			}
			
			return true;
		}
		
		@Override
		public void iteratePerIds(final BiConsumer<Long, ? super Class<?>> consumer)
		{
			this.typesPerIds.iterate(c -> consumer.accept(c.key(), c.value()));
		}
		
	}

}
