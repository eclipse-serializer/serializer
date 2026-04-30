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
 * Source of a {@link PersistenceTypeDictionary}. Combines a {@link PersistenceTypeDictionaryLoader} (which
 * supplies the textual on-disk form) with a {@link PersistenceTypeDictionaryCompiler} (which parses and
 * builds the live dictionary). Optional {@link Caching} decoration retains the compiled dictionary between
 * calls.
 *
 * @see PersistenceTypeDictionaryLoader
 * @see PersistenceTypeDictionaryCompiler
 * @see PersistenceTypeDictionaryManager
 */
public interface PersistenceTypeDictionaryProvider
{
	/**
	 * Loads, parses and builds the type dictionary, returning the live result.
	 *
	 * @return the type dictionary.
	 */
	public PersistenceTypeDictionary provideTypeDictionary();



	/**
	 * Creates a {@link Default} provider that loads via {@code loader} and compiles via {@code compiler} on
	 * every call.
	 *
	 * @param loader   the loader supplying the textual dictionary; must not be {@code null}.
	 * @param compiler the compiler turning the textual form into a live dictionary; must not be {@code null}.
	 *
	 * @return the new provider.
	 */
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

	/**
	 * Default non-caching {@link PersistenceTypeDictionaryProvider}: every call to
	 * {@link #provideTypeDictionary()} re-loads the textual form and re-compiles it.
	 */
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
	
	
	
	/**
	 * Wraps the passed provider in a {@link Caching} decorator that compiles the dictionary on first access
	 * and returns the same instance on every subsequent call until {@link Caching#clear()} is invoked.
	 *
	 * @param typeDictionaryImporter the underlying provider; must not be {@code null}.
	 *
	 * @return the caching decorator.
	 */
	public static PersistenceTypeDictionaryProvider.Caching Caching(
		final PersistenceTypeDictionaryProvider typeDictionaryImporter
	)
	{
		return new Caching(
			notNull(typeDictionaryImporter)
		);
	}

	/**
	 * Caching {@link PersistenceTypeDictionaryProvider} decorator: compiles its delegate on first call and
	 * returns the same instance until {@link #clear()} discards the cached dictionary. Synchronizes on the
	 * delegate.
	 */
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
		
		/**
		 * Discards the cached dictionary so that the next {@link #provideTypeDictionary()} call re-invokes the
		 * delegate.
		 */
		public final void clear()
		{
			synchronized(this.delegate)
			{
				this.cachedDictionary = null;
			}
		}
		
	}

}
