package org.eclipse.serializer.collections.types;

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

import org.eclipse.serializer.collections.IndexExceededException;

import java.util.Comparator;
import java.util.function.Consumer;

/**
 * Level 1 collection type defining the single demand for the collection's elements to be ordered.
 * <p>
 * Being ordered is defined as: An procedure affecting one element does not affect the order of all other elements.
 * Note that being ordered is not the same as being sorted. Being ordered only defines that there has to be a stable
 * order, while being sorted defines that the order is not only stable but also complies to a certain sorting logic.
 * <p>
 * The concept of being ordered introduces the concept of indexed element accessing as a consequence.
 * <p>
 * Sequence type collections are architectural on par with the other level 1 collection types set and bag.
 * <p>
 * Currently existing subtypes of sequence (level 2 collection types) are list (combining sequence and bag),
 * enum (combining sequence and set) and sortation (enhancing the contract from being ordered to being sorted).
 * <p>
 * Note that all collection types not being a subtype of sequence (like pure set and pure bag subtypes) are rather
 * academic and most probably only reasonably usable for high-end performance optimisations. This effectively
 * makes the sequence the dominant level 1 collection type, almost superseding the level 0 collection type collection
 * in practice.
 *
 */
public interface XSequence<E> extends XGettingSequence<E>, XCollection<E>
{
    public boolean input(long index, E element);

    public boolean nullInput(long index);

    @SuppressWarnings("unchecked")
    public long inputAll(long index, E... elements);

    public long inputAll(long index, E[] elements, int offset, int length);

    public long inputAll(long index, XGettingCollection<? extends E> elements);


    public boolean prepend(E element);

    public boolean nullPrepend();

    public default boolean shiftAdd(final E element)
    {
        return this.add(element);
    }

    public default boolean shiftPrepend(final E element)
    {
        return this.prepend(element);
    }

    public default boolean shiftPut(final E element)
    {
        return this.put(element);
    }

    @SuppressWarnings("unchecked")
    public XSequence<E> prependAll(E... elements);

    public XSequence<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

    public XSequence<E> prependAll(XGettingCollection<? extends E> elements);


    public boolean preput(E element);

    public boolean nullPreput();

    public default boolean shiftPreput(final E element)
    {
        return this.preput(element);
    }

    @SuppressWarnings("unchecked")
    public XSequence<E> preputAll(E... elements);

    public XSequence<E> preputAll(E[] elements, int offset, int length);

    public XSequence<E> preputAll(XGettingCollection<? extends E> elements);


    @Override
    @SuppressWarnings("unchecked")
    public XSequence<E> putAll(E... elements);

    @Override
    public XSequence<E> putAll(E[] elements, int srcStartIndex, int srcLength);

    @Override
    public XSequence<E> putAll(XGettingCollection<? extends E> elements);

    @Override
    @SuppressWarnings("unchecked")
    public XSequence<E> addAll(E... elements);

    @Override
    public XSequence<E> addAll(E[] elements, int srcStartIndex, int srcLength);

    @Override
    public XSequence<E> addAll(XGettingCollection<? extends E> elements);


    public boolean insert(long index, E element);

    public boolean nullInsert(long index);

    @SuppressWarnings("unchecked")
    public long insertAll(long index, E... elements);

    public long insertAll(long index, E[] elements, int offset, int length);

    public long insertAll(long index, XGettingCollection<? extends E> elements);



    public boolean set(long index, E element);

    public E setGet(long index, E element);

    // intentionally not returning old element for performance reasons. set(int, E) does that already.
    public void setFirst(E element);

    public void setLast(E element);

    @SuppressWarnings("unchecked")
    public XSequence<E> setAll(long index, E... elements);

    public XSequence<E> set(long index, E[] elements, int offset, int length);

    public XSequence<E> set(long index, XGettingSequence<? extends E> elements, long offset, long length);

    @Override
    public XSequence<E> copy();

    @Override
    public XSequence<E> toReversed();

    // (11.11.2025 TM)TODO: maybe define #shuffle here.
//	/**
//	 * Randomizes the elements in this {@link Shuffleable}.
//	 * @return this
//	 */
//	public XSequence<E> shuffle();

