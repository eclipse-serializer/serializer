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


public interface XPuttingEnum<E> extends XPuttingSet<E>, XPuttingSequence<E>, XAddingEnum<E>
{
	public interface Creator<E> extends XPuttingSet.Creator<E>, XPuttingSequence.Creator<E>, XAddingEnum.Creator<E>
	{
		@Override
		public XPuttingEnum<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XPuttingEnum<E> putAll(E... elements);

	@Override
	public XPuttingEnum<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPuttingEnum<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XPuttingEnum<E> addAll(E... elements);

	@Override
	public XPuttingEnum<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPuttingEnum<E> addAll(XGettingCollection<? extends E> elements);

}
