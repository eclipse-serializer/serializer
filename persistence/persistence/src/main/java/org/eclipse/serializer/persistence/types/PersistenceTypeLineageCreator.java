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

/**
 * Factory for empty {@link PersistenceTypeLineage} instances. Pluggable so that callers needing custom
 * lineage subtypes can supply their own implementation; the bundled {@link Default} produces plain
 * {@link PersistenceTypeLineage.Default}s.
 *
 * @see PersistenceTypeLineage
 */
public interface PersistenceTypeLineageCreator
{
	/**
	 * Creates a new empty lineage for the passed runtime class, deriving the type name from the class.
	 *
	 * @param type the runtime class.
	 *
	 * @return the newly created lineage.
	 */
	public PersistenceTypeLineage createTypeLineage(Class<?> type);

	/**
	 * Creates a new empty lineage for the passed type name and (possibly {@code null}) runtime class. Use
	 * this overload when the textual type name does not match the runtime class's
	 * {@link Class#getName() FQN} &mdash; e.g. for types whose name has been refactored.
	 *
	 * @param typeName the textual type name.
	 * @param type     the runtime class, or {@code null} if no runtime class exists.
	 *
	 * @return the newly created lineage.
	 */
	public PersistenceTypeLineage createTypeLineage(String typeName, Class<?> type);



	/**
	 * Creates a new {@link Default} lineage creator.
	 *
	 * @return the newly created creator.
	 */
	public static PersistenceTypeLineageCreator.Default New()
	{
		return new PersistenceTypeLineageCreator.Default();
	}

	/**
	 * Default {@link PersistenceTypeLineageCreator}: produces {@link PersistenceTypeLineage.Default}
	 * instances via {@link PersistenceTypeLineage#New(String, Class)}.
	 */
	public final class Default implements PersistenceTypeLineageCreator
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
		public PersistenceTypeLineage createTypeLineage(final String typeName, final Class<?> type)
		{
			return PersistenceTypeLineage.New(typeName, type);
		}
		
		@Override
		public PersistenceTypeLineage createTypeLineage(final Class<?> type)
		{
			return this.createTypeLineage(type.getName(), type);
		}
				
	}
	
}
