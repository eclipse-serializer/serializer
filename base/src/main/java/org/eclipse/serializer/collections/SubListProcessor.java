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

import org.eclipse.serializer.collections.interfaces.CapacityExtendable;
import org.eclipse.serializer.collections.types.*;
import org.eclipse.serializer.equality.Equalator;
import org.eclipse.serializer.typing.XTypes;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class SubListProcessor<E> extends SubListView<E> implements XList<E>
{
	/* (12.07.2012 TM)FIXME: complete SubListProcessor implementation
	 * See all "FIXME"s
	 */

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public SubListProcessor(final XList<E> list, final long fromIndex, final long toIndex)
	{
		super(list, fromIndex, toIndex);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	/////////////////////

	private void internalClear()
	{
		this.size = 0;
		this.length = 0;
		this.d = 1;
	}

	private void decrement()
	{
		this.size -= 1;
		this.length -= this.d;
	}

	private void decrement(final long amount)
	{
		this.size -= amount;
		this.length -= amount * this.d;
	}

	@Override
	public final void clear()
	{
		((XList<E>)this.list).removeRange(this.startIndex, this.length);
		this.internalClear();
	}

	@Override
	public final <P extends Consumer<? super E>> P process(final P procedure)
	{
		final long oldListSize = ((XList<E>)this.list).size();
		this.decrement(oldListSize - ((XList<E>)this.list).size());
		return procedure;
	}

	@Override
	public final long removeDuplicates(final Equalator<? super E> equalator)
	{
		final long removeCount, oldListSize = ((XList<E>)this.list).size();
		this.decrement(removeCount = oldListSize - ((XList<E>)this.list).size());
		return XTypes.to_int(removeCount);
	}

	@Override
	public final long remove(final E element)
	{
		final long removeCount, oldListSize = ((XList<E>)this.list).size();
		this.decrement(removeCount = oldListSize - ((XList<E>)this.list).size());
		return XTypes.to_int(removeCount);
	}

	@Override
	public final long removeAll(final XGettingCollection<? extends E> samples)
	{
		final long removeCount, oldListSize = ((XList<E>)this.list).size();
		this.decrement(removeCount = oldListSize - ((XList<E>)this.list).size());
		return XTypes.to_int(removeCount);
	}

	@Override
	public final long removeDuplicates()
	{
		final long removeCount, oldListSize = ((XList<E>)this.list).size();
		this.decrement(removeCount = oldListSize - ((XList<E>)this.list).size());
		return XTypes.to_int(removeCount);
	}

	@Override
	public final E retrieve(final E element)
	{
		final int oldListSize = XTypes.to_int(this.list.size());
		final E e = XUtilsCollection.rngRetrieve((XList<E>)this.list, this.startIndex, this.length, element);
		this.decrement(oldListSize - XTypes.to_int(this.list.size()));
		return e;
	}

	@Override
	public final E retrieveBy(final Predicate<? super E> predicate)
	{
		final int oldListSize = XTypes.to_int(this.list.size());
		final E e = XUtilsCollection.rngRetrieve(
			(XList<E>)this.list,
			this.startIndex,
			this.length,
			predicate
		);
		this.decrement(oldListSize - XTypes.to_int(this.list.size()));
		return e;
	}

	@Override
	public final boolean removeOne(final E element)
	{
		if(XUtilsCollection.rngRemoveOne((XList<E>)this.list, this.startIndex, this.length, element))
		{
			this.decrement();
			return true;
		}
		return false;
	}

	@Override
	public final long retainAll(final XGettingCollection<? extends E> samples)
	{
		final long removeCount, oldListSize = ((XList<E>)this.list).size();
		this.decrement(removeCount = oldListSize - ((XList<E>)this.list).size());
		return XTypes.to_int(removeCount);
	}

	@Override
	public final long removeBy(final Predicate<? super E> predicate)
	{
		final long removeCount, oldListSize = ((XList<E>)this.list).size();
		this.decrement(removeCount = oldListSize - ((XList<E>)this.list).size());
		return XTypes.to_int(removeCount);
	}

	@Override
	public final void truncate()
	{
		((XList<E>)this.list).removeRange(this.startIndex, this.length);
		this.internalClear();
	}

	@Override
	public final SubListProcessor<E> range(final long fromIndex, final long toIndex)
	{
		this.checkRange(fromIndex, toIndex);
		return new SubListProcessor<>(
			(XList<E>)this.list,
			this.startIndex + fromIndex * this.d,
			this.startIndex + toIndex * this.d
		);
	}

	@Override
	public final long consolidate()
	{
		return ((XList<E>)this.list).consolidate() > 0 ? 1 : 0;
	}

	@Override
	public final <C extends Consumer<? super E>> C moveTo(final C target, final Predicate<? super E> predicate)
	{
		final long oldListSize = ((XList<E>)this.list).size();
		this.decrement(oldListSize - ((XList<E>)this.list).size());
		return target;
	}

	@Override
	public final long optimize()
	{
		return ((XList<E>)this.list).optimize();
	}

	@Override
	public final <C extends Consumer<? super E>> C moveSelection(final C target, final long... indices)
	{
		final long oldListSize = ((XList<E>)this.list).size();
		((XList<E>)this.list).moveSelection(target, this.shiftIndices(indices));
		this.decrement(oldListSize - ((XList<E>)this.list).size());
		return target;
	}

	@Override
	public final E removeAt(final long index) throws UnsupportedOperationException
	{
		this.checkIndex(index);
		final E element = ((XList<E>)this.list).removeAt(index);
		this.decrement();
		return element;
	}

	@Override
	public final E fetch()
	{
		return this.removeAt(0);
	}

	@Override
	public final E pop()
	{
		return this.removeAt(this.getEndIndex());
	}

	@Override
	public final E pinch()
	{
		return this.size == 0 ? null : this.removeAt(0);
	}

	@Override
	public final E pick()
	{
		return this.size == 0 ? null : this.removeAt(this.getEndIndex());
	}

	@Override
	public final SubListProcessor<E> removeRange(final long startIndex, final long length)
	{
		this.checkVector(startIndex, length);
		final int oldListSize = XTypes.to_int(this.list.size());
		((XList<E>)this.list).removeRange(this.startIndex + startIndex * this.d, length * this.d);
		this.decrement(oldListSize - XTypes.to_int(this.list.size()));
		return this;
	}

	@Override
	public final SubListProcessor<E> retainRange(final long startIndex, final long length)
	{
		this.checkVector(startIndex, length);
		final int oldListSize = XTypes.to_int(this.list.size());
		((XList<E>)this.list).retainRange(this.startIndex + startIndex * this.d, length * this.d);
		this.decrement(oldListSize - XTypes.to_int(this.list.size()));
		return this;
	}

	@Override
	public final long removeSelection(final long[] indices)
	{
		final int removeCount, oldListSize = XTypes.to_int(this.list.size());
		((XList<E>)this.list).removeSelection(this.shiftIndices(indices));
		this.decrement(removeCount = oldListSize - XTypes.to_int(this.list.size()));
		return XTypes.to_int(removeCount);
	}

	@Override
	public final SubListProcessor<E> toReversed()
	{
		return new SubListProcessor<>((XList<E>)this.list, this.getEndIndex(), this.startIndex);
	}

	@Override
	public final SubListProcessor<E> copy()
	{
		return new SubListProcessor<>((XList<E>)this.list, this.startIndex, this.getEndIndex());
	}

	@Override
	public final long nullRemove()
	{
		final long removeCount, oldListSize = ((XList<E>)this.list).size();

		this.decrement(removeCount = oldListSize - ((XList<E>)this.list).size());
		return XTypes.to_int(removeCount);
	}

	@Override
	public final SubListView<E> view(final long fromIndex, final long toIndex)
	{
		this.checkRange(fromIndex, toIndex);
		return new SubListView<>(this.list, this.startIndex + fromIndex * this.d, this.startIndex + toIndex * this.d);
	}

	@Override
	public final boolean replaceOne(final E element, final E replacement)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}


	@Override
	public final long replace(final E element, final E replacement)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final long replaceAll(final XGettingCollection<? extends E> elements, final E replacement)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final boolean replaceOne(final Predicate<? super E> predicate, final E substitute)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final long replace(final Predicate<? super E> predicate, final E substitute)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final long substitute(final Function<? super E, ? extends E> mapper)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final long substitute(final Predicate<? super E> predicate, final Function<E, E> mapper)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}


	@Override
	public final boolean set(final long index, final E element)
		throws IndexOutOfBoundsException, ArrayIndexOutOfBoundsException
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final E setGet(final long index, final E element)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final void setFirst(final E element)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final void setLast(final E element)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubListProcessor<E> shiftTo(final long sourceIndex, final long targetIndex)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubListProcessor<E> shiftTo(final long sourceIndex, final long targetIndex, final long length)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubListProcessor<E> shiftBy(final long sourceIndex, final long distance)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubListProcessor<E> shiftBy(final long sourceIndex, final long distance, final long length)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@SafeVarargs
	@Override
	public final SubListProcessor<E> setAll(final long index, final E... elements)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubListProcessor<E> set(final long index, final E[] elements, final int offset, final int length)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubListProcessor<E> set(
		final long                           index   ,
		final XGettingSequence<? extends E> elements,
		final long                           offset  ,
		final long                           length
	)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubListProcessor<E> swap(final long indexA, final long indexB)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubListProcessor<E> swap(final long indexA, final long indexB, final long length)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubListProcessor<E> reverse()
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubListProcessor<E> fill(final long offset, final long length, final E element)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubListProcessor<E> sort(final Comparator<? super E> comparator)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

    @Override
    public boolean input(final long index, final E element)
    {
        // FIXME XSequence<E>#input()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public boolean nullInput(final long index)
    {
        // FIXME XSequence<E>#nullInput()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    @SuppressWarnings("unchecked")
    public long inputAll(final long index, final E... elements)
    {
        // FIXME XSequence<E>#inputAll()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public long inputAll(final long index, final E[] elements, final int offset, final int length)
    {
        // FIXME XSequence<E>#inputAll()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public long inputAll(final long index, final XGettingCollection<? extends E> elements)
    {
        // FIXME XSequence<E>#inputAll()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public boolean prepend(final E element)
    {
        // FIXME XSequence<E>#prepend()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public boolean nullPrepend()
    {
        // FIXME XSequence<E>#nullPrepend()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public boolean preput(final E element)
    {
        // FIXME XSequence<E>#preput()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public boolean nullPreput()
    {
        // FIXME XSequence<E>#nullPreput()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public boolean insert(final long index, final E element)
    {
        // FIXME XSequence<E>#insert()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public boolean nullInsert(final long index)
    {
        // FIXME XSequence<E>#nullInsert()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    @SuppressWarnings("unchecked")
    public long insertAll(final long index, final E... elements)
    {
        // FIXME XSequence<E>#insertAll()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public long insertAll(final long index, final E[] elements, final int offset, final int length)
    {
        // FIXME XSequence<E>#insertAll()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public long insertAll(final long index, final XGettingCollection<? extends E> elements)
    {
        // FIXME XSequence<E>#insertAll()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public boolean add(final E element)
    {
        // FIXME XCollection<E>#add()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public boolean nullAdd()
    {
        // FIXME XCollection<E>#nullAdd()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public boolean put(final E element)
    {
        // FIXME XCollection<E>#put()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public boolean nullPut()
    {
        // FIXME XCollection<E>#nullPut()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public CapacityExtendable ensureCapacity(final long minimalCapacity)
    {
        // FIXME CapacityExtendable#ensureCapacity()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public CapacityExtendable ensureFreeCapacity(final long minimalFreeCapacity)
    {
        // FIXME CapacityExtendable#ensureFreeCapacity()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public long currentCapacity()
    {
        // FIXME CapacityExtendable#currentCapacity()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    @SuppressWarnings("unchecked")
    public XList<E> addAll(final E... elements)
    {
        // FIXME XList<E>#addAll()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public XList<E> addAll(final E[] elements, final int offset, final int length)
    {
        // FIXME XList<E>#addAll()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public XList<E> addAll(final XGettingCollection<? extends E> elements)
    {
        // FIXME XList<E>#addAll()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    @SuppressWarnings("unchecked")
    public XList<E> putAll(final E... elements)
    {
        // FIXME XList<E>#putAll()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public XList<E> putAll(final E[] elements, final int offset, final int length)
    {
        // FIXME XList<E>#putAll()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public XList<E> putAll(final XGettingCollection<? extends E> elements)
    {
        // FIXME XList<E>#putAll()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    @SuppressWarnings("unchecked")
    public XList<E> prependAll(final E... elements)
    {
        // FIXME XList<E>#prependAll()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public XList<E> prependAll(final E[] elements, final int offset, final int length)
    {
        // FIXME XList<E>#prependAll()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public XList<E> prependAll(final XGettingCollection<? extends E> elements)
    {
        // FIXME XList<E>#prependAll()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    @SuppressWarnings("unchecked")
    public XList<E> preputAll(final E... elements)
    {
        // FIXME XList<E>#preputAll()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public XList<E> preputAll(final E[] elements, final int offset, final int length)
    {
        // FIXME XList<E>#preputAll()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public XList<E> preputAll(final XGettingCollection<? extends E> elements)
    {
        // FIXME XList<E>#preputAll()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

}
