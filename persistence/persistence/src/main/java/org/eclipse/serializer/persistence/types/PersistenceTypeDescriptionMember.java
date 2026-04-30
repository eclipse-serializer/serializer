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

import java.util.Iterator;
import java.util.Objects;

import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.equality.Equalator;
import org.eclipse.serializer.hashing.HashEqualator;
import org.eclipse.serializer.math.XMath;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;


/**
 * One entry within a {@link PersistenceTypeDescription}'s member sequence.
 * <p>
 * A member entry comes in one of three flavors:
 * <ul>
 * <li><b>Instance field</b> &mdash; a real persistable field of an object's binary form. Either
 *     {@linkplain PersistenceTypeDescriptionMemberFieldReflective reflective} (derived from a Java
 *     {@link java.lang.reflect.Field}) or {@linkplain PersistenceTypeDescriptionMemberFieldGeneric generic}
 *     (custom-defined in the type dictionary).</li>
 * <li><b>Primitive bit-layout definition</b> &mdash; a non-instance member that records the persistent length
 *     of a Java primitive type (see {@link PersistenceTypeDescriptionMemberPrimitiveDefinition}).</li>
 * <li><b>Enum constant entry</b> &mdash; a non-instance member that records the persistent name of a single
 *     enum constant (see {@link PersistenceTypeDescriptionMemberEnumConstant}).</li>
 * </ul>
 * <p>
 * <b>Identifier model.</b> A member is uniquely identified within its containing type description by
 * {@link #identifier()}, typically composed of a {@link #qualifier()} (e.g. the declaring class name for
 * reflective fields) and a {@link #name()} (the simple field name). The qualifier is what disambiguates
 * private fields with the same name inherited along a class hierarchy.
 * <p>
 * <b>Length model.</b> {@link #persistentMinimumLength()} and {@link #persistentMaximumLength()} bound the
 * member's contribution to an instance's persistent form. Equal min/max means a fixed-length member;
 * different min/max means variable length (e.g. a {@code byte[]} or a complex collection entry). The unit
 * of length is the persistent form's unit (typically bytes for the binary persister).
 * <p>
 * <b>Equality.</b> Two members compare equal at three different granularities, in increasing strictness:
 * {@link #equalsStructure(PersistenceTypeDescriptionMember) equalsStructure} (same type and simple name),
 * {@link #equalsDescription(PersistenceTypeDescriptionMember) equalsDescription} (additionally the same
 * qualifier) and {@link #isIdentical(PersistenceTypeDescriptionMember) isIdentical} (same full identifier).
 *
 * @see PersistenceTypeDescription
 * @see PersistenceTypeDescriptionMemberField
 * @see PersistenceTypeDescriptionMemberPrimitiveDefinition
 * @see PersistenceTypeDescriptionMemberEnumConstant
 */
public interface PersistenceTypeDescriptionMember
{
	/**
	 * The textual name of this member's type as it appears in the type dictionary. For reflective fields
	 * this is the field's declared type name; for generic fields it is the dictionary keyword identifying
	 * the persistent shape (e.g. {@code [byte]}, {@code [list]}); for primitive definitions and enum
	 * constants it may be {@code null} or a reserved keyword.
	 *
	 * @return this member's textual type name, or {@code null} if not applicable.
	 */
	public String typeName();
	
	/**
	 * A type-internal qualifier to distinct different members with equal "primary" name. E.g. reflection-based
	 * type handling where fields names are only unique in combination with their declaring class.
	 * <p>
	 * May be {@code null} if not applicable.
	 * 
	 * @return the member's qualifier string to ensure a unique {@link #identifier()} in a group of members.
	 */
	public String qualifier();
	
	/**
	 * The name of the member identifying it in its parent group of members.<br>
	 * E.g. "com.my.app.entities.Person#lastname".
	 * <p>
	 * May never be {@code null}.
	 * 
	 * @return the member's uniquely identifying name.
	 */
	public String identifier();
	
