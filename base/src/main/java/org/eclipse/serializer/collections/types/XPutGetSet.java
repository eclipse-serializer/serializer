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
public interface XPutGetSet<E> extends XPuttingSet<E>, XAddGetSet<E>, XPutGetCollection<E>
{
	public interface Factory<E> extends XPuttingSet.Creator<E>, XGettingSet.Creator<E>, XPutGetCollection.Creator<E>
	{
		@Override
		public XPutGetSet<E> newInstance();
	}



	public E putGet(E element);
	
	public E replace(E element);

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public XPutGetSet<E> putAll(E... elements);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public XPutGetSet<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public XPutGetSet<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XPutGetSet<E> addAll(E... elements);

	@Override
	public XPutGetSet<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPutGetSet<E> addAll(XGettingCollection<? extends E> elements);

	@Override
	public XPutGetSet<E> copy();

}
