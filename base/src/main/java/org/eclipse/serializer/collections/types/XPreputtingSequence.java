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

public interface XPreputtingSequence<E> extends XPrependingSequence<E>
{
	public interface Creator<E> extends XPrependingSequence.Creator<E>
	{
		@Override
		public XPreputtingSequence<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XPreputtingSequence<E> prependAll(E... elements);

	@Override
	public XPreputtingSequence<E> prependAll(E[] elements, int offset, int length);

	@Override
	public XPreputtingSequence<E> prependAll(XGettingCollection<? extends E> elements);

	public boolean preput(E element);

	public boolean nullPreput();

	@SuppressWarnings("unchecked")
	public XPreputtingSequence<E> preputAll(E... elements);

	public XPreputtingSequence<E> preputAll(E[] elements, int offset, int length);

	public XPreputtingSequence<E> preputAll(XGettingCollection<? extends E> elements);

}
