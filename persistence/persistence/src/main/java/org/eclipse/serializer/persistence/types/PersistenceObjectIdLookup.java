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
 * Minimal {@link Object}-to-{@code objectId} lookup abstraction. Returned ids are the persistence-internal
 * handles stored in the binary stream; an unregistered instance yields
 * {@link org.eclipse.serializer.reference.Swizzling#notFoundId()} rather than throwing.
 * <p>
 * Combined with {@link PersistenceObjectLookup} (the inverse {@code objectId}→{@code Object} direction) into
 * {@link PersistenceSwizzlingLookup}, which is what runtime registries actually implement.
 *
 * @see PersistenceObjectLookup
 * @see PersistenceSwizzlingLookup
 * @see org.eclipse.serializer.reference.Swizzling#notFoundId()
 */
public interface PersistenceObjectIdLookup
{
	/**
	 * Returns the object id registered for the passed instance, or
	 * {@link org.eclipse.serializer.reference.Swizzling#notFoundId()} if the instance is not known to this
	 * lookup.
	 *
	 * @param object the instance to look up.
	 *
	 * @return the registered object id, or the not-found sentinel if unknown.
	 */
	public long lookupObjectId(Object object);
}
