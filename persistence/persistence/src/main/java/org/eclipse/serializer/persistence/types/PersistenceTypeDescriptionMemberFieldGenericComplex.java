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

import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.collections.types.XImmutableSequence;

/**
 * A {@linkplain PersistenceTypeDescriptionMemberFieldGenericVariableLength variable-length} generic field
 * whose elements are themselves described by a nested sequence of generic field members. This is used to
 * describe persistent forms like a list of structured records (e.g. a {@code [list]} of
 * {@code [key, value]} pairs).
 * <p>
 * Two complex fields are equal in description only when they share type, name and qualifier <i>and</i>
 * their nested {@link #members()} sequence is element-wise equal in description. The nested members are
 * rendered into the dictionary inside a bracketed block, indented one level deeper than the parent.
 *
 * @see PersistenceTypeDescriptionMemberFieldGenericVariableLength
 * @see PersistenceTypeDescriptionMemberFieldGenericSimple
 */
public interface PersistenceTypeDescriptionMemberFieldGenericComplex
extends PersistenceTypeDescriptionMemberFieldGenericVariableLength
{
	/**
	 * The ordered sequence of nested members that describes the layout of a single element of this
	 * complex field.
	 *
	 * @return the nested members.
	 */
	public XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members();

	
	
	@Override
	public default boolean equalsDescription(final PersistenceTypeDescriptionMember other)
	{
		// does NOT call #equalsStructure to avoid redundant member iteration
		return PersistenceTypeDescriptionMember.equalTypeAndNameAndQualifier(this, other)
			&& other instanceof PersistenceTypeDescriptionMemberFieldGenericComplex
			&& PersistenceTypeDescriptionMember.equalDescriptions(
				this.members(),
				((PersistenceTypeDescriptionMemberFieldGenericComplex)other).members()
			)
		;
	}
	
	@Override
	public default boolean equalsStructure(final PersistenceTypeDescriptionMember other)
	{
		return PersistenceTypeDescriptionMemberFieldGenericVariableLength.super.equalsStructure(other)
			&& other instanceof PersistenceTypeDescriptionMemberFieldGenericComplex
			&& PersistenceTypeDescriptionMember.equalStructures(
				this.members(),
				((PersistenceTypeDescriptionMemberFieldGenericComplex)other).members()
			)
		;
	}

	@Override
	public default boolean equalsLayout(final PersistenceTypeDescriptionMember other)
	{
		return PersistenceTypeDescriptionMemberFieldGenericVariableLength.super.equalsLayout(other)
			&& other instanceof PersistenceTypeDescriptionMemberFieldGenericComplex
			&& PersistenceTypeDescriptionMember.equalLayouts(
				this.members(),
				((PersistenceTypeDescriptionMemberFieldGenericComplex)other).members()
			)
		;
	}
	
	/**
	 * Type-specific overload of
	 * {@link PersistenceTypeDescriptionMember#equalDescription(PersistenceTypeDescriptionMember,
	 * PersistenceTypeDescriptionMember)}.
	 *
	 * @param m1 the first complex field.
	 * @param m2 the second complex field.
	 *
	 * @return {@code true} if both have equal description.
	 */
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMemberFieldGenericComplex m1,
		final PersistenceTypeDescriptionMemberFieldGenericComplex m2
	)
	{
		return PersistenceTypeDescriptionMember.equalDescription(m1, m2);
	}

	/**
	 * Type-specific overload of
	 * {@link PersistenceTypeDescriptionMember#equalStructure(PersistenceTypeDescriptionMember,
	 * PersistenceTypeDescriptionMember)}.
	 *
	 * @param m1 the first complex field.
	 * @param m2 the second complex field.
	 *
	 * @return {@code true} if both have equal structure.
	 */
	public static boolean equalStructure(
		final PersistenceTypeDescriptionMemberFieldGenericComplex m1,
		final PersistenceTypeDescriptionMemberFieldGenericComplex m2
	)
	{
		return PersistenceTypeDescriptionMember.equalStructure(m1, m2);
	}
	
	@Override
	public default PersistenceTypeDefinitionMemberFieldGenericComplex createDefinitionMember(
		final PersistenceTypeDefinitionMemberCreator creator
	)
	{
		return creator.createDefinitionMember(this);
	}
	
	/**
	 * Convenience overload of
	 * {@link #New(String, String, XGettingSequence, long, long)} that omits the qualifier.
	 *
	 * @param name                    the field's simple name; must not be {@code null}.
	 * @param members                 the nested members describing one element; must not be {@code null}.
	 * @param persistentMinimumLength the lower bound of the field's persistent length; must be positive.
	 * @param persistentMaximumLength the upper bound of the field's persistent length; must be positive.
	 *
	 * @return a new complex field description.
	 */
	public static PersistenceTypeDescriptionMemberFieldGenericComplex New(
		final String                                                         name                   ,
		final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members                ,
		final long                                                           persistentMinimumLength,
		final long                                                           persistentMaximumLength
	)
	{
		return New(null, name, members, persistentMinimumLength, persistentMaximumLength);
	}

	/**
	 * Creates a complex generic field description.
	 *
	 * @param qualifier               the optional qualifier; may be {@code null}.
	 * @param name                    the field's simple name; must not be {@code null}.
	 * @param members                 the nested members describing one element; must not be {@code null}.
	 * @param persistentMinimumLength the lower bound of the field's persistent length; must be positive.
	 * @param persistentMaximumLength the upper bound of the field's persistent length; must be positive.
	 *
	 * @return a new complex field description.
	 */
	public static PersistenceTypeDescriptionMemberFieldGenericComplex New(
		final String                                                         qualifier              ,
		final String                                                         name                   ,
		final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members                ,
		final long                                                           persistentMinimumLength,
		final long                                                           persistentMaximumLength
	)
	{
		return new PersistenceTypeDescriptionMemberFieldGenericComplex.Default(
			 mayNull(qualifier)              ,
			 notNull(name)                   ,
			 notNull(members)                ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}

	public class Default
	extends PersistenceTypeDescriptionMemberFieldGenericVariableLength.Default
	implements PersistenceTypeDescriptionMemberFieldGenericComplex
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final XImmutableSequence<PersistenceTypeDescriptionMemberFieldGeneric> members;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final String                                                         qualifier              ,
			final String                                                         name                   ,
			final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members                ,
			final long                                                           persistentMinimumLength,
			final long                                                           persistentMaximumLength
		)
		{
			super(
				PersistenceTypeDictionary.Symbols.TYPE_COMPLEX,
				qualifier,
				name,
				PersistenceTypeDescriptionMember.determineHasReferences(members),
				persistentMinimumLength,
				persistentMaximumLength
			);
			this.members = members.immure();
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members()
		{
			return this.members;
		}

		@Override
		public void assembleTypeDescription(final PersistenceTypeDescriptionMemberAppender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}

	}

}
