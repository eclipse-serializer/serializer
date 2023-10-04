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

import org.eclipse.serializer.collections.types.XAddingSequence;
import org.eclipse.serializer.collections.types.XGettingCollection;

public interface XAddingSortation<E> extends XAddingSequence<E>, Sorted<E>
{
	public interface Factory<E> extends XAddingSequence.Creator<E>
	{
		@Override
		public XAddingSortation<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XAddingSortation<E> addAll(E... elements);
	@Override
	public XAddingSortation<E> addAll(E[] elements, int srcStartIndex, int srcLength);
	@Override
	public XAddingSortation<E> addAll(XGettingCollection<? extends E> elements);

}
