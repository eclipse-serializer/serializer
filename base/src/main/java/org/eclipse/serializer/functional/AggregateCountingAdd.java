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

import org.eclipse.serializer.collections.types.XCollection;

public class AggregateCountingAdd<E> implements Aggregator<E, Integer>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XCollection<? super E> target;
	private int addCount;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AggregateCountingAdd(final XCollection<? super E> target)
	{
		super();
		this.target = target;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E element)
	{
		if(this.target.add(element))
		{
			this.addCount++;
		}
	}

	@Override
	public final Integer yield()
	{
		return this.addCount;
	}

}
