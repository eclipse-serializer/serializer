package org.eclipse.serializer.afs.exceptions;

/*-
 * #%L
 * Eclipse Serializer Abstract File System
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

public class AfsExceptionUnresolvablePathElement extends AfsException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AfsExceptionUnresolvablePathElement()
	{
		super();
	}

	public AfsExceptionUnresolvablePathElement(final String message)
	{
		super(message);
	}

	public AfsExceptionUnresolvablePathElement(final Throwable cause)
	{
		super(cause);
	}

	public AfsExceptionUnresolvablePathElement(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	public AfsExceptionUnresolvablePathElement(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
