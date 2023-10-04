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

@FunctionalInterface
public interface PersistenceTypeDictionaryViewProvider extends PersistenceTypeDictionaryProvider
{
	@Override
	public PersistenceTypeDictionaryView provideTypeDictionary();
	
	
	
	public static PersistenceTypeDictionaryViewProvider Wrapper(
		final PersistenceTypeDictionaryView typeDictionary
	)
	{
		return new PersistenceTypeDictionaryViewProvider.Wrapper(
			notNull(typeDictionary)
		);
	}
	
	public final class Wrapper implements PersistenceTypeDictionaryViewProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		
		private final PersistenceTypeDictionaryView typeDictionary;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Wrapper(final PersistenceTypeDictionaryView typeDictionary)
		{
			super();
			this.typeDictionary = typeDictionary;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public PersistenceTypeDictionaryView provideTypeDictionary()
		{
			return this.typeDictionary;
		}
		
	}
	
}
