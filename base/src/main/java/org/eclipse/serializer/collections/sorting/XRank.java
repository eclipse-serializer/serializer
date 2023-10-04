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

import org.eclipse.serializer.collections.types.XBasicEnum;
import org.eclipse.serializer.collections.types.XGettingCollection;

public interface XRank<E> extends XSortation<E>, XBasicEnum<E>
{
	public interface Factory<E> extends XSortation.Factory<E>, XBasicEnum.Creator<E>
	{
		@Override
		public XRank<E> newInstance();

	}



	@Override
	public XRank<E> copy();

	@Override
	public XRank<E> toReversed();

	@SuppressWarnings("unchecked")
	@Override
	public XRank<E> addAll(E... elements);
	
	@Override
	public XRank<E> addAll(E[] elements, int srcStartIndex, int srcLength);
	
	@Override
	public XRank<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XRank<E> putAll(E... elements);
	
	@Override
	public XRank<E> putAll(E[] elements, int srcStartIndex, int srcLength);
	
	@Override
	public XRank<E> putAll(XGettingCollection<? extends E> elements);

}
