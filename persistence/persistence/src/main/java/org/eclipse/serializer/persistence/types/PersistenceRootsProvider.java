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
 * Supplies the {@link PersistenceRoots} instance and the type handler that knows how to read and write it.
 * The provider also retains the runtime roots between calls so subsequent {@link #provideRoots()} requests
 * return the same instance until {@link #updateRuntimeRoots(PersistenceRoots)} replaces it.
 * <p>
 * The handler-registration step is delegated to the provider because only the implementation knows which
 * {@code PersistenceRoots} subtype it produces, so only it can supply a matching custom type handler.
 *
 * @param <D> the persistence data type passed through to the registered handler.
 *
 * @see PersistenceRoots
 * @see PersistenceCustomTypeHandlerRegistry
 */
public interface PersistenceRootsProvider<D>
{
	/**
	 * Returns the current runtime roots, creating them on first access.
	 *
	 * @return the runtime roots.
	 */
	public PersistenceRoots provideRoots();

	/**
	 * Returns the current runtime roots without triggering creation. Returns {@code null} if no roots have
	 * been provided yet.
	 *
	 * @return the runtime roots, or {@code null} if none have been provided.
	 */
	public PersistenceRoots peekRoots();

	/**
	 * Replaces the cached runtime roots with the passed instance. Used by the loading code to install the
	 * roots reconstructed from disk.
	 *
	 * @param runtimeRoots the new runtime roots.
	 */
	public void updateRuntimeRoots(PersistenceRoots runtimeRoots);

	/**
	 * Only the {@link PersistenceRootsProvider} implementation can ensure that the handler fits the instance,
	 * so it has to do the registering as well.
	 *
	 * @param typeHandlerRegistry the type handler registry
	 * @param objectRegistry the object registry
	 */
	public void registerRootsTypeHandlerCreator(
		PersistenceCustomTypeHandlerRegistry<D> typeHandlerRegistry,
		PersistenceObjectRegistry               objectRegistry
	);


	/**
	 * Returns an {@link Empty} provider that contributes nothing &mdash; useful for setups that do not use
	 * named roots.
	 *
	 * @param <D> the persistence data type.
	 *
	 * @return the empty provider.
	 */
	public static <D> PersistenceRootsProvider<D> Empty()
	{
		return new Empty<>();
	}


	/**
	 * No-op {@link PersistenceRootsProvider}: returns {@code null} for both
	 * {@link #provideRoots()} and {@link #peekRoots()}, and does nothing in
	 * {@link #updateRuntimeRoots(PersistenceRoots)} or
	 * {@link #registerRootsTypeHandlerCreator(PersistenceCustomTypeHandlerRegistry, PersistenceObjectRegistry)}.
	 *
	 * @param <D> the persistence data type.
	 */
	public final class Empty<D> implements PersistenceRootsProvider<D>
	{
		Empty()
		{
			super();
		}

		@Override
		public PersistenceRoots provideRoots()
		{
			//no-op
			return null;
		}

		@Override
		public PersistenceRoots peekRoots()
		{
			//no-op
			return null;
		}

		@Override
		public void updateRuntimeRoots(final PersistenceRoots runtimeRoots)
		{
			//no-op
		}

		@Override
		public void registerRootsTypeHandlerCreator(
			final PersistenceCustomTypeHandlerRegistry<D> typeHandlerRegistry,
			final PersistenceObjectRegistry               objectRegistry
		)
		{
			//no-op
		}
	}

}
