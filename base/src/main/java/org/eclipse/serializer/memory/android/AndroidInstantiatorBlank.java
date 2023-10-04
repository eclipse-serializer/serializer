package org.eclipse.serializer.memory.android;

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

import org.eclipse.serializer.exceptions.InstantiationRuntimeException;
import org.eclipse.serializer.functional.DefaultInstantiator;


public final class AndroidInstantiatorBlank implements DefaultInstantiator
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final AndroidInstantiatorBlank New()
	{
		return new AndroidInstantiatorBlank();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	AndroidInstantiatorBlank()
	{
		super();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final <T> T instantiate(final Class<T> type) throws InstantiationRuntimeException
	{
		return AndroidInternals.instantiateBlank(type);
	}
		
}
