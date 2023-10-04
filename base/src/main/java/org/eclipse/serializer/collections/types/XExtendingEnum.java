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

public interface XExtendingEnum<E> extends XExtendingSequence<E>, XAddingEnum<E>, XPrependingEnum<E>
{
	public interface Creator<E> extends XExtendingSequence.Creator<E>, XAddingEnum.Creator<E>, XPrependingEnum.Creator<E>
	{
		@Override
		public XExtendingEnum<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XExtendingEnum<E> addAll(E... elements);
	
	@Override
	public XExtendingEnum<E> addAll(E[] elements, int srcStartIndex, int srcLength);
	
	@Override
	public XExtendingEnum<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XExtendingEnum<E> prependAll(E... elements);
	
	@Override
	public XExtendingEnum<E> prependAll(E[] elements, int srcStartIndex, int srcLength);
	
	@Override
	public XExtendingEnum<E> prependAll(XGettingCollection<? extends E> elements);

}
