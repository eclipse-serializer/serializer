package org.eclipse.serializer.util.xcsv;

/*-
 * #%L
 * Eclipse Serializer Base
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

import org.eclipse.serializer.chars._charArrayRange;
import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.collections.types.XGettingList;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.collections.types.XSequence;
import org.eclipse.serializer.exceptions.XCsvException;
import org.eclipse.serializer.typing.XTypes;


public interface XCsvEntityParser<T>
{

	XGettingList<T> parse(final _charArrayRange input);

	<C extends XSequence<? super T>> C parseInto(final _charArrayRange input, final C collector);



	abstract class Abstract<T> implements XCsvEntityParser<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		private static final int DEFAULT_COLLECTOR_CAPACITY = 1024;
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final int columnCount             ;
		private final int collectorInitialCapacity;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Abstract(final int columnCount)
		{
			this(columnCount, DEFAULT_COLLECTOR_CAPACITY);
		}

		public Abstract(final int columnCount, final int collectorInitialCapacity)
		{
			super();
			this.columnCount = columnCount;
			this.collectorInitialCapacity = collectorInitialCapacity;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		@Override
		public BulkList<T> parse(final _charArrayRange input)
		{
			return this.parseInto(input, this.collector());
		}

		protected BulkList<T> collector()
		{
			return new BulkList<>(this.collectorInitialCapacity);
		}
		
		protected int columnCount()
		{
			return this.columnCount;
		}
		
		protected void beginTable(
			final String                   tableName  ,
			final XGettingSequence<String> columnNames,
			final XGettingList<String>     columnTypes
		)
		{
			// no-op
		}
		
		protected void completeTable()
		{
			// no-op
		}

		protected void validateRow(final BulkList<String> row)
		{
			if(XTypes.to_int(row.size()) == this.columnCount())
			{
				return;
			}
			throw new XCsvException("Column count mismatch (" + row.size() + " != " + this.columnCount() + ")");
		}



		protected abstract T apply(BulkList<String> row);
	}

}
