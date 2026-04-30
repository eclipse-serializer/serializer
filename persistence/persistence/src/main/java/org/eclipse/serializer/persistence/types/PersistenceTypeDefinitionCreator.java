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

/**
 * Factory for {@link PersistenceTypeDefinition} instances.
 * <p>
 * The indirection over {@link PersistenceTypeDefinition#New} exists so that foundation configuration
 * can swap the concrete definition class &mdash; e.g. for instrumentation, alternative caching, or
 * test doubles &mdash; without callers needing to know about it. The default implementation simply
 * delegates to the standard factory.
 *
 * @see PersistenceTypeDefinition
 */
@FunctionalInterface
public interface PersistenceTypeDefinitionCreator
{
	/**
	 * Creates a new {@link PersistenceTypeDefinition} from the passed attributes.
	 *
	 * @param typeId          the type id.
	 * @param typeName        the dictionary-stable type name.
	 * @param runtimeTypeName the current runtime type name; may be {@code null}.
	 * @param runtimeType     the runtime {@link Class}; may be {@code null} if no runtime counterpart.
	 * @param allMembers      the full member sequence in dictionary order.
	 * @param instanceMembers the subset of {@code allMembers} that contributes to a persisted instance.
	 *
	 * @return a new {@link PersistenceTypeDefinition}.
	 */
	public PersistenceTypeDefinition createTypeDefinition(
		long                                                    typeId         ,
		String                                                  typeName       ,
		String                                                  runtimeTypeName,
		Class<?>                                                runtimeType    ,
		XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers     ,
		XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers
	);



	/**
	 * Creates the default creator that delegates to {@link PersistenceTypeDefinition#New}.
	 *
	 * @return a new default {@link PersistenceTypeDefinitionCreator}.
	 */
	public static PersistenceTypeDefinitionCreator.Default New()
	{
		return new PersistenceTypeDefinitionCreator.Default();
	}

	/**
	 * Default creator that delegates straight to {@link PersistenceTypeDefinition#New}.
	 */
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
