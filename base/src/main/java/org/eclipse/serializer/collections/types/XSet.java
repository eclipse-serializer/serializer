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
public interface XSet<E> extends XCollection<E>, XPutGetSet<E>, XProcessingSet<E>
{
	public interface Factory<E> extends XCollection.Factory<E>, XPutGetSet.Factory<E>, XProcessingSet.Factory<E>
	{
		@Override
		public XSet<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XSet<E> putAll(E... elements);

	@Override
	public XSet<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XSet<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XSet<E> addAll(E... elements);

	@Override
	public XSet<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XSet<E> addAll(XGettingCollection<? extends E> elements);

	@Override
	public XSet<E> copy();

}
