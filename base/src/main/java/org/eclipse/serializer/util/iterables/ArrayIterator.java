package org.eclipse.serializer.util.iterables;

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

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class ArrayIterator<E> implements Iterator<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final E[] array;
	private final int length;
	private int index;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ArrayIterator(final E[] array)
	{
		super();
		this.array = array;
		this.length = array.length;
		this.index = 0;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	/**
	 * @see Iterator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		return this.index < this.length;
	}

	/**
	 * @see Iterator#next()
	 */
	@Override
	public E next()
	{
		try
		{
			final int i;
			final E e = this.array[i = this.index];
			this.index = i + 1;
			return e;
		}
		catch(final IndexOutOfBoundsException e)
		{
			throw new NoSuchElementException();
		}
	}

	/**
	 *
	 * @throws UnsupportedOperationException because this operation is not supported
	 * @see Iterator#remove()
	 */
	@Override
	public void remove() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

}
