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

public final class Aggregate_doubleSum<E> implements Aggregator<E, Double>
{
	private final To_double<? super E> getter;

	private double sum;

	public Aggregate_doubleSum(final To_double<? super E> getter)
	{
		super();
		this.getter = getter;
	}

	@Override
	public final void accept(final E element)
	{
		this.sum += this.getter.apply(element);
	}

	@Override
	public final Double yield()
	{
		return this.sum;
	}

}
