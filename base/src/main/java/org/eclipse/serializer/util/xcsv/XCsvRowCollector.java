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

import org.eclipse.serializer.collections.types.XGettingList;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.functional._charRangeProcedure;


public interface XCsvRowCollector extends _charRangeProcedure
{
	public void beginTable(
		String                   tableName  ,
		XGettingSequence<String> columnNames,
		XGettingList<String> columnTypes
	);
	
	@Override
	public void accept(char[] data, int offset, int length);
	
	/**
	 * Calls without collected values (e.g. repeated calls) may not have undesired effects.
	 */
	public void completeRow();

	/**
	 * Calls without collected rows (e.g. repeated calls) may not have undesired effects.
	 */
	public void completeTable();
	
}
