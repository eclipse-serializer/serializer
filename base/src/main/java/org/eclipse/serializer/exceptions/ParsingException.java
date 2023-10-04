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

public class ParsingException extends BaseException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ParsingException()
	{
		super();
	}

	public ParsingException(final Throwable cause)
	{
		super(cause);
	}

	public ParsingException(final String message)
	{
		super(message);
	}

	public ParsingException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public ParsingException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
