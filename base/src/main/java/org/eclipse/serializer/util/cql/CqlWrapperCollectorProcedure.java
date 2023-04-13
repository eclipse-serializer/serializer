package org.eclipse.serializer.util.cql;

/*-
 * #%L
 * Eclipse Serializer Base
 * %%
 * Copyright (C) 2023 Eclipse Foundation
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
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
