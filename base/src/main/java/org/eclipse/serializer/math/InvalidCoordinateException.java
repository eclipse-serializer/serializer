package org.eclipse.serializer.math;

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

public class InvalidCoordinateException extends RuntimeException
{
	public InvalidCoordinateException()
	{
		super();
	}

	public InvalidCoordinateException(final String message)
	{
		super(message);
	}

	public InvalidCoordinateException(final Throwable cause)
	{
		super(cause);
	}

	public InvalidCoordinateException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

}
