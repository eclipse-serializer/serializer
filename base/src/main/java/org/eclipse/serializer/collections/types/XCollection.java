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

import org.eclipse.serializer.functional.Aggregator;



/**
 * A collection is the root type for all collections (level 0 collection type).
 *
 * @param <E> type of contained elements
 */
public interface XCollection<E> extends XPutGetCollection<E>, XProcessingCollection<E>
{
	public interface Factory<E> extends XProcessingCollection.Factory<E>, XPutGetCollection.Creator<E>
	{
		@Override
		public XCollection<E> newInstance();
	}



	@Override
	public default Aggregator<E, ? extends XCollection<E>> collector()
	{
		return new Aggregator<E, XCollection<E>>()
		{
			@Override
			public void accept(final E element)
			{
				XCollection.this.add(element);
			}

			@Override
			public XCollection<E> yield()
			{
				return XCollection.this;
			}
		};
	}


	@SuppressWarnings("unchecked")
	@Override
	public XCollection<E> putAll(E... elements);

	@Override
	public XCollection<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XCollection<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XCollection<E> addAll(E... elements);
	@Override

	public XCollection<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XCollection<E> addAll(XGettingCollection<? extends E> elements);

	@Override
	public XCollection<E> copy();

}