    /**
     * Sorts this collection according to the given comparator
     * and returns itself.
     * @param comparator to sort this collection
     * @return this
     */
    public XSequence<E> sort(Comparator<? super E> comparator);

    /**
     * Moves the element from the sourceIndex in the sequence to the targetIndex.<br>
     * All other elements are possibly moved to create the empty slot for the shifting element.
     * <p>
     * Does not expand or shrink the capacity of the sequence.
     * <p>
     * Throws a {@link IndexExceededException} if sourceIndex or targetIndex are
     * greater than the size of the sequence.
     *
     * @param sourceIndex points to the source element; Index of the source element
     * @param targetIndex points to the target element; Index of the target element
     * @return this
     */
    public XSequence<E> shiftTo(long sourceIndex, long targetIndex);

    /**
     * Moves multiple elements from the sourceIndex in the sequence to the targetIndex.<br>
     * All other elements are possibly moved to create the empty slot for the shifting element.
     * <p>
     * Does not expand or shrink the capacity of the sequence.
     * <p>
     * Throws a {@link IndexExceededException} if sourceIndex or targetIndex
     * exceed the size of the sequence.
     *
     * @param sourceIndex points to the source element; Index of the source element
     * @param targetIndex points to the target element; Index of the target element
     * @param length Amount of moved elements.
     * @return self
     */
    public XSequence<E> shiftTo(long sourceIndex, long targetIndex, long length);

    /**
     * Moves the element from the sourceIndex in the sequence to a higher index position.<br>
     * All other elements are possibly moved to create the empty slot for the shifting element.
     * ("to the right")
     * <p>
     * Does not expand or shrink the capacity of the sequence.
     * <p>
     * Throws a {@link IndexExceededException} if sourceIndex or targetIndex
     * (sourceIndex+distance) exceed the size of the sequence.
     *
     * @param sourceIndex points to the source element; Index of the source element
     * @param distance of how far the element should be moved.
     * Example: 1 moves the element from position 21 to position 22
     * @return self
     */
    public XSequence<E> shiftBy(long sourceIndex, long distance);

    /**
     * Moves multiple elements from the sourceIndex in the sequence to a higher index position.<br>
     * All other elements are possibly moved to create the empty slot for the shifting elements.
     * ("to the right")
     * <p>
     * Does not expand or shrink the capacity of the sequence.
     * <p>
     * Throws a {@link IndexExceededException} if sourceIndex or targetIndex
     * (sourceIndex+distance+length) exceed the size of the sequence.
     *
     * @param sourceIndex points to the source element; Index of the source element
     * @param distance of how far the element should be moved.
     * Example: 1 moves the element from position 21 to position 22
     * @param length Amount of moved elements.
     *
     * @return self
     */
    public XSequence<E> shiftBy(long sourceIndex, long distance, long length);

    public XSequence<E> swap(long indexA, long indexB);

    public XSequence<E> swap(long indexA, long indexB, long length);

    /**
     * Reverses the order of its own elements and returns itself.
     *
     * @return this
     */
    public XSequence<E> reverse();

    public E removeAt(long index); // remove and retrieve element at index or throw IndexOutOfBoundsException if invalid

    public XSequence<E> removeRange(long offset, long length);

    /**
     * Removing all elements but the ones from the offset (basically start index)
     * to the offset+length (end index).
     *
     * @param offset is the index of the first element to retain
     * @param length is the amount of elements to retain
     * @return this
     */
    public XSequence<E> retainRange(long offset, long length);

    public long removeSelection(long[] indices);

    //	@Override
//	public E fetch(); // remove and retrieve first or throw IndexOutOfBoundsException if empty (fetch ~= first)
    public E pop();   // remove and retrieve last  or throw IndexOutOfBoundsException if empty (stack conceptional pop)

    //	@Override
//	public E pinch(); // remove and retrieve first or null if empty (like forcefull extraction from collection's base)
    public E pick();  // remove and retrieve last  or null if empty (like easy extraction from collection's end)


    public <C extends Consumer<? super E>> C moveSelection(C target, long... indices);

    @Override
    public XGettingSequence<E> view(long fromIndex, long toIndex);

}
