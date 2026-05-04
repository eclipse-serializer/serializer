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

/**
 * Last-step transformation hook applied by the foundation to the four shared persistence components
 * (type-handler lookup/manager and object registry/manager) just before they are handed to a channel.
 * The default implementation is a pass-through; the bundled {@link LocalObjectRegistration} variant
 * {@link PersistenceObjectRegistry#Clone() clones} the registry and object manager so that each channel
 * gets its own private id-assignment context, isolating storers that run in parallel.
 *
 * @param <D> the persistence data type.
 *
 * @see PersistenceObjectRegistry
 * @see PersistenceObjectManager
 * @see PersistenceTypeHandlerLookup
 * @see PersistenceTypeHandlerManager
 */
public interface PersistenceContextDispatcher<D>
{
	// loading //

	/**
	 * Transforms the loading-side type-handler lookup. The default returns the input unchanged.
	 *
	 * @param typeHandlerLookup the shared lookup.
	 *
	 * @return the lookup to install in the channel.
	 */
	public default PersistenceTypeHandlerLookup<D> dispatchTypeHandlerLookup(
		final PersistenceTypeHandlerLookup<D> typeHandlerLookup
	)
	{
		return typeHandlerLookup;
	}

	/**
	 * Transforms the loading-side object registry. The default returns the input unchanged.
	 *
	 * @param objectRegistry the shared registry.
	 *
	 * @return the registry to install in the channel.
	 */
	public default PersistenceObjectRegistry dispatchObjectRegistry(
		final PersistenceObjectRegistry objectRegistry
	)
	{
		return objectRegistry;
	}

	// storing //

	/**
	 * Transforms the storing-side type-handler manager. The default returns the input unchanged.
	 *
	 * @param typeHandlerManager the shared manager.
	 *
	 * @return the manager to install in the channel.
	 */
	public default PersistenceTypeHandlerManager<D> dispatchTypeHandlerManager(
		final PersistenceTypeHandlerManager<D> typeHandlerManager
	)
	{
		return typeHandlerManager;
	}

	/**
	 * Transforms the storing-side object manager. The default returns the input unchanged.
	 *
	 * @param objectManager the shared manager.
	 *
	 * @return the manager to install in the channel.
	 */
	public default PersistenceObjectManager<D> dispatchObjectManager(
		final PersistenceObjectManager<D> objectManager
	)
	{
		return objectManager;
	}



	/**
	 * Returns a {@link PassThrough} dispatcher: every component is passed through unchanged.
	 *
	 * @param <D> the persistence data type.
	 *
	 * @return the pass-through dispatcher.
	 */
	public static <D> PersistenceContextDispatcher.PassThrough<D> PassThrough()
	{
		return new PersistenceContextDispatcher.PassThrough<>();
	}

	/**
	 * Returns a {@link LocalObjectRegistration} dispatcher: clones the registry and object manager so the
	 * channel gets its own id-assignment context.
	 *
	 * @param <D> the persistence data type.
	 *
	 * @return the local-object-registration dispatcher.
	 */
	public static <D> PersistenceContextDispatcher.LocalObjectRegistration<D> LocalObjectRegistration()
	{
		return new PersistenceContextDispatcher.LocalObjectRegistration<>();
	}

	/**
	 * Default {@link PersistenceContextDispatcher}: passes every component through unchanged.
	 *
	 * @param <D> the persistence data type.
	 */
	public final class PassThrough<D> implements PersistenceContextDispatcher<D>
	{
		PassThrough()
		{
			super();
		}

		// once again missing interface stateless instantiation.
	}

	/**
	 * {@link PersistenceContextDispatcher} that clones both the {@link PersistenceObjectRegistry} and the
	 * {@link PersistenceObjectManager} so each channel gets its own private id-assignment context. The
	 * type-handler lookup and manager are shared as-is.
	 *
	 * @param <D> the persistence data type.
	 */
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
