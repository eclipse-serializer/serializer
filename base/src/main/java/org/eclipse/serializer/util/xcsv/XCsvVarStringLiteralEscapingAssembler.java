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

import org.eclipse.serializer.chars.EscapeHandler;
import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.functional._charProcedure;

import static org.eclipse.serializer.util.X.notNull;

public class XCsvVarStringLiteralEscapingAssembler implements _charProcedure
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final XCsvVarStringLiteralEscapingAssembler New(final XCsvConfiguration csvConfig, final VarString vs)
	{
		return new XCsvVarStringLiteralEscapingAssembler(
			notNull(vs)                            ,
			        csvConfig.valueSeparator()     ,
			        csvConfig.literalDelimiter()   ,
			        csvConfig.escaper()            ,
			notNull(csvConfig.valueEscapeHandler())
		);
	}

	public static final XCsvVarStringLiteralEscapingAssembler New(final XCsvConfiguration csvConfig)
	{
		return New(csvConfig, VarString.New());
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final VarString     vs           ;
	final char          separator    ;
	final char          delimiter    ;
	final char          escaper      ;
	final EscapeHandler escapeHandler;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	XCsvVarStringLiteralEscapingAssembler(
		final VarString     vs           ,
		final char          separator    ,
		final char          delimiter    ,
		final char          escaper      ,
		final EscapeHandler escapeHandler
	)
	{
		super();
		this.vs            = vs           ;
		this.separator     = separator    ;
		this.delimiter     = delimiter    ;
		this.escaper       = escaper      ;
		this.escapeHandler = escapeHandler;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final char c)
	{
		if(this.escapeHandler.needsEscaping(c))
		{
			this.vs.add(this.escaper).add(this.escapeHandler.transformEscapedChar(c));
		}
		else
		{
			this.vs.add(c);
		}
	}

}
