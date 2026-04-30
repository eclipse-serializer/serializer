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

/**
 * {@link PersistenceTypeDictionaryProvider} variant that narrows the produced dictionary to a read-only
 * {@link PersistenceTypeDictionaryView}, i.e. one whose mutating methods reject calls. Used wherever consumers
 * must be prevented from registering further type definitions.
 *
 * @see PersistenceTypeDictionaryView
 * @see PersistenceTypeDictionaryProvider
 */
@FunctionalInterface
public interface PersistenceTypeDictionaryViewProvider extends PersistenceTypeDictionaryProvider
{
	@Override
	public PersistenceTypeDictionaryView provideTypeDictionary();



	/**
	 * Wraps an already-loaded {@link PersistenceTypeDictionaryView} as a provider that returns the same
	 * view on every call.
	 *
	 * @param typeDictionary the view to expose; must not be {@code null}.
	 *
	 * @return the wrapper provider.
	 */
	public static PersistenceTypeDictionaryViewProvider Wrapper(
		final PersistenceTypeDictionaryView typeDictionary
	)
	{
		return new PersistenceTypeDictionaryViewProvider.Wrapper(
			notNull(typeDictionary)
		);
	}

	/**
	 * Trivial {@link PersistenceTypeDictionaryViewProvider} that always returns the same view passed at
	 * construction time.
	 */
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
