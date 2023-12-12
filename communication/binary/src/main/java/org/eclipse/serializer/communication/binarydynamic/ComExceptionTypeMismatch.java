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

import org.eclipse.serializer.com.ComException;

public class ComExceptionTypeMismatch extends ComException
{
	private final long   typeId;
	private final String typeName;
	
	public ComExceptionTypeMismatch(final long typeId, final String typeName)
	{
		super(String.format("local type %s does not match to remote type with type id %d!",
			typeName,
			typeId
		));
		
		this.typeId = typeId;
		this.typeName = typeName;
	}

	protected long getTypeId()
	{
		return this.typeId;
	}

	protected String getType()
	{
		return this.typeName;
	}

}
