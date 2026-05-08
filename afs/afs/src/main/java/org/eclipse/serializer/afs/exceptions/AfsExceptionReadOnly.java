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
 * Thrown when a write-side operation is attempted on a file system that has been configured as
 * read-only via its {@link org.eclipse.serializer.afs.types.WriteController}.
 * <p>
 * Code obtains writable handles only after the active write controller permits it; this exception
 * is the runtime signal that a writing path executed even though writing was disabled.
 *
 * @see org.eclipse.serializer.afs.types.WriteController
 */
public class AfsExceptionReadOnly extends AfsException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Creates a new {@link AfsExceptionReadOnly} without a message or cause.
	 */
	public AfsExceptionReadOnly()
	{
		super();
	}

	/**
	 * Creates a new {@link AfsExceptionReadOnly} with the passed detail message.
	 *
	 * @param message the detail message.
	 */
	public AfsExceptionReadOnly(final String message)
	{
		super(message);
	}

	/**
	 * Creates a new {@link AfsExceptionReadOnly} wrapping the passed cause.
	 *
	 * @param cause the underlying cause.
	 */
	public AfsExceptionReadOnly(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * Creates a new {@link AfsExceptionReadOnly} with the passed detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the underlying cause.
	 */
	public AfsExceptionReadOnly(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	/**
	 * Creates a new {@link AfsExceptionReadOnly} with full control over suppression and stack trace writability.
	 *
	 * @param message            the detail message.
	 * @param cause              the underlying cause.
	 * @param enableSuppression  whether suppression is enabled.
	 * @param writableStackTrace whether the stack trace is writable.
	 */
	public AfsExceptionReadOnly(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
