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

public class NumberRangeException extends RuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public NumberRangeException()
	{
		super();
	}

	public NumberRangeException(final String message)
	{
		super(message);
	}

	public NumberRangeException(final Throwable cause)
	{
		super(cause);
	}

	public NumberRangeException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

}
