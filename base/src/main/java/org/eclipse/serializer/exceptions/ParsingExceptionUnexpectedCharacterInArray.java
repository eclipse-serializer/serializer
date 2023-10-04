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

public class ParsingExceptionUnexpectedCharacterInArray extends ParsingExceptionUnexpectedCharacter
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final char[] array;
	private final int    index;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ParsingExceptionUnexpectedCharacterInArray(
		final char[] array               ,
		final int    index               ,
		final char   expectedCharacter   ,
		final char   encounteredCharacter
	)
	{
		super(expectedCharacter, encounteredCharacter);
		this.array = array;
		this.index = index;
	}

	public ParsingExceptionUnexpectedCharacterInArray(
		final char[]    array               ,
		final int       index               ,
		final char      expectedCharacter   ,
		final char      encounteredCharacter,
		final Throwable cause
	)
	{
		super(expectedCharacter, encounteredCharacter, cause);
		this.array = array;
		this.index = index;
	}

	public ParsingExceptionUnexpectedCharacterInArray(
		final char[] array               ,
		final int    index               ,
		final char   expectedCharacter   ,
		final char   encounteredCharacter,
		final String message
	)
	{
		super(expectedCharacter, encounteredCharacter, message);
		this.array = array;
		this.index = index;
	}

	public ParsingExceptionUnexpectedCharacterInArray(
		final char[]    array               ,
		final int       index               ,
		final char      expectedCharacter   ,
		final char      encounteredCharacter,
		final String    message             ,
		final Throwable cause
	)
	{
		super(expectedCharacter, encounteredCharacter, message, cause);
		this.array = array;
		this.index = index;
	}

	public ParsingExceptionUnexpectedCharacterInArray(
		final char[]    array               ,
		final int       index               ,
		final char      expectedCharacter   ,
		final char      encounteredCharacter,
		final String    message             ,
		final Throwable cause               ,
		final boolean   enableSuppression   ,
		final boolean   writableStackTrace
	)
	{
		super(expectedCharacter, encounteredCharacter, message, cause, enableSuppression, writableStackTrace);
		this.array = array;
		this.index = index;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final char[] array()
	{
		return this.array;
	}
	
	public final int index()
	{
		return this.index;
	}
	
	@Override
	public String assembleDetailString()
	{
		return "Problem at index " + this.index + ": " + super.assembleDetailString();
	}
	
}
