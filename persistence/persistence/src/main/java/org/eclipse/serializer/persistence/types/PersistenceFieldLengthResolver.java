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

import java.lang.reflect.Field;

import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.reflect.XReflect;


/**
 * Resolves the minimum and maximum persistent length (in bytes) of a member, given either a {@link Field}, a
 * runtime {@link Class}, or a dictionary entry by type name. Used during type description and dictionary
 * compilation to compute the on-disk length bounds advertised by each
 * {@link PersistenceTypeDescriptionMember}.
 * <p>
 * The interface is layered: most overloads have default implementations that delegate to a small set of
 * abstract methods covering the cases the implementation must decide on its own (variable-length types,
 * primitive types, complex members). Reference values default to the byte size of {@link Persistence#objectIdType()}.
 *
 * @see PersistenceTypeDescriptionMember
 * @see PersistenceTypeDictionary#isVariableLength(String)
 */
public interface PersistenceFieldLengthResolver
{
	/**
	 * Resolves the minimum persistent length for the value of the passed reflected field by delegating to
	 * {@link #resolveMinimumLengthFromType(Class)} with {@code field.getType()}.
	 *
	 * @param t the field.
	 *
	 * @return the minimum persistent length in bytes.
	 */
	public default long resolveMinimumLengthFromField(final Field t)
	{
		return this.resolveMinimumLengthFromType(t.getType());
	}

	/**
	 * Resolves the maximum persistent length for the value of the passed reflected field by delegating to
	 * {@link #resolveMaximumLengthFromType(Class)} with {@code field.getType()}.
	 *
	 * @param t the field.
	 *
	 * @return the maximum persistent length in bytes.
	 */
	public default long resolveMaximumLengthFromField(final Field t)
	{
		return this.resolveMaximumLengthFromType(t.getType());
	}

	/**
	 * Resolves the minimum persistent length for a member from a textual dictionary entry: dispatches to
	 * the variable-length, primitive, or reference handling depending on {@code typeName}.
	 *
	 * @param declaringTypeName the name of the declaring type (used by variable-length resolution).
	 * @param memberName        the name of the member.
	 * @param typeName          the textual type name from the dictionary.
	 *
	 * @return the minimum persistent length in bytes.
	 */
	public default long resolveMinimumLengthFromDictionary(
		final String declaringTypeName,
		final String memberName       ,
		final String typeName
	)
	{
		if(PersistenceTypeDictionary.isVariableLength(typeName))
		{
			return this.variableLengthTypeMinimumLength(declaringTypeName, memberName, typeName);
		}

		if(XReflect.isPrimitiveTypeName(typeName))
		{
			return this.resolveMinimumLengthFromPrimitiveType(
				XReflect.tryResolvePrimitiveType(typeName)
			);
		}

		// everything else (neither variable length nor primitive) must be a reference value
		return this.referenceMinimumLength();
	}

	/**
	 * Resolves the maximum persistent length for a member from a textual dictionary entry: dispatches to
	 * the variable-length, primitive, or reference handling depending on {@code typeName}.
	 *
	 * @param declaringTypeName the name of the declaring type (used by variable-length resolution).
	 * @param memberName        the name of the member.
	 * @param typeName          the textual type name from the dictionary.
	 *
	 * @return the maximum persistent length in bytes.
	 */
	public default long resolveMaximumLengthFromDictionary(
		final String declaringTypeName,
		final String memberName       ,
		final String typeName
	)
	{
		if(PersistenceTypeDictionary.isVariableLength(typeName))
		{
			return this.variableLengthTypeMaximumLength(declaringTypeName, memberName, typeName);
		}

		if(XReflect.isPrimitiveTypeName(typeName))
		{
			return this.resolveMaximumLengthFromPrimitiveType(
				XReflect.tryResolvePrimitiveType(typeName)
			);
		}

		// everything else (neither variable length nor primitive) must be a reference value
		return this.referenceMaximumLength();
	}

