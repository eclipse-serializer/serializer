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
import java.util.function.Consumer;

public interface SortableProcedure<E> extends Consumer<E>
{
	public static <E> void sortIfApplicable(final Consumer<E> procedure, final Comparator<? super E> comparator)
	{
		if(comparator == null || !(procedure instanceof SortableProcedure<?>))
		{
			return;
		}
		((SortableProcedure<E>)procedure).sort(comparator);
	}

    /**
     * Sorts this procedure's content according to the given comparator and returns itself.
     * @param comparator to sort this collection
     * @return this
     */
    public SortableProcedure<E> sort(Comparator<? super E> comparator);
}
