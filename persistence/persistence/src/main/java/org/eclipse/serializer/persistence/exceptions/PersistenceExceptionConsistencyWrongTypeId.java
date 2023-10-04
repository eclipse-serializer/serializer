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



public class PersistenceExceptionConsistencyWrongTypeId extends PersistenceExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final Class<?> type     ;
	final long     actualTid;
	final long     passedTid;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistencyWrongTypeId(final Class<?> type, final long actualTid, final long passedTid)
	{
		super();
		this.type      = type     ;
		this.actualTid = actualTid;
		this.passedTid = passedTid;
	}

	@Override
	public String getMessage()
	{
		return "Wrong type id for " + this.type + ": actual tid: " + this.actualTid + ", passed tid: " + this.passedTid
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
