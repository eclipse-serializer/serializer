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

import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.collections.HashEnum;
import org.eclipse.serializer.collections.HashTable;
import org.eclipse.serializer.equality.Equalator;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTypeConsistency;
import org.eclipse.serializer.typing.KeyValue;
import org.eclipse.serializer.typing.TypeMappingLookup;
import org.eclipse.serializer.util.similarity.MatchValidator;
import org.eclipse.serializer.util.similarity.MultiMatch;
import org.eclipse.serializer.util.similarity.MultiMatchAssembler;
import org.eclipse.serializer.util.similarity.MultiMatcher;
import org.eclipse.serializer.util.similarity.Similarity;
import org.eclipse.serializer.util.similarity.Similator;

/**
 * Top-level orchestrator that turns "I have an outdated {@link PersistenceTypeDefinition} from the
 * dictionary and a current {@link PersistenceTypeHandler}" into a working
 * {@link PersistenceLegacyTypeHandler}.
 * <p>
 * The {@link Default} implementation walks a fixed precedence chain in
 * {@link #ensureLegacyTypeHandler}:
 * <ol>
 * <li><b>Supplier</b> &mdash; if the current handler implements
 *     {@link PersistenceLegacyTypeHandlerSupplier}, its supplied legacy handler is used after
 *     structural validation.</li>
 * <li><b>Custom by typeId</b> &mdash; a custom legacy handler matching the legacy typeId in the
 *     {@link PersistenceCustomTypeHandlerRegistry} is preferred (and validated against the legacy
 *     definition's structure).</li>
 * <li><b>Custom by structure</b> &mdash; otherwise a custom legacy handler matching only by structure
 *     is searched.</li>
 * <li><b>Synthesized</b> &mdash; finally, a legacy handler is built from scratch by:
 *     applying explicit refactoring mappings, similarity-matching the remaining unmapped members via
 *     a {@link MultiMatcher}, bundling everything into a {@link PersistenceLegacyTypeMappingResult}
 *     via the configured {@link PersistenceLegacyTypeMappingResultor}, and creating the actual
 *     handler via the {@link PersistenceLegacyTypeHandlerCreator}.</li>
 * </ol>
 * <p>
 * <b>Similarity model.</b> The similarity score uses {@link Float} type-similarity for non-name parts
 * and a member-matching {@link Similator} for member matching. An {@code explicit} match
 * (refactoring-mapping derived) carries the synthetic similarity {@code 2.0}, signalling
 * "stronger than any heuristic match" so it always wins.
 *
 * @param <D> the data target type.
 *
 * @see PersistenceLegacyTypeHandler
 * @see PersistenceLegacyTypeMappingResult
 * @see PersistenceLegacyTypeHandlerCreator
 */
public interface PersistenceLegacyTypeMapper<D>
{
	/**
	 * Returns a legacy handler that bridges {@code legacyTypeDefinition}'s persisted form onto
	 * {@code currentTypeHandler}'s runtime type. See the class-level docs for the resolution chain.
	 *
	 * @param <T>                  the runtime type.
	 * @param legacyTypeDefinition the legacy type definition from the dictionary.
	 * @param currentTypeHandler   the current handler the legacy data should be re-bound to.
	 *
	 * @return a legacy handler.
	 */
	public <T> PersistenceLegacyTypeHandler<D, T> ensureLegacyTypeHandler(
		PersistenceTypeDefinition    legacyTypeDefinition,
		PersistenceTypeHandler<D, T> currentTypeHandler
	);



	/**
	 * Default constants used when rendering legacy-mapping diagnostics.
	 */
	public interface Defaults
	{
		/**
		 * The synthetic similarity score that flags a match as resulting from an explicit refactoring
		 * mapping rather than from heuristic similarity.
		 *
		 * @return {@code 2.0} (intentionally above any heuristic value).
		 */
		public static double defaultExplicitMappingSimilarity()
		{
			// to indicate "super similarity", something beyond a similarity match: an explicit mapping.
			return 2.0;
		}

