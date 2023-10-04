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



public class InstantiationRuntimeException extends WrapperRuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public InstantiationRuntimeException(final InstantiationException actual)
	{
		super(actual);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public InstantiationException getActual()
	{
		// cast safety guaranteed by constructor
		return (InstantiationException)super.getActual();
	}

}
