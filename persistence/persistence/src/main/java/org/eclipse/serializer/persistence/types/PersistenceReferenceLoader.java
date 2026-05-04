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
 * Loader-side specialization of {@link PersistenceObjectIdAcceptor}: in addition to the lazy
 * {@code acceptObjectId} pattern (which only schedules the referenced id for resolution if it is reachable),
 * a reference loader can be told to load a referenced id <em>eagerly</em> &mdash; i.e. unconditionally,
 * regardless of any lazy traversal heuristics.
 *
 * @see PersistenceObjectIdAcceptor
 */
public interface PersistenceReferenceLoader extends PersistenceObjectIdAcceptor
{
	/**
	 * Marks the referenced id for eager loading: the referenced instance will be resolved even if the
	 * lazy-traversal logic would otherwise leave it unresolved.
	 *
	 * @param objectId the object id to require eagerly.
	 */
	public void requireReferenceEager(long objectId);
}
