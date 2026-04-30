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
 * Factory for empty {@link PersistenceTypeDictionary} instances. Indirection used by
 * {@link PersistenceTypeDictionaryBuilder} so that callers can inject the {@link PersistenceTypeLineageCreator}
 * that should back lineage creation in newly built dictionaries.
 *
 * @see PersistenceTypeDictionary
 * @see PersistenceTypeDictionaryBuilder
 */
public interface PersistenceTypeDictionaryCreator
{
	/**
	 * Creates a new, empty type dictionary backed by the lineage creator this factory was configured with.
	 *
	 * @return the new empty dictionary.
	 */
	public PersistenceTypeDictionary createTypeDictionary();



	/**
	 * Creates a {@link Default} creator that produces dictionaries backed by the passed lineage creator.
	 *
	 * @param typeLineageCreator the lineage creator each produced dictionary will use; must not be
	 *                           {@code null}.
	 *
	 * @return the new factory.
	 */
	public static PersistenceTypeDictionaryCreator.Default New(
		final PersistenceTypeLineageCreator typeLineageCreator
	)
	{
		return new PersistenceTypeDictionaryCreator.Default(
			notNull(typeLineageCreator)
		);
	}

	/**
	 * Default {@link PersistenceTypeDictionaryCreator} delegating to
	 * {@link PersistenceTypeDictionary#New(PersistenceTypeLineageCreator)}.
	 */
	public final class Default implements PersistenceTypeDictionaryCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeLineageCreator typeLineageCreator;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final PersistenceTypeLineageCreator typeLineageCreator)
		{
			super();
			this.typeLineageCreator = typeLineageCreator;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public PersistenceTypeDictionary createTypeDictionary()
		{
			return PersistenceTypeDictionary.New(this.typeLineageCreator);
		}
		
	}
	
}
