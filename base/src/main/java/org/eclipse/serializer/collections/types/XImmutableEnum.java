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


public interface XImmutableEnum<E> extends XImmutableSequence<E>, XImmutableSet<E>, XGettingEnum<E>
{
	public interface Factory<E> extends XImmutableSequence.Factory<E>, XImmutableSet.Factory<E>, XGettingEnum<E>
	{
		@Override
		public XImmutableEnum<E> newInstance();
	}


	
	@Override
	public XImmutableEnum<E> copy();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public XImmutableEnum<E> immure();

	@Override
	public XImmutableEnum<E> toReversed();

}
