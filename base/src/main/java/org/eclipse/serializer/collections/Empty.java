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

import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.collections.types.XImmutableEnum;
import org.eclipse.serializer.collections.types.XImmutableList;
import org.eclipse.serializer.equality.Equalator;
import org.eclipse.serializer.functional.IndexedAcceptor;
import org.eclipse.serializer.typing.Stateless;
import org.eclipse.serializer.util.X;
import org.eclipse.serializer.util.iterables.EmptyListIterator;


/**
 * Empty dummy collection to be used as a constant and stateless empty collection placeholder.
 * <p>
 * As there is no element at all, this type can be a List and a Set (Enum) at the same time, enabling it
 * to be used in any type situation.
 *
 * @param <E> the type of elements in this collection
 */
public final class Empty<E> implements XImmutableList<E>, XImmutableEnum<E>, Stateless
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public Empty()
	{
		super();
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final Empty<E> copy()
	{
		return new Empty<>();
	}

	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		return procedure;
	}

	@Override
	public final <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		return aggregate;
	}

	@Override
	public final <P extends IndexedAcceptor<? super E>> P iterateIndexed(final P procedure)
	{
		return procedure;
	}

	@Override
	public Empty<E> immure()
	{
		return this;
	}

	@Override
	public ListIterator<E> listIterator()
	{
		return new EmptyListIterator<>();
	}

	@Override
	public ListIterator<E> listIterator(final long index)
	{
		/* (20.11.2011)NOTE:
		 * the definition of java.util.list#listIterator(int) has issues for collections can be empty
		 * The exception definition says:
		 * throws IndexOutOfBoundsException if the index is out of range ({@code index < 0 || index > size()})
		 * Then what should happen if the list is empty and the method is called with index 0 for the first element?
		 * index is not < 0 and index is also not > size (which is 0). So no exception is to be thrown but
		 * a valid iterator has to be returned, validly returning one element (that at index 0).
		 * But which element should that be if the list is empty?
		 *
		 * The extended collection's backward-compatibility #listIterator(int) throws the correct exception in this
		 * case. Thus throwing one here.
		 */
		throw new IndexOutOfBoundsException("collection is empty");
	}

	@Override
	public Empty<E> range(final long fromIndex, final long toIndex)
	{
		throw new IndexOutOfBoundsException();
	}

	@Override
	public Empty<E> toReversed()
	{
		return new Empty<>();
	}

	@Override
	public <T extends Consumer<? super E>> T copySelection(final T target, final long... indices)
	{
		return target;
	}

	@Override
	public E get()
	{
		throw new IndexOutOfBoundsException();
	}

	@Override
	public E first()
	{
		throw new IndexOutOfBoundsException();
	}

	@Override
	public E at(final long index)
	{
		throw new IndexOutOfBoundsException();
	}

	@Override
	public long indexOf(final E element)
	{
		return -1;
	}

	@Override
	public long indexBy(final Predicate<? super E> predicate)
	{
		return -1;
	}

	@Override
	public boolean isSorted(final Comparator<? super E> comparator)
	{
		return true; // hehe
	}

	@Override
	public E last()
	{
		throw new IndexOutOfBoundsException();
	}

	@Override
	public long lastIndexOf(final E element)
	{
		return -1;
	}

	@Override
	public long lastIndexBy(final Predicate<? super E> predicate)
	{
		return -1;
	}

	@Override
	public long maxIndex(final Comparator<? super E> comparator)
	{
		return -1;
	}

	@Override
	public long minIndex(final Comparator<? super E> comparator)
	{
		return -1;
	}

	@Override
	public E peek()
	{
		return null;
	}

	@Override
	public E poll()
	{
		return null;
	}

	@Override
	public long scan(final Predicate<? super E> predicate)
	{
		return -1;
	}

	@Override
	public Empty<E> view()
	{
		return this;
	}

	@Override
	public Empty<E> view(final long lowIndex, final long highIndex)
	{
		throw new IndexOutOfBoundsException();
	}

	@Override
	public boolean containsSearched(final Predicate<? super E> predicate)
	{
		return false;
	}

	@Override
	public boolean applies(final Predicate<? super E> predicate)
	{
		return false;
	}

	@Override
	public boolean contains(final E element)
	{
		return false;
	}

	@Override
	public boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		return false;
	}

	@Override
	public boolean containsId(final E element)
	{
		return false;
	}

	@Override
	public <T extends Consumer<? super E>> T copyTo(final T target)
	{
		return target;
	}

	@Override
	public <T extends Consumer<? super E>> T filterTo(final T target, final Predicate<? super E> predicate)
	{
		return target;
	}

	@Override
	public long count(final E element)
	{
		return 0;
	}

	@Override
	public long countBy(final Predicate<? super E> predicate)
	{
		return 0;
	}

	@Override
	public <T extends Consumer<? super E>> T distinct(final T target)
	{
		return target;
	}

	@Override
	public <T extends Consumer<? super E>> T distinct(final T target, final Equalator<? super E> equalator)
	{
		return target;
	}

	@Override
	public Equalator<? super E> equality()
	{
		return Equalator.identity();
	}

	@Override
	public boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		return samples instanceof Empty<?>;
	}

	@Override
	public boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		return samples.isEmpty();
	}

	@Override
	public <T extends Consumer<? super E>> T except(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		return target;
	}

	@Override
	public boolean hasVolatileElements()
	{
		return false;
	}

	@Override
	public <T extends Consumer<? super E>> T intersect(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		return target;
	}

	@Override
	public boolean isEmpty()
	{
		return true;
	}

	@Override
	public Iterator<E> iterator()
	{
		return new EmptyListIterator<>();
	}

	@Override
	public E max(final Comparator<? super E> comparator)
	{
		return null;
	}

	@Override
	public E min(final Comparator<? super E> comparator)
	{
		return null;
	}

	@Override
	public boolean nullContained()
	{
		return false;
	}

	@Override
	public E seek(final E sample)
	{
		return null; // pretty ambigious, but well...
	}

	@Override
	public E search(final Predicate<? super E> predicate)
	{
		return null;
	}

	@Override
	public long size()
	{
		return 0;
	}

	@Override
	public Object[] toArray()
	{
		return new Object[0];
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		return X.Array(type, 0);
	}

	@Override
	public <T extends Consumer<? super E>> T union(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		return target;
	}

	@Override
	public boolean nullAllowed()
	{
		return false;
	}

	@Override
	public long remainingCapacity()
	{
		return 0;
	}

	@Override
	public boolean isFull()
	{
		return true; // again philosophical
	}

	@Override
	public long maximumCapacity()
	{
		return 0;
	}

}
