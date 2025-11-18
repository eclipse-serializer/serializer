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


import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Bag type collections make the single demand (thus being a level 1 collection type) that duplicate elements have
 * to be allowed, effectively being the opposite to set type collections.
 * <p>
 * The naming for the type is based on the conception that a bag can contain any elements (including duplicates),
 * but is definitely not ordered.
 * <p>
 * This will probably be a rather academic type and has been introduced more for reasons of completeness of the
 * typing architecture, as in practice, list type collections will be preferred to pure bag type collections.
 * <p>
 * Bag type collections are architectural on par with the other level 1 collection types set and sequence.
 * <p>
 * Currently, the only known to be useful subtype of a bag is the level 2 collection type list, combining bag
 * and sequence (order of elements).
 *
 * @param <E> type of contained elements
 *
 * @see XSet
 * @see XSequence
 * @see XList
 *
 * 
 */
public interface XBag<E> extends XGettingBag<E>, XCollection<E>
{

    @SuppressWarnings("unchecked")
    @Override
    public XBag<E> putAll(E... elements);

    @Override
    public XBag<E> putAll(E[] elements, int srcStartIndex, int srcLength);

    @Override
    public XBag<E> putAll(XGettingCollection<? extends E> elements);

    @SuppressWarnings("unchecked")
    @Override
    public XBag<E> addAll(E... elements);

    @Override
    public XBag<E> addAll(E[] elements, int srcStartIndex, int srcLength);

    @Override
    public XBag<E> addAll(XGettingCollection<? extends E> elements);

    @Override
    public XBag<E> copy();

    /**
     * Replaces the first element that is equal to the given element
     * with the replacement and then returns true.
     *
     * @param element to replace
     * @param replacement for the found element
     * @return {@code true} if element is found, {@code false} if not
     */
    public boolean replaceOne(E element, E replacement);

    public long replace(E element, E replacement);

    public long replaceAll(XGettingCollection<? extends E> elements, E replacement);

    public boolean replaceOne(Predicate<? super E> predicate, E replacement);

    public long replace(Predicate<? super E> predicate, E replacement);

    public long substitute(Predicate<? super E> predicate, Function<E, E> mapper);



}
