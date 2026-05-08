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
 * Thrown when a structural mutation cannot proceed because the target directory (or one of its
 * items) is currently in use by another user.
 * <p>
 * Typical triggers include attempting to mutate a directory while another thread already holds it
 * for mutation, or removing a root directory whose contents are still referenced by active file
 * usages.
 *
 * @see AfsExceptionMutation
 */
public class AfsExceptionMutationInUse extends AfsExceptionMutation
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Creates a new {@link AfsExceptionMutationInUse} without a message or cause.
	 */
	public AfsExceptionMutationInUse()
	{
		super();
	}

	/**
	 * Creates a new {@link AfsExceptionMutationInUse} with the passed detail message.
	 *
	 * @param message the detail message.
	 */
	public AfsExceptionMutationInUse(final String message)
	{
		super(message);
	}

	/**
	 * Creates a new {@link AfsExceptionMutationInUse} wrapping the passed cause.
	 *
	 * @param cause the underlying cause.
	 */
	public AfsExceptionMutationInUse(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * Creates a new {@link AfsExceptionMutationInUse} with the passed detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the underlying cause.
	 */
	public AfsExceptionMutationInUse(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	/**
	 * Creates a new {@link AfsExceptionMutationInUse} with full control over suppression and stack trace writability.
	 *
	 * @param message            the detail message.
	 * @param cause              the underlying cause.
	 * @param enableSuppression  whether suppression is enabled.
	 * @param writableStackTrace whether the stack trace is writable.
	 */
	public AfsExceptionMutationInUse(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
