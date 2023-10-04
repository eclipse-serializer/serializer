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

public interface XExpandingEnum<E> extends XExtendingEnum<E>, XPuttingEnum<E>, XPreputtingEnum<E>, XExpandingSequence<E>
{
	public interface Creator<E>
	extends
	XExtendingEnum.Creator<E>,
	XPuttingEnum.Creator<E>,
	XPreputtingEnum.Creator<E>,
	XExpandingSequence.Creator<E>
	{
		@Override
		public XExpandingEnum<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XExpandingEnum<E> addAll(E... elements);

	@Override
	public XExpandingEnum<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExpandingEnum<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XExpandingEnum<E> putAll(E... elements);

	@Override
	public XExpandingEnum<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExpandingEnum<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XExpandingEnum<E> prependAll(E... elements);

	@Override
	public XExpandingEnum<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExpandingEnum<E> prependAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XExpandingEnum<E> preputAll(E... elements);

	@Override
	public XExpandingEnum<E> preputAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExpandingEnum<E> preputAll(XGettingCollection<? extends E> elements);

}
