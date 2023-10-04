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

public final class Aggregate_doubleMin<E> implements Aggregator<E, Double>
{
	private final To_double<? super E> getter;

	private double minimum = Double.MAX_VALUE;

	public Aggregate_doubleMin(final To_double<? super E> getter)
	{
		super();
		this.getter = getter;
	}

	@Override
	public final void accept(final E element)
	{
		final double value = this.getter.apply(element);
		if(value < this.minimum)
		{
			this.minimum = value;
		}
	}

	@Override
	public final Double yield()
	{
		return this.minimum;
	}

}
