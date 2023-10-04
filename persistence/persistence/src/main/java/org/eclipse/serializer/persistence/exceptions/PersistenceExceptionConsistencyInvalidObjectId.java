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

public class PersistenceExceptionConsistencyInvalidObjectId extends PersistenceExceptionConsistencyInvalidId
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistencyInvalidObjectId(final long id)
	{
		super(id);
	}

	public PersistenceExceptionConsistencyInvalidObjectId(final long id, final String message)
	{
		super(id, message);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Invalid object id: " + this.getId()
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}

}
