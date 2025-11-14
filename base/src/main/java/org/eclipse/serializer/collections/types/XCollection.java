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

import org.eclipse.serializer.collections.interfaces.CapacityExtendable;
import org.eclipse.serializer.equality.Equalator;
import org.eclipse.serializer.functional.Aggregator;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


/**
 * A collection is the root type for all collections (level 0 collection type).
 *
 * @param <E> type of contained elements
 */
public interface XCollection<E> extends XGettingCollection<E>, Consumer<E>, CapacityExtendable
{

    @Override
    public default void accept(final E element)
    {
        this.add(element);
    }

    /**
     * Adds the passed element.
     * @param element to add
     * @return {@code true} if element was added; {@code false} if not
     */
    public boolean add(E element);

    public boolean nullAdd();

    public E fetch(); // remove and retrieve first or throw IndexOutOfBoundsException if empty (fetch ~= first)

    public E pinch(); // remove and retrieve first or null if empty (like forcefull extraction from collection's base)

    public E retrieve(E element); // remove and retrieve first occurance

    public E retrieveBy(Predicate<? super E> predicate); // remove and retrieve first equal

    public long removeDuplicates(Equalator<? super E> equalator);

    public long removeBy(Predicate<? super E> predicate);

    public <C extends Consumer<? super E>> C moveTo(C target, Predicate<? super E> predicate);

    /* (12.11.2025 TM)TODO: replaceAll~ logic for XCollection?
     * For Set-logic collections, this would effectively be a consolidation:
     * Remove n elements and replace with with ONE element.
     */


    public long substitute(Function<? super E, ? extends E> mapper);


    public default Aggregator<E, ? extends XCollection<E>> collector()
    {
        return new Aggregator<>()
        {
            @Override
            public void accept(final E element)
            {
                XCollection.this.add(element);
            }

            @Override
            public XCollection<E> yield()
            {
                return XCollection.this;
            }
        };
    }

    /**
     * Adds the specified element to this collection if it is not already present (optional operation).
     * @param element to add
     * @return true if this collection did not already contain the specified element
     */
    public boolean put(E element);

    public boolean nullPut();

    /**
     * Adds the specified elements to this collection if it is not already present (optional operation).
     * @param elements to add
     * @return this
     */
    @SuppressWarnings("unchecked")
    public XCollection<E> putAll(E... elements);

    /**
     * Adds the specified elements to this collection if it is not already present (optional operation).<br>
     * Only the elements with indizes from the srcStartIndex to the srcStartIndex+srcLength
     * are put in the collection.
     * @param elements to add
     * @param srcStartIndex start index of elements-array to add to collection
     * @param srcLength length of elements-array to add to collection
     * @return this
     */
    public XCollection<E> putAll(E[] elements, int srcStartIndex, int srcLength);

    /**
     * Adds the specified elements to this collection if it is not already present (optional operation).
     * @param elements to add
     * @return this
     */
    public XCollection<E> putAll(XGettingCollection<? extends E> elements);


    @SuppressWarnings("unchecked")
    public XCollection<E> addAll(E... elements);

    public XCollection<E> addAll(E[] elements, int srcStartIndex, int srcLength);

    public XCollection<E> addAll(XGettingCollection<? extends E> elements);

    @Override
    public XCollection<E> copy();

    /**
     * Clears all elements from the collection while leaving the capacity as it is.
     */
    public void clear();

    /**
     * Clears (and reinitializes if needed) this collection in the fastest possible way, i.e. by allocating a new and
     * empty internal storage of default capacity. The collection will be empty after calling this method.
     */
    public void truncate();

    public long consolidate();

    /**
     * Optimizes internal memory usage by rebuilding the storage to only occupy as much memory as needed to store
     * the currently contained elements in terms of the collection's current memory usage configuration
     * (e.g. hash density).
     * <p>
     * If this is not possible or not needed in the concreate implementation, this method does nothing.
     * <p>
     * Note that this method can consume a considerable amount of time depending on the implementation and should
     * only be called intentionally and accurately when reducing occupied memory is needed.
     *
     * @return the number of elements that can be added before the internal storage has to be adjusted.
     */
    public long optimize();

    public long nullRemove();

    // (29.09.2012 TM)XXX: rename to removeFirst (first occurance for non-sequence, first in order for sequence)
    // (29.09.2012 TM)XXX: add removeLast()? Would be more efficient for array storages to scan backwards.
    public boolean removeOne(E element);

    public long remove(E element);

    public long removeAll(XGettingCollection<? extends E> elements);

    /**
     * Removing all elements except the ones contained in the given elements-collection.
     * <p>
     * Basically intersect this collection with the given collection and only keeping the resulting elements.
     *
     * @param elements to retain
     * @return Number of deleted elements
     */
    public long retainAll(XGettingCollection<? extends E> elements);

    public long removeDuplicates();

    public <P extends Consumer<? super E>> P process(P procedure);

}
