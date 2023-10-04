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

public interface XExtendingSequence<E> extends XAddingSequence<E>, XPrependingSequence<E>
{
	public interface Creator<E> extends XAddingSequence.Creator<E>, XPrependingSequence.Creator<E>
	{
		@Override
		public XExtendingSequence<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XExtendingSequence<E> addAll(E... elements);

	@Override
	public XExtendingSequence<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExtendingSequence<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XExtendingSequence<E> prependAll(E... elements);

	@Override
	public XExtendingSequence<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExtendingSequence<E> prependAll(XGettingCollection<? extends E> elements);

}
