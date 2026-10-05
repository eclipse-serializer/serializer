package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
 * %%
 * Copyright (C) 2023 - 2026 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import static org.eclipse.serializer.util.X.notNull;

import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.collections.types.XImmutableSequence;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;

/**
 * A field whose value is not referenced by an object id but written into the owner's own binary form,
 * described by a nested sequence of members that mirrors the field type's own persistent layout.
 * <p>
 * The persistent form is a null marker byte followed by that layout, so the slot is fixed-length and the
 * content after the marker is byte-identical to what the field type's own entity form would contain. A
 * marker of {@link #NULL_MARKER_ABSENT} states that the field is {@code null}; the remaining bytes of the
 * slot are then zero and carry no meaning.
 * <p>
 * Inlining a field this way removes one entity, and with it one object id, per owner. It is only applicable
 * to a field whose declared type is statically known to be exactly the type described here, since the
 * persistent form carries no type information of its own.
 *
 * @see PersistenceTypeDescriptionMemberFieldReflective
 */
public interface PersistenceTypeDescriptionMemberFieldValueStruct
extends PersistenceTypeDescriptionMemberFieldReflective
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	/**
	 * The marker value stating that the inlined field is {@code null}.
	 */
	public static final byte NULL_MARKER_ABSENT = 0;

	/**
	 * The marker value stating that the inlined field holds a value.
	 */
	public static final byte NULL_MARKER_PRESENT = 1;

	/**
	 * The length of the null marker preceding the inlined content.
	 */
	public static final long NULL_MARKER_LENGTH = Byte.BYTES;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Calculates the fixed persistent length of an inlined slot holding the passed members: the null marker
	 * plus every member's own fixed length.
	 *
	 * @param members the members describing the inlined layout.
	 *
	 * @return the fixed persistent length of the slot.
	 *
	 * @throws PersistenceException if a member is not fixed-length, which would make the slot unskippable.
	 */
	public static long calculateStructLength(
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
	)
	{
		long length = NULL_MARKER_LENGTH;

		for(final PersistenceTypeDescriptionMember member : members)
		{
			if(!member.isFixedLength())
			{
				throw new PersistenceException(
					"Variable length member " + member.identifier() + " cannot be inlined."
				);
			}
			length += member.persistentMinimumLength();
		}

		return length;
	}

	/**
	 * Validates that no member of an inlined layout holds a reference and returns the resulting
	 * {@code hasReferences} state, which is consequently always {@literal false}.
	 * <p>
	 * An inlined slot is skipped as one fixed-length block wherever references are traversed, most
	 * critically by the storage garbage collector: an object id inside one would be invisible to it,
	 * so the entity it points to would be collected while still referenced. Nothing downstream can
	 * detect that, so a reference-bearing layout is refused wherever such a member is created,
	 * dictionary parsing included.
	 *
	 * @param name    the described field's name, used for reporting.
	 * @param members the members describing the inlined layout.
	 *
	 * @return always {@literal false}.
	 *
	 * @throws PersistenceException if a member holds references.
	 */
	public static boolean validateNoReferences(
		final String                                                       name   ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
	)
	{
		for(final PersistenceTypeDescriptionMember member : members)
		{
			if(member.hasReferences())
			{
				throw new PersistenceException(
					"Member " + member.identifier() + " of the inlined field " + name + " holds references,"
					+ " which an inlined slot must not: they would be invisible to reference traversal,"
					+ " the storage garbage collector included."
				);
			}
		}

		return false;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	/**
	 * The ordered sequence of members describing the inlined layout, in the same order the field type's own
	 * entity form uses.
	 *
	 * @return the nested members.
	 */
	public XGettingSequence<? extends PersistenceTypeDescriptionMemberField> members();

	@Override
	public default boolean equalsDescription(final PersistenceTypeDescriptionMember other)
	{
		// does NOT call #equalsStructure to avoid redundant member iteration
		return PersistenceTypeDescriptionMember.equalTypeAndNameAndQualifier(this, other)
			&& other instanceof PersistenceTypeDescriptionMemberFieldValueStruct
			&& PersistenceTypeDescriptionMember.equalDescriptions(
				this.members(),
				((PersistenceTypeDescriptionMemberFieldValueStruct)other).members()
			)
		;
	}

	@Override
	public default boolean equalsStructure(final PersistenceTypeDescriptionMember other)
	{
		return PersistenceTypeDescriptionMemberFieldReflective.super.equalsStructure(other)
			&& other instanceof PersistenceTypeDescriptionMemberFieldValueStruct
			&& PersistenceTypeDescriptionMember.equalStructures(
				this.members(),
				((PersistenceTypeDescriptionMemberFieldValueStruct)other).members()
			)
		;
	}

	/**
	 * Unlike every other member kind, the inlined layout's own members are compared by name as well as by
	 * type, which is what {@link PersistenceTypeDescriptionMember#equalsStructure} does.
	 * <p>
	 * Ignoring the name is safe where the legacy mapping pairs the members by name separately and only the
	 * bytes still have to be confirmed, which is the case for the member this one is - the field of its
	 * owner. Nothing pairs the members <i>inside</i> the layout, though: they would be paired by position
	 * alone, so two same-typed fields that swapped places would read layout-equal and each take the other's
	 * value with nothing said. The member's own name stays out of it, as it does everywhere else.
	 */
	@Override
	public default boolean equalsLayout(final PersistenceTypeDescriptionMember other)
	{
		return PersistenceTypeDescriptionMemberFieldReflective.super.equalsLayout(other)
			&& other instanceof PersistenceTypeDescriptionMemberFieldValueStruct
			&& PersistenceTypeDescriptionMember.equalStructures(
				this.members(),
				((PersistenceTypeDescriptionMemberFieldValueStruct)other).members()
			)
		;
	}

	@Override
	public default PersistenceTypeDefinitionMemberFieldValueStruct createDefinitionMember(
		final PersistenceTypeDefinitionMemberCreator creator
	)
	{
		return creator.createDefinitionMember(this);
	}

	/**
	 * Creates an inlined field description.
	 *
	 * @param typeName          the inlined field type's name; must not be {@code null}.
	 * @param declaringTypeName the fully qualified name of the declaring class; must not be {@code null}.
	 * @param name              the field's simple name; must not be {@code null}.
	 * @param members           the members describing the inlined layout; must not be {@code null}.
	 *
	 * @return a new inlined field description.
	 */
	public static PersistenceTypeDescriptionMemberFieldValueStruct New(
		final String typeName         ,
		final String declaringTypeName,
		final String name             ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMemberField> members
	)
	{
		return new PersistenceTypeDescriptionMemberFieldValueStruct.Default(
			notNull(typeName)         ,
			notNull(declaringTypeName),
			notNull(name)             ,
			notNull(members)
		);
	}

	public class Default
	extends PersistenceTypeDescriptionMemberField.Abstract
	implements PersistenceTypeDescriptionMemberFieldValueStruct
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final XImmutableSequence<? extends PersistenceTypeDescriptionMemberField> members;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final String typeName         ,
			final String declaringTypeName,
			final String name             ,
			final XGettingSequence<? extends PersistenceTypeDescriptionMemberField> members
		)
		{
			// the slot is fixed length, so the same value is both bounds; walking the members twice for it
			// would be pointless, and a super() call cannot be preceded by a local at this language level.
			this(typeName, declaringTypeName, name, members, calculateStructLength(members));
		}

		private Default(
			final String typeName         ,
			final String declaringTypeName,
			final String name             ,
			final XGettingSequence<? extends PersistenceTypeDescriptionMemberField> members,
			final long   structLength
		)
		{
			super(
				typeName         ,
				declaringTypeName,
				name             ,
				false            , // not a reference: the content is inlined, not pointed to
				false            , // not a primitive: it is a composite of its own members
				validateNoReferences(name, members),
				structLength     ,
				structLength
			);
			this.members = members.immure();
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public XGettingSequence<? extends PersistenceTypeDescriptionMemberField> members()
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
