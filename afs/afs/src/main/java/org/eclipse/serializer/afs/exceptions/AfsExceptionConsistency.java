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
 * Thrown when the AFS layer detects an internal invariant violation. Examples include an item
 * whose {@code fileSystem()} does not match the file system it was registered with, mismatched
 * root directory registrations for the same identifier, or attempts to unregister a file that
 * is neither retired nor currently registered.
 * <p>
 * A consistency exception always indicates a bug in the file system implementation or in client
 * code that bypassed the documented usage contract; it is not a recoverable runtime condition.
 */
public class AfsExceptionConsistency extends AfsException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Creates a new {@link AfsExceptionConsistency} without a message or cause.
	 */
	public AfsExceptionConsistency()
	{
		super();
	}

	/**
	 * Creates a new {@link AfsExceptionConsistency} with the passed detail message.
	 *
	 * @param message the detail message.
	 */
	public AfsExceptionConsistency(final String message)
	{
		super(message);
	}

	/**
	 * Creates a new {@link AfsExceptionConsistency} wrapping the passed cause.
	 *
	 * @param cause the underlying cause.
	 */
	public AfsExceptionConsistency(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * Creates a new {@link AfsExceptionConsistency} with the passed detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the underlying cause.
	 */
	public AfsExceptionConsistency(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	/**
	 * Creates a new {@link AfsExceptionConsistency} with full control over suppression and stack trace writability.
	 *
	 * @param message            the detail message.
	 * @param cause              the underlying cause.
	 * @param enableSuppression  whether suppression is enabled.
	 * @param writableStackTrace whether the stack trace is writable.
	 */
	public AfsExceptionConsistency(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
