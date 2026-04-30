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

import org.eclipse.serializer.util.X;
import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.collections.HashEnum;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.util.cql.CQL;

/**
 * The persistence-relevant description of a single Java type: its full type name plus the ordered list of
 * persistable members (fields, primitive bit-layouts, enum constant entries) that together define the layout
 * of an instance's persistent form.
 * <p>
 * A {@code PersistenceTypeDescription} is the textual / structural counterpart to a Java {@link Class}. It
 * is the unit that gets serialized into a {@link PersistenceTypeDictionary} and read back when interpreting
 * persistent data &mdash; including data written by older versions of an application whose runtime classes
 * may have been renamed, restructured, or removed.
 * <p>
 * <b>Identity vs. description.</b> The combination of {@link #typeId()} and {@link #typeName()} (inherited
 * from {@link PersistenceTypeIdentity}) identifies the type within the dictionary; the member sequences
 * describe its structure. Two descriptions can have <i>equal structure</i> ({@link #equalStructure}) without
 * sharing the same typeId, and two descriptions can have <i>equal description</i> ({@link #equalDescription})
 * but represent different points in the type's evolution if their typeIds differ.
 * <p>
 * <b>Sub-types.</b> {@link PersistenceTypeDefinition} extends this interface to additionally bind the
 * description to a runtime {@link Class}; concrete instances are usually {@link PersistenceTypeDefinition}s
 * obtained from a {@link PersistenceTypeDictionary}. The plain {@code PersistenceTypeDescription} view is
 * what code that does <i>not</i> need a runtime class (e.g. dictionary I/O, refactoring mapping, structural
 * comparison) operates on.
 *
 * @see PersistenceTypeIdentity
 * @see PersistenceTypeDescriptionMember
 * @see PersistenceTypeDefinition
 * @see PersistenceTypeDictionary
 */
public interface PersistenceTypeDescription extends PersistenceTypeIdentity
{
	@Override
	public String typeName();

	/**
	 * All members described by this type, in their declared dictionary order. This includes instance
	 * members as well as non-instance entries such as {@linkplain PersistenceTypeDescriptionMemberPrimitiveDefinition
	 * primitive bit-layout definitions} and {@linkplain PersistenceTypeDescriptionMemberEnumConstant enum
	 * constant entries}.
	 *
	 * @return all members of this description.
	 *
	 * @see #instanceMembers()
	 */
	public XGettingSequence<? extends PersistenceTypeDescriptionMember> allMembers();

	/**
	 * The subset of {@link #allMembers()} that contributes to a persisted instance's binary layout, i.e.
	 * the actual instance fields. Primitive bit-layout entries and enum constant entries are excluded.
	 *
	 * @return the instance members of this description.
	 *
	 * @see #allMembers()
	 * @see #instanceReferenceMembers()
	 * @see #instancePrimitiveMembers()
	 */
	public XGettingSequence<? extends PersistenceTypeDescriptionMember> instanceMembers();

	/**
	 * The subset of {@link #instanceMembers()} that holds object references (i.e. members for which
	 * {@link PersistenceTypeDescriptionMember#isReference()} is {@code true}).
	 *
	 * @return the reference-typed instance members of this description.
	 */
	public default XGettingSequence<? extends PersistenceTypeDescriptionMember> instanceReferenceMembers()
	{
		return CQL
			.<PersistenceTypeDescriptionMember>from(this.instanceMembers())
			.select(m -> m.isReference())
			.executeInto(HashEnum.<PersistenceTypeDescriptionMember>New())
		;
	}

	/**
	 * The subset of {@link #instanceMembers()} that holds primitive values (i.e. members for which
	 * {@link PersistenceTypeDescriptionMember#isPrimitive()} is {@code true}).
	 *
	 * @return the primitive-typed instance members of this description.
	 */
	public default XGettingSequence<? extends PersistenceTypeDescriptionMember> instancePrimitiveMembers()
	{
		return CQL
			.<PersistenceTypeDescriptionMember>from(this.instanceMembers())
			.select(m -> m.isPrimitive())
			.executeInto(HashEnum.<PersistenceTypeDescriptionMember>New())
		;
	}
	
	/* (30.06.2015 TM)TODO: PersistenceTypeDescription Generics
	 * Must consider Generics Type information as well, at least as a simple normalized String for
	 * equality comparison.
	 * Otherwise, changing type parameter won't be recognized by the type validation and
	 * loading/building of entities will result in heap pollution (wrong instance for the type).
	 * Example:
	 * Lazy<Person> changed to Lazy<Employee>.
	 * Currently, this is just recognized as Lazy.
	 * 
	 * (13.09.2018 TM)NOTE: both here and in the member description
	 */
	
	
	/**
	 * The separator character used between {@code typeId} and {@code typeName} in the textual form
	 * produced by {@link #buildTypeIdentifier(long, String)}.
	 *
	 * @return the type-identifier separator character ({@code ':'}).
	 */
	public static char typeIdentifierSeparator()
	{
		return ':';
	}

