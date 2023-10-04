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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.eclipse.serializer.math.XMath.positive;
import static org.eclipse.serializer.util.X.checkArrayRange;
import static org.eclipse.serializer.util.X.notNull;


public final class LimitedInputStream extends FilterInputStream
{
	public static LimitedInputStream New(
		final InputStream in   ,
		final long        limit
	)
	{
		return new LimitedInputStream(
			notNull(in)    ,
			positive(limit)
		);
	}


	private long left     ;
	private long mark = -1;

	private LimitedInputStream(
		final InputStream in   ,
		final long        limit
	)
	{
		super(in);
		this.left = limit;
	}

	@Override
	public int available() throws IOException
	{
		return checkArrayRange(Math.min(this.in.available(), this.left));
	}

	@Override
	public synchronized void mark(
		final int readLimit
	)
	{
		this.in.mark(readLimit);
		this.mark = this.left;
	}

	@Override
	public int read() throws IOException
	{
		if(this.left == 0)
		{
			return -1;
		}

		final int result = this.in.read();
		if(result != -1)
		{
			--this.left;
		}
		return result;
	}

	@Override
	public int read(
		final byte[] bytes ,
		final int    offset,
		final int    length
	)
	throws IOException
	{
		if(this.left == 0)
		{
			return -1;
		}

		final int result = this.in.read(
			bytes,
			offset,
			checkArrayRange(Math.min(length, this.left))
		);
		if(result > 0)
		{
			this.left -= result;
		}
		return result;
	}

	@Override
	public synchronized void reset() throws IOException
	{
		if(!this.in.markSupported())
		{
			throw new IOException("Mark not supported");
		}
		if(this.mark == -1)
		{
			throw new IOException("Mark not set");
		}

		this.in.reset();
		this.left = this.mark;
	}

	@Override
	public long skip(
		final long n
	)
	throws IOException
	{
		final long skipped = this.in.skip(
			Math.min(n, this.left)
		);
		this.left -= skipped;
		return skipped;
	}

}
