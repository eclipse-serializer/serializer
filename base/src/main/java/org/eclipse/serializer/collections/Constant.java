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
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.collections.types.XImmutableEnum;
import org.eclipse.serializer.collections.types.XImmutableList;
import org.eclipse.serializer.collections.types.XReferencing;
import org.eclipse.serializer.equality.Equalator;
import org.eclipse.serializer.functional.IndexedAcceptor;
import org.eclipse.serializer.typing.XTypes;
import org.eclipse.serializer.util.X;
import org.eclipse.serializer.util.iterables.TrivialIterator;


/**
 * Immutable singleton dummy collection used to pass a single instance masked as a collection.
 * <p>
 * As there is always only one element, this type can be a List and a Set (Enum) at the same time, enabling it
 * to be used in any type situation.
 *
 * @param <E> the type of elements in this collection
 * @see Singleton
 */
public class Constant<E> implements XImmutableList<E>, XImmutableEnum<E>, XReferencing<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final E element;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public Constant(final E element)
	{
		super();
		this.element = element;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	/**
	 * Convenience alias for {@link #first()}.
	 *
	 * @return the contained element.
	 */
	@Override
	public final E get()
	{
		return this.element;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final Constant<E> copy()
	{
		return new Constant<>(this.element);
	}

	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		procedure.accept(this.element);
		return procedure;
	}

	@Override
	public final <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		joiner.accept(this.element, aggregate);
		return aggregate;
	}

	@Override
	public final <P extends IndexedAcceptor<? super E>> P iterateIndexed(final P procedure)
	{
		procedure.accept(this.element, 0);
		return procedure;
	}

	@Override
	public final Constant<E> immure()
	{
		return this;
	}

	@Override
	public final ListIterator<E> listIterator()
	{
		return new TrivialIterator<>(this);
	}

	@Override
	public final ListIterator<E> listIterator(final long index)
	{
		if(index != 0)
		{
			throw new IndexOutOfBoundsException();
		}
		return new TrivialIterator<>(this);
	}

	@Override
	public final Constant<E> range(final long fromIndex, final long toIndex)
	{
		if(fromIndex != 0)
		{
			throw new IndexOutOfBoundsException();
		}
		if(toIndex != 0)
		{
			throw new IndexOutOfBoundsException();
		}
		return new Constant<>(this.element);
	}

	@Override
	public final Constant<E> toReversed()
	{
		return new Constant<>(this.element);
	}

	@Override
	public final <T extends Consumer<? super E>> T copySelection(final T target, final long... indices)
	{
		for(int i = 0; i < indices.length; i++)
		{
			if(indices[i] != 0)
			{
				throw new IndexOutOfBoundsException();
			}
		}
		target.accept(this.element);
		return target;
	}

	@Override
	public final E first()
	{
		return this.element;
	}

	@Override
	public final E at(final long index)
	{
		if(index != 0)
		{
			throw new IndexOutOfBoundsException();
		}
		return this.element;
	}

	@Override
	public final long indexOf(final E element)
	{
		if(element == this.element)
		{
			return 0;
		}
		return -1;
	}

	@Override
	public final long indexBy(final Predicate<? super E> predicate)
	{
		if(predicate.test(this.element))
		{
			return 0;
		}
		return -1;
	}

	@Override
	public final boolean isSorted(final Comparator<? super E> comparator)
	{
		return true; // hehe
	}

	@Override
	public final E last()
	{
		return this.element;
	}

	@Override
	public final long lastIndexOf(final E element)
	{
		if(element == this.element)
		{
			return 0;
		}
		return -1;
	}

	@Override
	public final long lastIndexBy(final Predicate<? super E> predicate)
	{
		if(predicate.test(this.element))
		{
			return 0;
		}
		return -1;
	}

	@Override
	public final long maxIndex(final Comparator<? super E> comparator)
	{
		return 0;
	}

	@Override
	public final long minIndex(final Comparator<? super E> comparator)
	{
		return 0;
	}

	@Override
	public final E peek()
	{
		return this.element;
	}

	@Override
	public final E poll()
	{
		return this.element;
	}

	@Override
	public final long scan(final Predicate<? super E> predicate)
	{
		if(predicate.test(this.element))
		{
			return 0;
		}
		return -1;
	}

	@Override
	public final Constant<E> view()
	{
		return this;
	}

	@Override
	public final Constant<E> view(final long lowIndex, final long highIndex)
	{
		if(lowIndex != 0)
		{
			throw new IndexOutOfBoundsException();
		}
		if(highIndex != 0)
		{
			throw new IndexOutOfBoundsException();
		}
		return this;
	}

	@Override
	public final boolean containsSearched(final Predicate<? super E> predicate)
	{
		return predicate.test(this.element);
	}

	@Override
	public final boolean applies(final Predicate<? super E> predicate)
	{
		return predicate.test(this.element);
	}

	@Override
	public final boolean contains(final E element)
	{
		return this.element == element;
	}

	@Override
	public final boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		final E element = this.element;
		return elements.applies(e ->
			e == element
		);
	}

	@Override
	public final boolean containsId(final E element)
	{
		return this.element == element;
	}

	@Override
	public final <T extends Consumer<? super E>> T copyTo(final T target)
	{
		target.accept(this.element);
		return target;
	}

	@Override
	public final <T extends Consumer<? super E>> T filterTo(final T target, final Predicate<? super E> predicate)
	{
		if(predicate.test(this.element))
		{
			target.accept(this.element);
		}
		return target;
	}

	@Override
	public final long count(final E element)
	{
		return this.element == element ? 1 : 0;
	}

	@Override
	public final long countBy(final Predicate<? super E> predicate)
	{
		return predicate.test(this.element) ? 1 : 0;
	}

	@Override
	public final <T extends Consumer<? super E>> T distinct(final T target)
	{
		target.accept(this.element);
		return target;
	}

	@Override
	public final <T extends Consumer<? super E>> T distinct(final T target, final Equalator<? super E> equalator)
	{
		target.accept(this.element);
		return target;
	}

	@Override
	public final Equalator<? super E> equality()
	{
		return Equalator.identity();
	}

	@Override
	public final boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(samples instanceof Constant)
		{
			return equalator.equal(this.element, ((Constant<? extends E>)samples).element);
		}
		return false;
	}

	@Override
	public final boolean equalsContent(
		final XGettingCollection<? extends E> samples  ,
		final Equalator<? super E>            equalator
	)
	{
		return XTypes.to_int(samples.size()) == 1 && equalator.equal(this.element, samples.get());
	}

	@Override
	public final <T extends Consumer<? super E>> T except(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final boolean hasVolatileElements()
	{
		return false;
	}

	@Override
	public final <T extends Consumer<? super E>> T intersect(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME  not implemented yet
	}

	@Override
	public final boolean isEmpty()
	{
		return false;
	}

	@Override
	public Iterator<E> iterator()
	{
		return new TrivialIterator<>(this);
	}

	@Override
	public final E max(final Comparator<? super E> comparator)
	{
		return this.element;
	}

	@Override
	public final E min(final Comparator<? super E> comparator)
	{
		return this.element;
	}

	@Override
	public final boolean nullContained()
	{
		return this.element == null;
	}

	@Override
	public final E seek(final E sample)
	{
		return this.element == sample ? sample : null;
	}

	@Override
	public final E search(final Predicate<? super E> predicate)
	{
		if(predicate.test(this.element))
		{
			return this.element;
		}
		return null;
	}

	@Override
	public final long size()
	{
		return 1;
	}

	@Override
	public Object[] toArray()
	{
		return new Object[]{this.element};
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		final E[] array = X.Array(type, 1);
		array[0] = this.element;
		return array;
	}

	@Override
	public final <T extends Consumer<? super E>> T union(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final boolean nullAllowed()
	{
		return false;
	}

	@Override
	public final long remainingCapacity()
	{
		return 0;
	}

	@Override
	public final boolean isFull()
	{
		return true;
	}

	@Override
	public final long maximumCapacity()
	{
		return 1;
	}


}