		/**
		 * @return the base character width used to format a similarity-token cell in diagnostic
		 *         output (3 characters for {@code -...->} plus 3 for the embedded number / keyword).
		 */
		public static int defaultMappingTokenBaseLength()
		{
			// The 3 characters "-"..."->" are mapping indicators. The special cases below don't have/need them.
			return 6;
		}

		/**
		 * @return the diagnostic token used for an explicitly mapped member ({@code "mapped"},
		 *         rendered as {@code "-mapped->"}).
		 */
		public static String defaultExplicitMappingString()
		{
			// yields "-mapped->" (9 characters)
			return "mapped";
		}

		/**
		 * @return the diagnostic token used for a member that exists in the current type but not in
		 *         the legacy type ({@code " NEW    >"}).
		 */
		public static String defaultNewMemberString()
		{
			// yields " NEW    >" (9 characters)
			return " NEW    >";
		}

		/**
		 * @return the diagnostic token used for a legacy member that no longer has a current
		 *         counterpart ({@code " REMOVED "}).
		 */
		public static String defaultDiscardedMemberString()
		{
			// yields " REMOVED " (9 characters)
			return " REMOVED ";
		}

	}

	/**
	 * Renders a {@link Similarity} as a textual cell for diagnostic output. Explicit-mapping matches
	 * are rendered with {@link Defaults#defaultExplicitMappingString()}, all others via the default
	 * similarity formatter.
	 *
	 * @param match the match to render.
	 *
	 * @return the textual cell.
	 */
	public static String similarityToString(final Similarity<PersistenceTypeDefinitionMember> match)
	{
		return match.similarity() == Defaults.defaultExplicitMappingSimilarity()
			? Defaults.defaultExplicitMappingString()
			: MultiMatchAssembler.Defaults.defaultSimilarityFormatter().format(match.similarity())
		;
	}

	/**
	 * Builds an explicit-mapping {@link Similarity} between the passed source and target members,
	 * carrying the synthetic similarity {@link Defaults#defaultExplicitMappingSimilarity()}.
	 *
	 * @param sourceMember the legacy member.
	 * @param targetMember the current member it maps to.
	 *
	 * @return an explicit-mapping similarity.
	 */
	public static Similarity<PersistenceTypeDefinitionMember> ExplicitMatch(
		final PersistenceTypeDefinitionMember sourceMember,
		final PersistenceTypeDefinitionMember targetMember
	)
	{
		return Similarity.New(
			notNull(sourceMember),
			Defaults.defaultExplicitMappingSimilarity(),
			notNull(targetMember)
		);
	}
	
	
	/**
	 * Creates the default {@link PersistenceLegacyTypeMapper}.
	 *
	 * @param <D>                             the data target type.
	 * @param typeDescriptionResolverProvider provides the resolver for refactoring rename lookups.
	 * @param typeSimilarity                  pre-computed type similarities used by the member
	 *                                        similator.
	 * @param customTypeHandlerRegistry       searched for hand-written legacy handlers before
	 *                                        synthesis.
	 * @param memberMatchingProvider          supplies the equalator / similator / validator used by
	 *                                        the {@link MultiMatcher}.
	 * @param resultor                        bundles the final mapping into a result, possibly with
	 *                                        user callbacks / logging.
	 * @param legacyTypeHandlerCreator        synthesizes the actual handler from the result.
	 *
	 * @return a new mapper.
	 */
	public static <D> PersistenceLegacyTypeMapper<D> New(
		final PersistenceTypeDescriptionResolverProvider  typeDescriptionResolverProvider,
		final TypeMappingLookup<Float>                    typeSimilarity             ,
		final PersistenceCustomTypeHandlerRegistry<D>     customTypeHandlerRegistry  ,
		final PersistenceMemberMatchingProvider           memberMatchingProvider     ,
		final PersistenceLegacyTypeMappingResultor<D>     resultor                   ,
		final PersistenceLegacyTypeHandlerCreator<D>      legacyTypeHandlerCreator
	)
	{
		return new PersistenceLegacyTypeMapper.Default<>(
			notNull(typeDescriptionResolverProvider),
			notNull(typeSimilarity)                 ,
			notNull(customTypeHandlerRegistry)      ,
			notNull(memberMatchingProvider)         ,
			notNull(resultor)                       ,
			notNull(legacyTypeHandlerCreator)
		);
	}

