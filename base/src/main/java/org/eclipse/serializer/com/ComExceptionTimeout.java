package org.eclipse.serializer.com;

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


public class ComExceptionTimeout extends ComException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ComExceptionTimeout()
	{
		super();
	}

	public ComExceptionTimeout(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ComExceptionTimeout(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public ComExceptionTimeout(final String message)
	{
		super(message);
	}

	public ComExceptionTimeout(final Throwable cause)
	{
		super(cause);
	}

}