	/**
	 * The simple or "primary" name of the member, if applicable. E.g. "lastName".
	 * <p>
	 * May be {@code null} if not applicable.
	 * 
	 * @return the member's simple name.
	 */
	public String name();
	
	/**
	 * Whether this member contributes to a persisted instance's binary form. Returns {@code true} for
	 * instance fields, {@code false} for non-instance entries such as primitive bit-layout definitions
	 * and enum constant entries.
	 *
	 * @return {@code true} if this member is an instance field, {@code false} otherwise.
	 */
	public boolean isInstanceMember();

	/**
	 * {@link #equalsStructure(PersistenceTypeDescriptionMember)} plus {@link #qualifier()} equality,
	 * to check if a member is really content-wise equal.
	 * 
	 * @param other the description to compare to
	 * @return if this and the other description are equal
	 * 
	 * @see #equalsStructure(PersistenceTypeDescriptionMember)
	 */
	public default boolean equalsDescription(final PersistenceTypeDescriptionMember other)
	{
		// calls #equalsStructure to include inheritance overrides. Important!
		return this.equalsStructure(other) && other != null
			&& Objects.equals(this.qualifier(), other.qualifier())
		;
	}

	/**
	 * Structure means equal order of members by type name and simple name.<br>
	 * Not qualifier, since that is only required for intra-type field identification
	 * 
	 * @param other the description to compare to
	 * @return if this and the other description's structure are equal
	 * 
	 * @see #equalDescription(PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember)
	 */
	public default boolean equalsStructure(final PersistenceTypeDescriptionMember other)
	{
		return equalTypeAndName(this, other);
	}
	
	/**
	 * Tests whether two members have the same {@link #typeName()} and {@link #name()}. The comparison
	 * is null-safe so that primitive-definition entries (which return {@code null} for both attributes)
	 * compare equal to each other when their primitive definitions match elsewhere.
	 *
	 * @param m1 the first member.
	 * @param m2 the second member.
	 *
	 * @return {@code true} if {@code typeName} and {@code name} are equal.
	 */
	public static boolean equalTypeAndName(
		final PersistenceTypeDescriptionMember m1,
		final PersistenceTypeDescriptionMember m2
	)
	{
		// attribute checks must be null-safe equals because of primitive definitions
		return m1 == m2 || m2 != null
			&& Objects.equals(m1.typeName(), m2.typeName())
			&& Objects.equals(m1.name()    , m2.name()    )
		;
	}

	/**
	 * Like {@link #equalTypeAndName(PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember)},
	 * additionally requiring equal {@link #qualifier()}.
	 *
	 * @param m1 the first member.
	 * @param m2 the second member.
	 *
	 * @return {@code true} if {@code typeName}, {@code name} and {@code qualifier} are equal.
	 */
	public static boolean equalTypeAndNameAndQualifier(
		final PersistenceTypeDescriptionMember m1,
		final PersistenceTypeDescriptionMember m2
	)
	{
		// attribute checks must be null-safe equals because of primitive definitions
		return equalTypeAndName(m1, m2) && m2 != null
			&& Objects.equals(m1.qualifier(), m2.qualifier())
		;
	}
	
	/**
	 * Tests whether the passed  {@link PersistenceTypeDescriptionMember} have the same "intended" structure, meaning
	 * same order of fields with same simple name (PersistenceTypeDescriptionMember{@link #name()}) and type name. <br>
	 * For example:<br>
	 * A {@link PersistenceTypeDescriptionMemberFieldReflective} and a {@link PersistenceTypeDescriptionMemberFieldGeneric}
	 * with different member qualifiers are still considered equal.<br>
	 * This is necessary for legacy type mapping to being able to write a custom legacy type handler that is
	 * compatible with a generic type handler derived from reflective information.
	 * 
	 * @param m1 the first member
	 * @param m2 the second member
	 * @return if the two members have the same structure
	 */
	public static boolean equalStructure(
		final PersistenceTypeDescriptionMember m1,
		final PersistenceTypeDescriptionMember m2
	)
	{
		// must delegate to the implementation since complex fields must deep-check their nested fields
		return m1 == m2 || m1 != null && m1.equalsStructure(m2);
	}
	
