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

import org.eclipse.serializer.collections.Set_long;
import org.eclipse.serializer.functional._longPredicate;

/**
 * Visitor handed to {@link PersistenceObjectRegistry#processLiveObjectIds(ObjectIdsProcessor)} so the
 * registry can either stream object ids one-by-one through a predicate (embedded mode) or hand the visitor a
 * bulk {@link Set_long} for batched processing (server mode). Each call site picks whichever variant fits
 * its workload &mdash; the comments reflect the rule of thumb.
 *
 * @see PersistenceObjectRegistry#processLiveObjectIds(ObjectIdsProcessor)
 * @see ObjectIdsSelector
 */
public interface ObjectIdsProcessor
{
	/**
	 * One-by-one processing of object ids. Efficient for embedded mode, horribly inefficient for server
	 * mode.
	 *
	 * @param objectIdsSelector predicate the registry calls for each live id.
	 */
	public void processObjectIdsByFilter(_longPredicate objectIdsSelector);

	/**
	 * Bulk processing of object ids. Most efficient way for server mode, inefficient for embedded mode.
	 *
	 * @return the set the registry should populate with live object ids.
	 */
	public Set_long provideObjectIdsBaseSet();
}
