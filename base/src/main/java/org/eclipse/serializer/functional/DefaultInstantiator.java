package org.eclipse.serializer.functional;

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
import org.eclipse.serializer.reflect.XReflect;

public interface DefaultInstantiator
{
	public <T> T instantiate(Class<T> type) throws InstantiationRuntimeException;
	
		
	
	public static DefaultInstantiator.Default Default()
	{
		return new Default();
	}
	
	public final class Default implements DefaultInstantiator
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final <T> T instantiate(final Class<T> type) throws InstantiationRuntimeException
		{
			return XReflect.defaultInstantiate(type);
		}
		
	}
	
}
