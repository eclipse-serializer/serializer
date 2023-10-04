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

import org.eclipse.serializer.util.X;


public final class LimitedRange<E> implements Predicate<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private int skip;
	private int limit;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public LimitedRange(final int skip, final int limit)
	{
		super();
		this.skip  = skip ;
		this.limit = limit;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final boolean test(final E e)
	{
		if(this.skip > 0)
		{
			this.skip--;
			return false;
		}
		if(this.limit > 0)
		{
			this.limit--;
			return true;
		}
		throw X.BREAK();
	}

}
