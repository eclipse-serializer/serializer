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

import java.io.IOException;

public class IORuntimeException extends WrapperRuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public IORuntimeException(final IOException actual)
	{
		super(actual);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public IOException getActual()
	{
		return (IOException)super.getActual(); // safe via constructor
	}



}

