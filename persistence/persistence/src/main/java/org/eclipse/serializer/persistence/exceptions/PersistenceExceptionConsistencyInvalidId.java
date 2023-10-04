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

public class PersistenceExceptionConsistencyInvalidId extends PersistenceExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final long id;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistencyInvalidId(final long id)
	{
		super();
		this.id = id;
	}
	
	public PersistenceExceptionConsistencyInvalidId(final long id, final String message)
	{
		super(message);
		this.id = id;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public final long getId()
	{
		return this.id;
	}

}
