package org.eclipse.serializer.persistence.exceptions;

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
 * Implemented by exceptions a {@link org.eclipse.serializer.persistence.types.PersistenceTarget}
 * (or its underlying engine) throws when data handed to it references object ids for which no
 * entity exists in the target — dangling references.
 * <p>
 * Storers that captured the referenced instances can use the reported ids to attempt automatic
 * healing: re-storing the still-live instances under their existing object ids and retrying the
 * write. The interface deliberately carries no dependency on any concrete target implementation;
 * target-side exception types implement it in addition to their own hierarchy.
 */
public interface PersistenceDanglingReferences
{
	/**
	 * @return the referenced object ids for which no entity exists in the target.
	 */
	public long[] missingObjectIds();
}
