
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


public class EntityExceptionMissingDataForVersion extends EntityException
{
	private final Entity entity;
	private final Object versionKey;
	
	public EntityExceptionMissingDataForVersion(final Entity entity, final Object versionKey)
	{
		super();
		
		this.entity     = entity;
		this.versionKey = versionKey;
	}
	
	public final Entity entity()
	{
		return this.entity;
	}
	
	public final Object versionKey()
	{
		return this.versionKey;
	}
	
	@Override
	public String assembleDetailString()
	{
		return VarString.New()
			.add("Missing data for version '")
			.add(this.versionKey)
			.add("' in ")
			.add(systemString(this.entity))
			.toString();
	}
}
