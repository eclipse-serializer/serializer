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
 * 
 *
 */
public interface XAddingSet<E> extends XAddingCollection<E>
{
	public interface Creator<E> extends XAddingCollection.Creator<E>
	{
		@Override
		public XAddingSet<E> newInstance();
	}

	@SuppressWarnings("unchecked")
	@Override
	public XAddingSet<E> addAll(E... elements);

	@Override
	public XAddingSet<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XAddingSet<E> addAll(XGettingCollection<? extends E> elements);
	
}
