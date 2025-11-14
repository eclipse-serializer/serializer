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
import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.collections.types.XList;
import org.eclipse.serializer.collections.types.XSequence;
import org.eclipse.serializer.equality.Equalator;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


public class SubListAccessor<E> extends SubListView<E> implements XList<E>
{
	/* (12.07.2012 TM)FIXME: complete SubListAccessor implementation
	 * See all "FIXME"s
	 */

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public SubListAccessor(final XList<E> list, final long fromIndex, final long toIndex)
	{
		super(list, fromIndex, toIndex);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	/////////////////////

	@Override
	public  long replace(final E element, final E replacement)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public  boolean replaceOne(final E element, final E replacement)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public  long substitute(final Function<? super E, ? extends E> mapper)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public  long substitute(final Predicate<? super E> predicate, final Function<E, E> mapper)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public  SubListAccessor<E> range(final long fromIndex, final long toIndex)
	{
		this.checkRange(fromIndex, toIndex);
		return new SubListAccessor<>(
			(XList<E>)this.list,
			this.startIndex + fromIndex * this.d,
			this.startIndex + toIndex * this.d
		);
	}

	@Override
	public  SubListAccessor<E> fill(final long offset, final long length, final E element)
	{
		this.checkVector(offset, length);
		((XList<E>)this.list).fill(this.startIndex + offset * this.d, length * this.d, element);
		return this;
	}

	@Override
	public  boolean replaceOne(final Predicate<? super E> predicate, final E substitute)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public  SubListAccessor<E> reverse()
	{
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SubListAccessor<E> setAll(final long offset, final E... elements)
	{
		this.checkVector(offset, elements.length);
		if(this.d > 0)
		{
			((XList<E>)this.list).setAll(this.startIndex + offset, elements);
		}
		else
		{
			((XList<E>)this.list).setAll(this.startIndex - offset, XArrays.toReversed(elements));
		}
		return this;
	}

	@Override
	public  SubListAccessor<E> set(final long offset, final E[] src, final int srcIndex, final int srcLength)
	{
		this.checkVector(offset, srcLength);
		if(this.d > 0)
		{
			((XList<E>)this.list).set(this.startIndex + offset * +1, src, srcIndex, srcLength);
		}
		else
		{
			final int revElementsStartIndex;
			if(srcLength == 0)
			{
				revElementsStartIndex = srcIndex;
			}
			else if(srcLength > 0)
			{
				revElementsStartIndex = srcIndex + srcLength - 1;
			}
			else
			{
				revElementsStartIndex = srcIndex + srcLength + 1;
			}
			((XList<E>)this.list).set(this.startIndex + offset * -1, src, revElementsStartIndex, -srcLength);
		}
		return this;
	}

	@Override
	public  SubListAccessor<E> set(
		final long                          offset        ,
		final XGettingSequence<? extends E> elements      ,
		final long                          elementsOffset,
		final long                          elementsLength
	)
	{
		this.checkVector(offset, elementsLength);
		if(this.d > 0)
		{
			((XList<E>)this.list).set(this.startIndex + offset, elements, elementsOffset, elementsLength);
		}
		else
		{
			final long revElementsStartIndex;
			if(elementsLength == 0)
			{
				revElementsStartIndex = elementsOffset;
			}
			else if(elementsLength > 0)
			{
				revElementsStartIndex = elementsOffset + elementsLength - 1;
			}
			else
			{
				revElementsStartIndex = elementsOffset + elementsLength + 1;
			}
			((XList<E>)this.list).set(
				this.startIndex - offset,
				elements,
				revElementsStartIndex,
				-elementsLength
			);
		}
		return this;
	}

	@Override
	public  void setFirst(final E element)
	{
		((XList<E>)this.list).setGet(this.startIndex, element);
	}

	@Override
	public  void setLast(final E element)
	{
		((XList<E>)this.list).setGet(this.getEndIndex(), element);
	}

	@Override
	public  SubListAccessor<E> sort(final Comparator<? super E> comparator)
	{
		XUtilsCollection.rngSort((XList<E>)this.list, this.startIndex, this.length, comparator);
		return this;
	}

	@Override
	public  SubListAccessor<E> shiftTo(final long sourceIndex, final long targetIndex)
	{
		this.checkIndex(sourceIndex);
		this.checkIndex(targetIndex);
		((XList<E>)this.list).shiftTo(sourceIndex, targetIndex);
		return this;
	}

	@Override
	public  SubListAccessor<E> shiftTo(final long sourceIndex, final long targetIndex, final long length)
	{
		this.checkVector(sourceIndex, length);
		this.checkVector(targetIndex, length);
		((XList<E>)this.list).shiftTo(sourceIndex, targetIndex, length);
		return this;
	}

	@Override
	public  SubListAccessor<E> shiftBy(final long sourceIndex, final long distance)
	{
		this.checkIndex(sourceIndex);
		this.checkIndex(sourceIndex + distance);
		((XList<E>)this.list).shiftTo(sourceIndex, distance);
		return this;
	}

	@Override
	public  SubListAccessor<E> shiftBy(final long sourceIndex, final long distance, final long length)
	{
		this.checkVector(sourceIndex, length);
		this.checkVector(sourceIndex + distance, length);
		((XList<E>)this.list).shiftTo(sourceIndex, distance, length);
		return this;
	}

	@Override
	public  SubListAccessor<E> swap(final long indexA, final long indexB)
	{
		this.checkIndex(indexA);
		this.checkIndex(indexB);
		((XList<E>)this.list).swap(this.startIndex + indexA * this.d, this.startIndex + indexB * this.d);
		return this;
	}

	@Override
	public  SubListAccessor<E> swap(final long indexA, final long indexB, final long length)
	{
		this.checkVector(indexA, length);
		this.checkVector(indexB, length);
		((XList<E>)this.list).swap(
			this.startIndex + indexA * this.d,
			this.startIndex + indexB * this.d,
			length * this.d
		);
		return this;
	}

	@Override
	public  long replace(final Predicate<? super E> predicate, final E substitute)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}


	@Override
	public  long replaceAll(final XGettingCollection<? extends E> elements, final E replacement)
	{
		throw new org.eclipse.serializer.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public  boolean set(final long index, final E element)
	{
		this.checkIndex(index);
		return ((XList<E>)this.list).set(this.startIndex + index * this.d, element);
	}

	@Override
	public  E setGet(final long index, final E element)
	{
		this.checkIndex(index);
		return ((XList<E>)this.list).setGet(this.startIndex + index * this.d, element);
	}

	@Override
	public  SubListView<E> view(final long fromIndex, final long toIndex)
	{
		this.checkRange(fromIndex, toIndex);
		return new SubListView<>(this.list, this.startIndex + fromIndex * this.d, this.startIndex + toIndex * this.d);
	}

	@Override
	public  SubListAccessor<E> toReversed()
	{
		return new SubListAccessor<>((XList<E>)this.list, this.getEndIndex(), this.startIndex);
	}

	@Override
	public  SubListAccessor<E> copy()
	{
		return new SubListAccessor<>((XList<E>)this.list, this.startIndex, this.getEndIndex());
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
    public E removeAt(final long index)
    {
        // FIXME XSequence<E>#removeAt()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public XSequence<E> removeRange(final long offset, final long length)
    {
        // FIXME XSequence<E>#removeRange()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public long removeSelection(final long[] indices)
    {
        // FIXME XSequence<E>#removeSelection()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public E pop()
    {
        // FIXME XSequence<E>#pop()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public E pick()
    {
        // FIXME XSequence<E>#pick()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public <C extends Consumer<? super E>> C moveSelection(final C target, final long... indices)
    {
        // FIXME XSequence<E>#moveSelection()
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
    public E fetch()
    {
        // FIXME XCollection<E>#fetch()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public E pinch()
    {
        // FIXME XCollection<E>#pinch()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public E retrieve(final E element)
    {
        // FIXME XCollection<E>#retrieve()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public E retrieveBy(final Predicate<? super E> predicate)
    {
        // FIXME XCollection<E>#retrieveBy()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public long removeDuplicates(final Equalator<? super E> equalator)
    {
        // FIXME XCollection<E>#removeDuplicates()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public long removeBy(final Predicate<? super E> predicate)
    {
        // FIXME XCollection<E>#removeBy()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public <C extends Consumer<? super E>> C moveTo(final C target, final Predicate<? super E> predicate)
    {
        // FIXME XCollection<E>#moveTo()
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
    public void clear()
    {
        // FIXME XCollection<E>#clear()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public void truncate()
    {
        // FIXME XCollection<E>#truncate()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public long consolidate()
    {
        // FIXME XCollection<E>#consolidate()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public long optimize()
    {
        // FIXME XCollection<E>#optimize()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public long nullRemove()
    {
        // FIXME XCollection<E>#nullRemove()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public boolean removeOne(final E element)
    {
        // FIXME XCollection<E>#removeOne()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public long remove(final E element)
    {
        // FIXME XCollection<E>#remove()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public long removeAll(final XGettingCollection<? extends E> elements)
    {
        // FIXME XCollection<E>#removeAll()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public long retainAll(final XGettingCollection<? extends E> elements)
    {
        // FIXME XCollection<E>#retainAll()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public long removeDuplicates()
    {
        // FIXME XCollection<E>#removeDuplicates()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }

    @Override
    public <P extends Consumer<? super E>> P process(final P procedure)
    {
        // FIXME XCollection<E>#process()
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

    @Override
    public XList<E> retainRange(final long offset, final long length)
    {
        // FIXME XList<E>#retainRange()
        throw new org.eclipse.serializer.meta.NotImplementedYetError();
    }
    
}
