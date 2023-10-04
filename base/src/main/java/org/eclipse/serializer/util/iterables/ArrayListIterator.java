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

import java.util.ListIterator;
import java.util.NoSuchElementException;

public final class ArrayListIterator<E> implements ListIterator<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final E[] array;
	private final int length;
	private int index;
	private int lastRet;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ArrayListIterator(final E[] array)
	{
		super();
		this.array = array;
		this.length = array.length;
		this.index = 0;
		this.lastRet = -1;
	}

	public ArrayListIterator(final E[] array, final int index)
	{
		super();
		this.array = array;
		this.length = array.length;
		if(index < 0 || index >= this.length)
		{
			throw new ArrayIndexOutOfBoundsException(index);
		}
		this.index = index;
		this.lastRet = -1;
	}

	/**
	 * 
	 * @param e the element to add
	 * @throws UnsupportedOperationException because this operation is not supported
	 * @see ListIterator#add(Object)
	 */
	@Override
	public void add(final E e) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();

	}

	/**
	 * @see ListIterator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		return this.index < this.length;
	}

	/**
	 * @see ListIterator#hasPrevious()
	 */
	@Override
	public boolean hasPrevious()
	{
		return this.index > 0;
	}

	/**
	 * @see ListIterator#next()
	 */
	@Override
	public E next()
	{
		try
		{
			final int i;
			final E e = this.array[i = this.index];
			this.lastRet = i;
			this.index = i + 1;
			return e;
		}
		catch(final IndexOutOfBoundsException e)
		{
			throw new NoSuchElementException();
		}
	}

	/**
	 * @see ListIterator#nextIndex()
	 */
	@Override
	public int nextIndex()
	{
		return this.index;
	}

	/**
	 * @see ListIterator#previous()
	 */
	@Override
	public E previous()
	{
		if(this.index == 0)
		{
			throw new NoSuchElementException();
		}
		final int i;
		final E   e = this.array[i = this.index - 1];
		this.lastRet = this.index = i;
		return e;
	}

	/**
	 * @see ListIterator#previousIndex()
	 */
	@Override
	public int previousIndex()
	{
		return this.index - 1;
	}

	/**
	 *
	 * @throws UnsupportedOperationException because this operation is not supported
	 * @see ListIterator#remove()
	 */
	@Override
	public void remove() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @see ListIterator#set(Object)
	 */
	@Override
	public void set(final E e)
	{
		if(this.lastRet == -1)
		{
			throw new IllegalStateException();
		}

		this.array[this.lastRet] = e;
	}

}
