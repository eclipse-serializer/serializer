package org.eclipse.serializer.persistence.binary.exceptions;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
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

import org.eclipse.serializer.persistence.exceptions.PersistenceException;

/*
 * XXX check usages of this type, replace by better typed exceptions
 */
public class BinaryPersistenceException extends PersistenceException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryPersistenceException()
	{
		this(null, null);
	}

	public BinaryPersistenceException(final String message)
	{
		this(message, null);
	}

	public BinaryPersistenceException(final Throwable cause)
	{
		this(null, cause);
	}

	public BinaryPersistenceException(final String message, final Throwable cause)
	{
		this(message, cause, true, true);
	}

	public BinaryPersistenceException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
