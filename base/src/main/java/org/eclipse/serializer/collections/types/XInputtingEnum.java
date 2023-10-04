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



public interface XInputtingEnum<E> extends XInsertingEnum<E>, XExpandingEnum<E>
{
	public interface Creator<E> extends XInsertingEnum.Creator<E>, XExpandingEnum.Creator<E>
	{
		@Override
		public XInputtingEnum<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XInputtingEnum<E> addAll(E... elements);

	@Override
	public XInputtingEnum<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInputtingEnum<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XInputtingEnum<E> putAll(E... elements);

	@Override
	public XInputtingEnum<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInputtingEnum<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XInputtingEnum<E> prependAll(E... elements);

	@Override
	public XInputtingEnum<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInputtingEnum<E> prependAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XInputtingEnum<E> preputAll(E... elements);

	@Override
	public XInputtingEnum<E> preputAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInputtingEnum<E> preputAll(XGettingCollection<? extends E> elements);

}
