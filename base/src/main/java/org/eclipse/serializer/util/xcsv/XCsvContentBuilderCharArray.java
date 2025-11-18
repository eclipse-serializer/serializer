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

import org.eclipse.serializer.chars.StringTable;
import org.eclipse.serializer.chars.XCsvParserCharArray;
import org.eclipse.serializer.chars._charArrayRange;
import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.collections.types.XEnum;
import org.eclipse.serializer.collections.types.XGettingList;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.collections.types.XList;
import org.eclipse.serializer.util.Substituter;

import java.util.function.Consumer;

import static org.eclipse.serializer.util.X.mayNull;
import static org.eclipse.serializer.util.X.notNull;


public final class XCsvContentBuilderCharArray implements XCsvContent.Builder<_charArrayRange>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static XCsvContentBuilderCharArray New()
	{
		return New(XCSV.configurationDefault());
	}
	
	public static XCsvContentBuilderCharArray New(
		final XCsvDataType dataType
	)
	{
		return New(null, dataType);
	}

	public static XCsvContentBuilderCharArray New(
		final XCsvConfiguration csvConfiguration
	)
	{
		return New(csvConfiguration, null);
	}
	
	public static XCsvContentBuilderCharArray New(
		final XCsvConfiguration csvConfiguration,
		final XCsvDataType      dataType
	)
	{
		return new XCsvContentBuilderCharArray(
                mayNull(dataType),
                mayNull(csvConfiguration),
                Substituter.<String>New(),
                XCsvParserCharArray.New(),
                new StringTable.Default.Creator()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XCsvDataType                dataType     ;
	private final XCsvConfiguration           configuration;
	private final Substituter<String>         stringCache  ;
	private final XCsvParser<_charArrayRange> parser       ;
	private final StringTable.Creator         tableCreator ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private XCsvContentBuilderCharArray(
		final XCsvDataType                dataType     ,
		final XCsvConfiguration           configuration,
		final Substituter<String>         stringCache  ,
		final XCsvParser<_charArrayRange> parser       ,
		final StringTable.Creator         tableCreator
	)
	{
		super();
		this.dataType      = dataType     ;
		this.configuration = configuration;
		this.stringCache   = stringCache  ;
		this.parser        = parser       ;
		this.tableCreator  = tableCreator ;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XCsvContent build(final String name, final _charArrayRange data)
	{
		final BulkList<StringTable> tables         = BulkList.New();
		final TableCollector        tableCollector = new TableCollector(tables, this.tableCreator, this.stringCache);

		final XCsvConfiguration effectiveConfig = this.parser.parseCsvData(
			this.dataType,
			this.configuration,
			data,
			tableCollector
		);

		return XCsvContent.Default.NewTranslated(name, tables, effectiveConfig);
	}



	public final class TableCollector implements XCsvRowCollector
	{
		private final Substituter<String>    stringCache   ;
		private final Consumer<StringTable> tableCollector;
		private final StringTable.Creator    tableCreator  ;

		private final BulkList<String[]>     rows;
		private final BulkList<String>       row ;

		private String                       tableName  ;
		private XGettingSequence<String> columnNames;
		private XGettingList<String> columnTypes;


		public TableCollector(
			final Consumer<StringTable> tableCollector,
			final StringTable.Creator    tableCreator  ,
			final Substituter<String>    stringCache
		)
		{
			super();
			this.stringCache    = notNull(stringCache)   ;
			this.tableCollector = notNull(tableCollector);
			this.tableCreator   = notNull(tableCreator)  ;
			this.rows           = new BulkList<>()       ;
			this.row            = new BulkList<>()       ;
		}

		@Override
		public void beginTable(
			final String                   tableName  ,
			final XGettingSequence<String> columnNames,
			final XGettingList<String>     columnTypes
		)
		{
			this.tableName   = tableName  ;
			this.columnNames = columnNames;
			this.columnTypes = columnTypes;
		}

		@Override
		public final void accept(final char[] data, final int offset, final int length)
		{
			this.row.add(this.stringCache.substitute(data == null ? null : new String(data, offset, length)));
		}

		@Override
		public final void completeRow()
		{
			if(this.row.isEmpty())
			{
				// either already completed or data ended after a line separator
				return;
			}
			this.rows.add(this.row.toArray(String.class));
			this.row.clear();
		}

		@Override
		public final void completeTable()
		{
			this.tableCollector.accept(
				this.tableCreator.createStringTable(
					this.tableName  ,
					this.columnNames,
					this.columnTypes,
					this.rows
				)
			);
			this.row.clear();
			this.rows.clear();
		}

	}

	static final class ColumnNamesCollector implements Consumer<String>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final XEnum<String> columnNames;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public ColumnNamesCollector(final XEnum<String> columnNames)
		{
			super();
			this.columnNames = notNull(columnNames);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void accept(final String columnName)
		{
			this.columnNames.add(columnName);
		}

	}

	static final class ColumnTypesCollector implements Consumer<String>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final XList<String> columnTypes;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public ColumnTypesCollector(final XList<String> columnTypes)
		{
			super();
			this.columnTypes = notNull(columnTypes);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void accept(final String columnName)
		{
			this.columnTypes.add(columnName);
		}

	}

}
