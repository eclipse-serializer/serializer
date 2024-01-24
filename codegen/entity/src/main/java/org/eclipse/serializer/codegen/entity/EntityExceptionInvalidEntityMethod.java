
package org.eclipse.serializer.codegen.entity;

/*-
 * #%L
 * Eclipse Serializer Codegen for Entities
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

import javax.lang.model.element.ExecutableElement;

import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.entity.EntityException;


public class EntityExceptionInvalidEntityMethod extends EntityException
{
	private final ExecutableElement method;
	
	public EntityExceptionInvalidEntityMethod(final ExecutableElement method)
	{
		super();
		
		this.method = method;
	}
	
	public final ExecutableElement method()
	{
		return this.method;
	}
	
	@Override
	public String assembleDetailString()
	{
		return VarString.New()
			.add("Invalid entity method: ")
			.add(this.method.getEnclosingElement()).add('#').add(this.method)
			.add("; only methods with return type, no type parameters")
			.add(", no parameters and no checked exceptions are supported.")
			.toString();
	}
}
