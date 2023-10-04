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

public interface ObjectIdsProcessor
{
	// one-by-one processing of objectIds. Efficient for embedded mode, horribly inefficient for server mode.
	public void processObjectIdsByFilter(_longPredicate objectIdsSelector);

	// for bulk processing of objectIds. Most efficient way for server mode, inefficient for embedded mode.
	public Set_long provideObjectIdsBaseSet();
}
