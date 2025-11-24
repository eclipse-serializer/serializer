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

import org.eclipse.serializer.collections.Constant;
import org.eclipse.serializer.equality.Equalator;
import org.eclipse.serializer.reference.Reference;

import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Simple Reference class to handle mutable references. Handle with care!
 * <p>
 * Note: In most cases, a mutable reference object like this should not be necessary if the program is well-structured.
 * Extensive use of this class where it would be better to restructure the program may end in even more structural
 * problems.<br>
 * Yet in some cases, a mutable reference really is needed or at least helps in creating cleaner structures.
 */
public interface XReference<E> extends XReferencing<E>, XList<E>, XEnum<E>, Reference<E>
{
    @Override
    public void set(E element);

    @Override
    public default void accept(final E element)
    {
        this.set(element);
    }


    @Override
    public boolean set(long index, E element);

    @Override
    public E setGet(long index, E element);

    @Override
    public void setFirst(E element);

    @Override
    public void setLast(E element);

    @Override
    public XReference<E> shiftTo(long sourceIndex, long targetIndex);

    @Override
    public XReference<E> shiftTo(long sourceIndex, long targetIndex, long length);

    @Override
    public XReference<E> shiftBy(long sourceIndex, long distance);

    @Override
    public XReference<E> shiftBy(long sourceIndex, long distance, long length);

    @SuppressWarnings("unchecked")
    @Override
    public XReference<E> setAll(long index, E... elements);

    @Override
    public XReference<E> set(long index, E[] elements, int offset, int length);

    @Override
    public XReference<E> set(long index, XGettingSequence<? extends E> elements, long offset, long length);

    @Override
    public XReference<E> swap(long indexA, long indexB);

    @Override
    public XReference<E> swap(long indexA, long indexB, long length);

    @Override
    public XReference<E> reverse();

    @Override
    public XReference<E> fill(long offset, long length, E element);

    @Override
    public XReference<E> sort(Comparator<? super E> comparator);

    @Override
    public E at(long index);

    @Override
    public E first();

    @Override
    public E last();

    @Override
    public E poll();

    @Override
    public E peek();

    @Override
    public long maxIndex(Comparator<? super E> comparator);

    @Override
    public long minIndex(Comparator<? super E> comparator);

    @Override
    public long indexOf(E element);

    @Override
    public long indexBy(Predicate<? super E> predicate);

    @Override
    public long lastIndexOf(E element);

    @Override
    public long lastIndexBy(Predicate<? super E> predicate);

    @Override
    public long scan(Predicate<? super E> predicate);

    @Override
    public boolean isSorted(Comparator<? super E> comparator);

    @Override
    public <T extends Consumer<? super E>> T copySelection(T target, long... indices);

    @Override
    public Iterator<E> iterator();

    @Override
    public Object[] toArray();

    @Override
    public boolean hasVolatileElements();

    @Override
    public long size();

    @Override
    public boolean isEmpty();

    @Override
    public Equalator<? super E> equality();

    @Override
    public E[] toArray(Class<E> type);

    @Override
    public boolean equals(XGettingCollection<? extends E> samples, Equalator<? super E> equalator);

    @Override
    public boolean equalsContent(XGettingCollection<? extends E> samples, Equalator<? super E> equalator);

    @Override
    public boolean nullContained();

    @Override
    public boolean containsId(E element);

    @Override
    public boolean contains(E element);

    @Override
    public boolean containsSearched(Predicate<? super E> predicate);

    @Override
    public boolean containsAll(XGettingCollection<? extends E> elements);

    @Override
    public boolean applies(Predicate<? super E> predicate);

    @Override
    public long count(E element);

    @Override
    public long countBy(Predicate<? super E> predicate);

    @Override
    public E search(Predicate<? super E> predicate);

    @Override
    public E seek(E sample);

    @Override
    public E max(Comparator<? super E> comparator);

    @Override
    public E min(Comparator<? super E> comparator);

    @Override
    public <T extends Consumer<? super E>> T distinct(T target);

    @Override
    public <T extends Consumer<? super E>> T distinct(T target, Equalator<? super E> equalator);

    @Override
    public <T extends Consumer<? super E>> T copyTo(T target);

    @Override
    public <T extends Consumer<? super E>> T filterTo(T target, Predicate<? super E> predicate);

    @Override
    public <T extends Consumer<? super E>> T union(XGettingCollection<? extends E> other, Equalator<? super E> equalator, T target);

    @Override
    public <T extends Consumer<? super E>> T intersect(XGettingCollection<? extends E> other, Equalator<? super E> equalator, T target);

    @Override
    public <T extends Consumer<? super E>> T except(XGettingCollection<? extends E> other, Equalator<? super E> equalator, T target);

    @Override
    public boolean nullAllowed();

    @Override
    public long maximumCapacity();

    @Override
    public long remainingCapacity();

    @Override
    public boolean isFull();

    @Override
    public Constant<E> immure();

    @Override
    public ListIterator<E> listIterator();

    @Override
    public ListIterator<E> listIterator(long index);

    @Override
    public XReference<E> copy();

    @Override
    public XReference<E> toReversed();

    @Override
    public XReferencing<E> view();

    @Override
    public XReferencing<E> view(long lowIndex, long highIndex);

    @Override
    public XReference<E> range(long fromIndex, long toIndex);

    @Override
    @SuppressWarnings("unchecked")
    public XReference<E> putAll(E... elements);

    @Override
    public XReference<E> putAll(E[] elements, int offset, int length);

    @Override
    public XReference<E> putAll(XGettingCollection<? extends E> elements);

    @SuppressWarnings("unchecked")
    @Override
    public XReference<E> preputAll(E... elements);

    @Override
    public XReference<E> preputAll(E[] elements, int offset, int length);

    @Override
    public XReference<E> preputAll(XGettingCollection<? extends E> elements);

    @SuppressWarnings("unchecked")
    @Override
    public XReference<E> addAll(E... elements);

    @Override
    public XReference<E> addAll(E[] elements, int offset, int length);

    @Override
    public XReference<E> addAll(XGettingCollection<? extends E> elements);

    @SuppressWarnings("unchecked")
    @Override
    public XReference<E> prependAll(E... elements);

    @Override
    public XReference<E> prependAll(E[] elements, int offset, int length);

    @Override
    public XReference<E> prependAll(XGettingCollection<? extends E> elements);
}
