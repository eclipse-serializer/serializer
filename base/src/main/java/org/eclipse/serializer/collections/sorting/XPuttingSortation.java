package org.eclipse.serializer.collections.sorting;

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

import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.collections.types.XPuttingSequence;

public interface XPuttingSortation<E> extends XAddingSortation<E>, XPuttingSequence<E>
{
	public interface Factory<E> extends XAddingSortation.Factory<E>, XPuttingSequence.Creator<E>
	{
		@Override
		public XPuttingSortation<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XPuttingSortation<E> addAll(E... elements);
	@Override
	public XPuttingSortation<E> addAll(E[] elements, int srcStartIndex, int srcLength);
	@Override
	public XPuttingSortation<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XPuttingSortation<E> putAll(E... elements);
	@Override
	public XPuttingSortation<E> putAll(E[] elements, int srcStartIndex, int srcLength);
	@Override
	public XPuttingSortation<E> putAll(XGettingCollection<? extends E> elements);

}
