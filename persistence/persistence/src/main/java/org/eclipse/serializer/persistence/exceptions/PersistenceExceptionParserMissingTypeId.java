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

public class PersistenceExceptionParserMissingTypeId extends PersistenceExceptionParser
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionParserMissingTypeId(
		final int index
	)
	{
		this(index, null, null);
	}

	public PersistenceExceptionParserMissingTypeId(
		final int index,
		final String message
	)
	{
		this(index, message, null);
	}

	public PersistenceExceptionParserMissingTypeId(
		final int index,
		final Throwable cause
	)
	{
		this(index, null, cause);
	}

	public PersistenceExceptionParserMissingTypeId(
		final int index,
		final String message, final Throwable cause
	)
	{
		this(index, message, cause, true, true);
	}

	public PersistenceExceptionParserMissingTypeId(
		final int index,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(index, message, cause, enableSuppression, writableStackTrace);
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	@Override
	public String getMessage()
	{
		return "Missing type id at index " + this.getIndex() + "."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
