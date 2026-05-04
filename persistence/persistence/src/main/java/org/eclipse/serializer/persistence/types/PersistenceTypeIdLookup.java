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
 * Minimal {@link Class}-to-{@code typeId} lookup abstraction. Returned ids are the persistence-internal handles
 * stored in the binary stream; an unknown type yields {@link org.eclipse.serializer.reference.Swizzling#notFoundId()}
 * rather than throwing.
 * <p>
 * Implemented standalone by the native-type lookup created in
 * {@link Persistence#createDefaultTypeLookup()} (which only resolves the JDK primitive and reference types
 * registered at startup) and extended by {@link PersistenceTypeLookup} for full bidirectional type/id mapping.
 *
 * @see PersistenceTypeLookup
 * @see Persistence#createDefaultTypeLookup()
 * @see org.eclipse.serializer.reference.Swizzling#notFoundId()
 */
public interface PersistenceTypeIdLookup
{
	/**
	 * Returns the type id registered for the passed {@link Class}, or
	 * {@link org.eclipse.serializer.reference.Swizzling#notFoundId()} if the type is not known to this lookup.
	 *
	 * @param type the type to look up.
	 *
	 * @return the registered type id, or the not-found sentinel if unknown.
	 */
	public long lookupTypeId(Class<?> type);
}
