package org.eclipse.serializer.exceptions;

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


public class ParsingExceptionUnexpectedCharacter extends ParsingException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final char expectedCharacter   ;
	private final char encounteredCharacter;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ParsingExceptionUnexpectedCharacter(
		final char expectedCharacter   ,
		final char encounteredCharacter
	)
	{
		super();
		this.expectedCharacter    = expectedCharacter   ;
		this.encounteredCharacter = encounteredCharacter;
	}

	public ParsingExceptionUnexpectedCharacter(
		final char      expectedCharacter   ,
		final char      encounteredCharacter,
		final Throwable cause
	)
	{
		super(cause);
		this.expectedCharacter    = expectedCharacter   ;
		this.encounteredCharacter = encounteredCharacter;
	}

	public ParsingExceptionUnexpectedCharacter(
		final char   expectedCharacter   ,
		final char   encounteredCharacter,
		final String message
	)
	{
		super(message);
		this.expectedCharacter    = expectedCharacter   ;
		this.encounteredCharacter = encounteredCharacter;
	}

	public ParsingExceptionUnexpectedCharacter(
		final char      expectedCharacter   ,
		final char      encounteredCharacter,
		final String    message             ,
		final Throwable cause
	)
	{
		super(message, cause);
		this.expectedCharacter    = expectedCharacter   ;
		this.encounteredCharacter = encounteredCharacter;
	}

	public ParsingExceptionUnexpectedCharacter(
		final char      expectedCharacter   ,
		final char      encounteredCharacter,
		final String    message             ,
		final Throwable cause               ,
		final boolean   enableSuppression   ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.expectedCharacter    = expectedCharacter   ;
		this.encounteredCharacter = encounteredCharacter;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final char expectedCharacter()
	{
		return this.expectedCharacter;
	}
	
	public final char encounteredCharacter()
	{
		return this.encounteredCharacter;
	}
	
	@Override
	public String assembleDetailString()
	{
		return "Encountered character '"
			+ this.encounteredCharacter
			+ "' is not the expected character '"
			+ this.expectedCharacter
			+ "'."
		;
	}
	
}
