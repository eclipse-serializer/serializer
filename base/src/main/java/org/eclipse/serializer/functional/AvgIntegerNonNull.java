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

import org.eclipse.serializer.collections.types.XGettingCollection;


public final class AvgIntegerNonNull implements Aggregator<Integer, Integer>
{
	private int sum  ;
	private int count;

	public AvgIntegerNonNull()
	{
		super();
	}

	public AvgIntegerNonNull(final XGettingCollection<Integer> c)
	{
		super();
		c.iterate(this);
	}

	@Override
	public final void accept(final Integer n)
	{
		if(n == null)
		{
			return;
		}
		this.sum += n;
		this.count++;
	}

	@Override
	public final Integer yield()
	{
		return this.count == 0 ? null : this.sum / this.count;
	}

}
