package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
 * %%
 * Copyright (C) 2023 - 2026 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

/**
 * Internal callback surface a storer offers to the surrounding persistence layer. Defined as a separate
 * type from {@link Storer} so the methods stay out of the top-level public API: only the persistence
 * layer should be able to trigger them.
 *
 * @see Storer
 * @see PersistenceRoots
 */
public interface PersistenceStoringCallback
{
	/**
	 * Forces pending root changes to be stored even if the root has been stored allready.
	 * 
	 * @param pendingStoreRoot the modifed root.
	 */
	void forceRootStore(PersistenceRoots pendingStoreRoot);
}
