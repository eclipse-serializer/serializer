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
import org.eclipse.serializer.functional.SortingAggregator;

public final class CqlWrapperCollectorProcedure<O, T extends Consumer<O>> implements SortingAggregator<O, T>
{
	final T target;

	CqlWrapperCollectorProcedure(final T target)
	{
		super();
		this.target = target;
	}

	@Override
	public final void accept(final O element)
	{
		this.target.accept(element);
	}

	@Override
	public final T yield()
	{
		return this.target;
	}

	@Override
	public CqlWrapperCollectorProcedure<O, T> sort(final Comparator<? super O> order)
	{
		SortableProcedure.<O>sortIfApplicable(this.target, order);
		return this;
	}

}
