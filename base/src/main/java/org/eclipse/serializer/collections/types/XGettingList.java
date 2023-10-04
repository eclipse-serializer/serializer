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

import java.util.ListIterator;

import org.eclipse.serializer.collections.interfaces.ExtendedList;
import org.eclipse.serializer.collections.old.OldList;

public interface XGettingList<E> extends XGettingSequence<E>, XGettingBag<E>, ExtendedList<E>
{
	public interface Factory<E> extends XGettingCollection.Creator<E>
	{
		@Override
		public XGettingList<E> newInstance();
	}



	/**
	 * {@inheritDoc}
	 */
	@Override
	public XImmutableList<E> immure();

	// java.util.List List Iterators
	public ListIterator<E> listIterator();
	
	public ListIterator<E> listIterator(long index);

	@Override
	public OldList<E> old();

	@Override
	public XGettingList<E> copy();

	@Override
	public XGettingList<E> toReversed();

	@Override
	public XGettingList<E> view();
	
	@Override
	public XGettingList<E> view(long lowIndex, long highIndex);

	@Override
	public XGettingList<E> range(long fromIndex, long toIndex);

}
