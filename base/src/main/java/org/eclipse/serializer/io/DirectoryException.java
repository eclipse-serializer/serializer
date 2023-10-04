package org.eclipse.serializer.io;

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

import java.io.File;


public class DirectoryException extends FileException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public DirectoryException(final File subject)
	{
		super(subject);
	}

	public DirectoryException(final File subject, final String message, final Throwable cause)
	{
		super(subject, message, cause);
	}

	public DirectoryException(final File subject, final String message)
	{
		super(subject, message);
	}

	public DirectoryException(final File subject, final Throwable cause)
	{
		super(subject, cause);
	}
	
}
