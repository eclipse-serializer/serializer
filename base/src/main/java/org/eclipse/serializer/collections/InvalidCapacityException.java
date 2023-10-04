package org.eclipse.serializer.collections;

/*-
 * #%L
 * Eclipse Serializer Base
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

public class InvalidCapacityException extends RuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final long maximumCapacity;
	private final long desiredCapacity;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public InvalidCapacityException()
	{
		this(null, null);
	}

	public InvalidCapacityException(final Throwable cause)
	{
		this(null, cause);
	}

	public InvalidCapacityException(final String message)
	{
		this(message, null);
	}

	public InvalidCapacityException(final String message, final Throwable cause)
	{
		super(message, cause);
		this.maximumCapacity = -1;
		this.desiredCapacity = -1;
	}

	public InvalidCapacityException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.maximumCapacity = -1;
		this.desiredCapacity = -1;
	}

	public InvalidCapacityException(final long maximumCapacity, final long desiredCapacity)
	{
		this(maximumCapacity, desiredCapacity, null, null);
	}

	public InvalidCapacityException(final long maximumCapacity, final long desiredCapacity, final Throwable cause)
	{
		this(maximumCapacity, desiredCapacity, null, cause);
	}

	public InvalidCapacityException(final long maximumCapacity, final long desiredCapacity, final String message)
	{
		this(maximumCapacity, desiredCapacity, message, null);
	}

	public InvalidCapacityException(
		final long      maximumCapacity   ,
		final long      desiredCapacity   ,
		final String    message           ,
		final Throwable cause
	)
	{
		super(message, cause);
		this.maximumCapacity = maximumCapacity;
		this.desiredCapacity = desiredCapacity;
	}

	public InvalidCapacityException(
		final long      maximumCapacity   ,
		final long      desiredCapacity   ,
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.maximumCapacity = maximumCapacity;
		this.desiredCapacity = desiredCapacity;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public long getMaximumCapacity()
	{
		return this.maximumCapacity;
	}
	public long getDesiredCapacity()
	{
		return this.desiredCapacity;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "maximum capacity: " + this.maximumCapacity
			+ ", desired capacity: " + this.desiredCapacity
			+ ", message: " + super.getMessage()
		;
	}



}
