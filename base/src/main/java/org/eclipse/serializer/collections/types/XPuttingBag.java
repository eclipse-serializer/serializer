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


public interface XPuttingBag<E> extends XAddingBag<E>, XPuttingCollection<E>
{
	public interface Creator<E> extends XAddingBag.Factory<E>, XPuttingCollection.Creator<E>
	{
		@Override
		public XPuttingBag<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XAddingCollection<E> addAll(E... elements);

	@Override
	public XAddingCollection<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XAddingCollection<E> addAll(XGettingCollection<? extends E> elements);


	/**
	 * {@inheritDoc}
	 * <p>
	 * In this implementation it is identical to {@link XPuttingBag#addAll(Object...)}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public XPuttingCollection<E> putAll(E... elements);

	/**
	 * {@inheritDoc}
	 * <p>
	 * In this implementation it is identical to {@link XPuttingBag#addAll(Object[], int, int)}
	 */
	@Override
	public XPuttingCollection<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	/**
	 * {@inheritDoc}
	 * <p>
	 * In this implementation it is identical to {@link XPuttingBag#addAll(XGettingCollection)}
	 */
	@Override
	public XPuttingCollection<E> putAll(XGettingCollection<? extends E> elements);

}
