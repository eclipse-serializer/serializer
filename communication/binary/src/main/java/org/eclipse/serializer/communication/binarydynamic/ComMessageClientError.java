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

public class ComMessageClientError extends ComMessageStatus
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final String errorMessage;

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComMessageClientError(final RuntimeException runtimeException)
	{
		super(false);
		this.errorMessage = runtimeException.getMessage();
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public String getErrorMessage()
	{
		return this.errorMessage;
	}

}
