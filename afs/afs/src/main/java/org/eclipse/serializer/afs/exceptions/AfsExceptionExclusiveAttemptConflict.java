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
 * Thrown when an attempt to acquire exclusive (writing) access to a file fails because a different
 * user already holds exclusive access to it.
 * <p>
 * Two users cannot hold exclusive access to the same file simultaneously; the existing exclusive
 * holder must release the file before another exclusive request can succeed.
 *
 * @see AfsExceptionExclusiveAttemptSharedUserConflict
 * @see AfsExceptionSharedAttemptExclusiveUserConflict
 */
public class AfsExceptionExclusiveAttemptConflict extends AfsExceptionConflict
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Creates a new {@link AfsExceptionExclusiveAttemptConflict} without a message or cause.
	 */
	public AfsExceptionExclusiveAttemptConflict()
	{
		super();
	}

	/**
	 * Creates a new {@link AfsExceptionExclusiveAttemptConflict} with the passed detail message.
	 *
	 * @param message the detail message.
	 */
	public AfsExceptionExclusiveAttemptConflict(final String message)
	{
		super(message);
	}

	/**
	 * Creates a new {@link AfsExceptionExclusiveAttemptConflict} wrapping the passed cause.
	 *
	 * @param cause the underlying cause.
	 */
	public AfsExceptionExclusiveAttemptConflict(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * Creates a new {@link AfsExceptionExclusiveAttemptConflict} with the passed detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the underlying cause.
	 */
	public AfsExceptionExclusiveAttemptConflict(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	/**
	 * Creates a new {@link AfsExceptionExclusiveAttemptConflict} with full control over suppression and stack trace writability.
	 *
	 * @param message            the detail message.
	 * @param cause              the underlying cause.
	 * @param enableSuppression  whether suppression is enabled.
	 * @param writableStackTrace whether the stack trace is writable.
	 */
	public AfsExceptionExclusiveAttemptConflict(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
