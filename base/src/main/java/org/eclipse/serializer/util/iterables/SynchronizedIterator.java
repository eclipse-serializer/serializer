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

public class SynchronizedIterator<E> implements Iterator<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Iterator<E> iterator;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public SynchronizedIterator(final Iterator<E> iterator)
	{
		super();
		this.iterator = iterator;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public synchronized boolean hasNext()
	{
		return this.iterator.hasNext();
	}

	@Override
	public synchronized void remove()
	{
		this.iterator.remove();
	}

	@Override
	public synchronized E next()
	{
		return this.iterator.next();
	}

}
