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

import org.eclipse.serializer.collections.types.XSequence;
import org.eclipse.serializer.functional.Aggregator;

import java.util.Comparator;
import java.util.function.BiConsumer;

public final class CqlWrapperCollectorLinkingSorting<O, R extends XSequence<O>> implements Aggregator<O, R>
{
	final R                     target;
	final BiConsumer<O, R>     linker;
	final Comparator<? super O> order ;

	CqlWrapperCollectorLinkingSorting(final R target, final BiConsumer<O, R> linker, final Comparator<? super O> order)
	{
		super();
		this.target = target;
		this.linker = linker;
		this.order  = order ;
	}

	@Override
	public final void accept(final O element)
	{
		this.linker.accept(element, this.target);
	}

	@Override
	public final R yield()
	{
		this.target.sort(this.order);
		return this.target;
	}

}
