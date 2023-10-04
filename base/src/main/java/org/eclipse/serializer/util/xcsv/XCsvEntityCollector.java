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


import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.collections.types.XGettingList;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.collections.types.XSequence;
import org.eclipse.serializer.typing.XTypes;

import static org.eclipse.serializer.util.X.notNull;

public final class XCsvEntityCollector<T> implements XCsvRowCollector
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XCsvEntityParser.Abstract<T> parser;
	private final XSequence<? super T> rows  ;
	private final BulkList<String>     row   ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public XCsvEntityCollector(
		final XCsvEntityParser.Abstract<T> parser,
		final XSequence<? super T>                      rows
	)
	{
		super();
		this.parser = notNull(parser) ;
		this.rows   = notNull(rows)   ;
		this.row    = new BulkList<>();
	}

	@Override
	public void beginTable(
		final String tableName,
		final XGettingSequence<String> columnNames,
		final XGettingList<String> columnTypes
	)
	{
		this.parser.beginTable(tableName, columnNames, columnTypes);
	}

	@Override
	public void accept(final char[] data, final int offset, final int length)
	{
		this.row.add(data == null ? null : new String(data, offset, length));
	}

	@Override
	public void completeRow()
	{
		try
		{
			this.parser.validateRow(this.row);
		}
		catch(final Exception e)
		{
			throw new RuntimeException("Row validation failed at row " + this.rows.size(), e);
		}
		final T entity;
		try
		{
			entity = this.parser.apply(this.row);
		}
		catch(final RuntimeException e)
		{
			throw new RuntimeException("Exception while parsing row " + XTypes.to_int(this.rows.size()), e);
		}
		this.rows.add(entity);
		this.row.clear();
	}

	@Override
	public void completeTable()
	{
		this.row.clear();
		this.parser.completeTable();
	}

	public XSequence<? super T> rows()
	{
		return this.rows;
	}

}
