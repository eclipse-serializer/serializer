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

import java.util.function.Consumer;


/**
 * Helps to aggregate multiple elements to one collected object.
 * 
 * @param <E> type of object to collect/aggregate
 * @param <R> type of resulting aggregation
 *
 */
@FunctionalInterface
public interface Aggregator<E, R> extends Consumer<E>
{
	/**
	 * Resets the aggregation. (e.g. removes all the aggregated elements from the collection)
	 * @return this
	 */
	public default Aggregator<E, R> reset()
	{
		// no-op in default implementation (no state to reset)
		return this;
	}

	/**
	 * Aggregate single element to the collecting object.
	 * 
	 * @param element to aggregate
	 */
	@Override
	public void accept(E element);

	/**
	 * Builds or aggregates the added elements to one collected object.
	 * @return the collected object
	 */
	public default R yield()
	{
		return null;
	}



	public interface Creator<E, R>
	{
		public Aggregator<E, R> createAggregator();
	}

}
