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

@FunctionalInterface
public interface ByteOrderTargeting<T extends ByteOrderTargeting<?>>
{
	public ByteOrder getTargetByteOrder();
	
	public default boolean isByteOrderMismatch()
	{
		return isByteOrderMismatch(this.getTargetByteOrder());
	}
	

	
	public static boolean isByteOrderMismatch(final ByteOrder targetByteOrder)
	{
		return targetByteOrder != ByteOrder.nativeOrder();
	}
	
	
	public interface Mutable<T extends ByteOrderTargeting.Mutable<?>> extends ByteOrderTargeting<T>
	{
		public T setTargetByteOrder(ByteOrder targetByteOrder);
	}
	
}
