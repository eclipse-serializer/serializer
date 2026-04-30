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

import org.eclipse.serializer.chars.VarString;

/**
 * Wires a {@link PersistenceTypeDictionaryAssembler} together with a
 * {@link PersistenceTypeDictionaryStorer} to write the textual form of a {@link PersistenceTypeDictionary} to
 * its persistent location in one step. Used both at runtime (when the dictionary is updated by registering new
 * type definitions) and ad-hoc for export/debugging.
 *
 * @see PersistenceTypeDictionaryAssembler
 * @see PersistenceTypeDictionaryStorer
 */
public interface PersistenceTypeDictionaryExporter
{
	/**
	 * Assembles {@code typeDictionary} into its textual form and writes it to the configured storer.
	 *
	 * @param typeDictionary the dictionary to export.
	 */
	public void exportTypeDictionary(PersistenceTypeDictionary typeDictionary);


	/**
	 * Creates an exporter using a default {@link PersistenceTypeDictionaryAssembler} and the passed storer.
	 *
	 * @param storer the sink for the textual dictionary; must not be {@code null}.
	 *
	 * @return the new exporter.
	 */
	public static PersistenceTypeDictionaryExporter New(
		final PersistenceTypeDictionaryStorer storer
	)
	{
		return New(
			PersistenceTypeDictionaryAssembler.New(),
			storer
		);
	}

	/**
	 * Creates an exporter combining the passed assembler and storer.
	 *
	 * @param assembler the assembler producing the textual form; must not be {@code null}.
	 * @param storer    the sink for the textual dictionary; must not be {@code null}.
	 *
	 * @return the new exporter.
	 */
	public static PersistenceTypeDictionaryExporter New(
		final PersistenceTypeDictionaryAssembler assembler,
		final PersistenceTypeDictionaryStorer    storer
	)
	{
		return new PersistenceTypeDictionaryExporter.Default(
			notNull(assembler),
			notNull(storer)
		);
	}


	/**
	 * Default {@link PersistenceTypeDictionaryExporter}: assembles the dictionary into a string and forwards
	 * it to the storer.
	 */
	public final class Default implements PersistenceTypeDictionaryExporter
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceTypeDictionaryAssembler assembler;
		private final PersistenceTypeDictionaryStorer    storer   ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceTypeDictionaryAssembler assembler,
			final PersistenceTypeDictionaryStorer    storer
		)
		{
			super();
			this.assembler = assembler;
			this.storer    = storer   ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void exportTypeDictionary(final PersistenceTypeDictionary typeDictionary)
		{
			final String typeDictionaryString = this.assembler.assemble(
				VarString.New(),
				typeDictionary
			).toString();
			
			this.storer.storeTypeDictionary(typeDictionaryString);
		}

	}

}
