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
import org.eclipse.serializer.collections.types.XPuttingSequence;

public class SubCollector<E> extends SubView<E> implements XPuttingSequence<E>
{

	@SafeVarargs
	@Override
	public final SubCollector<E> putAll(final E... elements)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubCollector<E> putAll(final E[] elements, final int srcStartIndex, final int srcLength)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubCollector<E> putAll(final XGettingCollection<? extends E> elements)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final boolean nullPut()
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final boolean put(final E element)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final boolean add(final E element)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@SafeVarargs
	@Override
	public final SubCollector<E> addAll(final E... elements)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubCollector<E> addAll(final E[] elements, final int srcStartIndex, final int srcLength)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubCollector<E> addAll(final XGettingCollection<? extends E> elements)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final void accept(final E element)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final boolean nullAdd()
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final long currentCapacity()
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubCollector<E> ensureCapacity(final long minimalCapacity)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubCollector<E> ensureFreeCapacity(final long minimalFreeCapacity)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final long optimize()
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

}
