
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

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.collections.types.XImmutableEnum;
import org.eclipse.serializer.equality.Equalator;
import org.eclipse.serializer.functional.IndexedAcceptor;
import org.eclipse.serializer.typing.XTypes;
import org.eclipse.serializer.util.iterables.ReadOnlyListIterator;


/**
 * Wrapper class that reduces the services provided by any wrapped {@link XGettingEnum} to only those of
 * {@link XGettingEnum}, effectively making the wrapped {@link XGettingEnum} instance immutable (or read-only)
 * if used through an instance of this class.
 * <p>
 * All methods declared in {@link XGettingEnum} are transparently passed to the wrapped enuM.<br>
 * All modifying methods declared in {@link Collection} and {@link List}
 * (all variations of add~(), remove~() and retain~() as well as set() and clear()) immediately throw an
 * {@link UnsupportedOperationException} when called.
 * <p>
 * This concept can be very useful if a class wants to provide public read access to an internal enuM without
 * either the danger of the enuM being modified from the outside or the need to copy the whole enuM on every access.
 *
 */
public class EnumView<E> implements XGettingEnum<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XGettingEnum<E> subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public EnumView(final XGettingEnum<E> subject)
	{
		super();
		this.subject = subject;
	}




	///////////////////////////////////////////////////////////////////////////
	// constant override methods //
	//////////////////////////////

	@Override
	public XImmutableEnum<E> immure()
	{
		return this.subject.immure();
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public Equalator<? super E> equality()
	{
		return this.subject.equality();
	}

	@Override
	public boolean hasVolatileElements()
	{
		return this.subject.hasVolatileElements();
	}

	@Override
	public boolean containsSearched(final Predicate<? super E> predicate)
	{
		return this.subject.containsSearched(predicate);
	}

	@Override
	public boolean applies(final Predicate<? super E> predicate)
	{
		return this.subject.applies(predicate);
	}

	@Override
	public boolean nullAllowed()
	{
		return true;
	}

	@Override
	public boolean nullContained()
	{
		return this.subject.nullContained();
	}

	@Override
	public boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		return this.subject.containsAll(elements);
	}

	@Override
	public boolean contains(final E element)
	{
		return this.subject.contains(element);
	}

	@Override
	public boolean containsId(final E element)
	{
		return this.subject.containsId(element);
	}

	@Override
	public EnumView<E> copy()
	{
		return new EnumView<>(this.subject);
	}

	@Override
	public <C extends Consumer<? super E>> C filterTo(final C target, final Predicate<? super E> predicate)
	{
		return this.subject.filterTo(target, predicate);
	}

	@Override
	public <C extends Consumer<? super E>> C copyTo(final C target)
	{
		return this.subject.copyTo(target);
	}

	@Override
	public long count(final E element)
	{
		return this.subject.count(element);
	}

	@Override
	public long countBy(final Predicate<? super E> predicate)
	{
		return this.subject.countBy(predicate);
	}

	@Override
	public <C extends Consumer<? super E>> C distinct(final C target, final Equalator<? super E> equalator)
	{
		return this.subject.distinct(target, equalator);
	}

	@Override
	public <C extends Consumer<? super E>> C distinct(final C target)
	{
		return this.subject.distinct(target);
	}

	@Override
	public boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		return this.subject.equals(this.subject, equalator);
	}

	@Override
	public boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		return this.subject.equalsContent(this.subject, equalator);
	}

	@Override
	public <C extends Consumer<? super E>> C except(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		return this.subject.except(other, equalator, target);
	}

	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		return this.subject.iterate(procedure);
	}

	@Override
	public final <P extends IndexedAcceptor<? super E>> P iterateIndexed(final P procedure)
	{
		return this.subject.iterateIndexed(procedure);
	}

	@Override
	public final <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		return this.subject.join(joiner, aggregate);
	}

	@Override
	public E get()
	{
		return this.subject.get();
	}

	@Override
	public E first()
	{
		return this.subject.first();
	}

	@Override
	public E last()
	{
		return this.subject.last();
	}

	@Override
	public E poll()
	{
		return this.subject.poll();
	}

	@Override
	public E peek()
	{
		return this.subject.peek();
	}

	@Override
	public long indexBy(final Predicate<? super E> predicate)
	{
		return this.subject.indexBy(predicate);
	}

	@Override
	public long indexOf(final E element)
	{
		return this.subject.indexOf(element);
	}

	@Override
	public <C extends Consumer<? super E>> C intersect(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		return this.subject.intersect(other, equalator, target);
	}

	@Override
	public boolean isSorted(final Comparator<? super E> comparator)
	{
		return this.subject.isSorted(comparator);
	}

	@Override
	public long lastIndexBy(final Predicate<? super E> predicate)
	{
		return this.subject.lastIndexBy(predicate);
	}

	@Override
	public long lastIndexOf(final E element)
	{
		return this.subject.lastIndexOf(element);
	}

	@Override
	public E max(final Comparator<? super E> comparator)
	{
		return this.subject.max(comparator);
	}

	@Override
	public long maxIndex(final Comparator<? super E> comparator)
	{
		return this.subject.maxIndex(comparator);
	}

	@Override
	public E min(final Comparator<? super E> comparator)
	{
		return this.subject.min(comparator);
	}

	@Override
	public long minIndex(final Comparator<? super E> comparator)
	{
		return this.subject.minIndex(comparator);
	}

	@Override
	public long scan(final Predicate<? super E> predicate)
	{
		return this.subject.scan(predicate);
	}

	@Override
	public E seek(final E sample)
	{
		return this.subject.seek(sample);
	}

	@Override
	public E search(final Predicate<? super E> predicate)
	{
		return this.subject.search(predicate);
	}

	@Override
	public EnumView<E> view()
	{
		return this;
	}

	@Override
	public XGettingEnum<E> view(final long lowIndex, final long highIndex)
	{
		// (24.08.2011)FIXME: SubEnumView
		throw new org.eclipse.serializer.meta.NotImplementedYetError();
	}

	@Override
	public XGettingEnum<E> range(final long fromIndex, final long toIndex)
	{
		return this.subject.range(fromIndex, toIndex);
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		return this.subject.toArray(type);
	}

	@Override
	public EnumView<E> toReversed()
	{
		return new EnumView<>(this.subject.toReversed());
	}

	@Override
	public <C extends Consumer<? super E>> C union(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		return this.subject.union(other, equalator, target);
	}

	@Override
	public <C extends Consumer<? super E>> C copySelection(final C target, final long... indices)
	{
		return this.subject.copySelection(target, indices);
	}

	@Deprecated
	@Override
	public int hashCode()
	{
		return this.subject.hashCode();
	}

	@Deprecated
	@Override
	public boolean equals(final Object o)
	{
		return this.subject.equals(o);
	}

	@Override
	public boolean isEmpty()
	{
		return this.subject.isEmpty();
	}

	@Override
	public E at(final long index)
	{
		return this.subject.at(index);
	}

	@Override
	public long size()
	{
		return XTypes.to_int(this.subject.size());
	}

	@Override
	public long maximumCapacity()
	{
		return this.subject.maximumCapacity();
	}

	@Override
	public boolean isFull()
	{
		return this.subject.isFull();
	}

	@Override
	public long remainingCapacity()
	{
		return this.subject.remainingCapacity();
	}

	@Override
	public Object[] toArray()
	{
		return this.subject.toArray();
	}

	@Override
	public Iterator<E> iterator()
	{
		return new ReadOnlyListIterator<>(this.subject);
	}

	@Override
	public String toString()
	{
		return this.subject.toString();
	}

}
