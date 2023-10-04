package org.eclipse.serializer.exceptions;

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



public class MultiCauseException extends BaseException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final Throwable[] causes;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public MultiCauseException(final Throwable... causes)
	{
		super();
		this.causes = causes;
	}
	
	public MultiCauseException(final Throwable[] causes, final Throwable cause)
	{
		super(cause);
		this.causes = causes;
	}
	
	public MultiCauseException(final Throwable[] causes, final String message)
	{
		super(message);
		this.causes = causes;
	}
	
	public MultiCauseException(final Throwable[] causes, final String message, final Throwable cause)
	{
		super(message, cause);
		this.causes = causes;
	}
	
	public MultiCauseException(
		final Throwable[] causes            ,
		final String      message           ,
		final Throwable   cause             ,
		final boolean     enableSuppression ,
		final boolean     writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.causes = causes;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public Throwable[] causes()
	{
		return this.causes;
	}
	
}
