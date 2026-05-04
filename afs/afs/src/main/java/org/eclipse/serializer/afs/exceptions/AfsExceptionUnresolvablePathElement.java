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
 * Thrown during path resolution when a directory in the path chain does not contain a child item
 * matching the requested identifier.
 * <p>
 * The exception is raised by lookup-style resolution that requires every path element to exist;
 * the {@code ensure}-style methods that auto-create missing directories do not throw it.
 *
 * @see AfsExceptionUnresolvableRoot
 */
public class AfsExceptionUnresolvablePathElement extends AfsException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Creates a new {@link AfsExceptionUnresolvablePathElement} without a message or cause.
	 */
	public AfsExceptionUnresolvablePathElement()
	{
		super();
	}

	/**
	 * Creates a new {@link AfsExceptionUnresolvablePathElement} with the passed detail message.
	 *
	 * @param message the detail message.
	 */
	public AfsExceptionUnresolvablePathElement(final String message)
	{
		super(message);
	}

	/**
	 * Creates a new {@link AfsExceptionUnresolvablePathElement} wrapping the passed cause.
	 *
	 * @param cause the underlying cause.
	 */
	public AfsExceptionUnresolvablePathElement(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * Creates a new {@link AfsExceptionUnresolvablePathElement} with the passed detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the underlying cause.
	 */
	public AfsExceptionUnresolvablePathElement(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	/**
	 * Creates a new {@link AfsExceptionUnresolvablePathElement} with full control over suppression and stack trace writability.
	 *
	 * @param message            the detail message.
	 * @param cause              the underlying cause.
	 * @param enableSuppression  whether suppression is enabled.
	 * @param writableStackTrace whether the stack trace is writable.
	 */
	public AfsExceptionUnresolvablePathElement(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
