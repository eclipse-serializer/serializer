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

public interface XExtendingList<E> extends XExtendingSequence<E>, XAddingList<E>, XPrependingList<E>
{
	public interface Creator<E> extends XExtendingSequence.Creator<E>, XAddingList.Creator<E>, XPrependingList.Creator<E>
	{
		@Override
		public XExtendingList<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XExtendingList<E> addAll(E... elements);

	@Override
	public XExtendingList<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExtendingList<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XExtendingList<E> prependAll(E... elements);

	@Override
	public XExtendingList<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExtendingList<E> prependAll(XGettingCollection<? extends E> elements);

}
