package org.eclipse.serializer.persistence.binary.types;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
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
 * Functional callback that reads a contiguous range of native memory starting at a raw address. Used by
 * the binary persistence layer to feed previously-written buffers (or memory-mapped storage regions) to
 * the read pipeline without intermediate copying.
 */
@FunctionalInterface
public interface MemoryRangeReader
{
	/**
	 * @param address the start address of the memory range.
	 * @param length  the length of the range in bytes.
	 */
	public void readMemory(long address, long length);
}