	/**
	 * Builds the canonical textual form of a type identifier as
	 * {@code <typeId><separator><typeName>}, where {@code <separator>} is
	 * {@link #typeIdentifierSeparator()} (i.e. {@code ':'}); e.g. {@code "1234:com.example.Person"}.
	 * Used for diagnostic output and as a stable string key for refactoring-mapping lookups.
	 *
	 * @param typeId   the type id.
	 * @param typeName the fully qualified type name.
	 *
	 * @return the textual type identifier.
	 */
	public static String buildTypeIdentifier(final long typeId, final String typeName)
	{
		// simple string concatenation syntax messes up the char adding.
		return VarString.New(100).add(typeId).add(typeIdentifierSeparator()).add(typeName).toString();
	}

	/**
	 * Convenience overload of {@link #buildTypeIdentifier(long, String)} that reads {@code typeId}
	 * and {@code typeName} from the passed description.
	 *
	 * @param typeDescription the description whose identifier shall be built.
	 *
	 * @return the textual type identifier of {@code typeDescription}.
	 */
	public static String buildTypeIdentifier(final PersistenceTypeDescription typeDescription)
	{
		return buildTypeIdentifier(typeDescription.typeId(), typeDescription.typeName());
	}

	/**
	 * Returns this description's identifier in the canonical textual form produced by
	 * {@link #buildTypeIdentifier(PersistenceTypeDescription)}.
	 *
	 * @return this description's textual type identifier.
	 */
	public default String toTypeIdentifier()
	{
		return buildTypeIdentifier(this);
	}

	/**
	 * Tests whether two descriptions describe the same content: equal {@link #typeName()} and
	 * member-by-member equality via {@link PersistenceTypeDescriptionMember#equalDescription}. The
	 * {@link #typeId()} is intentionally <b>not</b> compared &mdash; this method asks "do these two
	 * descriptions describe the same shape?", regardless of which dictionary version they originated
	 * from.
	 *
	 * @param td1 the first description.
	 * @param td2 the second description.
	 *
	 * @return {@code true} if both descriptions are content-wise equal.
	 *
	 * @see #equalStructure(PersistenceTypeDescription, PersistenceTypeDescription)
	 */
	public static boolean equalDescription(
		final PersistenceTypeDescription td1,
		final PersistenceTypeDescription td2
	)
	{
		return td1 == td2 || td1 != null && td2 != null
			&& td1.typeName().equals(td1.typeName())
			&& PersistenceTypeDescriptionMember.equalDescriptions(td1.allMembers(), td2.allMembers())
		;
	}

	/**
	 * Like {@link #equalDescription(PersistenceTypeDescription, PersistenceTypeDescription)}, but compares
	 * members using {@link PersistenceTypeDescriptionMember#equalStructure}, which ignores the
	 * member-internal {@link PersistenceTypeDescriptionMember#qualifier() qualifier}. As a result, a
	 * reflective field description and a generic field description with the same simple name and type name
	 * are considered equal here &mdash; this is what legacy-type mapping needs to validate that a custom
	 * legacy handler is compatible with a reflectively derived current handler.
	 *
	 * @param td1 the first description.
	 * @param td2 the second description.
	 *
	 * @return {@code true} if both descriptions' structures are equal.
	 *
	 * @see #equalDescription(PersistenceTypeDescription, PersistenceTypeDescription)
	 */
	public static boolean equalStructure(
		final PersistenceTypeDescription td1,
		final PersistenceTypeDescription td2
	)
	{
		return td1 == td2 || td1 != null && td2 != null
			&& td1.typeName().equals(td1.typeName())
			&& PersistenceTypeDescriptionMember.equalStructures(td1.allMembers(), td2.allMembers())
		;
	}
	
	
	/**
	 * Creates a minimal {@link PersistenceTypeDescription} that carries only {@code typeId} and
	 * {@code typeName} and reports no members. This is useful for situations where only the type's
	 * identity is needed (e.g. dictionary lookup keys, diagnostic output) without the cost of
	 * constructing a full member sequence.
	 *
	 * @param typeId   the type id.
	 * @param typeName the fully qualified type name.
	 *
	 * @return a new identity-only {@link PersistenceTypeDescription}.
	 */
	public static PersistenceTypeDescription Identity(final long typeId, final String typeName)
	{
		return new PersistenceTypeDescription.Identity(typeId, typeName);
	}

	/**
	 * An identity-only {@link PersistenceTypeDescription} that reports no members. Created via
	 * {@link PersistenceTypeDescription#Identity(long, String)}.
	 */
	public final class Identity implements PersistenceTypeDescription
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final long   typeId  ;
		private final String typeName;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Identity(final long typeId, final String typeName)
		{
			super();
			this.typeId   = typeId  ;
			this.typeName = typeName;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long typeId()
		{
			return this.typeId;
		}

		@Override
		public final String typeName()
		{
			return this.typeName;
		}
		
		@Override
		public final XGettingSequence<? extends PersistenceTypeDescriptionMember> allMembers()
		{
			return X.empty();
		}

		@Override
		public final XGettingSequence<? extends PersistenceTypeDescriptionMember> instanceMembers()
		{
			return X.empty();
		}
		
	}
	
}
