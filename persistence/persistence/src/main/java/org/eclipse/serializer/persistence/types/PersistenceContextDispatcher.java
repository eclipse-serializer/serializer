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

public interface PersistenceContextDispatcher<D>
{
	// loading //
	
	public default PersistenceTypeHandlerLookup<D> dispatchTypeHandlerLookup(
		final PersistenceTypeHandlerLookup<D> typeHandlerLookup
	)
	{
		return typeHandlerLookup;
	}
	
	public default PersistenceObjectRegistry dispatchObjectRegistry(
		final PersistenceObjectRegistry objectRegistry
	)
	{
		return objectRegistry;
	}
	
	// storing //
	
	public default PersistenceTypeHandlerManager<D> dispatchTypeHandlerManager(
		final PersistenceTypeHandlerManager<D> typeHandlerManager
	)
	{
		return typeHandlerManager;
	}
	
	public default PersistenceObjectManager<D> dispatchObjectManager(
		final PersistenceObjectManager<D> objectManager
	)
	{
		return objectManager;
	}
	
	
	
	public static <D> PersistenceContextDispatcher.PassThrough<D> PassThrough()
	{
		return new PersistenceContextDispatcher.PassThrough<>();
	}
	
	public static <D> PersistenceContextDispatcher.LocalObjectRegistration<D> LocalObjectRegistration()
	{
		return new PersistenceContextDispatcher.LocalObjectRegistration<>();
	}
	
	public final class PassThrough<D> implements PersistenceContextDispatcher<D>
	{
		PassThrough()
		{
			super();
		}
		
		// once again missing interface stateless instantiation.
	}
	
	public final class LocalObjectRegistration<D> implements PersistenceContextDispatcher<D>
	{
		LocalObjectRegistration()
		{
			super();
		}
		
		@Override
		public final PersistenceObjectRegistry dispatchObjectRegistry(
			final PersistenceObjectRegistry objectRegistry
		)
		{
			return objectRegistry.Clone();
		}
		
		@Override
		public final PersistenceObjectManager<D> dispatchObjectManager(
			final PersistenceObjectManager<D> objectManager
		)
		{
			return objectManager.Clone();
		}
		
	}
	
}
