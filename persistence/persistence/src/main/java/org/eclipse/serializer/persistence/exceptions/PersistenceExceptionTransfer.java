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

public class PersistenceExceptionTransfer extends PersistenceException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTransfer()
	{
		this(null, null);
	}

	public PersistenceExceptionTransfer(final String message)
	{
		this(message, null);
	}

	public PersistenceExceptionTransfer(final Throwable cause)
	{
		this(null, cause);
	}

	public PersistenceExceptionTransfer(final String message, final Throwable cause)
	{
		this(message, cause, true, true);
	}

	public PersistenceExceptionTransfer(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
