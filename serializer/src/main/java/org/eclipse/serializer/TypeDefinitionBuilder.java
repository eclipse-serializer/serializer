package org.eclipse.serializer;

/*-
 * #%L
 * Eclipse Serializer
 * %%
 * Copyright (C) 2023 Eclipse Foundation
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.collections.EqHashEnum;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.collections.types.XGettingTable;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionCreator;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMember;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberCreator;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescription;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionMember;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionResolver;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionResolverProvider;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryAssembler;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryBuilder;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryEntry;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryParser;

/**
 * Create PersistenceTypeDefinitions from type strings created by the
 * {@link PersistenceTypeDictionaryAssembler}
 */
public interface TypeDefinitionBuilder
{
	public XGettingSequence<PersistenceTypeDefinition> buildTypeDefinitions(final String typeEntry);
	
	public static class Default implements TypeDefinitionBuilder
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeDictionaryParser            typeDictionaryParser           ;
		private final PersistenceTypeDefinitionCreator           typeDefinitionCreator          ;
		private final PersistenceTypeDescriptionResolverProvider typeDescriptionResolverProvider;
	
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Default(
			final PersistenceTypeDictionaryParser            typeDictionaryParser           ,
			final PersistenceTypeDefinitionCreator           typeDefinitionCreator          ,
			final PersistenceTypeDescriptionResolverProvider typeDescriptionResolverProvider
		)
		{
			super();
			this.typeDictionaryParser            = typeDictionaryParser           ;
			this.typeDefinitionCreator           = typeDefinitionCreator          ;
			this.typeDescriptionResolverProvider = typeDescriptionResolverProvider;
			
		}
	
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public XGettingSequence<PersistenceTypeDefinition> buildTypeDefinitions(final String typeEntry)
		{
			final PersistenceTypeDescriptionResolver typeResolver = this.typeDescriptionResolverProvider.provideTypeDescriptionResolver();
			
			final XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries = this.typeDictionaryParser.parseTypeDictionaryEntries(typeEntry);
			
			final XGettingTable<Long, PersistenceTypeDictionaryEntry> uniqueTypeIdEntries = PersistenceTypeDictionaryBuilder.ensureUniqueTypeIds(entries);
			
			final PersistenceTypeDefinitionMemberCreator memberCreator =
				PersistenceTypeDefinitionMemberCreator.New(uniqueTypeIdEntries.values(), typeResolver)
			;
							
			final BulkList<PersistenceTypeDefinition> typeDefs = BulkList.New(uniqueTypeIdEntries.size());
			for(final PersistenceTypeDescription e : uniqueTypeIdEntries.values())
			{
	
				final EqHashEnum<PersistenceTypeDefinitionMember> allMembers =
					EqHashEnum.New(PersistenceTypeDescriptionMember.identityHashEqualator())
				;
				final EqHashEnum<PersistenceTypeDefinitionMember> instanceMembers =
					EqHashEnum.New(PersistenceTypeDescriptionMember.identityHashEqualator())
				;
				
				PersistenceTypeDictionaryBuilder.buildDefinitionMembers(memberCreator, e, allMembers, instanceMembers);
				
				final String   runtimeTypeName = typeResolver.resolveRuntimeTypeName(e);
				final Class<?> type            = runtimeTypeName == null
					? null
					: typeResolver.tryResolveType(runtimeTypeName)
				;
				
				final PersistenceTypeDefinition typeDef = this.typeDefinitionCreator.createTypeDefinition(
					e.typeId()     ,
					e.typeName()   ,
					runtimeTypeName,
					type           ,
					allMembers     ,
					instanceMembers
				);
				
				typeDefs.add(typeDef);
			}
			
			return typeDefs;
		}
	}
}
