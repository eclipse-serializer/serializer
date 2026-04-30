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

import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.reflect.XReflect;

/**
 * A {@link PersistenceTypeHandler} bound to an <i>outdated</i> {@link PersistenceTypeDefinition}, used
 * exclusively for <b>reading</b> legacy persistent data whose layout no longer matches the current
 * runtime type.
 * <p>
 * <b>Read-only by contract.</b> Storing is unsupported &mdash; legacy handlers are only ever consulted
 * during loading. Calling {@link #store} throws {@link UnsupportedOperationException}.
 * <p>
 * <b>How they are produced.</b> A legacy handler is normally created by
 * {@link PersistenceLegacyTypeHandlerCreator} from a {@link PersistenceLegacyTypeMappingResult} that
 * captures how the legacy members map onto the current type's members (some explicitly via the
 * refactoring mapping, some by similarity matching, some discarded, some marked new). For unchanged
 * structures the creator returns a {@link PersistenceLegacyTypeHandlerWrapper} that simply forwards
 * to the current handler; for enum-only renamings it returns a
 * {@link PersistenceLegacyTypeHandlerWrapperEnum} that additionally remaps ordinals; otherwise the
 * data-format-specific subclass synthesizes a translating reflective handler.
 * <p>
 * <b>{@link Abstract} base.</b> Captures the bound legacy {@link PersistenceTypeDefinition} and routes
 * every dictionary-relevant accessor (typeId, typeName, members, length bounds, reference flags) to
 * it &mdash; ensuring the handler reports the persisted-form view rather than the current-runtime
 * view. Concrete subclasses only need to implement the read paths
 * ({@link #create}, {@link #updateState}, {@link #complete}, reference iteration).
 *
 * @param <D> the data target type.
 * @param <T> the runtime type the legacy data should be re-bound to.
 *
 * @see PersistenceTypeHandler
 * @see PersistenceLegacyTypeHandlerCreator
 * @see PersistenceLegacyTypeMapper
 */
public interface PersistenceLegacyTypeHandler<D, T> extends PersistenceTypeHandler<D, T>
{
	/**
	 * Legacy handlers are constructed already bound to their typeId (taken from the legacy type
	 * definition). Re-initializing with the same typeId is a tolerated no-op; any other value is a
	 * conflict and throws.
	 *
	 * @param typeId the typeId being assigned.
	 *
	 * @return this handler.
	 *
	 * @throws PersistenceException if {@code typeId} differs from the already-bound typeId.
	 */
	@Override
	public default PersistenceLegacyTypeHandler<D, T> initialize(final long typeId)
	{
		if(typeId == this.typeId())
		{
			return this;
		}

		// (01.06.2018 TM)NOTE: /!\ copied from PersistenceTypeHandler#initializeTypeId
		throw new PersistenceException(
			"Specified type ID " + typeId
			+ " conflicts with already initialized type ID "
			+ this.typeId()
		);
	}

	/**
	 * Always throws {@link UnsupportedOperationException}: legacy handlers are read-only.
	 *
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public default void store(
		final D                          data    ,
		final T                          instance,
		final long                       objectId,
		final PersistenceStoreHandler<D> handler
	)
	{
		throw new UnsupportedOperationException(
			PersistenceLegacyTypeHandler.class.getSimpleName()
			+ " for type " + this.toTypeIdentifier()
			+ " may never store anything."
		);
	}

	/**
	 * Returns {@code null} to signal that the constants root entry for this legacy type should be
	 * discarded during root resolving &mdash; a legacy enum's constants are remapped via the ordinal
	 * map produced during legacy-handler creation, not via the constants-root mechanism.
	 *
	 * @return always {@code null}.
	 */
	@Override
	public default Object[] collectEnumConstants()
	{
		// indicate discarding of constants root entry during root resolving
		return null;
	}


	/**
	 * Resolves the current enum constant for a legacy enum value. Reads the persisted ordinal from
	 * {@code data}, looks up the mapped current ordinal in {@code ordinalMap}, and returns the
	 * corresponding constant on the handler's runtime type.
	 * <p>
	 * A {@code null} entry in {@code ordinalMap} signals an intentionally deleted constant &mdash; in
	 * that case {@code null} is returned and the loaded enum reference becomes {@code null}.
	 *
	 * @param <T>         the enum type.
	 * @param <D>         the data target type.
	 * @param typeHandler the legacy handler reading the persisted ordinal.
	 * @param data        the persisted form.
	 * @param ordinalMap  legacy-ordinal {@literal ->} current-ordinal map; {@code null} entries mean
	 *                    "constant deleted".
	 *
	 * @return the resolved enum constant, or {@code null} if the legacy ordinal was mapped to deletion.
	 */
	public static <T, D> T resolveEnumConstant(
		final PersistenceLegacyTypeHandler<D, T> typeHandler,
		final D                                  data       ,
		final Integer[]                          ordinalMap
	)
	{
		final int     persistedEnumOrdinal = typeHandler.getPersistedEnumOrdinal(data);
		final Integer mappedOrdinal        = ordinalMap[persistedEnumOrdinal];
		if(mappedOrdinal == null)
		{
			// enum constant intentionally deleted, return null as instance (effectively "deleting" it on load)
			return null;
		}
		
		return XReflect.resolveEnumConstantInstanceTyped(typeHandler.type(), mappedOrdinal.intValue());
	}
	
	
	
	/**
	 * Abstract base class that holds the bound legacy {@link PersistenceTypeDefinition} and routes
	 * every dictionary-relevant accessor (typeId, typeName, members, length bounds, reference flags)
	 * to it. Subclasses only need to implement the actual read paths.
	 *
	 * @param <D> the data target type.
	 * @param <T> the runtime type the legacy data should be re-bound to.
	 */
	public abstract class Abstract<D, T> implements PersistenceLegacyTypeHandler<D, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceTypeDefinition typeDefinition;
		
		

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(final PersistenceTypeDefinition typeDefinition)
		{
			super();
			this.typeDefinition = typeDefinition;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final long typeId()
		{
			return this.typeDefinition.typeId();
		}
		
		@Override
		public final String runtimeTypeName()
		{
			return this.typeDefinition.runtimeTypeName();
		}

		@Override
		public final String typeName()
		{
			return this.typeDefinition.typeName();
		}

		@Override
		public final boolean isPrimitiveType()
		{
			return this.typeDefinition.isPrimitiveType();
		}

		// persisted-form-related methods, so the old type definition has be used //

		/**
		 * @return the bound legacy type definition that this handler reads.
		 */
		public PersistenceTypeDefinition legacyTypeDefinition()
		{
			return this.typeDefinition;
		}

		@Override
		public final XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
		{
			return this.typeDefinition.allMembers();
		}

		@Override
		public final XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers()
		{
			return this.typeDefinition.instanceMembers();
		}
		
		@Override
		public final long membersPersistedLengthMinimum()
		{
			return this.typeDefinition.membersPersistedLengthMinimum();
		}
		
		@Override
		public final long membersPersistedLengthMaximum()
		{
			return this.typeDefinition.membersPersistedLengthMaximum();
		}

		@Override
		public final boolean hasPersistedReferences()
		{
			return this.typeDefinition.hasPersistedReferences();
		}

		@Override
		public final boolean hasPersistedVariableLength()
		{
			return this.typeDefinition.hasPersistedVariableLength();
		}

		@Override
		public final boolean hasVaryingPersistedLengthInstances()
		{
			return this.typeDefinition.hasVaryingPersistedLengthInstances();
		}
		
		// end of persisted-form-related methods //
	
	}
	
}

