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
 * Thrown when an attempt to acquire exclusive (writing) access to a file fails because there are
 * still shared (reading) users holding the file.
 * <p>
 * Exclusive access requires that no other user is reading the file; all shared users must release
 * the file before exclusive access can be granted.
 *
 * @see AfsExceptionExclusiveAttemptConflict
 * @see AfsExceptionSharedAttemptExclusiveUserConflict
 */
public class AfsExceptionExclusiveAttemptSharedUserConflict extends AfsExceptionConflict
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Creates a new {@link AfsExceptionExclusiveAttemptSharedUserConflict} without a message or cause.
	 */
	public AfsExceptionExclusiveAttemptSharedUserConflict()
	{
		super();
	}

	/**
	 * Creates a new {@link AfsExceptionExclusiveAttemptSharedUserConflict} with the passed detail message.
	 *
	 * @param message the detail message.
	 */
	public AfsExceptionExclusiveAttemptSharedUserConflict(final String message)
	{
		super(message);
	}

	/**
	 * Creates a new {@link AfsExceptionExclusiveAttemptSharedUserConflict} wrapping the passed cause.
	 *
	 * @param cause the underlying cause.
	 */
	public AfsExceptionExclusiveAttemptSharedUserConflict(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * Creates a new {@link AfsExceptionExclusiveAttemptSharedUserConflict} with the passed detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the underlying cause.
	 */
	public AfsExceptionExclusiveAttemptSharedUserConflict(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	/**
	 * Creates a new {@link AfsExceptionExclusiveAttemptSharedUserConflict} with full control over suppression and stack trace writability.
	 *
	 * @param message            the detail message.
	 * @param cause              the underlying cause.
	 * @param enableSuppression  whether suppression is enabled.
	 * @param writableStackTrace whether the stack trace is writable.
	 */
	public AfsExceptionExclusiveAttemptSharedUserConflict(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
