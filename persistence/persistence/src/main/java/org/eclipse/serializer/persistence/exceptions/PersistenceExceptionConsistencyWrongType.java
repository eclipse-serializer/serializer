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



public class PersistenceExceptionConsistencyWrongType extends PersistenceExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final long     tid       ;
	final Class<?> actualType;
	final Class<?> passedType;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistencyWrongType(final long tid, final Class<?> actualType, final Class<?> passedType)
	{
		super();
		this.tid        = tid       ;
		this.actualType = actualType;
		this.passedType = passedType;
	}
	
	@Override
	public String getMessage()
	{
//		return super.getMessage();
		return "TypeId: " + this.tid
			+ ", actual type: " + (this.actualType == null ? null : this.actualType.getCanonicalName())
			+ ", passed type: " + (this.passedType == null ? null : this.passedType.getCanonicalName())
		;
	}



}
