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

import org.eclipse.serializer.reference.Referencing;


public class TrivialIterator<E> implements ListIterator<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	Referencing<E> parent;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public TrivialIterator(final Referencing<E> parent)
	{
		super();
		this.parent = parent;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public boolean hasNext()
	{
		return this.parent != null;
	}

	@Override
	public E next()
	{
		final E element = this.parent.get();
		this.parent = null;
		return element;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasPrevious()
	{
		return false;
	}

	@Override
	public E previous()
	{
		throw new NoSuchElementException();
	}

	@Override
	public int nextIndex()
	{
		return 1;
	}

	@Override
	public int previousIndex()
	{
		return -1;
	}

	@Override
	public void set(final E e)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(final E e)
	{
		throw new UnsupportedOperationException();
	}

}
