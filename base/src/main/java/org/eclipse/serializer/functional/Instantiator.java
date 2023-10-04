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

import static org.eclipse.serializer.util.X.notNull;

import java.lang.reflect.Constructor;

import org.eclipse.serializer.exceptions.InstantiationRuntimeException;


public interface Instantiator<T>
{
	public T instantiate() throws InstantiationRuntimeException;
	
	
	
	public static <T> Instantiator<T> WrapDefaultConstructor(final Constructor<T> constructor)
	{
		return new WrappingDefaultConstructor<>(
			notNull(constructor)
		);
	}
	
	public final class WrappingDefaultConstructor<T> implements Instantiator<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Constructor<T> constructor;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		WrappingDefaultConstructor(final Constructor<T> constructor)
		{
			super();
			this.constructor = constructor;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		@Override
		public T instantiate() throws InstantiationRuntimeException
		{
			try
			{
				return this.constructor.newInstance();
			}
			catch(final InstantiationException e)
			{
				throw new InstantiationRuntimeException(e);
			}
			catch(final Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		
	}
	
}
