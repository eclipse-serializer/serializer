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
 * Thrown when an item cannot be accessed because a structural mutation (e.g. a directory operation
 * that adds, removes or renames children) is in progress on an enclosing directory by a different
 * user.
 * <p>
 * Structural mutations require exclusive access to the affected directory; concurrent access by
 * other users is rejected with this exception until the mutation completes.
 *
 * @see AfsExceptionMutationInUse
 */
public class AfsExceptionMutation extends AfsException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Creates a new {@link AfsExceptionMutation} without a message or cause.
	 */
	public AfsExceptionMutation()
	{
		super();
	}

	/**
	 * Creates a new {@link AfsExceptionMutation} with the passed detail message.
	 *
	 * @param message the detail message.
	 */
	public AfsExceptionMutation(final String message)
	{
		super(message);
	}

	/**
	 * Creates a new {@link AfsExceptionMutation} wrapping the passed cause.
	 *
	 * @param cause the underlying cause.
	 */
	public AfsExceptionMutation(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * Creates a new {@link AfsExceptionMutation} with the passed detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the underlying cause.
	 */
	public AfsExceptionMutation(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	/**
	 * Creates a new {@link AfsExceptionMutation} with full control over suppression and stack trace writability.
	 *
	 * @param message            the detail message.
	 * @param cause              the underlying cause.
	 * @param enableSuppression  whether suppression is enabled.
	 * @param writableStackTrace whether the stack trace is writable.
	 */
	public AfsExceptionMutation(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