	/**
	 * Resolves the minimum persistent length for the passed runtime type: primitive types delegate to
	 * {@link #resolveMinimumLengthFromPrimitiveType(Class)}, every other type is treated as a reference.
	 *
	 * @param type the runtime type.
	 *
	 * @return the minimum persistent length in bytes.
	 */
	public default long resolveMinimumLengthFromType(final Class<?> type)
	{
		return type.isPrimitive()
			? this.resolveMinimumLengthFromPrimitiveType(type)
			: this.referenceMinimumLength()
		;
	}

	/**
	 * Resolves the maximum persistent length for the passed runtime type: primitive types delegate to
	 * {@link #resolveMaximumLengthFromPrimitiveType(Class)}, every other type is treated as a reference.
	 *
	 * @param type the runtime type.
	 *
	 * @return the maximum persistent length in bytes.
	 */
	public default long resolveMaximumLengthFromType(final Class<?> type)
	{
		return type.isPrimitive()
			? this.resolveMaximumLengthFromPrimitiveType(type)
			: this.referenceMaximumLength()
		;
	}

	/**
	 * The minimum persistent length of a reference value. Defaults to the byte size of the type returned
	 * by {@link Persistence#objectIdType()}.
	 *
	 * @return the minimum reference length in bytes.
	 */
	public default long referenceMinimumLength()
	{
		return this.resolveMinimumLengthFromPrimitiveType(Persistence.objectIdType());
	}

	/**
	 * The maximum persistent length of a reference value. Defaults to the byte size of the type returned
	 * by {@link Persistence#objectIdType()} (references are fixed-width).
	 *
	 * @return the maximum reference length in bytes.
	 */
	public default long referenceMaximumLength()
	{
		return this.resolveMinimumLengthFromPrimitiveType(Persistence.objectIdType());
	}

	/**
	 * The minimum persistent length of a variable-length type identified by name (e.g. binary lists,
	 * strings).
	 *
	 * @param declaringTypeName the name of the declaring type.
	 * @param memberName        the name of the member.
	 * @param typeName          the textual type name.
	 *
	 * @return the minimum length in bytes.
	 */
	public long variableLengthTypeMinimumLength(
		String declaringTypeName,
		String memberName       ,
		String typeName
	);

	/**
	 * The maximum persistent length of a variable-length type identified by name. Implementations may
	 * return {@link Long#MAX_VALUE} to indicate "unbounded".
	 *
	 * @param declaringTypeName the name of the declaring type.
	 * @param memberName        the name of the member.
	 * @param typeName          the textual type name.
	 *
	 * @return the maximum length in bytes.
	 */
	public long variableLengthTypeMaximumLength(
		String declaringTypeName,
		String memberName       ,
		String typeName
	);

	/**
	 * The minimum persistent length of the passed primitive type.
	 *
	 * @param primitiveType the primitive class.
	 *
	 * @return the minimum length in bytes.
	 */
	public long resolveMinimumLengthFromPrimitiveType(Class<?> primitiveType);

	/**
	 * The maximum persistent length of the passed primitive type.
	 *
	 * @param primitiveType the primitive class.
	 *
	 * @return the maximum length in bytes.
	 */
	public long resolveMaximumLengthFromPrimitiveType(Class<?> primitiveType);

	/**
	 * The minimum persistent length of a complex member (i.e. one whose value is composed of nested
	 * members rather than a single primitive or reference).
	 *
	 * @param memberName    the name of the complex member.
	 * @param typeName      the textual type name.
	 * @param nestedMembers the descriptions of the nested members.
	 *
	 * @return the minimum length in bytes.
	 */
	public long resolveComplexMemberMinimumLength(
		String                                                                  memberName   ,
		String                                                                  typeName     ,
		XGettingSequence<? extends PersistenceTypeDescriptionMemberFieldGeneric> nestedMembers
	);

	/**
	 * The maximum persistent length of a complex member.
	 *
	 * @param memberName    the name of the complex member.
	 * @param typeName      the textual type name.
	 * @param nestedMembers the descriptions of the nested members.
	 *
	 * @return the maximum length in bytes.
	 */
	public long resolveComplexMemberMaximumLength(
		String                                                                  memberName   ,
		String                                                                  typeName     ,
		XGettingSequence<? extends PersistenceTypeDescriptionMemberFieldGeneric> nestedMembers
	);

}
