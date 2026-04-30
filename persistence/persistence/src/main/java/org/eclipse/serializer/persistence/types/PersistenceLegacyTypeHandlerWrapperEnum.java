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

/**
 * Specialized {@link PersistenceLegacyTypeHandlerWrapper} for legacy <b>enum</b> types whose constants
 * have been added, removed, or reordered relative to the current type.
 * <p>
 * Overrides {@link #create} to consult the {@code ordinalMapping} array (built by
 * {@link PersistenceLegacyTypeHandlerCreator.Abstract#deriveEnumOrdinalMapping}) instead of the
 * forwarded current handler's {@code create} &mdash; the persisted ordinal is translated to the
 * current ordinal before the constant is resolved on the runtime enum class. A {@code null} entry in
 * the mapping signals an intentionally deleted constant; in that case loading yields {@code null}.
 * <p>
 * Storing remains forbidden as for any legacy handler.
 *
 * @param <D> the data target type.
 * @param <T> the enum type.
 *
 * @see PersistenceLegacyTypeHandler#resolveEnumConstant
 */
public class PersistenceLegacyTypeHandlerWrapperEnum<D, T>
extends PersistenceLegacyTypeHandlerWrapper<D, T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Creates a new {@link PersistenceLegacyTypeHandlerWrapperEnum}.
	 *
	 * @param <D>                  the data target type.
	 * @param <T>                  the enum type.
	 * @param legacyTypeDefinition the bound legacy type definition.
	 * @param currentTypeHandler   the current enum handler.
	 * @param ordinalMapping       legacy-ordinal-to-current-ordinal map; {@code null} entries mean
	 *                             "constant deleted".
	 *
	 * @return a new wrapper.
	 */
	public static <D, T> PersistenceLegacyTypeHandlerWrapperEnum<D, T> New(
		final PersistenceTypeDefinition    legacyTypeDefinition,
		final PersistenceTypeHandler<D, T> currentTypeHandler  ,
		final Integer[]                    ordinalMapping
	)
	{
		return new PersistenceLegacyTypeHandlerWrapperEnum<>(
			notNull(legacyTypeDefinition),
			notNull(currentTypeHandler),
			notNull(ordinalMapping)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Integer[] ordinalMapping;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	PersistenceLegacyTypeHandlerWrapperEnum(
		final PersistenceTypeDefinition    legacyTypeDefinition,
		final PersistenceTypeHandler<D, T> currentTypeHandler  ,
		final Integer[]                    ordinalMapping
	)
	{
		super(legacyTypeDefinition, currentTypeHandler);
		this.ordinalMapping = ordinalMapping;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public T create(final D data, final PersistenceLoadHandler handler)
	{
		// this is all there is on this level for this implementation / case.
		return PersistenceLegacyTypeHandler.resolveEnumConstant(this, data, this.ordinalMapping);
	}
	
}
