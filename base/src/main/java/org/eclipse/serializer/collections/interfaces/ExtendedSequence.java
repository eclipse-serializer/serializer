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

import org.eclipse.serializer.collections.types.XSequence;
import org.eclipse.serializer.collections.types.XTable;

/**
 * Marker interface indicating that a type is ordered. Order super type for {@link XSequence} and {@link XTable}.
 * <p>
 * An ordered collection is defined as a collection where size-changing procedures like adding (putting) or removing
 * an element does not affect the order of the remaining elements contained in the collection (with "remaining"
 * meaning to exclude all elements that have to be removed from the collection for adding the new element).
 * Note that this applies to straight forward collection types like lists where every element is simply appended at the
 * end as well as to sorted collections, where new elements are sorted in at the appropriate place in the collection.
 * <p>
 * This definition does NOT apply to pure set or bag implementations, like {@link java.util.HashSet}, where elements
 * do have an internal order as well, but one that can dramatically change with potentionally any newly added element.
 */
public interface ExtendedSequence<E> extends ExtendedCollection<E>
{
	// marker interface
}
