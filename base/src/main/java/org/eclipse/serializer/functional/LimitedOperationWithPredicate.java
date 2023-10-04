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
import java.util.function.Predicate;

import org.eclipse.serializer.branching.ThrowBreak;
import org.eclipse.serializer.util.X;

public final class LimitedOperationWithPredicate<E> implements Consumer<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private int skip;
	private int lim;
	private final Predicate<? super E> predicate;
	private final Consumer<? super E> procedure;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public LimitedOperationWithPredicate(
		final int skip,
		final int limit,
		final Predicate<? super E> predicate,
		final Consumer<? super E> procedure
	)
	{
		super();
		this.skip = skip;
		this.lim = limit;
		this.predicate = predicate;
		this.procedure = procedure;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E e)
	{
		try
		{
			if(!this.predicate.test(e))
			{
				return;
			}
			if(this.skip > 0)
			{
				this.skip--;
				return;
			}
			this.procedure.accept(e);
			if(--this.lim == 0)
			{
				throw X.BREAK();
			}
		}
		catch(final ThrowBreak t)
		{
			throw t;
		}
	}

}
