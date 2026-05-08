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

/**
 * Thrown when an operation is attempted through a file usage handle (an {@link
 * org.eclipse.serializer.afs.types.AReadableFile} or {@link
 * org.eclipse.serializer.afs.types.AWritableFile}) that has already been released and is therefore
 * retired.
 * <p>
 * A retired handle no longer represents a live access claim; the caller must obtain a new handle
 * via the access manager before performing any further I/O.
 */
public class AfsExceptionRetiredFile extends AfsException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Creates a new {@link AfsExceptionRetiredFile} without a message or cause.
	 */
	public AfsExceptionRetiredFile()
	{
		super();
	}

	/**
	 * Creates a new {@link AfsExceptionRetiredFile} with the passed detail message.
	 *
	 * @param message the detail message.
	 */
	public AfsExceptionRetiredFile(final String message)
	{
		super(message);
	}

	/**
	 * Creates a new {@link AfsExceptionRetiredFile} wrapping the passed cause.
	 *
	 * @param cause the underlying cause.
	 */
	public AfsExceptionRetiredFile(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * Creates a new {@link AfsExceptionRetiredFile} with the passed detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the underlying cause.
	 */
	public AfsExceptionRetiredFile(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	/**
	 * Creates a new {@link AfsExceptionRetiredFile} with full control over suppression and stack trace writability.
	 *
	 * @param message            the detail message.
	 * @param cause              the underlying cause.
	 * @param enableSuppression  whether suppression is enabled.
	 * @param writableStackTrace whether the stack trace is writable.
	 */
	public AfsExceptionRetiredFile(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
