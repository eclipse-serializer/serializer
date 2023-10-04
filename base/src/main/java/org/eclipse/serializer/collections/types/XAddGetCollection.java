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
public interface XAddGetCollection<E> extends XGettingCollection<E>, XAddingCollection<E>
{
	public interface Creator<E> extends XAddingCollection.Creator<E>, XGettingCollection.Creator<E>
	{
		@Override
		public XAddGetCollection<E> newInstance();
	}

		

	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XAddGetCollection<E> copy();

	@SuppressWarnings("unchecked")
	@Override
	public XAddGetCollection<E> addAll(E... elements);

	@Override
	public XAddGetCollection<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XAddGetCollection<E> addAll(XGettingCollection<? extends E> elements);
	
}
