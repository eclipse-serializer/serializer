package org.eclipse.serializer.persistence.binary.types;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
 * %%
 * Copyright (C) 2023 Eclipse Foundation
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static org.eclipse.serializer.util.X.mayNull;
import static org.eclipse.serializer.util.X.notNull;

import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.persistence.binary.internal.BinaryHandlerGenericEnum;
import org.eclipse.serializer.persistence.types.PersistenceLegacyTypeHandlingListener;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandler;
import org.eclipse.serializer.reflect.XReflect;
import org.eclipse.serializer.typing.KeyValue;


public class BinaryLegacyTypeHandlerGenericEnum<T>
extends AbstractBinaryLegacyTypeHandlerReflective<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static <T> BinaryLegacyTypeHandlerGenericEnum<T> New(
		final PersistenceTypeDefinition                       typeDefinition              ,
		final PersistenceTypeHandler<Binary, T>               typeHandler                 ,
		final XGettingEnum<KeyValue<Long, BinaryValueSetter>> translatorsWithTargetOffsets,
		final PersistenceLegacyTypeHandlingListener<Binary>   listener                    ,
		final boolean                                         switchByteOrder
	)
	{
		return new BinaryLegacyTypeHandlerGenericEnum<>(
			notNull(typeDefinition)                      ,
			notNull(typeHandler)                         ,
			toTranslators(translatorsWithTargetOffsets)  ,
			toTargetOffsets(translatorsWithTargetOffsets),
			mayNull(listener)                            ,
			switchByteOrder
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	// offsets must be determined per handler instance since different types have different persistent form offsets.
	private final long binaryOffsetOrdinal;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryLegacyTypeHandlerGenericEnum(
		final PersistenceTypeDefinition                     typeDefinition  ,
		final PersistenceTypeHandler<Binary, T>             typeHandler     ,
		final BinaryValueSetter[]                           valueTranslators,
		final long[]                                        targetOffsets   ,
		final PersistenceLegacyTypeHandlingListener<Binary> listener        ,
		final boolean                                       switchByteOrder
	)
	{
		super(typeDefinition, typeHandler, valueTranslators, targetOffsets, listener, switchByteOrder);
		this.binaryOffsetOrdinal = BinaryHandlerGenericEnum.calculateBinaryOffsetOrdinal(typeDefinition);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	// note on initializing methods: excluding the java.lang.Enum fields must already be excluded in valueTranslators

	public int getOrdinal(final Binary data)
	{
		return data.read_int(this.binaryOffsetOrdinal);
	}

	@Override
	protected T internalCreate(final Binary data, final PersistenceLoadHandler handler)
	{
		return XReflect.resolveEnumConstantInstanceTyped(this.type(), this.getOrdinal(data));
	}

	@Override
	public void updateState(final Binary rawData, final T instance, final PersistenceLoadHandler handler)
	{
		// debug hook
		super.updateState(rawData, instance, handler);
	}

}
