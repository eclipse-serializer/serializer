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


public interface XImmutableList<E> extends XImmutableSequence<E>, XImmutableBag<E>, XGettingList<E>
{
	public interface Factory<E> extends XImmutableSequence.Factory<E>, XGettingList.Factory<E>, XImmutableBag.Factory<E>
	{
		@Override
		public XImmutableList<E> newInstance();
	}


	
	@Override
	public XImmutableList<E> copy();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public XImmutableList<E> immure();

	@Override
	public XImmutableList<E> toReversed();

	@Override
	public XImmutableList<E> range(long fromIndex, long toIndex);

	@Override
	public XImmutableList<E> view();

	@Override
	public XImmutableList<E> view(long lowIndex, long highIndex);
}
