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
 * Single-callback consumer for object ids encountered while traversing the binary representation of a
 * persistent graph. Used by reference traversers (notably the binary
 * {@code BinaryReferenceTraverser}) that walk over the reference slots of a stored instance and notify the
 * acceptor for each reference, without materializing the referenced object.
 * <p>
 * Visitors that need to hand the object back together with its instance and type handler should implement
 * {@link PersistenceObjectIdRequestor} instead, which adds richer registration callbacks.
 *
 * @see PersistenceObjectIdRequestor
 */
@FunctionalInterface
public interface PersistenceObjectIdAcceptor
{
	/**
	 * Called for each object id discovered during traversal.
	 *
	 * @param objectId the encountered object id.
	 */
	public void acceptObjectId(long objectId);
}
