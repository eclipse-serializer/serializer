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

import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.types.PersistenceFieldLengthResolver;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionMemberFieldGeneric;

public interface BinaryFieldLengthResolver extends PersistenceFieldLengthResolver
{
	@Override
	public default long resolveMinimumLengthFromPrimitiveType(final Class<?> primitiveType)
	{
		// binary length is equal to memory byte size
		return XMemory.byteSizePrimitive(primitiveType);
	}

	@Override
	public default long resolveMaximumLengthFromPrimitiveType(final Class<?> primitiveType)
	{
		// binary length is equal to memory byte size
		return XMemory.byteSizePrimitive(primitiveType);
	}

	@Override
	public default long variableLengthTypeMinimumLength(
		final String declaringTypeName,
		final String memberName       ,
		final String typeName
	)
	{
		return Binary.binaryListMinimumLength();
	}

	@Override
	public default long variableLengthTypeMaximumLength(
		final String declaringTypeName,
		final String memberName       ,
		final String typeName
	)
	{
		return Binary.binaryListMaximumLength();
	}

	@Override
	public default long resolveComplexMemberMinimumLength(
		final String                                                                  memberName   ,
		final String                                                                  typeName     ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMemberFieldGeneric> nestedMembers
	)
	{
		return Binary.binaryListMinimumLength();
	}

	@Override
	public default long resolveComplexMemberMaximumLength(
		final String                                                                  memberName   ,
		final String                                                                  typeName     ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMemberFieldGeneric> nestedMembers
	)
	{
		return Binary.binaryListMaximumLength();
	}



	public final class Default implements BinaryFieldLengthResolver
	{
		// empty default implementation. Something is missing in the new default method concept
	}

}
