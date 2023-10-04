package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
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
import org.eclipse.serializer.memory.XMemory;


public interface PersistenceInstantiator<D>
{
	public <T> T instantiate(Class<T> type, D data) throws InstantiationRuntimeException;
		


	public static <T> T instantiateBlank(final Class<T> type)
	{
		return XMemory.instantiateBlank(type);
	}
	
	
	
	public static <D> PersistenceInstantiator<D> New()
	{
		return new PersistenceInstantiator.Default<>();
	}
	
	public final class Default<D> implements PersistenceInstantiator<D>, PersistenceTypeInstantiatorProvider<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public <T> T instantiate(final Class<T> type, final D data)
			throws InstantiationRuntimeException
		{
			return PersistenceInstantiator.instantiateBlank(type);
		}
		
		@Override
		public <T> PersistenceTypeInstantiator<D, T> provideTypeInstantiator(final Class<T> type)
		{
			return PersistenceTypeInstantiator.New(type, this);
		}
		
	}
	
}
