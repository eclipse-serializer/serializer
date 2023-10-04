package org.eclipse.serializer.persistence.exceptions;

/*-
 * #%L
 * Eclipse Serializer Persistence
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

import org.eclipse.serializer.exceptions.BaseException;

/*
 * XXX check usages of this type, replace by better typed exceptions
 */
public class PersistenceException extends BaseException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceException()
	{
		super();
	}

	public PersistenceException(final String message)
	{
		super(message);
	}

	public PersistenceException(final Throwable cause)
	{
		super(cause);
	}

	public PersistenceException(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	public PersistenceException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