	public class Default<D> implements PersistenceLegacyTypeMapper<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceTypeDescriptionResolverProvider typeDescriptionResolverProvider;
		private final TypeMappingLookup<Float>                   typeSimilarity                 ;
		private final PersistenceCustomTypeHandlerRegistry<D>    customTypeHandlerRegistry      ;
		private final PersistenceMemberMatchingProvider          memberMatchingProvider         ;
		private final PersistenceLegacyTypeMappingResultor<D>    resultor                       ;
		private final PersistenceLegacyTypeHandlerCreator<D>     legacyTypeHandlerCreator       ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default(
			final PersistenceTypeDescriptionResolverProvider typeDescriptionResolverProvider,
			final TypeMappingLookup<Float>                   typeSimilarity                 ,
			final PersistenceCustomTypeHandlerRegistry<D>    customTypeHandlerRegistry      ,
			final PersistenceMemberMatchingProvider          memberMatchingProvider         ,
			final PersistenceLegacyTypeMappingResultor<D>    resultor                       ,
			final PersistenceLegacyTypeHandlerCreator<D>     legacyTypeHandlerCreator
		)
		{
			super();
			this.typeDescriptionResolverProvider = typeDescriptionResolverProvider;
			this.typeSimilarity                  = typeSimilarity                 ;
			this.customTypeHandlerRegistry       = customTypeHandlerRegistry      ;
			this.memberMatchingProvider          = memberMatchingProvider         ;
			this.resultor                        = resultor                       ;
			this.legacyTypeHandlerCreator        = legacyTypeHandlerCreator       ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		private <T> PersistenceLegacyTypeHandler<D, T> createLegacyTypeHandler(
			final PersistenceTypeDefinition    legacyTypeDefinition,
			final PersistenceTypeHandler<D, T> currentTypeHandler
		)
		{
			// explicit mappings take precedence
			final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings  ;
			final HashEnum<PersistenceTypeDefinitionMember>                                   explicitNewMembers;
			
			this.createExplicitMappings(
				explicitMappings   = HashTable.New(),
				explicitNewMembers = HashEnum.New() ,
				legacyTypeDefinition,
				currentTypeHandler
			);

			// heuristical matching is a applied to the remaining unmapped members
			final MultiMatch<PersistenceTypeDefinitionMember> match = this.match(
				legacyTypeDefinition,
				currentTypeHandler  ,
				explicitMappings    ,
				explicitNewMembers
			);
			
			// bundle the mappings into a result, potentially with user callback, validation, modification, logging, etc.
			final PersistenceLegacyTypeMappingResult<D, T> validResult = this.resultor.createMappingResult(
				legacyTypeDefinition,
				currentTypeHandler  ,
				explicitMappings    ,
				explicitNewMembers  ,
				match
			);
			
			// creating a type handler from the finalized valid result
			return this.legacyTypeHandlerCreator.createLegacyTypeHandler(validResult);
		}
		
		private void createExplicitMappings(
			final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings    ,
			final HashEnum<PersistenceTypeDefinitionMember>                                   explicitNewMembers  ,
			final PersistenceTypeDefinition                                                   legacyTypeDefinition,
			final PersistenceTypeHandler<D, ?>                                                currentTypeHandler
		)
		{
			final PersistenceTypeDescriptionResolver resolver = this.typeDescriptionResolverProvider.provideTypeDescriptionResolver();
			
			for(final PersistenceTypeDefinitionMember currentMember : currentTypeHandler.allMembers())
			{
				if(resolver.isNewCurrentTypeMember(currentTypeHandler, currentMember))
				{
					explicitNewMembers.add(currentMember);
				}
			}
			
			for(final PersistenceTypeDefinitionMember sourceMember : legacyTypeDefinition.allMembers())
			{
				// value might be null to indicate deletion. Member might not be resolvable (= mapped) at all.
				final KeyValue<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> resolved =
					resolver.resolveMember(legacyTypeDefinition, sourceMember, currentTypeHandler)
				;
				
				if(resolved == null)
				{
					continue;
				}
				if(explicitNewMembers.contains(resolved.value()))
				{
					throw new PersistenceException(
						"Duplicate target entry " + resolved.value().identifier()
						+ " for type " + currentTypeHandler.toTypeIdentifier() + "."
					);
				}
				if(!explicitMappings.add(resolved))
				{
					throw new PersistenceExceptionTypeConsistency(
						"Duplicate member mapping for legacy/source member \"" + sourceMember.identifier() + "\""
						+ " in legacy type " + legacyTypeDefinition.toTypeIdentifier()
					);
				}
			}
		}
		
		private static boolean hasNoElements(final BulkList<?> list)
		{
			return list.applies(e -> e == null);
		}
				
		private MultiMatch<PersistenceTypeDefinitionMember> match(
			final PersistenceTypeDefinition                                                   legacyTypeDefinition,
			final PersistenceTypeHandler<D, ?>                                                currentTypeHandler  ,
			final HashTable<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings    ,
			final HashEnum<PersistenceTypeDefinitionMember>                                   explicitNewMembers
		)
		{
			final BulkList<? extends PersistenceTypeDefinitionMember> sourceMembers = BulkList.New(
				legacyTypeDefinition.allMembers()
			);
			final BulkList<? extends PersistenceTypeDefinitionMember> targetMembers = BulkList.New(
				currentTypeHandler.allMembers()
			);
			
			// null out all explicitly mapped members before matching
			sourceMembers.replace(m ->
				explicitMappings.keys().contains(m),
				null
			);
			targetMembers.replace(m ->
				explicitNewMembers.contains(m) || explicitMappings.values().contains(m),
				null
			);
			
			// if no more elements are left to be matched, return null to signal no matching at all.
			if(hasNoElements(sourceMembers) || hasNoElements(targetMembers))
			{
				return null;
			}
			
			final MultiMatch<PersistenceTypeDefinitionMember> match = this.match(sourceMembers, targetMembers);
			
			return match;
		}
		
		private MultiMatch<PersistenceTypeDefinitionMember> match(
			final BulkList<? extends PersistenceTypeDefinitionMember> sourceMembers,
			final BulkList<? extends PersistenceTypeDefinitionMember> targetMembers
		)
		{
			final PersistenceMemberMatchingProvider          provider  = this.memberMatchingProvider;
			final TypeMappingLookup<Float>                   typeSimis = this.typeSimilarity;
			final Equalator<PersistenceTypeDefinitionMember> equalator = provider.provideMemberMatchingEqualator();
			final Similator<PersistenceTypeDefinitionMember> similator = provider.provideMemberMatchingSimilator(
				typeSimis
			);
			final MatchValidator<PersistenceTypeDefinitionMember> validator = provider.provideMemberMatchValidator();
			
			final MultiMatcher<PersistenceTypeDefinitionMember> matcher =
				MultiMatcher.<PersistenceTypeDefinitionMember>New()
				.setEqualator(equalator)
				.setSimilator(similator)
				.setValidator(validator)
			;
			
			final MultiMatch<PersistenceTypeDefinitionMember> match = matcher.match(sourceMembers, targetMembers);
			
			return match;
		}
		
		private <T> PersistenceLegacyTypeHandler<D, T> lookupCustomHandler(
			final PersistenceTypeDefinition legacyTypeDefinition
		)
		{
			PersistenceLegacyTypeHandler<D, T> matchingHandler = this.lookupCustomHandlerByTypeId(legacyTypeDefinition);
			if(matchingHandler == null)
			{
				matchingHandler = this.lookupCustomHandlerByStructure(legacyTypeDefinition);
			}
			
			return matchingHandler;
		}
		
		private <T> PersistenceLegacyTypeHandler<D, T> lookupCustomHandlerByTypeId(
			final PersistenceTypeDefinition legacyTypeDefinition
		)
		{
			final Class<?> type   = legacyTypeDefinition.type()  ;
			final long     typeId = legacyTypeDefinition.typeId();
			
			// cast safety ensured by checking the typename, which "is" the T.
			@SuppressWarnings("unchecked")
			final PersistenceLegacyTypeHandler<D, T> legacyTypeHandlerbyId = (PersistenceLegacyTypeHandler<D, T>)
				this.customTypeHandlerRegistry.legacyTypeHandlers()
				.search(h ->
					h.typeId() == typeId
				)
			;
			
			if(legacyTypeHandlerbyId == null)
			{
				return null;
			}
			
			validateLegacyTypeHandler(type, legacyTypeDefinition, legacyTypeHandlerbyId);
			
			return legacyTypeHandlerbyId;
		}



		private <T> void validateLegacyTypeHandler(
			final Class<?> type,
			final PersistenceTypeDefinition legacyTypeDefinition,
			final PersistenceLegacyTypeHandler<D, T> legacyTypeHandlerbyId) 
		{
			// validate if the found handler with matching explicit typeId also has matching type and structure
			if(type != null && type != legacyTypeDefinition.type()
				|| !PersistenceTypeDescription.equalStructure(legacyTypeHandlerbyId, legacyTypeDefinition)
			)
			{
				throw new PersistenceExceptionTypeConsistency(
					"Type handler structure mismatch for " + legacyTypeDefinition.toTypeIdentifier()
				);
			}
		}
		
		private <T> PersistenceLegacyTypeHandler<D, T> lookupCustomHandlerByStructure(
			final PersistenceTypeDefinition legacyTypeDefinition
		)
		{
			// if runtime type is non-null, the found type handler must have the same type, of course.
			final Class<?> type = legacyTypeDefinition.type();
			
			// cast safety ensured by checking the typename, which "is" the T.
			@SuppressWarnings("unchecked")
			final PersistenceLegacyTypeHandler<D, T> matchingLegacyTypeHandler = (PersistenceLegacyTypeHandler<D, T>)
				this.customTypeHandlerRegistry.legacyTypeHandlers()
				.search(h ->
					(type == null || h.type() == type)
					&& PersistenceTypeDescription.equalStructure(h, legacyTypeDefinition)
				)
			;
			
			// intentionally no validation of 0-TypeId here, since the following initialization already does validation.
			return matchingLegacyTypeHandler;
		}
		
		@Override
		public <T> PersistenceLegacyTypeHandler<D, T> ensureLegacyTypeHandler(
			final PersistenceTypeDefinition    legacyTypeDefinition,
			final PersistenceTypeHandler<D, T> currentTypeHandler
		)
		{
			//check for supplied handler
			if(currentTypeHandler instanceof PersistenceLegacyTypeHandlerSupplier supplier) 
			{
				@SuppressWarnings("unchecked")
				PersistenceLegacyTypeHandler<D, T> legacyTypeHandler = supplier.getLegacyTypeHandler();
				validateLegacyTypeHandler(legacyTypeDefinition.type(), legacyTypeDefinition, legacyTypeHandler);
				return legacyTypeHandler.initialize(legacyTypeDefinition.typeId());
			} 
			
			
			// check for a custom handler with matching structure
			final PersistenceLegacyTypeHandler<D, T> customHandler = this.lookupCustomHandler(legacyTypeDefinition);
			if(customHandler != null)
			{
				/*
				 * must initialize TypeHandler with given TypeId
				 * (potentially creating an initialized instance from an uninitialized prototype handler instance)
				 * note that #lookupCustomHandler already does member structure validation
				 */
				return customHandler.initialize(legacyTypeDefinition.typeId());
			}
			
			// at this point a legacy handler must be creatable or something went wrong.
			return this.createLegacyTypeHandler(legacyTypeDefinition, currentTypeHandler);
		}
		
	}
	
}
