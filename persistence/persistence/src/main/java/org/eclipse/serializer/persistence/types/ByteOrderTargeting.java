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

import java.nio.ByteOrder;

/**
 * Capability marker for components that produce or consume binary data in a specific {@link ByteOrder}.
 * Used by the binary layer to swap byte-order on the fly when the target order does not match the JVM's
 * native order.
 * <p>
 * The {@link Mutable} sub-interface adds a setter so foundations can be reconfigured before the persistence
 * components are built; once everything is wired up, the order is supposed to be stable.
 *
 * @param <T> the self-type used for fluent setter chaining in {@link Mutable}.
 *
 * @see ByteOrder
 */
@FunctionalInterface
public interface ByteOrderTargeting<T extends ByteOrderTargeting<?>>
{
	/**
	 * The byte order this component produces or expects.
	 *
	 * @return the target byte order.
	 */
	public ByteOrder getTargetByteOrder();

	/**
	 * Whether the {@linkplain #getTargetByteOrder() target byte order} differs from the JVM's native order
	 * &mdash; in which case byte-order swapping is required during read/write.
	 *
	 * @return {@code true} if the target order differs from native order.
	 */
	public default boolean isByteOrderMismatch()
	{
		return isByteOrderMismatch(this.getTargetByteOrder());
	}


	/**
	 * Whether the passed byte order differs from the JVM's native order.
	 *
	 * @param targetByteOrder the byte order to check.
	 *
	 * @return {@code true} if {@code targetByteOrder} differs from {@link ByteOrder#nativeOrder()}.
	 */
	public static boolean isByteOrderMismatch(final ByteOrder targetByteOrder)
	{
		return targetByteOrder != ByteOrder.nativeOrder();
	}


	/**
	 * Mutable extension of {@link ByteOrderTargeting} that adds a setter for the target byte order. Used
	 * by foundations during configuration; the setter is not expected to be called once the persistence
	 * components are running.
	 *
	 * @param <T> the self-type returned by the setter for fluent chaining.
	 */
	public interface Mutable<T extends ByteOrderTargeting.Mutable<?>> extends ByteOrderTargeting<T>
	{
		/**
		 * Sets the target byte order.
		 *
		 * @param targetByteOrder the new target byte order.
		 *
		 * @return this instance, for fluent chaining.
		 */
		public T setTargetByteOrder(ByteOrder targetByteOrder);
	}

}
