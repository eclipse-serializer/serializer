package org.eclipse.serializer.collections.sorting;

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

import java.util.Comparator;

/**
 * Single concern type defining that a sub type can be sorted according to an external {@link Comparator}.
 * <p>
 * This type is mutually exclusive to {@link Sorted}.
 *
 * @param <E> the type of the input to the operation
 *
 */
public interface Sortable<E>
{
	/**
	 * Sorts this collection according to the given comparator
	 * and returns itself.
	 * @param comparator to sort this collection
	 * @return this
	 */
	public Sortable<E> sort(Comparator<? super E> comparator);
}
