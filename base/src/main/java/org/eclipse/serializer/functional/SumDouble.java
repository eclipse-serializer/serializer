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


public final class SumDouble implements Aggregator<Double, Double>
{
	private double sum;

	@Override
	public final void accept(final Double n)
	{
		if(n != null)
		{
			this.sum += n;
		}
	}

	@Override
	public final Double yield()
	{
		return this.sum;
	}

}
