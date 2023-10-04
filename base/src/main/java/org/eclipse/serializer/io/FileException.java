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


public class FileException extends RuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final File subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public FileException(final File subject)
	{
		super();
		this.subject = subject;
	}

	public FileException(final File subject, final String message, final Throwable cause)
	{
		super(message, cause);
		this.subject = subject;
	}

	public FileException(final File subject, final String message)
	{
		super(message);
		this.subject = subject;
	}

	public FileException(final File subject, final Throwable cause)
	{
		super(cause);
		this.subject = subject;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public File getSubject()
	{
		return this.subject;
	}
	
	@Override
	public String getMessage()
	{
		return super.getMessage() + " " + this.subject;
	}

}
