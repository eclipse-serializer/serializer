
package org.eclipse.serializer.entity;

/*-
 * #%L
 * Eclipse Serializer Base
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

import static org.eclipse.serializer.chars.XChars.systemString;

import org.eclipse.serializer.chars.VarString;


public class EntityExceptionInaccessibleEntityType extends EntityException
{
	private final Entity entity;
	
	public EntityExceptionInaccessibleEntityType(final Entity entity)
	{
		super();
		
		this.entity = entity;
	}
	
	public final Entity entity()
	{
		return this.entity;
	}
	
	@Override
	public String assembleDetailString()
	{
		return VarString.New()
			.add("Inaccessible entity type: ")
			.add(systemString(this.entity))
			.toString();
	}
}
