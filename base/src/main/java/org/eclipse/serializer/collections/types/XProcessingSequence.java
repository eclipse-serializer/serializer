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



import java.util.function.Consumer;

public interface XProcessingSequence<E> extends XRemovingSequence<E>, XGettingSequence<E>, XProcessingCollection<E>
{
	public interface Factory<E>
	extends XRemovingSequence.Factory<E>, XGettingSequence.Factory<E>, XProcessingCollection.Factory<E>
	{
		@Override
		public XProcessingSequence<E> newInstance();
	}


	/**
	 * Remove and retrieve element at index or throw IndexOutOfBoundsException if invalid.
	 * @param index index of item to be retrieved.
	 * @return Item at index or IndexOutOfBoundsException if invalid.
	 */
	public E removeAt(long index);

	/**
	 * Remove and retrieve last  or throw IndexOutOfBoundsException if empty (stack conceptional pop).
	 * @return Last item or IndexOutOfBoundsException if empty.
	 */
	public E pop();

	/**
	 * Remove and retrieve last  or null if empty (like easy extraction from collection's end).
	 * @return Last item or null if empty.
	 */
	public E pick();


	@Override
	public XProcessingSequence<E> toReversed();

	public <C extends Consumer<? super E>> C moveSelection(C target, long... indices);

	@Override
	public XGettingSequence<E> view(long fromIndex, long toIndex);

}
