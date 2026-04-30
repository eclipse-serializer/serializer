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
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.collections.types.XImmutableEnum;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.typing.KeyValue;

/**
 * Resolves textual identifiers found in a stored {@link PersistenceTypeDictionary} to the current
 * runtime types and members of the application, applying any configured {@link PersistenceRefactoringMapping}
 * along the way.
 * <p>
 * The resolver is the central plumbing for type evolution: when a stored class has been renamed, moved
 * to another package, had fields renamed, or had members removed, the refactoring mapping projects
 * outdated identifiers (typically {@code className#fieldName}, but for root instances also arbitrary
 * strings) onto current identifiers. A {@code null} mapping value signals deliberate deletion &mdash;
 * the type or member has no current runtime counterpart.
 * <p>
 * Type-level resolution proceeds via {@link #resolveRuntimeTypeName(PersistenceTypeDescription)} (and the
 * convenience overloads {@link #resolveRuntimeType} / {@link #tryResolveRuntimeType}); member-level
 * resolution via {@link #resolveMember}. The type-name overload of {@code resolveRuntimeTypeName} is
 * used for plain string identifiers (e.g. root names), while the {@link PersistenceTypeDescription} overload
 * walks all configured {@link PersistenceRefactoringTypeIdentifierBuilder}s in priority order.
 *
 * @see PersistenceRefactoringMapping
 * @see PersistenceTypeDescriptionResolverProvider
 */
public interface PersistenceTypeDescriptionResolver extends PersistenceTypeResolver
{
	/**
	 * Returns a key-value pair with the passed source identifier as the key and a mapped target identifier
	 * as the value. The value can be potentially null to indicate deletion.
	 * If the lookup did not yield any result, {@code null} is returned.
	 *
	 * @param sourceIdentifier the source identifier
	 * @return a key-value pair with the passed source identifier as the key and a mapped target identifier
	 * as the value
	 */
	public KeyValue<String, String> lookup(String sourceIdentifier);

	/**
	 * Resolves the current runtime type name for the passed {@link PersistenceTypeDescription} by walking
	 * the configured source-type identifier builders in descending priority and consulting the refactoring
	 * mapping for each. Returns the description's own {@code typeName} if no mapping entry applies, or
	 * {@code null} if a mapping entry explicitly marks the type as deleted (no runtime counterpart).
	 *
	 * @param typeDescription the type description to resolve.
	 *
	 * @return the current runtime type name, or {@code null} if the type is mapped to "deleted".
	 */
	public String resolveRuntimeTypeName(PersistenceTypeDescription typeDescription);

	/**
	 * Resolves a plain textual type-name identifier &mdash; e.g. a root-instance name &mdash; using the
	 * refactoring mapping's direct lookup. If no mapping entry exists, the input is returned unchanged.
	 * If a mapping entry maps to {@code null}, {@code null} is returned to indicate deletion.
	 *
	 * @param descriptionTypeName the source-side identifier.
	 *
	 * @return the mapped current identifier, the input unchanged, or {@code null} for deletion.
	 */
	public default String resolveRuntimeTypeName(final String descriptionTypeName)
	{
		final KeyValue<String, String> entry = this.lookup(descriptionTypeName);
		
		if(entry == null)
		{
			// no mapping entry, return the descriptionTypeName itself
			return descriptionTypeName;
		}

		// can be null for types explicitly marked as no more having a runtime type (unreachable / "deleted")
		return entry.value();
	}
	

	
	/**
	 * Resolves and {@linkplain PersistenceTypeResolver#resolveType class-loads} the current runtime
	 * {@link Class} for the passed type description. Throws if the resolved name cannot be loaded.
	 *
	 * @param typeDescription the type description to resolve.
	 *
	 * @return the runtime {@link Class}, or {@code null} if the type is mapped to "deleted".
	 */
	public default Class<?> resolveRuntimeType(final PersistenceTypeDescription typeDescription)
	{
		final String runtimeTypeName = this.resolveRuntimeTypeName(typeDescription);
		return this.resolveType(runtimeTypeName);
	}

