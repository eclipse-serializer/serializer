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

public class PersistenceLegacyTypeHandlerWrapperEnum<D, T>
extends PersistenceLegacyTypeHandlerWrapper<D, T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
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
