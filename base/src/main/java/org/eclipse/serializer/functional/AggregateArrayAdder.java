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

import java.util.function.Predicate;

public final class AggregateArrayAdder<E> implements Aggregator<E, Integer>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Predicate<? super E> predicate;
	private final E[] array;
	private int i;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AggregateArrayAdder(final Predicate<? super E> predicate, final E[] array, final int i)
	{
		super();
		this.predicate = predicate;
		this.array = array;
		this.i = i;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E e)
	{
		if(!this.predicate.test(e))
		{
			return;
		}
		this.array[this.i++] = e;
	}

	@Override
	public final Integer yield()
	{
		return this.i;
	}

}
