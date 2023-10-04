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

import org.eclipse.serializer.collections.interfaces.ExtendedSequence;


public interface XAddingSequence<E> extends XAddingCollection<E>, ExtendedSequence<E>
{
	public interface Creator<E> extends XAddingCollection.Creator<E>
	{
		@Override
		public XAddingSequence<E> newInstance();
	}

	@SuppressWarnings("unchecked")
	@Override
	public XAddingSequence<E> addAll(E... elements);

	@Override
	public XAddingSequence<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XAddingSequence<E> addAll(XGettingCollection<? extends E> elements);

}
