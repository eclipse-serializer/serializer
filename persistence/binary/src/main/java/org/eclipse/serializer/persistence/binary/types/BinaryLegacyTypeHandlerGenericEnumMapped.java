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

import static org.eclipse.serializer.util.X.mayNull;
import static org.eclipse.serializer.util.X.notNull;

import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.persistence.types.PersistenceLegacyTypeHandler;
import org.eclipse.serializer.persistence.types.PersistenceLegacyTypeHandlingListener;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandler;
import org.eclipse.serializer.typing.KeyValue;


public class BinaryLegacyTypeHandlerGenericEnumMapped<T>
extends BinaryLegacyTypeHandlerGenericEnum<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static <T> BinaryLegacyTypeHandlerGenericEnumMapped<T> New(
		final PersistenceTypeDefinition                       typeDefinition              ,
		final PersistenceTypeHandler<Binary, T>               typeHandler                 ,
		final XGettingEnum<KeyValue<Long, BinaryValueSetter>> translatorsWithTargetOffsets,
		final Integer[]                                       ordinalMapping              ,
		final PersistenceLegacyTypeHandlingListener<Binary>   listener                    ,
		final boolean                                         switchByteOrder
	)
	{
		return new BinaryLegacyTypeHandlerGenericEnumMapped<>(
			notNull(typeDefinition)                      ,
			notNull(typeHandler)                         ,
			toTranslators(translatorsWithTargetOffsets)  ,
			toTargetOffsets(translatorsWithTargetOffsets),
			notNull(ordinalMapping)                      ,
			mayNull(listener)                            ,
			switchByteOrder
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Integer[] ordinalMapping;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryLegacyTypeHandlerGenericEnumMapped(
		final PersistenceTypeDefinition                     typeDefinition  ,
		final PersistenceTypeHandler<Binary, T>             typeHandler     ,
		final BinaryValueSetter[]                           valueTranslators,
		final long[]                                        targetOffsets   ,
		final Integer[]                                     ordinalMapping  ,
		final PersistenceLegacyTypeHandlingListener<Binary> listener        ,
		final boolean                                       switchByteOrder
	)
	{
		super(typeDefinition, typeHandler, valueTranslators, targetOffsets, listener, switchByteOrder);
		this.ordinalMapping = ordinalMapping;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	protected T internalCreate(final Binary data, final PersistenceLoadHandler handler)
	{
		return PersistenceLegacyTypeHandler.resolveEnumConstant(this, data, this.ordinalMapping);
	}

	@Override
	public void updateState(final Binary rawData, final T instance, final PersistenceLoadHandler handler)
	{
		// debug hook
		super.updateState(rawData, instance, handler);
	}

}
