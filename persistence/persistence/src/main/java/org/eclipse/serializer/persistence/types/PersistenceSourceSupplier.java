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

import org.eclipse.serializer.reference.ObjectSwizzling;

/**
 * Pairs a {@link PersistenceSource} with the {@link ObjectSwizzling} capability required to resolve object
 * ids while reading from it. Loaders consult the supplier when a referenced instance must be materialized
 * lazily &mdash; the swizzling side resolves the id, falling back to the {@link #source()} when the id has
 * to be loaded from disk.
 *
 * @param <D> the persistence data type produced by the source.
 *
 * @see PersistenceSource
 * @see ObjectSwizzling
 */
public interface PersistenceSourceSupplier<D> extends ObjectSwizzling
{
	@Override
	public Object getObject(long objectId);

	/**
	 * The {@link PersistenceSource} this supplier reads from when an object id has to be resolved through
	 * fresh I/O.
	 *
	 * @return the persistence source.
	 */
	public PersistenceSource<D> source();
}
