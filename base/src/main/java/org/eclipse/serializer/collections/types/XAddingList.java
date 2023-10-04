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

import org.eclipse.serializer.collections.interfaces.ExtendedList;


public interface XAddingList<E> extends XAddingSequence<E>, XAddingBag<E>, ExtendedList<E>
{
	public interface Creator<E> extends XAddingSequence.Creator<E>
	{
		@Override
		public XAddingList<E> newInstance();
	}

	@SuppressWarnings("unchecked")
	@Override
	public XAddingList<E> addAll(E... elements);

	@Override
	public XAddingList<E> addAll(E[] elements, int offset, int length);

	@Override
	public XAddingList<E> addAll(XGettingCollection<? extends E> elements);

}
