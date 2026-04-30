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

import static org.eclipse.serializer.util.X.mayNull;
import static org.eclipse.serializer.util.X.notNull;
import static org.eclipse.serializer.math.XMath.positive;

/**
 * A {@linkplain PersistenceTypeDescriptionMemberFieldGeneric generic field} that holds a single fixed-length
 * value &mdash; either a primitive (e.g. {@code long}, {@code int}) or a single object reference.
 * <p>
 * The {@code isReference} flag passed at construction time fully determines the {@code isPrimitive} and
 * {@code hasReferences} flags: a non-reference simple field is implicitly primitive, and a reference simple
 * field implicitly contains a reference. Persistent length is fixed (min == max).
 *
 * @see PersistenceTypeDescriptionMemberFieldGenericVariableLength
 * @see PersistenceTypeDescriptionMemberFieldGenericComplex
 */
public interface PersistenceTypeDescriptionMemberFieldGenericSimple
extends PersistenceTypeDescriptionMemberFieldGeneric
{
	@Override
	public default PersistenceTypeDefinitionMemberFieldGenericSimple createDefinitionMember(
		final PersistenceTypeDefinitionMemberCreator creator
	)
	{
		return creator.createDefinitionMember(this);
	}
	
	/**
	 * Type-specific overload of
	 * {@link PersistenceTypeDescriptionMember#equalDescription(PersistenceTypeDescriptionMember,
	 * PersistenceTypeDescriptionMember)}.
	 *
	 * @param m1 the first simple field.
	 * @param m2 the second simple field.
	 *
	 * @return {@code true} if both have equal description.
	 */
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMemberFieldGenericSimple m1,
		final PersistenceTypeDescriptionMemberFieldGenericSimple m2
	)
	{
		return PersistenceTypeDescriptionMember.equalDescription(m1, m2);
	}

	/**
	 * Type-specific overload of
	 * {@link PersistenceTypeDescriptionMember#equalStructure(PersistenceTypeDescriptionMember,
	 * PersistenceTypeDescriptionMember)}.
	 *
	 * @param m1 the first simple field.
	 * @param m2 the second simple field.
	 *
	 * @return {@code true} if both have equal structure.
	 */
	public static boolean equalStructure(
		final PersistenceTypeDescriptionMemberFieldGenericSimple m1,
		final PersistenceTypeDescriptionMemberFieldGenericSimple m2
	)
	{
		return PersistenceTypeDescriptionMember.equalStructure(m1, m2);
	}



	/**
	 * Convenience overload of {@link #New(String, String, String, boolean, long, long)} without qualifier.
	 *
	 * @param typeName                the textual type name; must not be {@code null}.
	 * @param name                    the field's simple name; must not be {@code null}.
	 * @param isReference             whether the field holds a reference (otherwise primitive).
	 * @param persistentMinimumLength the persistent length lower bound; must be positive.
	 * @param persistentMaximumLength the persistent length upper bound; must be positive.
	 *
	 * @return a new simple field description.
	 */
	public static PersistenceTypeDescriptionMemberFieldGenericSimple.Default New(
		final String  typeName               ,
		final String  name                   ,
		final boolean isReference            ,
		final long    persistentMinimumLength,
		final long    persistentMaximumLength
	)
	{
		return New(typeName, null, name, isReference, persistentMinimumLength, persistentMaximumLength);
	}

	/**
	 * Creates a simple generic field description.
	 *
	 * @param typeName                the textual type name; must not be {@code null}.
	 * @param qualifier               the optional qualifier; may be {@code null}.
	 * @param name                    the field's simple name; must not be {@code null}.
	 * @param isReference             whether the field holds a reference (otherwise primitive).
	 * @param persistentMinimumLength the persistent length lower bound; must be positive.
	 * @param persistentMaximumLength the persistent length upper bound; must be positive.
	 *
	 * @return a new simple field description.
	 */
	public static PersistenceTypeDescriptionMemberFieldGenericSimple.Default New(
		final String  typeName               ,
		final String  qualifier              ,
		final String  name                   ,
		final boolean isReference            ,
		final long    persistentMinimumLength,
		final long    persistentMaximumLength
	)
	{
		return new PersistenceTypeDescriptionMemberFieldGenericSimple.Default(
			 notNull(typeName)               ,
			 mayNull(qualifier)              ,
			 notNull(name)                   ,
			         isReference             ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}
	
	public final class Default
	extends PersistenceTypeDescriptionMemberFieldGeneric.Abstract
	implements PersistenceTypeDescriptionMemberFieldGenericSimple
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final String  typeName           ,
			final String  qualifier          ,
			final String  name               ,
			final boolean isReference        ,
			final long    persistentMinLength,
			final long    persistentMaxLength
		)
		{
			super(
				typeName,
				qualifier,
				name,
				isReference,
				!isReference,
				isReference,
				persistentMinLength,
				persistentMaxLength
			);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void assembleTypeDescription(final PersistenceTypeDescriptionMemberAppender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}

	}

}
