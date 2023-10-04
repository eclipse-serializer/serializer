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
public interface XPutGetCollection<E> extends XAddGetCollection<E>, XPuttingCollection<E>
{
	public interface Creator<E> extends XPuttingCollection.Creator<E>, XAddGetCollection.Creator<E>
	{
		@Override
		public XPutGetCollection<E> newInstance();
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XPutGetCollection<E> copy();

	@SuppressWarnings("unchecked")
	@Override
	public XPutGetCollection<E> addAll(E... elements);

	@Override
	public XPutGetCollection<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPutGetCollection<E> addAll(XGettingCollection<? extends E> elements);

	@Override
	@SuppressWarnings("unchecked")
	public XPutGetCollection<E> putAll(E... elements);
	
	@Override
	public XPutGetCollection<E> putAll(E[] elements, int srcStartIndex, int srcLength);
	
	@Override
	public XPutGetCollection<E> putAll(XGettingCollection<? extends E> elements);
	
}
