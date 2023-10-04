package org.eclipse.serializer.collections.types;

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

import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.functional.Aggregator;
import org.eclipse.serializer.typing.Immutable;

public interface XImmutableCollection<E> extends XGettingCollection<E>, Immutable
{
	public interface Factory<E> extends XGettingCollection.Creator<E>
	{
		@Override
		public XImmutableCollection<E> newInstance();
	}


	public static <E> Aggregator<E, XImmutableCollection<E>> Builder()
	{
		return Builder(1);
	}

	public static <E> Aggregator<E, XImmutableCollection<E>> Builder(final long initialCapacity)
	{
		return new Aggregator<>()
		{
			private final BulkList<E> newInstance = BulkList.New(initialCapacity);

			@Override
			public final void accept(final E element)
			{
				this.newInstance.add(element);
			}

			@Override
			public final XImmutableCollection<E> yield()
			{
				return this.newInstance.immure();
			}
		};
	}



	@Override
	public XImmutableCollection<E> copy();

	/**
	 * Always returns the already immutable collection instance itself
	 * <p>
	 * For spawning a copy of the collection instance, see {@link #copy()}
	 *
	 * @return a reference to the instance itself.
	 * @see #copy()
	 */
	@Override
	public XImmutableCollection<E> immure();

}
