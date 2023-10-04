package org.eclipse.serializer.io;

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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import static org.eclipse.serializer.util.X.notNull;


public final class ByteBufferOutputStream extends OutputStream
{
	public static ByteBufferOutputStream New(
		final ByteBuffer targetBuffer
	)
	{
		return new ByteBufferOutputStream(
			notNull(targetBuffer)
		);
	}


	private final ByteBuffer targetBuffer;

	private ByteBufferOutputStream(
		final ByteBuffer targetBuffer
	)
	{
		super();
		this.targetBuffer = targetBuffer;
	}

	@Override
	public void write(
		final int b
	)
	throws IOException
	{
		this.targetBuffer.put((byte)b);
	}

	@Override
	public void write(
		final byte[] bytes ,
		final int    offset,
		final int    length
	)
	throws IOException
	{
		notNull(bytes);
		if(offset < 0
		|| offset > bytes.length
		|| length < 0
		|| offset + length > bytes.length
		|| offset + length < 0)
		{
			throw new IndexOutOfBoundsException();
		}
		if(length == 0)
		{
			return;
		}

		this.targetBuffer.put(bytes, offset, length);
	}

}
