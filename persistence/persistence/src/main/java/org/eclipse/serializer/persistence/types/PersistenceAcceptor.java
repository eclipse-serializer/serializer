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
 * Bi-callback consumer for {@code (objectId, instance)} pairs encountered while iterating registered
 * objects. Used by {@link PersistenceObjectRegistry#iterateEntries(PersistenceAcceptor)} and the
 * {@link PersistenceLocalObjectIdRegistry#iterateMergeableEntries(PersistenceAcceptor)} merge step.
 * <p>
 * Distinct from {@link PersistenceObjectIdAcceptor}, which receives only the object id.
 *
 * @see PersistenceObjectIdAcceptor
 * @see PersistenceObjectRegistry
 */
@FunctionalInterface
public interface PersistenceAcceptor
{
	/**
	 * Called for each registered {@code (objectId, instance)} pair.
	 *
	 * @param objectId the registered object id.
	 * @param instance the registered instance.
	 */
	public void accept(long objectId, Object instance);



	/**
	 * No-op implementation, suitable as a method reference where a non-{@code null} acceptor is required
	 * but no work needs to be done.
	 *
	 * @param objectId ignored.
	 * @param instance ignored.
	 */
	public static void noOp(final long objectId, final Object instance)
	{
		// no-op
	}

}
