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
 * Bidirectional swizzling lookup: combines the {@code Object}&rarr;{@code objectId} direction
 * ({@link PersistenceObjectIdLookup}) with the {@code objectId}&rarr;{@code Object} direction
 * ({@link PersistenceObjectLookup}) into the single capability that runtime registries (notably
 * {@link PersistenceObjectRegistry}) need in order to "swizzle" between persisted ids and live instances
 * during loading and storing.
 * <p>
 * Pure typing interface; it adds no new methods on top of the two parents.
 *
 * @see PersistenceObjectIdLookup
 * @see PersistenceObjectLookup
 * @see PersistenceObjectRegistry
 */
public interface PersistenceSwizzlingLookup extends PersistenceObjectIdLookup, PersistenceObjectLookup
{
	// only typing interface so far
}
