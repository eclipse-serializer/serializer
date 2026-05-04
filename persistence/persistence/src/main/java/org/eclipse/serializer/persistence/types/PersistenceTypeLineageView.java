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

import org.eclipse.serializer.collections.EqConstHashTable;
import org.eclipse.serializer.collections.types.XGettingTable;


/**
 * Immutable snapshot of a {@link PersistenceTypeLineage}. Implements the same interface but rejects all
 * mutating operations; the entries map is wrapped as an immutable hash table.
 * <p>
 * Created via {@link #New(PersistenceTypeLineage)}, which copies the lineage's state under its monitor and
 * returns a self-contained snapshot that callers can read without further synchronization.
 *
 * @see PersistenceTypeLineage
 */
public interface PersistenceTypeLineageView extends PersistenceTypeLineage
{
	@Override
	public default PersistenceTypeLineageView view()
	{
		return this;
	}

	@Override
	public default boolean registerTypeDefinition(final PersistenceTypeDefinition typeDefinition)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public default boolean setRuntimeTypeDefinition(final PersistenceTypeDefinition runtimeDefinition)
	{
		throw new UnsupportedOperationException();
	}


	/**
	 * Creates an immutable snapshot of {@code typeLineage}. The lineage's state is read under its monitor
	 * and copied into the snapshot, so subsequent mutations of the source lineage are not reflected.
	 *
	 * @param typeLineage the lineage to snapshot.
	 *
	 * @return the immutable view.
	 */
	public static PersistenceTypeLineageView New(final PersistenceTypeLineage typeLineage)
	{
		synchronized(typeLineage)
		{
			return new PersistenceTypeLineageView.Default(
				typeLineage.typeName()                     ,
				typeLineage.type()                         ,
				EqConstHashTable.New(typeLineage.entries()),
				typeLineage.runtimeDefinition()
			);
		}
	}

	/**
	 * Default immutable {@link PersistenceTypeLineageView}. Backed by an
	 * {@link org.eclipse.serializer.collections.EqConstHashTable}; all mutating methods throw
	 * {@link UnsupportedOperationException}.
	 */
	public final class Default implements PersistenceTypeLineageView
	{
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final String                                            runtimeTypeName  ;
		final Class<?>                                          runtimeType      ;
		final EqConstHashTable<Long, PersistenceTypeDefinition> entries          ;
		final PersistenceTypeDefinition                         runtimeDefinition;



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final String                                            runtimeTypeName  ,
			final Class<?>                                          runtimeType      ,
			final EqConstHashTable<Long, PersistenceTypeDefinition> entries          ,
			final PersistenceTypeDefinition                         runtimeDefinition
		)
		{
			super();
			this.runtimeTypeName   = runtimeTypeName  ;
			this.runtimeType       = runtimeType      ;
			this.entries           = entries          ;
			this.runtimeDefinition = runtimeDefinition;
		}



		////////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final String typeName()
		{
			return this.runtimeTypeName;
		}

		@Override
		public final XGettingTable<Long, PersistenceTypeDefinition> entries()
		{
			return this.entries;
		}

		@Override
		public final Class<?> type()
		{
			return this.runtimeType;
		}

		@Override
		public final PersistenceTypeDefinition runtimeDefinition()
		{
			return this.runtimeDefinition;
		}
		
		@Override
		public final PersistenceTypeDefinition latest()
		{
			return this.entries.values().peek();
		}
				
	}

}
