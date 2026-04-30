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
 * A {@linkplain PersistenceTypeDescriptionMemberFieldGeneric generic field} of variable persistent length
 * whose elements are not described by a nested member sequence &mdash; i.e. a flat variable-length blob such
 * as {@code [byte]} or {@code [char]} chunks. For variable-length entries that <i>do</i> carry a nested
 * structure, see the subtype {@link PersistenceTypeDescriptionMemberFieldGenericComplex}.
 * <p>
 * {@link #isVariableLength()} is hardcoded to {@code true} for all instances of this interface, regardless
 * of the persistent length range that was actually configured.
 *
 * @see PersistenceTypeDescriptionMemberFieldGenericSimple
 * @see PersistenceTypeDescriptionMemberFieldGenericComplex
 */
public interface PersistenceTypeDescriptionMemberFieldGenericVariableLength
extends PersistenceTypeDescriptionMemberFieldGeneric
{
	@Override
	public default boolean isVariableLength()
	{
		return true;
	}
	
	@Override
	public default boolean equalsStructure(final PersistenceTypeDescriptionMember other)
	{
		// the type check is the only specific thing here.
		return other instanceof PersistenceTypeDescriptionMemberFieldGenericVariableLength
			&& PersistenceTypeDescriptionMemberFieldGeneric.super.equalsStructure(other)
		;
	}

	@Override
	public default boolean equalsLayout(final PersistenceTypeDescriptionMember other)
	{
		// the type check is the only specific thing here.
		return other instanceof PersistenceTypeDescriptionMemberFieldGenericVariableLength
			&& PersistenceTypeDescriptionMemberFieldGeneric.super.equalsLayout(other)
		;
	}
	
	/**
	 * Type-specific overload of
	 * {@link PersistenceTypeDescriptionMember#equalDescription(PersistenceTypeDescriptionMember,
	 * PersistenceTypeDescriptionMember)}.
	 *
	 * @param m1 the first variable-length field.
	 * @param m2 the second variable-length field.
	 *
	 * @return {@code true} if both have equal description.
	 */
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMemberFieldGenericVariableLength m1,
		final PersistenceTypeDescriptionMemberFieldGenericVariableLength m2
	)
	{
		return PersistenceTypeDescriptionMember.equalDescription(m1, m2);
	}

	/**
	 * Type-specific overload of
	 * {@link PersistenceTypeDescriptionMember#equalStructure(PersistenceTypeDescriptionMember,
	 * PersistenceTypeDescriptionMember)}.
	 *
	 * @param m1 the first variable-length field.
	 * @param m2 the second variable-length field.
	 *
	 * @return {@code true} if both have equal structure.
	 */
	public static boolean equalStructure(
		final PersistenceTypeDescriptionMemberFieldGenericVariableLength m1,
		final PersistenceTypeDescriptionMemberFieldGenericVariableLength m2
	)
	{
		return PersistenceTypeDescriptionMember.equalStructure(m1, m2);
	}
	
	@Override
	public default PersistenceTypeDefinitionMemberFieldGenericVariableLength createDefinitionMember(
		final PersistenceTypeDefinitionMemberCreator creator
	)
	{
		return creator.createDefinitionMember(this);
	}

	

	/**
	 * Convenience overload of {@link #New(String, String, String, long, long)} without qualifier.
	 *
	 * @param typeName                the textual type name; must not be {@code null}.
	 * @param name                    the field's simple name; must not be {@code null}.
	 * @param persistentMinimumLength the persistent length lower bound; must be positive.
	 * @param persistentMaximumLength the persistent length upper bound; must be positive.
	 *
	 * @return a new variable-length generic field description.
	 */
	public static PersistenceTypeDescriptionMemberFieldGenericVariableLength.Default New(
		final String typeName               ,
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return New(typeName, null, name, persistentMinimumLength, persistentMaximumLength);
	}

	/**
	 * Creates a flat variable-length generic field description (non-reference, non-primitive). For
	 * variable-length entries with nested structure use
	 * {@link PersistenceTypeDescriptionMemberFieldGenericComplex#New}.
	 *
	 * @param typeName                the textual type name; must not be {@code null}.
	 * @param qualifier               the optional qualifier; may be {@code null}.
	 * @param name                    the field's simple name; must not be {@code null}.
	 * @param persistentMinimumLength the persistent length lower bound; must be positive.
	 * @param persistentMaximumLength the persistent length upper bound; must be positive.
	 *
	 * @return a new variable-length generic field description.
	 */
	public static PersistenceTypeDescriptionMemberFieldGenericVariableLength.Default New(
		final String typeName               ,
		final String qualifier              ,
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return new PersistenceTypeDescriptionMemberFieldGenericVariableLength.Default(
			 notNull(typeName),
			 mayNull(qualifier),
			 notNull(name),
			         false,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}

	public class Default
	extends PersistenceTypeDescriptionMemberFieldGeneric.Abstract
	implements PersistenceTypeDescriptionMemberFieldGenericVariableLength
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final String  typeName               ,
			final String  qualifier              ,
			final String  name                   ,
			final boolean hasReferences          ,
			final long    persistentMinimumLength,
			final long    persistentMaximumLength
		)
		{
			super(
				typeName               ,
				qualifier              ,
				name                   ,
				false                  ,
				false                  ,
				hasReferences          ,
				persistentMinimumLength,
				persistentMaximumLength
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
