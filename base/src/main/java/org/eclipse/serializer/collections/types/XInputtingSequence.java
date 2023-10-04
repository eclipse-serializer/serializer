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



public interface XInputtingSequence<E> extends XInsertingSequence<E>, XExpandingSequence<E>
{
	public interface Creator<E> extends XInsertingSequence.Creator<E>, XExpandingSequence.Creator<E>
	{
		@Override
		public XInputtingSequence<E> newInstance();
	}



	public boolean input(long index, E element);

	public boolean nullInput(long index);

	@SuppressWarnings("unchecked")
	public long inputAll(long index, E... elements);

	public long inputAll(long index, E[] elements, int offset, int length);

	public long inputAll(long index, XGettingCollection<? extends E> elements);



	@SuppressWarnings("unchecked")
	@Override
	public XInputtingSequence<E> addAll(E... elements);

	@Override
	public XInputtingSequence<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInputtingSequence<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")

	@Override
	public XInputtingSequence<E> putAll(E... elements);

	@Override
	public XInputtingSequence<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInputtingSequence<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XInputtingSequence<E> prependAll(E... elements);

	@Override
	public XInputtingSequence<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInputtingSequence<E> prependAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XInputtingSequence<E> preputAll(E... elements);

	@Override
	public XInputtingSequence<E> preputAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInputtingSequence<E> preputAll(XGettingCollection<? extends E> elements);

}
