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

public interface PersistenceLoader extends PersistenceRetrieving
{
	public PersistenceRoots loadRoots();

	public void registerSkip(long objectId);

	/**
	 * Iterate over all known objects of the current Loader
	 *
	 * @param iterator PersistenceAcceptor
	 */
	void iterateEntries(final PersistenceAcceptor iterator);

	public interface Creator<D>
	{
		public PersistenceLoader createLoader(
			PersistenceTypeHandlerLookup<D> typeLookup,
			PersistenceObjectRegistry       registry  ,
			Persister                       persister ,
			PersistenceSourceSupplier<D>    source
		);
	}

}
