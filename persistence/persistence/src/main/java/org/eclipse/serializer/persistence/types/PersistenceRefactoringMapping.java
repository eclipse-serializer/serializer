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

import org.eclipse.serializer.util.X;
import org.eclipse.serializer.collections.EqConstHashEnum;
import org.eclipse.serializer.collections.EqConstHashTable;
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.collections.types.XGettingTable;
import org.eclipse.serializer.collections.types.XImmutableEnum;
import org.eclipse.serializer.collections.types.XImmutableTable;
import org.eclipse.serializer.typing.KeyValue;

/**
 * A mapping that projects outdated identifiers (usually className#fieldName, but in case of root instances
 * also potentially arbitrary strings) to current identifiers.
 * 
 * 
 *
 */
public interface PersistenceRefactoringMapping
{
	public KeyValue<String, String> lookup(String key);
	
	public boolean isNewElement(String targetKey);
	

	
	public static PersistenceRefactoringMapping New()
	{
		return new Default(
			X.emptyTable(),
			X.empty()
		);
	}
	
	public static PersistenceRefactoringMapping New(
		final XGettingTable<String, String> entries
	)
	{
		return new Default(
			EqConstHashTable.New(entries),
			X.empty()
		);
	}
		
	public static PersistenceRefactoringMapping New(
		final XGettingTable<String, String> entries    ,
		final XGettingEnum<String>          newElements
	)
	{
		return new Default(
			EqConstHashTable.New(entries),
			EqConstHashEnum.New(newElements)
		);
	}
	
	public final class Default implements PersistenceRefactoringMapping
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final XImmutableTable<String, String> entries    ;
		private final XImmutableEnum<String>          newElements;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final XImmutableTable<String, String> entries    ,
			final XImmutableEnum<String>          newElements
		)
		{
			super();
			this.entries     = entries    ;
			this.newElements = newElements;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final KeyValue<String, String> lookup(final String key)
		{
			return this.entries.lookup(key);
		}
		
		@Override
		public final boolean isNewElement(final String targetKey)
		{
			return this.newElements.contains(targetKey);
		}
		
	}
	
}
