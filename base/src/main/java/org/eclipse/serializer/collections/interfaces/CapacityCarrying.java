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

public interface CapacityCarrying extends Sized
{
	/**
	 * Returns the maximum amount of elements this carrier instance can contain.<br>
	 * The actual value may be depending on the configuration of the concrete instance or may depend only on the
	 * implementation of the carrier (meaning it is constant for all instances of the implementation,
	 * e.g. {@link Integer#MAX_VALUE})
	 *
	 * @return the maximum amount of elements this carrier instance can contain.
	 */
	public long maximumCapacity();

	/**
	 * @return the amount of elements this carrier instance can collect before reaching its maximimum capacity.
	 *
	 */
	public default long remainingCapacity()
	{
		return this.maximumCapacity() - this.size();
	}

	/**
	 * @return true if the current capacity cannot be increased anymore.
	 */
	public default boolean isFull()
	{
		return this.remainingCapacity() == 0L;
	}

}
