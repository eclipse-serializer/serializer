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

import org.eclipse.serializer.collections.HashEnum;
import org.eclipse.serializer.collections.types.XEnum;
import org.eclipse.serializer.reflect.XReflect;

/**
 * Strategy for turning a {@code (typeDefinition, member)} pair into the textual identifier used as a key in
 * the user-supplied refactoring map ({@link PersistenceRefactoringMapping}). Several builders are typically
 * tried in priority order: a refactoring rule matches as soon as one builder produces the same identifier on
 * both sides of the rename.
 * <p>
 * Bundled builders, in descending priority:
 * <ul>
 * <li>{@link #toTypeIdIdentifier} &mdash; {@code <typeId>:<typeName>#<memberIdentifier>}; unambiguous because
 * it includes the type id.</li>
 * <li>{@link #toGlobalNameIdentifier} &mdash; {@code <typeName>#<memberIdentifier>}; ambiguous if the same
 * member appears in multiple historical type definitions of the same name.</li>
 * <li>{@link #toTypeInternalIdentifier} &mdash; just the member's qualified identifier; ambiguous across
 * unrelated types.</li>
 * <li>{@link #toUniqueUnqualifiedIdentifier} &mdash; {@code #<simpleName>}; only valid when the simple name
 * is unique within its enclosing type. Reserved for the current/runtime side, which is the side that can
 * still introspect ambiguity.</li>
 * </ul>
 *
 * @see PersistenceRefactoringTypeIdentifierBuilder
 * @see PersistenceRefactoringMapping
 */
public interface PersistenceRefactoringMemberIdentifierBuilder
{
	/**
	 * Builds the textual identifier for {@code member} in the context of {@code typeDef}. Returning
	 * {@code null} indicates that this builder cannot produce an unambiguous identifier and should be
	 * skipped.
	 *
	 * @param typeDef the enclosing type definition.
	 * @param member  the member to identify.
	 *
	 * @return the textual identifier, or {@code null} if this builder cannot produce one.
	 */
	public String buildMemberIdentifier(PersistenceTypeDefinition typeDef, PersistenceTypeDescriptionMember member);


	/**
	 * Default builder set for the legacy (source) side of a refactoring mapping: type-id-qualified, global
	 * name, and type-internal identifier. The unqualified-simple-name builder is intentionally excluded
	 * because legacy types may not be introspectable for ambiguity.
	 *
	 * @return the default legacy member identifier builders, in priority order.
	 */
	public static XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> createDefaultRefactoringLegacyMemberIdentifierBuilders()
	{
		return HashEnum.New(
			PersistenceRefactoringMemberIdentifierBuilder::toTypeIdIdentifier      ,
			PersistenceRefactoringMemberIdentifierBuilder::toGlobalNameIdentifier  ,
			PersistenceRefactoringMemberIdentifierBuilder::toTypeInternalIdentifier
		);
	}

	/**
	 * Default builder set for the current (target) side of a refactoring mapping: same as the legacy set
	 * plus the unqualified-simple-name builder, which is safe here because the runtime types can be
	 * introspected to verify uniqueness.
	 *
	 * @return the default current member identifier builders, in priority order.
	 */
	public static XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> createDefaultRefactoringCurrentMemberIdentifierBuilders()
	{
		return HashEnum.New(
			PersistenceRefactoringMemberIdentifierBuilder::toTypeIdIdentifier           ,
			PersistenceRefactoringMemberIdentifierBuilder::toGlobalNameIdentifier       ,
			PersistenceRefactoringMemberIdentifierBuilder::toTypeInternalIdentifier     ,
			PersistenceRefactoringMemberIdentifierBuilder::toUniqueUnqualifiedIdentifier
		);
	}

	/**
	 * The character separating the type-side prefix from the member name in the textual identifiers
	 * (typically {@code '#'}, sourced from {@link XReflect#fieldIdentifierDelimiter()}).
	 *
	 * @return the delimiter character.
	 */
	public static char memberDelimiter()
	{
		return XReflect.fieldIdentifierDelimiter();
	}

	/**
	 * Type-id-qualified member identifier:
	 * {@code <typeId>:<typeName><delimiter><memberIdentifier>}. Unambiguous because it includes the type
	 * id.
	 *
	 * @param typeDefinition the enclosing type definition.
	 * @param member         the member to identify.
	 *
	 * @return the type-id-qualified identifier.
	 */
	public static String toTypeIdIdentifier(
		final PersistenceTypeDefinition     typeDefinition,
		final PersistenceTypeDescriptionMember member
	)
	{
		return typeDefinition.toTypeIdentifier() + memberDelimiter() + toTypeInternalIdentifier(member);
	}

	/**
	 * Global-name member identifier: {@code <typeName><delimiter><memberIdentifier>}. Ambiguous if multiple
	 * historical type definitions share the same {@code typeName}.
	 *
	 * @param typeDefinition the enclosing type definition.
	 * @param member         the member to identify.
	 *
	 * @return the global-name identifier.
	 */
	public static String toGlobalNameIdentifier(
		final PersistenceTypeDefinition     typeDefinition,
		final PersistenceTypeDescriptionMember member
	)
	{
		return typeDefinition.typeName() + memberDelimiter() + toTypeInternalIdentifier(member);
	}

	/**
	 * Type-internal member identifier: just the member's qualified {@link PersistenceTypeDescriptionMember#identifier()}.
	 * Ambiguous across unrelated types but useful for refactoring rules that don't depend on the enclosing type.
	 *
	 * @param typeDefinition the enclosing type definition (unused; present for the functional-interface
	 *                       signature).
	 * @param member         the member to identify.
	 *
	 * @return the type-internal identifier.
	 */
	public static String toTypeInternalIdentifier(
		final PersistenceTypeDefinition     typeDefinition,
		final PersistenceTypeDescriptionMember member
	)
	{
		return toTypeInternalIdentifier(member);
	}

	/**
	 * Convenience overload of {@link #toTypeInternalIdentifier(PersistenceTypeDefinition, PersistenceTypeDescriptionMember)}
	 * that just returns {@code member.identifier()}.
	 *
	 * @param member the member to identify.
	 *
	 * @return the member's qualified identifier.
	 */
	public static String toTypeInternalIdentifier(final PersistenceTypeDescriptionMember member)
	{
		return member.identifier();
	}

	/**
	 * Unqualified-simple-name member identifier: {@code <delimiter><simpleName>}. Returns {@code null} if
	 * the simple name is not unique within {@code typeDefinition}, since an ambiguous name cannot serve as
	 * a refactoring key.
	 *
	 * @param typeDefinition the enclosing type definition.
	 * @param member         the member to identify.
	 *
	 * @return the unqualified identifier, or {@code null} if the simple name is ambiguous.
	 */
	public static String toUniqueUnqualifiedIdentifier(
		final PersistenceTypeDefinition        typeDefinition,
		final PersistenceTypeDescriptionMember member
	)
	{
		final String memberSimpleName = member.name();
		
		for(final PersistenceTypeDescriptionMember m : typeDefinition.allMembers())
		{
			if(m == member)
			{
				continue;
			}
			
			// if the simple name is not unique, it cannot be used as a mapping target
			if(m.name().equals(memberSimpleName))
			{
				return null;
			}
		}
		
		return memberDelimiter() + memberSimpleName;
	}
	
}
