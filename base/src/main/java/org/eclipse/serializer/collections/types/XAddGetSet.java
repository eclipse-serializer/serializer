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



/**
 * @param <E> type of contained elements
 * 
 *
 */
public interface XAddGetSet<E> extends XAddingSet<E>, XGettingSet<E>, XAddGetCollection<E>
{
	public interface Factory<E> extends XAddingSet.Creator<E>, XGettingSet.Creator<E>, XAddGetCollection.Creator<E>
	{
		@Override
		public XAddGetSet<E> newInstance();
	}



	public E addGet(E element);
	
	public E deduplicate(E element);

	@SuppressWarnings("unchecked")
	@Override
	public XAddGetSet<E> addAll(E... elements);

	@Override
	public XAddGetSet<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XAddGetSet<E> addAll(XGettingCollection<? extends E> elements);

	@Override
	public XAddGetSet<E> copy();

}
