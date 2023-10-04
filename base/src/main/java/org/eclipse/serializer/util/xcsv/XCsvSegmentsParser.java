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


public interface XCsvSegmentsParser<I>
{
	public void parseSegments(I input);



	public interface Provider<I>
	{
		public XCsvSegmentsParser<I> provideSegmentsParser(XCsvConfiguration config, XCsvRowCollector rowAggregator);
	}
}
