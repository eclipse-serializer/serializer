package org.eclipse.serializer.persistence.exceptions;

/*-
 * #%L
 * Eclipse Serializer Persistence
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

public class PersistenceExceptionTypeNotPersistable extends PersistenceException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Class<?> type;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeNotPersistable(
		final Class<?> type
	)
	{
		this(type, null, null);
	}

	public PersistenceExceptionTypeNotPersistable(
		final Class<?> type,
		final String message
	)
	{
		this(type, message, null);
	}

	public PersistenceExceptionTypeNotPersistable(
		final Class<?> type,
		final Throwable cause
	)
	{
		this(type, null, cause);
	}

	public PersistenceExceptionTypeNotPersistable(
		final Class<?> type,
		final String message, final Throwable cause
	)
	{
		this(type, message, cause, true, true);
	}

	public PersistenceExceptionTypeNotPersistable(
		final Class<?> type,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.type = type;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Class<?> getType()
	{
		return this.type;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Type not persistable: \"" + this.type + "\"."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}

}
