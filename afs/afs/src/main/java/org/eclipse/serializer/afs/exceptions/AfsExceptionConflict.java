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
 * Common supertype for access conflicts surfaced by {@link org.eclipse.serializer.afs.types.AccessManager}
 * when a request for shared (read) or exclusive (write) access cannot be granted because of an
 * incompatible existing usage.
 * <p>
 * The concrete subtype distinguishes which side of the conflict triggered the failure:
 * an exclusive attempt against existing exclusive usage, an exclusive attempt against existing
 * shared users, or a shared attempt against an existing exclusive user.
 *
 * @see AfsExceptionExclusiveAttemptConflict
 * @see AfsExceptionExclusiveAttemptSharedUserConflict
 * @see AfsExceptionSharedAttemptExclusiveUserConflict
 */
public class AfsExceptionConflict extends AfsException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Creates a new {@link AfsExceptionConflict} without a message or cause.
	 */
	public AfsExceptionConflict()
	{
		super();
	}

	/**
	 * Creates a new {@link AfsExceptionConflict} with the passed detail message.
	 *
	 * @param message the detail message.
	 */
	public AfsExceptionConflict(final String message)
	{
		super(message);
	}

	/**
	 * Creates a new {@link AfsExceptionConflict} wrapping the passed cause.
	 *
	 * @param cause the underlying cause.
	 */
	public AfsExceptionConflict(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * Creates a new {@link AfsExceptionConflict} with the passed detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the underlying cause.
	 */
	public AfsExceptionConflict(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	/**
	 * Creates a new {@link AfsExceptionConflict} with full control over suppression and stack trace writability.
	 *
	 * @param message            the detail message.
	 * @param cause              the underlying cause.
	 * @param enableSuppression  whether suppression is enabled.
	 * @param writableStackTrace whether the stack trace is writable.
	 */
	public AfsExceptionConflict(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
