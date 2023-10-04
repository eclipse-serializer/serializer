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
import org.eclipse.serializer.collections.types.XGettingSequence;

public interface XPutGetSortation<E> extends XGettingSequence<E>, XPuttingSortation<E>
{
	public interface Factory<E> extends XGettingSequence.Factory<E>, XPuttingSortation.Factory<E>
	{
		@Override
		public XPutGetSortation<E> newInstance();
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XPutGetSortation<E> copy();

	@Override
	public XPutGetSortation<E> toReversed();

	@Override
	@SuppressWarnings("unchecked")
	public XPutGetSortation<E> addAll(E... elements);
	
	@Override
	public XPutGetSortation<E> addAll(E[] elements, int srcStartIndex, int srcLength);
	
	@Override
	public XPutGetSortation<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XPutGetSortation<E> putAll(E... elements);
	
	@Override
	public XPutGetSortation<E> putAll(E[] elements, int srcStartIndex, int srcLength);
	
	@Override
	public XPutGetSortation<E> putAll(XGettingCollection<? extends E> elements);

}
