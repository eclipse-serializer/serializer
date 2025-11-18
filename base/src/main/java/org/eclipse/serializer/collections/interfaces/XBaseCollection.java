package org.eclipse.serializer.collections.interfaces;

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

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * Basic interface that contains all general procedures that are common to any type of extended collection.
 */
public interface XBaseCollection extends Sized
{
    /**
     * Returns the maximum number of elements this carrier instance can contain.<br>
     * The actual value may depend on the configuration of the concrete instance or may depend only on the
     * implementation of the carrier (meaning it is constant for all instances of the implementation,
     * e.g. {@link Integer#MAX_VALUE})
     *
     * @return the maximum number of elements this carrier instance can contain.
     */
    public long maximumCapacity();

    /**
     * @return the number of elements this carrier instance can collect before reaching its maximum capacity.
     *
     */
    public default long remainingCapacity()
    {
        return this.maximumCapacity() - this.size();
    }

    /**
     * @return true if the current capacity cannot be increased any more.
     */
    public default boolean isFull()
    {
        return this.remainingCapacity() == 0L;
    }


    /**
     * Defines whether null-elements are allowed inside the collection or not.
     * @return {@code true} if null is allowed inside the collection; {@code false} if not
     */
    // funnily, this is the only method (so far) common to both getting and adding concerns.
    public boolean nullAllowed();

    /**
     * Tells whether this collection contains volatile elements.<br>
     * An element is volatile if it can become no longer reachable by the collection without being removed from the
     * collection. Examples are {@link WeakReference} of {@link SoftReference} or implementations of collection entries
     * that remove the element contained in an entry by some means outside the collection.<br>
     * Note that {@link WeakReference} instances that are added to a a simple (non-volatile) implementation of a
     * collection do <b>not</b> make the collection volatile, as the elements themselves (the reference instances) are still
     * strongly referenced.
     *
     * @return {@code true} if the collection contains volatile elements.
     */
    public boolean hasVolatileElements();

}
