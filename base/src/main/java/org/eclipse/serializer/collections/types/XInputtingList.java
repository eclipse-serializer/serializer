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



public interface XInputtingList<E> extends XInputtingSequence<E>, XExpandingList<E>
{
	public interface Factory<E> extends XInputtingSequence.Creator<E>, XExpandingList.Factory<E>
	{
		@Override
		public XInputtingList<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XInputtingList<E> addAll(E... elements);

	@Override
	public XInputtingList<E> addAll(E[] elements, int offset, int length);

	@Override
	public XInputtingList<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XInputtingList<E> putAll(E... elements);

	@Override
	public XInputtingList<E> putAll(E[] elements, int offset, int length);

	@Override
	public XInputtingList<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XInputtingList<E> prependAll(E... elements);

	@Override
	public XInputtingList<E> prependAll(E[] elements, int offset, int length);

	@Override
	public XInputtingList<E> prependAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XInputtingList<E> preputAll(E... elements);

	@Override
	public XInputtingList<E> preputAll(E[] elements, int offset, int length);

	@Override
	public XInputtingList<E> preputAll(XGettingCollection<? extends E> elements);

}
