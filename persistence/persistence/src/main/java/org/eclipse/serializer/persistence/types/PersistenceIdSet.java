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

import org.eclipse.serializer.collections.CapacityExceededException;
import org.eclipse.serializer.collections.interfaces.Sized;
import org.eclipse.serializer.collections.interfaces._longCollector;
import org.eclipse.serializer.functional._longIterable;
import org.eclipse.serializer.functional._longProcedure;

/**
 * Append-only set of object ids backed by a primitive {@code long[]}. The persistence layer accumulates the
 * ids of instances that need to be loaded or stored into one of these and then iterates the result via
 * {@link #iterate(_longProcedure)} once the collection phase is done.
 * <p>
 * Despite the name, the {@link Default} implementation does not actually deduplicate: it appends every
 * accepted id to its backing array. Callers that need uniqueness must enforce it themselves.
 *
 * @see _longIterable
 */
public interface PersistenceIdSet extends _longIterable, Sized
{
	@Override
	public long size();

	@Override
	public void iterate(_longProcedure iterator);



	/**
	 * Default {@link PersistenceIdSet} backed by a {@code long[]} that grows by powers of two when full.
	 * Implements {@link _longCollector#accept(long)} for collector-style population. Not thread-safe.
	 */
	final class Default implements PersistenceIdSet, _longCollector
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		private static final int DEFAULT_CAPACITY = 64;



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private long[] data = new long[DEFAULT_CAPACITY];
		private int    size;



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void accept(final long id)
		{
			if(this.size >= this.data.length)
			{
				if(this.size >= Integer.MAX_VALUE)
				{
					throw new CapacityExceededException();
				}
				System.arraycopy(this.data, 0, this.data = new long[(int)(this.data.length * 2.0f)], 0, this.size);
			}
			this.data[this.size++] = id;
		}

		@Override
		public void iterate(final _longProcedure procedure)
		{
			final long[] data = this.data;
			final int    size = this.size;

			for(int i = 0; i < size; i++)
			{
				procedure.accept(data[i]);
			}
		}

		@Override
		public long size()
		{
			return this.size;
		}

		@Override
		public boolean isEmpty()
		{
			return this.size == 0;
		}

	}

}