	/**
	 * Like {@link #resolveRuntimeType(PersistenceTypeDescription)}, but returns {@code null} instead of
	 * throwing if the resolved name cannot be loaded. Use when missing runtime types are expected and
	 * should be handled by the caller (e.g. legacy-handler synthesis).
	 *
	 * @param typeDescription the type description to resolve.
	 *
	 * @return the runtime {@link Class}, or {@code null} if not loadable / deleted.
	 */
	public default Class<?> tryResolveRuntimeType(final PersistenceTypeDescription typeDescription)
	{
		final String runtimeTypeName = this.resolveRuntimeTypeName(typeDescription);
		return this.tryResolveType(runtimeTypeName);
	}
	
	/**
	 * Returns a key-value pair with the passed source member as the key and a mapped target member
	 * as the value. The value can be potentially null to indicate deletion.
	 * If the lookup did not yield any result, {@code null} is returned.
	 * 
	 * @param sourceType the source type
	 * @param sourceMember the source member
	 * @param targetType the target type
	 * @return a key-value pair with the passed source member as the key and a mapped target member
	 * as the value
	 */
	public KeyValue<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> resolveMember(
		PersistenceTypeDefinition       sourceType  ,
		PersistenceTypeDefinitionMember sourceMember,
		PersistenceTypeDefinition       targetType
	);

	/**
	 * Tests whether the passed current-type member is explicitly registered as <i>new</i> in the
	 * refactoring mapping &mdash; i.e. a member that legacy data is not expected to provide a value for
	 * and that the legacy-mapping logic must therefore initialize from scratch rather than rejecting it
	 * as an unmapped addition.
	 *
	 * @param currentTypeDefinition the current type definition.
	 * @param currentTypeMember     the current member.
	 *
	 * @return {@code true} if the member is registered as new.
	 */
	public boolean isNewCurrentTypeMember(
		PersistenceTypeDefinition       currentTypeDefinition,
		PersistenceTypeDefinitionMember currentTypeMember
	);


	/**
	 * Creates a new {@link PersistenceTypeDescriptionResolver}.
	 *
	 * @param typeResolver                   the {@link PersistenceTypeResolver} used for class loading.
	 * @param refactoringMapping             the underlying mapping consulted for identifier rewrites.
	 * @param sourceTypeIdentifierBuilders   builders that produce candidate source-type identifiers, tried
	 *                                       in descending priority.
	 * @param sourceMemberIdentifierBuilders builders that produce candidate source-member identifiers,
	 *                                       tried in descending priority.
	 * @param targetMemberIdentifierBuilders builders used to recognize the target member named by a mapping
	 *                                       entry's value, and to test whether a current member is a "new"
	 *                                       member.
	 *
	 * @return a new resolver.
	 */
	public static PersistenceTypeDescriptionResolver New(
		final PersistenceTypeResolver                                               typeResolver                  ,
		final PersistenceRefactoringMapping                                         refactoringMapping            ,
		final XGettingEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ,
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders,
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders
	)
	{
		return new Default(
			typeResolver                           ,
			refactoringMapping                     ,
			sourceTypeIdentifierBuilders  .immure(),
			sourceMemberIdentifierBuilders.immure(),
			targetMemberIdentifierBuilders.immure()
		);
	}
	
	public final class Default implements PersistenceTypeDescriptionResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceTypeResolver                                                 typeResolver                  ;
		final PersistenceRefactoringMapping                                           refactoringMapping            ;
		final XImmutableEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ;
		final XImmutableEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders;
		final XImmutableEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final PersistenceTypeResolver                                                 typeResolver                  ,
			final PersistenceRefactoringMapping                                           refactoringMapping            ,
			final XImmutableEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ,
			final XImmutableEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders,
			final XImmutableEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders
		)
		{
			super();
			this.typeResolver                   = typeResolver                  ;
			this.refactoringMapping             = refactoringMapping            ;
			this.sourceTypeIdentifierBuilders   = sourceTypeIdentifierBuilders  ;
			this.sourceMemberIdentifierBuilders = sourceMemberIdentifierBuilders;
			this.targetMemberIdentifierBuilders = targetMemberIdentifierBuilders;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public ClassLoader getTypeResolvingClassLoader(final String typeName)
		{
			return this.typeResolver.getTypeResolvingClassLoader(typeName);
		}
				
		@Override
		public String resolveRuntimeTypeName(final PersistenceTypeDescription typeDescription)
		{
			// search for a mapping entry with identifier builders in descending order of priority.
			final PersistenceRefactoringMapping refactoringMapping = this.refactoringMapping;
			for(final PersistenceRefactoringTypeIdentifierBuilder idBuilder : this.sourceTypeIdentifierBuilders)
			{
				final String                   identifier = idBuilder.buildTypeIdentifier(typeDescription);
				final KeyValue<String, String> entry      = refactoringMapping.lookup(identifier);
				if(entry == null)
				{
					continue;
				}
				
				// can be null for types explicitly marked as no more having a runtime type (unreachable / "deleted")
				return entry.value();
			}
			
			// if no refactoring entry could be found, the original type name still applies.
			return typeDescription.typeName();
		}

		@Override
		public final KeyValue<String, String> lookup(final String sourceIdentifier)
		{
			return this.refactoringMapping.lookup(sourceIdentifier);
		}
		
		@Override
		public boolean isNewCurrentTypeMember(
			final PersistenceTypeDefinition       currentTypeDefinition,
			final PersistenceTypeDefinitionMember currentTypeMember
		)
		{
			for(final PersistenceRefactoringMemberIdentifierBuilder idBuilder : this.targetMemberIdentifierBuilders)
			{
				final String identifier = idBuilder.buildMemberIdentifier(currentTypeDefinition, currentTypeMember);
				if(this.refactoringMapping.isNewElement(identifier))
				{
					return true;
				}
			}

			return false;
		}

		@Override
		public KeyValue<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> resolveMember(
			final PersistenceTypeDefinition       sourceType  ,
			final PersistenceTypeDefinitionMember sourceMember,
			final PersistenceTypeDefinition       targetType
		)
		{
			// search for a mapping entry with identifier builders in descending order of priority.
			final PersistenceRefactoringMapping refactoringMapping = this.refactoringMapping;
			for(final PersistenceRefactoringMemberIdentifierBuilder idBuilder : this.sourceMemberIdentifierBuilders)
			{
				final String                   identifier = idBuilder.buildMemberIdentifier(sourceType, sourceMember);
				final KeyValue<String, String> entry      = refactoringMapping.lookup(identifier);
				if(entry == null)
				{
					continue;
				}
				
				return this.resolveTarget(sourceType, sourceMember, targetType, entry.value());
			}

			// no refacting entry could be found
			return null;
		}
		
		private KeyValue<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> resolveTarget(
			final PersistenceTypeDefinition       sourceType            ,
			final PersistenceTypeDefinitionMember sourceMember          ,
			final PersistenceTypeDefinition       targetType            ,
			final String                          targetMemberIdentifier
		)
		{
			if(targetMemberIdentifier == null)
			{
				// indicated deletion
				return X.KeyValue(sourceMember, null);
			}
			
			for(final PersistenceTypeDefinitionMember targetMember : targetType.allMembers())
			{
				for(final PersistenceRefactoringMemberIdentifierBuilder idBuilder : this.targetMemberIdentifierBuilders)
				{
					final String identifier = idBuilder.buildMemberIdentifier(targetType, targetMember);
					if(identifier.equals(targetMemberIdentifier))
					{
						return X.KeyValue(sourceMember, targetMember);
					}
				}
			}
			
			// if a target member mapping was found but cannot be resolved, something is wrong.
			throw new PersistenceException(
				"Unresolvable type member refactoring mapping: "
				+ sourceType.toTypeIdentifier() + '#' + sourceMember.identifier()
				+ " -> \"" + targetMemberIdentifier + "\" in type "
				+ targetType.toRuntimeTypeIdentifier()
			);
		}
				
	}
	
}
