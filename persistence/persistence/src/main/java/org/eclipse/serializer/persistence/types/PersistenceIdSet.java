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

public interface PersistenceIdSet extends _longIterable, Sized
{
	@Override
	public long size();

	@Override
	public void iterate(_longProcedure iterator);



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
