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

public class PersistenceExceptionConsistencyObjectId extends PersistenceExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final Object reference;
	final long   actualOid;
	final long   passedOid;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistencyObjectId(final Object reference, final long actualOid, final long passedOid)
	{
		super();
		this.reference = reference;
		this.actualOid = actualOid;
		this.passedOid = passedOid;
	}

	@Override
	public String getMessage()
	{
		return "Inconsistent Object id. Registered: " + this.actualOid + ", passed: " + this.passedOid;
	}



}
