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

public interface XPrependingList<E> extends XPrependingSequence<E>, ExtendedList<E>
{
	public interface Creator<E> extends XPrependingSequence.Creator<E>
	{
		@Override
		public XPrependingList<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XPrependingList<E> prependAll(E... elements);

	@Override
	public XPrependingList<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPrependingList<E> prependAll(XGettingCollection<? extends E> elements);

}
