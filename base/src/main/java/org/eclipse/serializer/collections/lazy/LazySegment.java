package org.eclipse.serializer.collections.lazy;

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

public interface LazySegment<E> {

	/**
	 * get the number of contained element in this segment.
	 * 
	 * @return number of contained elements;
	 */
	int size();
	
	/**
	 * Check if the lazy loaded data of this segment has been loaded.
	 * 
	 * @return true if loaded, otherwise false.
	 */
	boolean isLoaded();

	/**
	 * Check if this segment has modifications that are not yet persisted.
	 * 
	 * @return true if there are modifications not yet persisted, otherwise false.
	 */
	boolean isModified();

	/**
	 * Unload the lazy data of this segment
	 */
	void unloadSegment();

	boolean unloadAllowed();

	void allowUnload(final boolean allow);

	E getData();

}
