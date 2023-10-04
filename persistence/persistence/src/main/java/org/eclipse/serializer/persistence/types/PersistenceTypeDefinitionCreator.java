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

import org.eclipse.serializer.collections.types.XGettingEnum;

@FunctionalInterface
public interface PersistenceTypeDefinitionCreator
{
	public PersistenceTypeDefinition createTypeDefinition(
		long                                                    typeId         ,
		String                                                  typeName       ,
		String                                                  runtimeTypeName,
		Class<?>                                                runtimeType    ,
		XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers     ,
		XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers
	);
	
	
	
	public static PersistenceTypeDefinitionCreator.Default New()
	{
		return new PersistenceTypeDefinitionCreator.Default();
	}
			
	public final class Default implements PersistenceTypeDefinitionCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public PersistenceTypeDefinition createTypeDefinition(
			final long                                                    typeId         ,
			final String                                                  typeName       ,
			final String                                                  runtimeTypeName,
			final Class<?>                                                runtimeType    ,
			final XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers     ,
			final XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers
		)
		{
			return PersistenceTypeDefinition.New(typeId, typeName, runtimeTypeName, runtimeType, allMembers, instanceMembers);
		}
		
	}
	
}
