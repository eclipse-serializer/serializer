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
 * Per-operation loading workhorse: produced by a {@link Creator} for one logical load and consumed by the
 * surrounding {@link PersistenceManager}. Specializes {@link PersistenceRetrieving} (the application-facing
 * read API) with a hook for loading the persistent root set and bookkeeping methods that let the surrounding
 * persistence layer steer traversal.
 * <p>
 * Counterpart of {@link PersistenceStorer} on the read side. Loaders are short-lived &mdash; one per
 * operation &mdash; and read through the {@link PersistenceSourceSupplier} they were built against.
 *
 * @see PersistenceRetrieving
 * @see PersistenceStorer
 * @see PersistenceManager
 */
public interface PersistenceLoader extends PersistenceRetrieving
{
	/**
	 * Loads and returns the persistent root set. Used by the surrounding persistence layer at startup to
	 * reconstruct the application's root reference and the named root entries.
	 *
	 * @return the loaded roots, or {@code null} if no roots have been persisted yet.
	 */
	public PersistenceRoots loadRoots();

	/**
	 * Marks the passed object id as already-resolved so the loader will not attempt to load it during the
	 * current operation. Used to short-circuit traversal for instances the surrounding layer has already
	 * registered through other means.
	 *
	 * @param objectId the object id to skip.
	 */
	public void registerSkip(long objectId);

	/**
	 * Iterate over all known objects of the current Loader
	 *
	 * @param iterator PersistenceAcceptor
	 */
	void iterateEntries(final PersistenceAcceptor iterator);

	/**
	 * Pluggable factory for {@link PersistenceLoader} instances. Stored on the foundation so each binding
	 * (e.g. the binary layer) can wire its own loader implementation.
	 *
	 * @param <D> the persistence data type passed through to the source.
	 */
	public interface Creator<D>
	{
		/**
		 * Creates a fresh loader for one logical load operation.
		 *
		 * @param typeLookup the type handler lookup the loader will consult.
		 * @param registry   the object registry the loader will populate.
		 * @param persister  the persister facade the loader is part of.
		 * @param source     the source supplier the loader will read from.
		 *
		 * @return the newly created loader.
		 */
		public PersistenceLoader createLoader(
			PersistenceTypeHandlerLookup<D> typeLookup,
			PersistenceObjectRegistry       registry  ,
			Persister                       persister ,
			PersistenceSourceSupplier<D>    source
		);
	}

}
