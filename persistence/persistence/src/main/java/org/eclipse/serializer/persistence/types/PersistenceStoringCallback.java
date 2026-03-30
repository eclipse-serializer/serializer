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
 *  This interface defines storer callbacks that shoud not be exposed to the top level Storer API.
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
