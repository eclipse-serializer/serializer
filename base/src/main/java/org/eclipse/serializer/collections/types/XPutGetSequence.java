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



public interface XPutGetSequence<E> extends XPuttingSequence<E>, XGettingSequence<E>, XPutGetCollection<E>
{
	public interface Factory<E> extends XPuttingSequence.Creator<E>, XPutGetCollection.Creator<E>
	{
		@Override
		public XPutGetSequence<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XPutGetSequence<E> putAll(E... elements);

	@Override
	public XPutGetSequence<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPutGetSequence<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XPutGetSequence<E> addAll(E... elements);

	@Override
	public XPutGetSequence<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPutGetSequence<E> addAll(XGettingCollection<? extends E> elements);

	@Override
	public XPutGetSequence<E> copy();

	@Override
	public XPutGetSequence<E> toReversed();

}
