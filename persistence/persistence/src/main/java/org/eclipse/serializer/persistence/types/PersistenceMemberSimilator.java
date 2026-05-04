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

import static org.eclipse.serializer.util.X.notNull;

import org.eclipse.serializer.chars.Levenshtein;
import org.eclipse.serializer.typing.TypeMappingLookup;
import org.eclipse.serializer.util.similarity.Similator;

/**
 * Computes a similarity score in {@code [0.0, 1.0]} for a pair of {@link PersistenceTypeDefinitionMember}s,
 * combining name similarity ({@link Levenshtein}-based) with type similarity from a configured
 * {@link TypeMappingLookup}. Used by the legacy-mapping algorithm to identify the best target member for
 * each obsolete one when no explicit refactoring rule is present.
 * <p>
 * Members that differ in their {@code isEnumConstant()} flag are never matched (score {@code 0.0}). The
 * final score is the average of the name-similarity factor (which is itself scaled by a qualifier match
 * factor) and the type-similarity factor.
 *
 * @see PersistenceMemberMatchingProvider
 */
public interface PersistenceMemberSimilator extends Similator<PersistenceTypeDefinitionMember>
{
	/**
	 * Creates a new {@link Default} similator that uses {@code typeSimilarity} to score type pairs.
	 *
	 * @param typeSimilarity the type-similarity lookup; must not be {@code null}.
	 *
	 * @return the newly created similator.
	 */
	public static PersistenceMemberSimilator New(final TypeMappingLookup<Float>  typeSimilarity)
	{
		return new PersistenceMemberSimilator.Default(
			notNull(typeSimilarity)
		);
	}

	/**
	 * Default {@link PersistenceMemberSimilator}. Combines name similarity (Levenshtein, scaled by a
	 * qualifier-match factor) with type similarity from the configured {@link TypeMappingLookup}.
	 */
	public final class Default implements PersistenceMemberSimilator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final TypeMappingLookup<Float> typeSimilarity;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final TypeMappingLookup<Float>typeSimilarity)
		{
			super();
			this.typeSimilarity = typeSimilarity;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final double evaluate(
			final PersistenceTypeDefinitionMember sourceMember,
			final PersistenceTypeDefinitionMember targetMember
		)
		{
			if(sourceMember.isEnumConstant() != targetMember.isEnumConstant())
			{
				// may never even begin consider to match enum constant fields and non-enum-constant fields.
				return 0.0;
			}
			
			final float nameSimilarity = this.calculateSimilarityByName(sourceMember, targetMember);
			final float typeSimilarity = this.calculateSimilaritybyType(sourceMember, targetMember);
			
//			XDebug.println(
//				sourceMember.name()
//				+ "\t---[" + nameSimilarity + "," + typeSimilarity + "=" + (nameSimilarity + typeSimilarity ) / 2.0f
//				+ "]--->\t"
//				+ targetMember.name()
//			);
			
			return (nameSimilarity + typeSimilarity ) / 2.0f;
		}
		
		private float calculateSimilarityByName(
			final PersistenceTypeDefinitionMember sourceMember,
			final PersistenceTypeDefinitionMember targetMember
		)
		{
			/*
			 * Cannot do a quick-check for perfect matches, here, because a refactoring mapping
			 * might map a type name (qualifier) on the source side to another one on the target side.
			 * Doing a quick check on simple equality might cause an ambiguity for such cases.
			 */
			
			final float nameSimilarity = Levenshtein.similarity(
				sourceMember.name(),
				targetMember.name()
			);
			final float qualifierFactor = this.calculateQualifierSimilarityFactor(
				sourceMember.runtimeQualifier(),
				targetMember.runtimeQualifier()
			);
			
			return qualifierFactor * nameSimilarity;
		}
		
		private float calculateQualifierSimilarityFactor(
			final String sourceQualifier,
			final String targetQualifier
		)
		{
			// not much point in calculating similarity between qualifiers. Either they are equal or not.
			return sourceQualifier == null
				? targetQualifier == null
					? 1.0f
					: 0.5f
				: sourceQualifier.equals(targetQualifier)
					? 1.0f
					: 0.5f
			;
		}
		
		private float calculateSimilaritybyType(
			final PersistenceTypeDefinitionMember sourceMember,
			final PersistenceTypeDefinitionMember targetMember
		)
		{
			final Class<?> sourceType = sourceMember.type();
			final Class<?> targetType = targetMember.type();
			
			if(sourceType != null && targetType != null)
			{
				return this.calculateTypeSimilarity(sourceType, targetType);
			}

			// not much point in calculating similarity between unresolvable types. Either they are equal or not.
			return sourceMember.typeName().equals(targetMember.typeName())
				? 1.0f
				: 0.5f
			;
		}
		
		private float calculateTypeSimilarity(final Class<?> type1, final Class<?> type2)
		{
			if(type1 == type2)
			{
				return 1.0f;
			}
			
			final Float mappedSimilarity = this.typeSimilarity.lookup(type1, type2);
			if(mappedSimilarity != null)
			{
				return mappedSimilarity;
			}
			
			return 0.0f;
		}
		
	}
	
}
