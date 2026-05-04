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
 * Thrown during path resolution when no root directory is registered with the requested identifier
 * in the file system.
 * <p>
 * Roots must be created or registered with the {@link
 * org.eclipse.serializer.afs.types.AFileSystem} before they can be resolved by identifier;
 * lookup-style resolution that requires the root to already exist raises this exception when it
 * is missing.
 *
 * @see AfsExceptionUnresolvablePathElement
 */
public class AfsExceptionUnresolvableRoot extends AfsException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Creates a new {@link AfsExceptionUnresolvableRoot} without a message or cause.
	 */
	public AfsExceptionUnresolvableRoot()
	{
		super();
	}

	/**
	 * Creates a new {@link AfsExceptionUnresolvableRoot} with the passed detail message.
	 *
	 * @param message the detail message.
	 */
	public AfsExceptionUnresolvableRoot(final String message)
	{
		super(message);
	}

	/**
	 * Creates a new {@link AfsExceptionUnresolvableRoot} wrapping the passed cause.
	 *
	 * @param cause the underlying cause.
	 */
	public AfsExceptionUnresolvableRoot(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * Creates a new {@link AfsExceptionUnresolvableRoot} with the passed detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the underlying cause.
	 */
	public AfsExceptionUnresolvableRoot(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	/**
	 * Creates a new {@link AfsExceptionUnresolvableRoot} with full control over suppression and stack trace writability.
	 *
	 * @param message            the detail message.
	 * @param cause              the underlying cause.
	 * @param enableSuppression  whether suppression is enabled.
	 * @param writableStackTrace whether the stack trace is writable.
	 */
	public AfsExceptionUnresolvableRoot(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
