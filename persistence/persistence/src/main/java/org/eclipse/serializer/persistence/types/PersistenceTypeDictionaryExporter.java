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

public interface PersistenceTypeDictionaryExporter
{
	public void exportTypeDictionary(PersistenceTypeDictionary typeDictionary);

	
	public static PersistenceTypeDictionaryExporter New(
		final PersistenceTypeDictionaryStorer storer
	)
	{
		return New(
			PersistenceTypeDictionaryAssembler.New(),
			storer
		);
	}
	
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
