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

public class BinaryPersistenceExceptionInvalidList extends BinaryPersistenceException
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static String messageBody()
	{
		return "Invalid list data";
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final long entityLength   ;
	private final long objectId       ;
	private final long typeId         ;
	private final long listStartOffset;
	private final long listTotalLength;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryPersistenceExceptionInvalidList(
		final long entityLength   ,
		final long objectId       ,
		final long typeId         ,
		final long listStartOffset,
		final long listTotalLength
	)
	{
		super();
		this.entityLength    = entityLength   ;
		this.objectId        = objectId       ;
		this.typeId          = typeId         ;
		this.listStartOffset = listStartOffset;
		this.listTotalLength = listTotalLength;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	protected String assembleDetailStringBody()
	{
		return messageBody() + ": " +
			"entityLength = "     + this.entityLength     + ", " +
			"objectId = "         + this.objectId         + ", " +
			"typeId = "           + this.typeId           + ", " +
			"listStartOffset = "  + this.listStartOffset  + ", " +
			"listTotalLength = "  + this.listTotalLength
		;
	}
	
	@Override
	public String assembleDetailString()
	{
		return this.assembleDetailStringBody() + ".";
	}
	
}
