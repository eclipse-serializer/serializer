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
import org.eclipse.serializer.persistence.types.PersistenceLegacyTypeHandlingListener;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandler;
import org.eclipse.serializer.reflect.XReflect;
import org.eclipse.serializer.typing.KeyValue;


/**
 * Reflective legacy handler for enum classes whose <em>static</em> structure (the set and order of enum
 * constants) has not changed since the legacy version. Resolves the instance by reading the persisted
 * ordinal directly and looking up the matching enum constant on the current type. For changed constant
 * structures, see {@link BinaryLegacyTypeHandlerGenericEnumMapped}.
 *
 * @param <T> the enum runtime type produced by this handler.
 *
 * @see BinaryLegacyTypeHandlerGenericEnumMapped
 * @see AbstractBinaryLegacyTypeHandlerReflective
 */
public class BinaryLegacyTypeHandlerGenericEnum<T>
extends AbstractBinaryLegacyTypeHandlerReflective<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Creates a new {@link BinaryLegacyTypeHandlerGenericEnum} for the given legacy/current type pairing.
	 *
	 * @param typeDefinition               the legacy type definition describing the persisted layout.
	 * @param typeHandler                  the current type handler that owns the enum constants.
	 * @param translatorsWithTargetOffsets ordered offset/translator pairs derived from the legacy mapping.
	 * @param listener                     optional listener invoked on each legacy creation, may be {@code null}.
	 * @param switchByteOrder              whether persisted values use a non-native byte order.
	 *
	 * @param <T> the enum runtime type produced by the handler.
	 *
	 * @return the newly created legacy handler.
	 */
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

	/**
	 * Reads the persisted enum ordinal from the entity data at this handler's predetermined offset.
	 *
	 * @param data the persisted entity data.
	 *
	 * @return the persisted enum ordinal.
	 */
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
	
	@Override
	public int getPersistedEnumOrdinal(final Binary data)
	{
		return data.read_int(this.binaryOffsetOrdinal);			
	}
}
