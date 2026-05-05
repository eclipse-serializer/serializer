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

import java.nio.ByteBuffer;


/**
 * Aggregation of one or more direct {@link ByteBuffer}s that together hold a contiguous logical block of
 * persisted binary data. Implementations are the storing side ({@link ChunksBuffer}, where buffers grow as
 * entities are appended) and the loading side ({@link ChunksWrapper}, which wraps already-filled buffers
 * read from a source). {@link Binary} extends this contract so a single binary handle can stand in for
 * either role.
 *
 * @see ChunksBuffer
 * @see ChunksWrapper
 * @see Binary
 */
public interface Chunk
{
	/**
	 * @return the underlying direct byte buffers, in order. Holding the array does not transfer ownership.
	 */
	public ByteBuffer[] buffers();

	/**
	 * Resets the chunk to an empty state, ready for reuse.
	 */
	public void clear();

	/**
	 * @return {@code true} if no data has been written to this chunk yet.
	 */
	public boolean isEmpty();

	/**
	 * @return the total number of bytes currently held across all buffers.
	 */
	public long totalLength();

}
