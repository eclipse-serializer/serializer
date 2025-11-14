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


/**
 * The capacity of a capacity carrying type (e.g. a collection) defines the number of elements it can carry
 * in the current state before an internal rebuild becomes necessary. The capacity can be, but does not have to be,
 * the size of the internal storage (e.g. an array) itself. It can also be a meta value derived from the actual
 * storage size, like "threshold" in hash collections.
 */
public interface CapacityExtendable extends XBaseCollection
{
	public CapacityExtendable ensureCapacity(long minimalCapacity);

	/**
	 * Ensures that the next {@literal minimalFreeCapacity} elements can be actually added in a fast way,
	 * meaning for example no internal storage rebuild will be necessary.
	 * 
	 * @param minimalFreeCapacity the capacity to ensure
	 * @return this
	 */
	public CapacityExtendable ensureFreeCapacity(long minimalFreeCapacity);

	/**
	 * Returns the current amount of elements this instance can hold before a storage rebuild becomes necessary.
	 * <p>
	 * For carrier implementations that don't have a concept of storage rebuilding (like linked list for example)
	 * this method returns the same value as {@link #maximumCapacity()}.
	 *
	 * @return the current capacity of this instance before a rebuild is required.
	 */
	public long currentCapacity();



	public default long currentFreeCapacity()
	{
		return this.currentCapacity() - this.size();
	}

}
