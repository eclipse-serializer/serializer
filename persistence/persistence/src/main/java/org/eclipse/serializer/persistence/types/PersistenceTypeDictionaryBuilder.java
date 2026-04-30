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

import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.collections.EqHashEnum;
import org.eclipse.serializer.collections.EqHashTable;
import org.eclipse.serializer.collections.XSort;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.collections.types.XGettingTable;
import org.eclipse.serializer.collections.types.XSequence;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;

import static org.eclipse.serializer.util.X.notNull;


/**
 * Forms a live {@link PersistenceTypeDictionary} out of the unvalidated
 * {@link PersistenceTypeDictionaryEntry entries} produced by {@link PersistenceTypeDictionaryParser}.
 * <p>
 * This is the validation and resolution stage of the load pipeline: each entry is checked for unique type ID,
 * its members are translated into {@link PersistenceTypeDefinitionMember definition members} via a
 * {@link PersistenceTypeDefinitionMemberCreator}, and the original (possibly deprecated) type name is mapped to
 * a current runtime type using a {@link PersistenceTypeDescriptionResolver} that internally applies the
 * configured refactoring mappings.
 *
 * @see PersistenceTypeDictionaryParser
 * @see PersistenceTypeDictionaryCompiler
 * @see PersistenceTypeDescriptionResolver
 */
@FunctionalInterface
public interface PersistenceTypeDictionaryBuilder
{
	/**
	 * Builds a {@link PersistenceTypeDictionary} from parsed entries, performing the validation and
	 * runtime-type resolution described in the {@linkplain PersistenceTypeDictionaryBuilder type-level
	 * Javadoc}.
	 *
	 * @param entries the parsed entries; may be {@code null} (treated as empty).
	 *
	 * @return the resulting type dictionary.
	 */
	public PersistenceTypeDictionary buildTypeDictionary(XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries);



	/**
	 * Indexes the passed entries by their {@link PersistenceTypeDictionaryEntry#typeId() typeId}, sorted
	 * ascending, and rejects duplicates.
	 *
	 * @param entries the entries to index; may be {@code null} (treated as empty).
	 *
	 * @return a {@link XGettingTable} mapping typeId to entry.
	 *
	 * @throws PersistenceException if two entries share the same typeId.
	 */
	public static XGettingTable<Long, PersistenceTypeDictionaryEntry> ensureUniqueTypeIds(
		final XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries
	)
	{
		final EqHashTable<Long, PersistenceTypeDictionaryEntry> uniqueTypeIdEntries = EqHashTable.New();
		
		// entries may be null, e.g. when there is no imported type dictionary, yet.
		if(entries != null)
		{
			for(final PersistenceTypeDictionaryEntry e : entries)
			{
				if(!uniqueTypeIdEntries.add(e.typeId(), e))
				{
					throw new PersistenceException("TypeId conflict for " + e.typeId() + " " + e.typeName());
				}
			}
			XSort.valueSort(uniqueTypeIdEntries.keys(), Long::compare);
		}
		
		return uniqueTypeIdEntries;
	}
		
	/**
	 * Reusable static implementation of the build pipeline used by {@link Default}: deduplicates by typeId,
	 * resolves each entry's runtime type via the resolver (applying any configured refactoring mappings),
	 * builds and validates {@link PersistenceTypeDefinitionMember definition members}, and bulk-registers the
	 * resulting definitions into a freshly created dictionary.
	 *
	 * @param typeDictionaryCreator factory for the empty target dictionary.
	 * @param typeDefinitionCreator factory for individual type definitions.
	 * @param typeResolver          resolver that maps original type names to current runtime types and applies
	 *                              the configured refactoring mappings.
	 * @param entries               the parsed entries; may be {@code null} (treated as empty).
	 *
	 * @return the populated type dictionary.
	 */
	public static PersistenceTypeDictionary buildTypeDictionary(
		final PersistenceTypeDictionaryCreator                           typeDictionaryCreator,
		final PersistenceTypeDefinitionCreator                           typeDefinitionCreator,
		final PersistenceTypeDescriptionResolver                             typeResolver,
		final XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries
	)
	{
		final XGettingTable<Long, PersistenceTypeDictionaryEntry> uniqueTypeIdEntries = ensureUniqueTypeIds(entries);
		
		final PersistenceTypeDefinitionMemberCreator memberCreator =
			PersistenceTypeDefinitionMemberCreator.New(uniqueTypeIdEntries.values(), typeResolver)
		;
						
		final BulkList<PersistenceTypeDefinition> typeDefs = BulkList.New(uniqueTypeIdEntries.size());
		for(final PersistenceTypeDescription e : uniqueTypeIdEntries.values())
		{
			/*
			 * The type entry just contains all member entries as they are written in the dictionary,
			 * even if they are inconsistent (e.g. duplicates) or no longer resolvable to a runtime type.
			 * The point where unvalidated entries are formed into valid definitions is exactely here,
			 * so here has to be the validation and type mapping.
			 */
			/*
			 * The type resolver must also handle refactoring mappings internally.
			 * Not just mapping types with unresolvably deprecated names to their currently named runtime type,
			 * but also rerouting conflicted name changes.
			 * Consider the following case:
			 * Class A is part of the dictionary.
			 * During the developement process, it gets renamed to "B" and a new Class is created with the name "A".
			 * Design-wise, the entry saying "A" must now be mapped to the type B.
			 * Without refactoring mapping, the name "A" could still be resolved to a valid runtime class,
			 * but it would be the wrong one.
			 */
			

			final EqHashEnum<PersistenceTypeDefinitionMember> allMembers =
				EqHashEnum.New(PersistenceTypeDescriptionMember.identityHashEqualator())
			;
			final EqHashEnum<PersistenceTypeDefinitionMember> instanceMembers =
				EqHashEnum.New(PersistenceTypeDescriptionMember.identityHashEqualator())
			;
			
			buildDefinitionMembers(memberCreator, e, allMembers, instanceMembers);
			
			final String   runtimeTypeName = typeResolver.resolveRuntimeTypeName(e);
			final Class<?> type            = runtimeTypeName == null
				? null
				: typeResolver.tryResolveType(runtimeTypeName)
			;
			
			final PersistenceTypeDefinition typeDef = typeDefinitionCreator.createTypeDefinition(
				e.typeId()     ,
				e.typeName()   ,
				runtimeTypeName,
				type           ,
				allMembers     ,
				instanceMembers
			);
			
			typeDefs.add(typeDef);
		}

		// collected type definitions are bulk-registered for efficiency reasons (only sort once)
		final PersistenceTypeDictionary typeDictionary = typeDictionaryCreator.createTypeDictionary();
		typeDictionary.registerTypeDefinitions(typeDefs);
				
		return typeDictionary;
	}
	