	/**
	 * Static counterpart to {@link #equalsDescription(PersistenceTypeDescriptionMember)} that handles a
	 * {@code null} {@code m1}. Delegation to the instance method is required so that complex (nested)
	 * member descriptions can deep-check their nested members.
	 *
	 * @param m1 the first member.
	 * @param m2 the second member.
	 *
	 * @return {@code true} if both members have equal description.
	 */
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMember m1,
		final PersistenceTypeDescriptionMember m2
	)
	{
		// must delegate to the implementation since complex fields must deep-check their nested fields
		return m1 == m2 || m1 != null && m1.equalsDescription(m2);
	}



	/**
	 * Adds the {@linkplain #persistentMinimumLength() persistent minimum length} of every passed member
	 * to {@code startValue}, using saturating addition (the result is capped at {@link Long#MAX_VALUE}
	 * rather than overflowing).
	 *
	 * @param startValue the initial length value.
	 * @param members    the members whose minimum lengths shall be summed in.
	 *
	 * @return the resulting summed minimum length, capped at {@link Long#MAX_VALUE}.
	 */
	public static long calculatePersistentMinimumLength(
		final long                                                 startValue,
		final Iterable<? extends PersistenceTypeDescriptionMember> members
	)
	{
		long length = startValue;
		for(final PersistenceTypeDescriptionMember member : members)
		{
			length = XMath.addCapped(length, member.persistentMinimumLength());
		}
		
		return length;
	}
	
	/**
	 * Adds the {@linkplain #persistentMaximumLength() persistent maximum length} of every passed member
	 * to {@code startValue}, using saturating addition (the result is capped at {@link Long#MAX_VALUE}
	 * rather than overflowing).
	 *
	 * @param startValue the initial length value.
	 * @param members    the members whose maximum lengths shall be summed in.
	 *
	 * @return the resulting summed maximum length, capped at {@link Long#MAX_VALUE}.
	 */
	public static long calculatePersistentMaximumLength(
		final long                                                 startValue,
		final Iterable<? extends PersistenceTypeDescriptionMember> members
	)
	{
		long length = startValue;
		for(final PersistenceTypeDescriptionMember member : members)
		{
			length = XMath.addCapped(length, member.persistentMaximumLength());
		}

		return length;
	}


	/**
	 * Visitor entry point: the member dispatches itself to the appropriate
	 * {@link PersistenceTypeDescriptionMemberAppender#appendTypeMemberDescription} overload so that the
	 * appender can render this member into a textual type-dictionary form. Each subtype implements this
	 * method to call the visitor overload that matches its own kind.
	 *
	 * @param assembler the appender that receives this member's textual rendition.
	 */
	public void assembleTypeDescription(PersistenceTypeDescriptionMemberAppender assembler);

	/**
	 * @return if this member directly is a reference.
	 *
	 */
	public boolean isReference();

	/**
	 * @return if this member is primitive value.
	 *
	 */
	public boolean isPrimitive();

	/**
	 * @return if this member is a primitive type definition instead of a field definition.
	 *
	 */
	public boolean isPrimitiveDefinition();

	/**
	 * @return if this member is an enum constant name definition instead of an instance field definition.
	 */
	public boolean isEnumConstant();

	/**
	 * @return if this field contains references. Either because it is a reference itself,
	 * see {@link #isReference()}, or because it is a complex type that contains one or more
	 * nested members that have references.
	 *
	 */
	public boolean hasReferences();

	/**
	 * @return {@code true} if {@link #persistentMinimumLength()} differs from
	 *         {@link #persistentMaximumLength()} (e.g. variable-length array fields).
	 */
	public default boolean isVariableLength()
	{
		return this.persistentMinimumLength() != this.persistentMaximumLength();
	}

	/**
	 * @return {@code true} if {@link #persistentMinimumLength()} equals
	 *         {@link #persistentMaximumLength()} (e.g. primitives and references).
	 */
	public default boolean isFixedLength()
	{
		return this.persistentMinimumLength() == this.persistentMaximumLength();
	}

	/**
	 * Returns the lowest possible length value that a member of the persistent form for values of the type
	 * represented by this instance can have.
	 * The precise meaning of the length value depends on the actual persistence form.
	 *
	 * @return the persistent form length of null if variable length.
	 * @see #persistentMaximumLength()
	 */
	public long persistentMinimumLength();

	/**
	 * Returns the highest possible length value that a member of the persistent form for values of the type
	 * represented by this instance can have.
	 * The precise meaning of the length value depends on the actual persistence form.
	 *
	 * @return the persistent form length of null if variable length.
	 * @see #persistentMinimumLength()
	 */
	public long persistentMaximumLength();

	/**
	 * Tests whether the passed length is consistent with this member's persistent length range
	 * &mdash; i.e. whether it lies within {@code [persistentMinimumLength, persistentMaximumLength]}.
	 *
	 * @param persistentLength the length value to validate.
	 *
	 * @return {@code true} if the length is valid for this member.
	 */
	public boolean isValidPersistentLength(long persistentLength);

	/**
	 * Like {@link #isValidPersistentLength(long)} but throws a
	 * {@link PersistenceException} if the length is outside the permitted range.
	 *
	 * @param persistentLength the length value to validate.
	 *
	 * @throws PersistenceException if the length is invalid.
	 */
	public void validatePersistentLength(long persistentLength);

	/**
	 * Tests whether this member has the same {@link #identifier()} as the passed member &mdash; the
	 * strictest equality used between members. Used as the identity key when collecting members in
	 * identifier-keyed collections.
	 *
	 * @param other the member to compare to.
	 *
	 * @return {@code true} if both members share the same identifier.
	 *
	 * @see #identityHashEqualator()
	 */
	public default boolean isIdentical(final PersistenceTypeDescriptionMember other)
	{
		return isIdentical(this, other);
	}

	/**
	 * Static null-safe counterpart to {@link #isIdentical(PersistenceTypeDescriptionMember)}.
	 *
	 * @param m1 the first member.
	 * @param m2 the second member.
	 *
	 * @return {@code true} if both members share the same identifier.
	 */
	public static boolean isIdentical(
		final PersistenceTypeDescriptionMember m1,
		final PersistenceTypeDescriptionMember m2
	)
	{
		return m1 == m2 || m1 != null && m2 != null
			&& m1.identifier().equals(m2.identifier())
		;
	}

	/**
	 * The hash function paired with {@link #isIdentical(PersistenceTypeDescriptionMember,
	 * PersistenceTypeDescriptionMember)}: the hash of the member's {@link #identifier()}, or {@code 0}
	 * for {@code null}.
	 *
	 * @param member the member to hash, or {@code null}.
	 *
	 * @return the identity hash.
	 */
	public static int identityHash(final PersistenceTypeDescriptionMember member)
	{
		return member == null
			? 0
			: member.identifier().hashCode()
		;
	}

	/**
	 * The shared {@link HashEqualator} singleton implementing identifier-based equality, suitable for
	 * keying members in identifier-indexed hash collections.
	 *
	 * @return the identity hash equalator.
	 */
	public static IdentityHashEqualator identityHashEqualator()
	{
		return IdentityHashEqualator.SINGLETON;
	}
	
	/**
	 * {@link HashEqualator} implementation that pairs {@link #identityHash(PersistenceTypeDescriptionMember)}
	 * with {@link #isIdentical(PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember)}.
	 * Use {@link #identityHashEqualator()} to obtain the singleton.
	 */
	public final class IdentityHashEqualator implements HashEqualator<PersistenceTypeDescriptionMember>
	{
		static final PersistenceTypeDescriptionMember.IdentityHashEqualator SINGLETON =
			new PersistenceTypeDescriptionMember.IdentityHashEqualator(
		);

		@Override
		public final int hash(final PersistenceTypeDescriptionMember member)
		{
			return identityHash(member);
		}

		@Override
		public final boolean equal(
			final PersistenceTypeDescriptionMember m1,
			final PersistenceTypeDescriptionMember m2
		)
		{
			return isIdentical(m1, m2);
		}
		
	}
		
	/**
	 * @param members the members to inspect.
	 *
	 * @return {@code true} if any of the passed members reports {@link #hasReferences()}.
	 */
	public static boolean determineHasReferences(final Iterable<? extends PersistenceTypeDescriptionMember> members)
	{
		for(final PersistenceTypeDescriptionMember member : members)
		{
			if(member.hasReferences())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Determines whether the passed member sequence describes a primitive type, i.e. consists of exactly
	 * one {@linkplain #isPrimitiveDefinition() primitive bit-layout entry}.
	 *
	 * @param members the members to inspect.
	 *
	 * @return {@code true} if {@code members} describes a primitive type.
	 */
	public static boolean determineIsPrimitive(
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
	)
	{
		return members.size() == 1 && members.get().isPrimitiveDefinition();
	}

	/**
	 * Pairwise compares two member sequences using
	 * {@link #equalDescription(PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember)}.
	 *
	 * @param members1 the first sequence.
	 * @param members2 the second sequence.
	 *
	 * @return {@code true} if the sequences are pairwise equal in description.
	 */
	public static boolean equalDescriptions(
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members1,
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members2
	)
	{
		return equalMembers(members1, members2, PersistenceTypeDescriptionMember::equalDescription);
	}

	/**
	 * Pairwise compares two member sequences using
	 * {@link #equalStructure(PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember)}.
	 *
	 * @param members1 the first sequence.
	 * @param members2 the second sequence.
	 *
	 * @return {@code true} if the sequences are pairwise equal in structure.
	 */
	public static boolean equalStructures(
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members1,
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members2
	)
	{
		return equalMembers(members1, members2, PersistenceTypeDescriptionMember::equalStructure);
	}

	/**
	 * Pairwise iterates two member sequences in lock-step and applies the passed {@link Equalator} to
	 * every pair. The iteration intentionally proceeds while <i>either</i> iterator has elements, padding
	 * the exhausted side with {@code null}, so the equalator can decide how to handle size mismatches.
	 *
	 * @param members1  the first sequence.
	 * @param members2  the second sequence.
	 * @param equalator the equalator to apply to each pair.
	 *
	 * @return {@code true} if every pair is equal under {@code equalator}.
	 */
	public static boolean equalMembers(
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members1 ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members2 ,
		final Equalator<PersistenceTypeDescriptionMember>                  equalator
	)
	{
		// (01.07.2015 TM)NOTE: must iterate explicitly to guarantee equalator calls (avoid size-based early-aborting)
		final Iterator<? extends PersistenceTypeDescriptionMember> it1 = members1.iterator();
		final Iterator<? extends PersistenceTypeDescriptionMember> it2 = members2.iterator();

		// intentionally OR to give equalator a chance to handle size mismatches as well (indicated by null)
		while(it1.hasNext() || it2.hasNext())
		{
			final PersistenceTypeDescriptionMember member1 = it1.hasNext() ? it1.next() : null;
			final PersistenceTypeDescriptionMember member2 = it2.hasNext() ? it2.next() : null;

			if(!equalator.equal(member1, member2))
			{
				return false;
			}
		}

		// neither member-member mismatch nor size mismatch, so members must be in order and equal
		return true;
	}

	
	/**
	 * Visitor counterpart to {@link #assembleTypeDescription(PersistenceTypeDescriptionMemberAppender)}
	 * that lifts this member from its description-only form to a {@link PersistenceTypeDefinitionMember},
	 * i.e. a member additionally bound to a runtime representation. The implementation dispatches to the
	 * matching overload of {@link PersistenceTypeDefinitionMemberCreator}.
	 *
	 * @param creator the creator producing the runtime-bound definition member.
	 *
	 * @return the definition-member counterpart of this description member.
	 */
	public PersistenceTypeDefinitionMember createDefinitionMember(PersistenceTypeDefinitionMemberCreator creator);

}
