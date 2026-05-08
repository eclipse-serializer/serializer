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
 * Thrown when an attempt to acquire shared (reading) access to a file fails because the file is
 * already held exclusively by a different user.
 * <p>
 * While a file is held exclusively, no other user may obtain shared or exclusive access to it.
 * The exclusive holder must release the file before shared access can be granted.
 *
 * @see AfsExceptionExclusiveAttemptConflict
 * @see AfsExceptionExclusiveAttemptSharedUserConflict
 */
public class AfsExceptionSharedAttemptExclusiveUserConflict extends AfsExceptionConflict
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Creates a new {@link AfsExceptionSharedAttemptExclusiveUserConflict} without a message or cause.
	 */
	public AfsExceptionSharedAttemptExclusiveUserConflict()
	{
		super();
	}

	/**
	 * Creates a new {@link AfsExceptionSharedAttemptExclusiveUserConflict} with the passed detail message.
	 *
	 * @param message the detail message.
	 */
	public AfsExceptionSharedAttemptExclusiveUserConflict(final String message)
	{
		super(message);
	}

	/**
	 * Creates a new {@link AfsExceptionSharedAttemptExclusiveUserConflict} wrapping the passed cause.
	 *
	 * @param cause the underlying cause.
	 */
	public AfsExceptionSharedAttemptExclusiveUserConflict(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * Creates a new {@link AfsExceptionSharedAttemptExclusiveUserConflict} with the passed detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the underlying cause.
	 */
	public AfsExceptionSharedAttemptExclusiveUserConflict(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	/**
	 * Creates a new {@link AfsExceptionSharedAttemptExclusiveUserConflict} with full control over suppression and stack trace writability.
	 *
	 * @param message            the detail message.
	 * @param cause              the underlying cause.
	 * @param enableSuppression  whether suppression is enabled.
	 * @param writableStackTrace whether the stack trace is writable.
	 */
	public AfsExceptionSharedAttemptExclusiveUserConflict(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
