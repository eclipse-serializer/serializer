package org.eclipse.serializer.util.cql;

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

import java.util.Comparator;
import java.util.function.Consumer;

import org.eclipse.serializer.collections.sorting.SortableProcedure;
import org.eclipse.serializer.collections.types.XIterable;
import org.eclipse.serializer.functional.Aggregator;

public final class CqlWrapperCollectorSequenceSorting<O, R extends Consumer<O> & XIterable<O>>
implements Aggregator<O, R>
{
	final R target;
	final Comparator<? super O> order;

	CqlWrapperCollectorSequenceSorting(final R target, final Comparator<? super O> order)
	{
		super();
		this.target = target;
		this.order  = order ;
	}

	@Override
	public final void accept(final O element)
	{
		this.target.accept(element);
	}

	@Override
	public final R yield()
	{
		SortableProcedure.sortIfApplicable(this.target, this.order);
		return this.target;
	}

}
