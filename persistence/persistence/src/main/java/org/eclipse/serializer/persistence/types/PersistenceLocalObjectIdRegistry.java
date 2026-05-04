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
 * Per-storer overlay registry that buffers id assignments made during a single storing operation before they
 * are merged back into the shared {@link PersistenceObjectManager}. Lets a storer track exactly which
 * instances it (re)registered without polluting the global registry until the operation is committed.
 * <p>
 * As a {@link PersistenceObjectIdRequestor}, the local registry is also notified whenever the parent manager
 * assigns an id to an instance &mdash; that is the mechanism by which it observes the id assignments to
 * mirror locally.
 *
 * @param <D> the persistence data type passed through to the {@link PersistenceTypeHandler}.
 *
 * @see PersistenceObjectManager
 * @see PersistenceObjectIdRequestor
 */
public interface PersistenceLocalObjectIdRegistry<D> extends PersistenceObjectIdRequestor<D>
{
	/**
	 * The parent {@link PersistenceObjectManager} this local registry is attached to.
	 *
	 * @return the parent object manager.
	 */
	public PersistenceObjectManager<D> parentObjectManager();

	/**
	 * Returns the object id assigned to {@code object} during the current storing operation, delegating up
	 * to the parent manager if the local registry does not yet know it. Newly assigned ids are reported via
	 * {@code objectIdRequestor}.
	 *
	 * @param <T>               the instance type.
	 * @param object            the instance to look up or register.
	 * @param objectIdRequestor the requestor to notify if the parent manager assigns a new id.
	 * @param optionalHandler   the type handler responsible for {@code object}, or {@code null} if not yet
	 *                          known.
	 *
	 * @return the (possibly newly assigned) object id.
	 */
	public <T> long lookupObjectId(
		T                               object           ,
		PersistenceObjectIdRequestor<D> objectIdRequestor,
		PersistenceTypeHandler<D, T>    optionalHandler
	);

	/**
	 * Iterates every locally registered entry that is eligible to be merged into the parent manager,
	 * invoking the acceptor per {@code (objectId, instance)} pair.
	 *
	 * @param iterator the acceptor to invoke for each mergeable entry.
	 */
	public void iterateMergeableEntries(PersistenceAcceptor iterator);
}
