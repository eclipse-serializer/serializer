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

public interface PersistenceTypeLineageCreator
{
	public PersistenceTypeLineage createTypeLineage(Class<?> type);
	
	public PersistenceTypeLineage createTypeLineage(String typeName, Class<?> type);
	
		
	
	public static PersistenceTypeLineageCreator.Default New()
	{
		return new PersistenceTypeLineageCreator.Default();
	}
	
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
