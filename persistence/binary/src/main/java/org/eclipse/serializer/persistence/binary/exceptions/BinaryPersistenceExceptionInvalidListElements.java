package org.eclipse.serializer.persistence.binary.exceptions;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
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

public class BinaryPersistenceExceptionInvalidListElements extends BinaryPersistenceExceptionInvalidList
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final long listElementCount;
	private final long elementLength   ;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryPersistenceExceptionInvalidListElements(
		final long entityLength    ,
		final long objectId        ,
		final long typeId          ,
		final long listStartOffset ,
		final long listTotalLength ,
		final long listElementCount,
		final long elementLength
	)
	{
		super(entityLength, objectId, typeId, listStartOffset, listTotalLength);
		this.listElementCount = listElementCount;
		this.elementLength    = elementLength   ;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	protected String assembleDetailStringBody()
	{
		return super.assembleDetailStringBody() + ", " +
			"listElementCount = " + this.listElementCount + ", " +
			"elementLength = "    + this.elementLength
		;
	}
	
}
