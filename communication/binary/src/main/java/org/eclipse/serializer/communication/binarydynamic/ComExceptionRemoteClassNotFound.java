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

/**
 * Thrown when a typeDefinition received from the remote host
 * contains a type that can't be resolved to an exiting class
 * on the local system.
 *
 */
public class ComExceptionRemoteClassNotFound extends ComException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Constructs a <code>ComExceptionRemoteClassNotFound</code> with no detail message.
	 * 
	 * @param typeName the type name of the missing class
	 */
	public ComExceptionRemoteClassNotFound(final String typeName)
	{
		super("Class not found: " + typeName);
	}

}
