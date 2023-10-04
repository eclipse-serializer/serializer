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


public interface PersistenceTypeDictionaryProvider
{
	public PersistenceTypeDictionary provideTypeDictionary();

	
	
	public static PersistenceTypeDictionaryProvider.Default New(
		final PersistenceTypeDictionaryLoader   loader  ,
		final PersistenceTypeDictionaryCompiler compiler
	)
	{
		return new PersistenceTypeDictionaryProvider.Default(
			notNull(loader)  ,
			notNull(compiler)
		);
	}

	public final class Default implements PersistenceTypeDictionaryProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceTypeDictionaryLoader   loader  ;
		private final PersistenceTypeDictionaryCompiler compiler;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceTypeDictionaryLoader   loader  ,
			final PersistenceTypeDictionaryCompiler compiler
		)
		{
			super();
			this.loader   = loader  ;
			this.compiler = compiler;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public PersistenceTypeDictionary provideTypeDictionary()
		{
			final String              typeDictionaryString = this.loader.loadTypeDictionary();
			final PersistenceTypeDictionary typeDictionary = this.compiler.compileTypeDictionary(typeDictionaryString);
			
			return typeDictionary;
		}

	}
	
	
	
	public static PersistenceTypeDictionaryProvider.Caching Caching(
		final PersistenceTypeDictionaryProvider typeDictionaryImporter
	)
	{
		return new Caching(
			notNull(typeDictionaryImporter)
		);
	}
	
	public final class Caching implements PersistenceTypeDictionaryProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final     PersistenceTypeDictionaryProvider delegate        ;
		private transient PersistenceTypeDictionary         cachedDictionary;


		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Caching(final PersistenceTypeDictionaryProvider delegate)
		{
			super();
			this.delegate = delegate;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final PersistenceTypeDictionary provideTypeDictionary()
		{
			synchronized(this.delegate)
			{
				if(this.cachedDictionary == null)
				{
					this.cachedDictionary = this.delegate.provideTypeDictionary();
				}
				return this.cachedDictionary;
			}
		}
		
		public final void clear()
		{
			synchronized(this.delegate)
			{
				this.cachedDictionary = null;
			}
		}
		
	}

}
