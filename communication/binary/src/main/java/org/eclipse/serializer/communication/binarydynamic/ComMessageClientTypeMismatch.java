package org.eclipse.serializer.communication.binarydynamic;

/*-
 * #%L
 * Eclipse Serializer Communication Binary
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

public class ComMessageClientTypeMismatch extends ComMessageStatus
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final long typeId;
	private final String typeName;

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComMessageClientTypeMismatch(final long typeId, String typeName)
	{
		super(false);
		this.typeId = typeId;
		this.typeName = typeName;
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	protected long getTypeId()
	{
		return this.typeId;
	}


	protected String getType()
	{
		return this.typeName;
	}
}
