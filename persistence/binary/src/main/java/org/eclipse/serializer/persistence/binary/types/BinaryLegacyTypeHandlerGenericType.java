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
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandlerReflective;
import org.eclipse.serializer.typing.KeyValue;

/**
 * Generic reflective legacy handler for non-enum classes: applies the per-member value translators directly
 * into the in-memory field offsets of an instance produced by the wrapped current
 * {@link PersistenceTypeHandlerReflective}. This is the default reflective legacy handler picked by the
 * binary {@link BinaryLegacyTypeHandlerCreator} for plain (non-enum) classes.
 *
 * @param <T> the runtime type produced by this handler.
 *
 * @see AbstractBinaryLegacyTypeHandlerReflective
 * @see BinaryLegacyTypeHandlerGenericEnum
 */
public class BinaryLegacyTypeHandlerGenericType<T>
extends AbstractBinaryLegacyTypeHandlerReflective<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Creates a new {@link BinaryLegacyTypeHandlerGenericType} for the given legacy/current type pairing.
	 *
	 * @param typeDefinition               the legacy type definition describing the persisted layout.
	 * @param typeHandler                  the current reflective type handler that produces instances.
	 * @param translatorsWithTargetOffsets ordered offset/translator pairs derived from the legacy mapping.
	 * @param listener                     optional listener invoked on each legacy creation, may be {@code null}.
	 * @param switchByteOrder              whether persisted values use a non-native byte order.
	 *
	 * @param <T> the runtime type produced by the handler.
	 *
	 * @return the newly created legacy handler.
	 */
	public static <T> BinaryLegacyTypeHandlerGenericType<T> New(
		final PersistenceTypeDefinition                       typeDefinition              ,
		final PersistenceTypeHandlerReflective<Binary, T>     typeHandler                 ,
		final XGettingEnum<KeyValue<Long, BinaryValueSetter>> translatorsWithTargetOffsets,
		final PersistenceLegacyTypeHandlingListener<Binary>   listener                    ,
		final boolean                                         switchByteOrder
	)
	{
		return new BinaryLegacyTypeHandlerGenericType<>(
			notNull(typeDefinition)                      ,
			notNull(typeHandler)                         ,
			toTranslators(translatorsWithTargetOffsets)  ,
			toTargetOffsets(translatorsWithTargetOffsets),
			mayNull(listener)                            ,
			switchByteOrder
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryLegacyTypeHandlerGenericType(
		final PersistenceTypeDefinition                     typeDefinition  ,
		final PersistenceTypeHandlerReflective<Binary, T>   typeHandler     ,
		final BinaryValueSetter[]                           valueTranslators,
		final long[]                                        targetOffsets   ,
		final PersistenceLegacyTypeHandlingListener<Binary> listener        ,
		final boolean                                       switchByteOrder
	)
	{
		super(typeDefinition, typeHandler, valueTranslators, targetOffsets, listener, switchByteOrder);
	}

}
