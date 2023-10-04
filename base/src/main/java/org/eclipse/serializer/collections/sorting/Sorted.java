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

import org.eclipse.serializer.collections.interfaces.ExtendedSequence;


/**
 * Single concern type defining that a sub type is always sorted according to an internal {@link Comparator}.
 * <p>
 * This definition extends the definition of being ordered.
 * <p>
 * This type is mutually exclusive to {@link Sortable}.
 *
 * @param <E> the type of the input to the operation
 */
public interface Sorted<E> extends ExtendedSequence<E>
{
	/**
	 *
	 * @return the {@link Comparator} that defines the sorting order of this {@link Sorted} instance.
	 */
	public Comparator<? super E> getComparator();
}