	/**
	 * Translates the {@linkplain PersistenceTypeDescription#allMembers() all-members sequence} of the passed
	 * description into {@link PersistenceTypeDefinitionMember} instances and partitions them into
	 * {@code allMembers} and {@code instanceMembers} (the latter receiving only those members for which
	 * {@link PersistenceTypeDescriptionMember#isInstanceMember()} is {@code true}).
	 *
	 * @param memberCreator   factory for the resulting definition members.
	 * @param typeDescription the source description whose members shall be translated.
	 * @param allMembers      sink that receives every translated member.
	 * @param instanceMembers sink that receives only the {@linkplain PersistenceTypeDescriptionMember#isInstanceMember()
	 *                        instance members}.
	 *
	 * @throws PersistenceException if {@code typeDescription} contains two members with identical
	 *                              {@link PersistenceTypeDescriptionMember#identifier() identifier}.
	 */
	public static void buildDefinitionMembers(
		final PersistenceTypeDefinitionMemberCreator             memberCreator  ,
		final PersistenceTypeDescription                         typeDescription,
		final XSequence<? super PersistenceTypeDefinitionMember> allMembers     ,
		final XSequence<? super PersistenceTypeDefinitionMember> instanceMembers
	)
	{
		for(final PersistenceTypeDescriptionMember member : typeDescription.allMembers())
		{
			final PersistenceTypeDefinitionMember definitionMember = member.createDefinitionMember(memberCreator);
			if(!allMembers.add(definitionMember))
			{
				throw new PersistenceException("Duplicate type member entry: " + member.identifier());
			}
			if(definitionMember.isInstanceMember())
			{
				instanceMembers.add(definitionMember);
			}
		}
	}
	
	
	
	/**
	 * Creates a {@link Default} builder bound to the given creators and resolver provider.
	 *
	 * @param typeDictionaryCreator factory for the empty target dictionary; must not be {@code null}.
	 * @param typeDefinitionCreator factory for individual type definitions; must not be {@code null}.
	 * @param typeResolverProvider  provider of the resolver applied during the build; must not be
	 *                              {@code null}.
	 *
	 * @return the new builder.
	 */
	public static PersistenceTypeDictionaryBuilder.Default New(
		final PersistenceTypeDictionaryCreator typeDictionaryCreator,
		final PersistenceTypeDefinitionCreator typeDefinitionCreator,
		final PersistenceTypeDescriptionResolverProvider  typeResolverProvider
	)
	{
		return new PersistenceTypeDictionaryBuilder.Default(
			notNull(typeDictionaryCreator),
			notNull(typeDefinitionCreator),
			notNull(typeResolverProvider)
		);
	}
	
	/**
	 * Default {@link PersistenceTypeDictionaryBuilder} implementation. Holds the creators and the
	 * {@link PersistenceTypeDescriptionResolverProvider}; delegates the actual build to the static
	 * {@link #buildTypeDictionary(PersistenceTypeDictionaryCreator, PersistenceTypeDefinitionCreator,
	 * PersistenceTypeDescriptionResolver, XGettingSequence)} pipeline.
	 */
	public class Default implements PersistenceTypeDictionaryBuilder
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final PersistenceTypeDictionaryCreator typeDictionaryCreator;
		final PersistenceTypeDefinitionCreator typeDefinitionCreator;
		final PersistenceTypeDescriptionResolverProvider typeResolverProvider ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final PersistenceTypeDictionaryCreator typeDictionaryCreator,
			final PersistenceTypeDefinitionCreator typeDefinitionCreator,
			final PersistenceTypeDescriptionResolverProvider  typeResolverProvider
		)
		{
			super();
			this.typeDictionaryCreator = typeDictionaryCreator;
			this.typeDefinitionCreator = typeDefinitionCreator;
			this.typeResolverProvider  = typeResolverProvider ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		@Override
		public PersistenceTypeDictionary buildTypeDictionary(
			final XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries
		)
		{
			/* (29.09.2017 TM)NOTE:
			 * This is what clean code should look like:
			 * - the interface defining the behavior.
			 * - the implementation holding data / references and choosing which logic to use with the data.
			 * - the actual logic modularized into static methods to be reusable for other implementations.
			 * Also:
			 * - small methods without nested loops to better support JITting.
			 * - properly named methods and variables.
			 * - explanatory comments where naming isn't self-explanatory.
			 */
			return PersistenceTypeDictionaryBuilder.buildTypeDictionary(
				this.typeDictionaryCreator                 ,
				this.typeDefinitionCreator                 ,
				this.typeResolverProvider.provideTypeDescriptionResolver(),
				entries
			);
		}
				
	}
	
}
