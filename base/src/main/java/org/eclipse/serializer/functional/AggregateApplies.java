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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.function.Predicate;

import org.eclipse.serializer.util.X;

public class AggregateApplies<E> implements Aggregator<E, Boolean>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private Boolean applies = TRUE;
	private final Predicate<? super E> predicate;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AggregateApplies(final Predicate<? super E> predicate)
	{
		super();
		this.predicate = predicate;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E element)
	{
		if(!this.predicate.test(element))
		{
			this.applies = FALSE;
			throw X.BREAK();
		}
	}

	@Override
	public final Boolean yield()
	{
		return this.applies;
	}

}
