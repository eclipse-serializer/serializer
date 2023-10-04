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

import org.eclipse.serializer.collections.types.XBasicList;
import org.eclipse.serializer.collections.types.XGettingCollection;

public interface XLadder<E> extends XSortation<E>, XBasicList<E>
{
	public interface Factory<E> extends XSortation.Factory<E>, XBasicList.Creator<E>
	{
		@Override
		public XLadder<E> newInstance();

	}



	@Override
	public XLadder<E> copy();

	@Override
	public XLadder<E> toReversed();

	@SuppressWarnings("unchecked")
	@Override
	public XLadder<E> putAll(E... elements);
	@Override
	public XLadder<E> putAll(E[] elements, int srcStartIndex, int srcLength);
	@Override
	public XLadder<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XLadder<E> addAll(E... elements);
	@Override
	public XLadder<E> addAll(E[] elements, int srcStartIndex, int srcLength);
	@Override
	public XLadder<E> addAll(XGettingCollection<? extends E> elements);

}
