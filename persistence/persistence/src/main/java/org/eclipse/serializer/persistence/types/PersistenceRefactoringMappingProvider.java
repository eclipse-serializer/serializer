package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
 * %%
 * Copyright (C) 2023 Eclipse Foundation
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import org.eclipse.serializer.util.X;
import org.eclipse.serializer.collections.EqHashEnum;
import org.eclipse.serializer.collections.EqHashTable;
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.collections.types.XGettingTable;
import org.eclipse.serializer.typing.KeyValue;

public interface PersistenceRefactoringMappingProvider
{
	public PersistenceRefactoringMapping provideRefactoringMapping();
	
	public static PersistenceRefactoringMappingProvider NewEmpty()
	{
		return new PersistenceRefactoringMappingProvider.Default(
			X.emptyTable(),
			X.empty()
		);
	}
	
	/**
	 * Quick provisional documentation:
	 * <p>
	 * A compatability variant of {@link #New(XGettingSequence)} to allow both XCollections and JDK collections
	 * to be passed.
	 * 
	 * @param entries the refactoring mapping entries to be used.
	 * @return a {@link PersistenceRefactoringMappingProvider} instance using the passed entries.
	 */
	public static PersistenceRefactoringMappingProvider New(
		final Iterable<? extends KeyValue<String, String>> entries
	)
	{
		final EqHashTable<String, String> table       = EqHashTable.New();
		final EqHashEnum<String>          newElements = EqHashEnum .New();
		
		for(final KeyValue<String, String> entry : entries)
		{
			if(entry.key() == null)
			{
				newElements.add(entry.value());
			}
			else
			{
				table.add(entry);
			}
		}
		
		return new PersistenceRefactoringMappingProvider.Default(table, newElements);
	}
	
	/**
	 * Quick provisional documentation:
	 * <p>
	 * A {@code XGettingTable<K, V>} is a {@code XGettingSequence<KeyValue<K, V>>}.<br>
	 * For example {@link EqHashTable}:
	 * <pre>{@code
	 * EqHashTable<String, String> entries = EqHashTable.New(
	 *     X.KeyValue("key1", "value1"),
	 *     X.KeyValue("key2", "value2")
	 * );
	 * }</pre>
	 * 
	 * @param entries the refactoring mapping entries to be used.
	 * @return a {@link PersistenceRefactoringMappingProvider} instance using the passed entries.
	 */
	public static PersistenceRefactoringMappingProvider New(
		final XGettingSequence<KeyValue<String, String>> entries
	)
	{
		return New((Iterable<? extends KeyValue<String, String>>)entries);
	}
	
	public final class Default implements PersistenceRefactoringMappingProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final XGettingTable<String, String> entries    ;
		private final XGettingEnum<String>          newElements;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final XGettingTable<String, String> entries    ,
			final XGettingEnum<String>          newElements
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
		public PersistenceRefactoringMapping provideRefactoringMapping()
		{
			// nifty: immure at creation time, not before.
			return new PersistenceRefactoringMapping.Default(
				this.entries.immure()    ,
				this.newElements.immure()
			);
		}
		
	}
	
}
