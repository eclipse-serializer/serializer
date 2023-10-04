
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


public class EntityExceptionIdentityMismatch extends EntityException
{
	private final Entity entity1;
	private final Entity entity2;
	
	public EntityExceptionIdentityMismatch(final Entity entity1, final Entity entity2)
	{
		super();
		
		this.entity1 = entity1;
		this.entity2 = entity2;
	}
	
	public final Entity entity1()
	{
		return this.entity1;
	}
	
	public final Entity entity2()
	{
		return this.entity2;
	}
	
	@Override
	public String assembleDetailString()
	{
		return VarString.New()
			.add("Entity identity mismatch: ")
			.add(systemString(this.entity1))
			.add(" != ")
			.add(systemString(this.entity2))
			.toString();
	}
}
