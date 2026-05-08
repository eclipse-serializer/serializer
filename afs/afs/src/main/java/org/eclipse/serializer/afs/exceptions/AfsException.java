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

import org.eclipse.serializer.exceptions.BaseException;

/**
 * Root of the abstract file system exception hierarchy. All failures originating from the AFS layer
 * (path resolution, access management, mutation conflicts, read-only enforcement, etc.) are reported
 * as a subtype of this exception.
 * <p>
 * Catching {@code AfsException} matches every AFS-specific failure mode without also catching
 * unrelated runtime exceptions. For more targeted handling, catch one of the subtypes instead.
 *
 * @see AfsExceptionConflict
 * @see AfsExceptionConsistency
 * @see AfsExceptionMutation
 * @see AfsExceptionReadOnly
 * @see AfsExceptionRetiredFile
 * @see AfsExceptionUnresolvablePathElement
 * @see AfsExceptionUnresolvableRoot
 */
public class AfsException extends BaseException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Creates a new {@link AfsException} without a message or cause.
	 */
	public AfsException()
	{
		super();
	}

	/**
	 * Creates a new {@link AfsException} with the passed detail message.
	 *
	 * @param message the detail message.
	 */
	public AfsException(final String message)
	{
		super(message);
	}

	/**
	 * Creates a new {@link AfsException} wrapping the passed cause.
	 *
	 * @param cause the underlying cause.
	 */
	public AfsException(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * Creates a new {@link AfsException} with the passed detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the underlying cause.
	 */
	public AfsException(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	/**
	 * Creates a new {@link AfsException} with full control over suppression and stack trace writability.
	 *
	 * @param message            the detail message.
	 * @param cause              the underlying cause.
	 * @param enableSuppression  whether suppression is enabled.
	 * @param writableStackTrace whether the stack trace is writable.
	 */
	public AfsException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
