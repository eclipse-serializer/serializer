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

import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionConsistency;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionConsistencyUnknownTid;
import org.eclipse.serializer.reference.Swizzling;


/**
 * Central type-id allocator: adds id-generating semantics on top of a {@link PersistenceTypeRegistry} by
 * pairing it with a {@link PersistenceTypeIdProvider}. {@code ensureTypeId} returns the existing id for an
 * already-registered type or atomically assigns and registers a new one; {@code ensureType} performs the
 * inverse lookup and throws if the id is unknown.
 * <p>
 * Registering a type id additionally walks the class's superclass chain, so the entire ancestry is recorded
 * &mdash; subsequent persistence operations can rely on every ancestor being registered as well.
 *
 * @see PersistenceTypeRegistry
 * @see PersistenceTypeIdProvider
 * @see PersistenceObjectManager
 */
public interface PersistenceTypeManager extends PersistenceTypeRegistry
{
	/**
	 * Returns the type id associated with {@code type}, assigning and registering a new id if the type is
	 * not yet known. The superclass chain of {@code type} is registered as a side effect.
	 *
	 * @param type the type to look up or register.
	 *
	 * @return the (possibly newly assigned) type id.
	 */
	public long ensureTypeId(Class<?> type);

	/**
	 * Returns the {@link Class} associated with the passed type id, or throws if the id is not registered.
	 *
	 * @param typeId the type id to resolve.
	 *
	 * @return the registered class.
	 *
	 * @throws org.eclipse.serializer.persistence.exceptions.PersistenceExceptionConsistencyUnknownTid if the
	 *         id is not registered.
	 */
	public Class<?> ensureType(long typeId);

	/**
	 * The current highest assigned type id, as reported by the underlying provider.
	 *
	 * @return the current highest assigned type id.
	 */
	public long currentTypeId();

	/**
	 * Advances the underlying provider's highest assigned type id to {@code highestTypeId}. Refuses to move
	 * the value backwards: passing a value below the current id is rejected.
	 *
	 * @param highestTypeId the new highest assigned type id.
	 *
	 * @throws IllegalArgumentException if the current id is already above {@code highestTypeId}.
	 */
	public void updateCurrentHighestTypeId(long highestTypeId);



	/**
	 * Creates a new {@link Default} manager that delegates lookups and registrations to {@code registry} and
	 * obtains new ids from {@code tidProvider}.
	 *
	 * @param registry    the type registry; must not be {@code null}.
	 * @param tidProvider the id provider; must not be {@code null}.
	 *
	 * @return the newly created manager.
	 */
	public static PersistenceTypeManager.Default New(
		final PersistenceTypeRegistry   registry   ,
		final PersistenceTypeIdProvider tidProvider
	)
	{
		return new PersistenceTypeManager.Default(
			notNull(registry)   ,
			notNull(tidProvider)
		);
	}

	/**
	 * Default {@link PersistenceTypeManager} composing a {@link PersistenceTypeRegistry} with a
	 * {@link PersistenceTypeIdProvider}. All registry-mutating operations synchronize on the registry
	 * instance.
	 */
	public final class Default implements PersistenceTypeManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final PersistenceTypeRegistry   typeRegistry;
		final PersistenceTypeIdProvider tidProvider ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceTypeRegistry   registry   ,
			final PersistenceTypeIdProvider tidProvider
		)
		{
			super();
			this.typeRegistry = notNull(registry)   ;
			this.tidProvider  = notNull(tidProvider);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		protected long createNewTypeId()
		{
			return this.tidProvider.provideNextTypeId();
		}

		protected long internalEnsureTypeId(final Class<?> type)
		{
			long typeId;
			synchronized(this.typeRegistry)
			{
				// if not found either assign new oid or return the meanwhile registered oid
				if(Swizzling.isFoundId(typeId = this.typeRegistry.lookupTypeId(type)))
				{
					return typeId;
				}
				typeId = this.createNewTypeId();

				this.typeRegistry.registerType(typeId, type);
				if(type.getSuperclass() != null)
				{
					this.ensureTypeId(type.getSuperclass());
				}
			}
			return typeId;
		}

		@Override
		public long lookupTypeId(final Class<?> type)
		{
			return this.typeRegistry.lookupTypeId(type);
		}

		@Override
		public <T> Class<T> lookupType(final long tid)
		{
			return this.typeRegistry.lookupType(tid);
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
		public boolean registerTypes(final Iterable<? extends PersistenceTypeLink> types)
			throws PersistenceExceptionConsistency
		{
			return this.typeRegistry.registerTypes(types);
		}

		@Override
		public boolean registerType(final long tid, final Class<?> type)
		{
			return this.typeRegistry.registerType(tid, type);
		}

		@Override
		public long ensureTypeId(final Class<?> type)
		{
			final long typeId; // quick read-only check for already registered tid
			if(Swizzling.isFoundId(typeId = this.typeRegistry.lookupTypeId(type)))
			{
				// already present/found typeId is returned.
				return typeId;
			}
			
			// typeId not found, so a new typeId is ensured returned.
			return this.internalEnsureTypeId(type);
		}

		@Override
		public final long currentTypeId()
		{
			synchronized(this.typeRegistry)
			{
				return this.tidProvider.currentTypeId();
			}
		}

		@Override
		public void updateCurrentHighestTypeId(final long highestTypeId)
		{
			synchronized(this.typeRegistry)
			{
				final long currentTypeId = this.tidProvider.currentTypeId();
				if(currentTypeId > highestTypeId)
				{
					throw new IllegalArgumentException(
						"Current highest type id already passed desired new highest type id: "
						+ currentTypeId + " > " + highestTypeId
					);
				}
				this.tidProvider.updateCurrentTypeId(highestTypeId);
			}
		}
		
		@Override
		public Class<?> ensureType(final long typeId)
		{
			Class<?> type;
			if((type = this.typeRegistry.lookupType(typeId)) == null)
			{
				throw new PersistenceExceptionConsistencyUnknownTid(typeId);
			}
			return type;
		}
		
		@Override
		public void iteratePerIds(final BiConsumer<Long, ? super Class<?>> consumer)
		{
			this.typeRegistry.iteratePerIds(consumer);
		}
		
	}

}
