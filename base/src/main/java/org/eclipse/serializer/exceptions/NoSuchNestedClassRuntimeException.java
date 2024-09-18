package org.eclipse.serializer.exceptions;

/*-
 * #%L
 * Eclipse Serializer Base
 * %%
 * Copyright (C) 2023 - 2024 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

/**
 * Indicates that no nested class with the specified name was found
 * in class c.
 */
public class NoSuchNestedClassRuntimeException extends BaseException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public NoSuchNestedClassRuntimeException(final Class<?> c, final String name)
	{
		super("No nested class " + name  + " found in class " + c.getName());
	}

}
