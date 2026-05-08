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
 * Per-instance callback handed to {@link PersistenceTypeHandler}s during loading. Bundles the
 * {@code objectId → Object} lookup the handler needs to resolve referenced ids with hooks that let the
 * handler delegate type validation and root-resolution decisions back to the load logic.
 * <p>
 * The {@link Persister} reachable via {@link #getPersister()} is the bidirectional facade combining loading
 * with storing; the {@link #getObjectRetriever()} default narrows it to the read side.
 *
 * @see PersistenceTypeHandler
 * @see Persister
 * @see PersistenceObjectLookup
 */
public interface PersistenceLoadHandler extends PersistenceObjectLookup
{
	@Override
	public Object lookupObject(long objectId);

	/**
	 * The retriever used to resolve referenced ids. Defaults to the {@link #getPersister()} narrowed to
	 * its read side.
	 *
	 * @return the object retriever.
	 */
	public default ObjectSwizzling getObjectRetriever()
	{
		return this.getPersister();
	}

	/**
	 * The {@link Persister} backing this load operation. Type handlers consult it when they need access
	 * to the full read/write facade rather than the narrow {@link ObjectSwizzling}.
	 *
	 * @return the persister.
	 */
	public Persister getPersister();

	/**
	 * Validates that the loaded {@code object} is type-compatible with what the dictionary recorded under
	 * {@code objectId}. Implementations throw on mismatch.
	 *
	 * @param object   the loaded instance.
	 * @param objectId the object id under which it is registered.
	 */
	public void validateType(Object object, long objectId);

	/**
	 * Marks the passed instance as the persistent root resolved during loading. Hook used by the roots
	 * type handler to feed the freshly loaded root back into the foundation.
	 *
	 * @param rootInstance the loaded root instance.
	 * @param rootObjectId the object id under which it is registered.
	 */
	public void requireRoot(Object rootInstance, long rootObjectId);

	/**
	 * Legacy hook for refactoring an obsolete custom root identifier to the unified one. Retained for
	 * backwards compatibility with old persistence layers; no longer used by current code.
	 *
	 * @param rootInstance       the root instance.
	 * @param customRootObjectId the old custom-root object id.
	 *
	 * @deprecated retained for legacy persistence layers; new code should use {@link #requireRoot(Object, long)}.
	 */
	@Deprecated
	public void registerCustomRootRefactoring(Object rootInstance, long customRootObjectId);

	/**
	 * Legacy hook for refactoring the obsolete default-root identifier to the unified one. Retained for
	 * backwards compatibility with old persistence layers; no longer used by current code.
	 *
	 * @param rootInstance        the root instance.
	 * @param defaultRootObjectId the old default-root object id.
	 *
	 * @deprecated retained for legacy persistence layers; new code should use {@link #requireRoot(Object, long)}.
	 */
	@Deprecated
	public void registerDefaultRootRefactoring(Object rootInstance, long defaultRootObjectId);

}
