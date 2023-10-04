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


public interface XCsvParser<I>
{
	public XCsvConfiguration parseCsvData(
		XCsvDataType                   dataType              ,
		XCsvConfiguration              config                ,
		I                              input                 ,
		XCsvSegmentsParser.Provider<I> segmentsParserProvider,
		XCsvRowCollector               rowAggregator
	);
	
	public default XCsvConfiguration parseCsvData(
		final XCsvConfiguration              config                ,
		final I                              input                 ,
		final XCsvSegmentsParser.Provider<I> segmentsParserProvider,
		final XCsvRowCollector               rowAggregator
	)
	{
		return this.parseCsvData(null, config, input, segmentsParserProvider, rowAggregator);
	}
	
	public default XCsvConfiguration parseCsvData(
		final XCsvConfiguration config       ,
		final I                 input        ,
		final XCsvRowCollector  rowAggregator
	)
	{
		return this.parseCsvData(null, config, input, null, rowAggregator);
	}

	public default XCsvConfiguration parseCsvData(
		final XCsvDataType      dataType     ,
		final XCsvConfiguration config       ,
		final I                 input        ,
		final XCsvRowCollector  rowAggregator
	)
	{
		return this.parseCsvData(dataType, config, input, null, rowAggregator);
	}


	public interface Creator<D>
	{
		public XCsvParser<D> createCsvStringParser();
	}

	public interface Provider<D>
	{
		public XCsvParser<D> provideCsvStringParser();

		public void disposeCsvStringParser(XCsvParser<D> parser);
	}

}
