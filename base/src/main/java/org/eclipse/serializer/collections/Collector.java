package org.eclipse.serializer.collections;

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

import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.collections.types.XPuttingCollection;
import org.eclipse.serializer.typing.XTypes;


public final class Collector<E> implements XPuttingCollection<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XPuttingCollection<E> subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public Collector(final XPuttingCollection<E> collection)
	{
		super();
		this.subject = collection;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public boolean nullAdd()
	{
		return this.subject.nullAdd();
	}

	@Override
	public boolean add(final E e)
	{
		return this.subject.add(e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collector<E> addAll(final E... elements)
	{
		this.subject.addAll(elements);
		return this;
	}

	@Override
	public Collector<E> addAll(final E[] elements, final int offset, final int length)
	{
		this.subject.addAll(elements, offset, length);
		return this;
	}

	@Override
	public Collector<E> addAll(final XGettingCollection<? extends E> elements)
	{
		this.subject.addAll(elements);
		return this;
	}

	@Override
	public boolean nullPut()
	{
		return this.subject.nullAdd();
	}

	@Override
	public void accept(final E e)
	{
		this.subject.add(e);
	}

	@Override
	public boolean put(final E element)
	{
		return this.subject.add(element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collector<E> putAll(final E... elements)
	{
		this.subject.addAll(elements);
		return this;
	}

	@Override
	public Collector<E> putAll(final E[] elements, final int offset, final int length)
	{
		this.subject.addAll(elements, offset, length);
		return this;
	}

	@Override
	public Collector<E> putAll(final XGettingCollection<? extends E> elements)
	{
		this.subject.addAll(elements);
		return this;
	}



	@Override
	public Collector<E> ensureCapacity(final long minimalCapacity)
	{
		this.subject.ensureCapacity(minimalCapacity);
		return this;
	}

	@Override
	public long currentCapacity()
	{
		return this.subject.currentCapacity();
	}

	@Override
	public long maximumCapacity()
	{
		return this.subject.maximumCapacity();
	}

	@Override
	public boolean isFull()
	{
		return XTypes.to_int(this.subject.size()) >= this.subject.maximumCapacity();
	}

	@Override
	public long remainingCapacity()
	{
		return this.subject.remainingCapacity();
	}

	@Override
	public Collector<E> ensureFreeCapacity(final long minimalFreeCapacity)
	{
		this.subject.ensureFreeCapacity(minimalFreeCapacity);
		return this;
	}

	@Override
	public long optimize()
	{
		return this.subject.optimize();
	}

	@Override
	public boolean hasVolatileElements()
	{
		return this.subject.hasVolatileElements();
	}

	@Override
	public boolean nullAllowed()
	{
		return this.subject.nullAllowed();
	}

	@Override
	public boolean isEmpty() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long size() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}



}
