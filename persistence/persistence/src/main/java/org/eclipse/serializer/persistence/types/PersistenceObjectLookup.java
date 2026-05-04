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
 * Minimal {@code objectId}-to-{@link Object} lookup abstraction. Counterpart to
 * {@link PersistenceObjectIdLookup}, which goes the other direction; the two are combined into
 * {@link PersistenceSwizzlingLookup}, which is what runtime registries actually implement.
 * <p>
 * Implementations return {@code null} when the requested id is not registered (no exception is thrown).
 *
 * @see PersistenceObjectIdLookup
 * @see PersistenceSwizzlingLookup
 */
public interface PersistenceObjectLookup
{
	/**
	 * Returns the instance registered for the passed {@code objectId}, or {@code null} if the id is not
	 * registered.
	 *
	 * @param objectId the object id to look up.
	 *
	 * @return the registered instance, or {@code null} if unknown.
	 */
	public Object lookupObject(long objectId);
}
