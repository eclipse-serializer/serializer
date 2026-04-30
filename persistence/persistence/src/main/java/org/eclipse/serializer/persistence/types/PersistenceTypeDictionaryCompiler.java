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

import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionParser;

/**
 * Composes a {@link PersistenceTypeDictionaryParser} and a {@link PersistenceTypeDictionaryBuilder} into a
 * single step that turns the textual on-disk dictionary form into a live {@link PersistenceTypeDictionary}.
 *
 * @see PersistenceTypeDictionaryParser
 * @see PersistenceTypeDictionaryBuilder
 * @see PersistenceTypeDictionaryProvider
 */
public interface PersistenceTypeDictionaryCompiler
{
	/**
	 * Parses the passed textual dictionary and builds a {@link PersistenceTypeDictionary} from the resulting
	 * entries.
	 *
	 * @param input the textual dictionary; an empty input yields an empty dictionary.
	 *
	 * @return the compiled type dictionary.
	 *
	 * @throws PersistenceExceptionParser if {@code input} is not a syntactically valid dictionary.
	 */
	public PersistenceTypeDictionary compileTypeDictionary(String input)
		throws PersistenceExceptionParser
	;



	/**
	 * Creates a {@link Default} compiler that uses the passed parser and builder.
	 *
	 * @param parser  the parser to use; must not be {@code null}.
	 * @param builder the builder to use; must not be {@code null}.
	 *
	 * @return the new compiler.
	 */
	public static PersistenceTypeDictionaryCompiler.Default New(
		final PersistenceTypeDictionaryParser  parser ,
		final PersistenceTypeDictionaryBuilder builder
	)
	{
		return new PersistenceTypeDictionaryCompiler.Default(
			notNull(parser) ,
			notNull(builder)
		);
	}

	/**
	 * Default {@link PersistenceTypeDictionaryCompiler}: simply chains
	 * {@link PersistenceTypeDictionaryParser#parseTypeDictionaryEntries(String)} into
	 * {@link PersistenceTypeDictionaryBuilder#buildTypeDictionary(XGettingSequence)}.
	 */
	public final class Default implements PersistenceTypeDictionaryCompiler
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceTypeDictionaryParser  parser ;
		private final PersistenceTypeDictionaryBuilder builder;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceTypeDictionaryParser  parser ,
			final PersistenceTypeDictionaryBuilder builder
		)
		{
			super();
			this.parser  = parser ;
			this.builder = builder;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////
		
		@Override
		public PersistenceTypeDictionary compileTypeDictionary(final String input) throws PersistenceExceptionParser
		{
			final XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries =
				this.parser.parseTypeDictionaryEntries(input)
			;
			final PersistenceTypeDictionary typeDictionary =
				this.builder.buildTypeDictionary(entries)
			;
			
			return typeDictionary;
		}

	}
	
}
