package org.eclipse.serializer.collections.interfaces;

/*-
 * #%L
 * Eclipse Serializer Base
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

import java.lang.ref.WeakReference;

public interface ConsolidatableCollection
{
	/**
	 * Consolidates the internal storage of this collection by discarding all elements of the internal storage that
	 * have become obsolete or otherwise unneeded anymore. (e.g. {@link WeakReference} entries whose reference has
	 * been cleared).
	 * If this is not possible or not needed in the concrete implementation, this method does nothing and returns 0.
	 * @return the number of discarded entries.
	 */
	public long consolidate();
}
