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

public interface XEnum<E> extends XGettingEnum<E>, XSet<E>, XSequence<E>
{
	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XEnum<E> copy();

	@Override
	public XEnum<E> toReversed();

	@Override
	public XEnum<E> reverse();

	@Override
	public XEnum<E> sort(Comparator<? super E> comparator);

	@Override
	public XEnum<E> range(final long lowIndex, final long highIndex);

	@SuppressWarnings("unchecked")
	@Override
	public XEnum<E> addAll(E... elements);

	@Override
	public XEnum<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XEnum<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XEnum<E> putAll(E... elements);

	@Override
	public XEnum<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XEnum<E> putAll(XGettingCollection<? extends E> elements);

	@Override
	public XEnum<E> prependAll(E... elements);

	@Override
	public XEnum<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XEnum<E> prependAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XEnum<E> preputAll(E... elements);

	@Override
	public XEnum<E> preputAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XEnum<E> preputAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XEnum<E> setAll(long index, E... elements);

	@Override
	public XEnum<E> set(long index, E[] elements, int offset, int length);

	@Override
	public XEnum<E> set(long index, XGettingSequence<? extends E> elements, long offset, long length);

	@Override
	public XEnum<E> swap(long indexA, long indexB);

	@Override
	public XEnum<E> swap(long indexA, long indexB, long length);

}
