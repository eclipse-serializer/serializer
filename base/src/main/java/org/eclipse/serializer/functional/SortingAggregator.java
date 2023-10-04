package org.eclipse.serializer.functional;

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

import org.eclipse.serializer.collections.sorting.SortableProcedure;

public interface SortingAggregator<E, R> extends Aggregator<E, R>, SortableProcedure<E>
{
	@Override
	public SortingAggregator<E, R> sort(Comparator<? super E> order);
}
