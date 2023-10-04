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

import java.util.Comparator;

import org.eclipse.serializer.collections.interfaces.ReleasingCollection;

public interface XSettingSequence<E> extends XSortableSequence<E>, ReleasingCollection<E>
{
	public interface Creator<E> extends XSortableSequence.Creator<E>
	{
		@Override
		public XSettingSequence<E> newInstance();
	}


	public boolean set(long index, E element);

	public E setGet(long index, E element);

	// intentionally not returning old element for performance reasons. set(int, E) does that already.
	public void setFirst(E element);

	public void setLast(E element);

	@SuppressWarnings("unchecked")
	public XSettingSequence<E> setAll(long index, E... elements);

	public XSettingSequence<E> set(long index, E[] elements, int offset, int length);

	public XSettingSequence<E> set(long index, XGettingSequence<? extends E> elements, long offset, long length);



	@Override
	public XSettingSequence<E> swap(long indexA, long indexB);

	@Override
	public XSettingSequence<E> swap(long indexA, long indexB, long length);

	@Override
	public XSettingSequence<E> reverse();

	@Override
	public XSettingSequence<E> sort(Comparator<? super E> comparator);

	@Override
	public XSettingSequence<E> copy();

	@Override
	public XSettingSequence<E> toReversed();

	@Override
	public XSettingSequence<E> range(long fromIndex, long toIndex);

}
