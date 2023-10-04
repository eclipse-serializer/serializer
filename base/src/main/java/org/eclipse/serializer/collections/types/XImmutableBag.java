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


public interface XImmutableBag<E> extends XImmutableCollection<E>, XGettingBag<E>
{
	public interface Factory<E> extends XImmutableCollection.Factory<E>, XGettingBag.Factory<E>
	{
		@Override
		public XImmutableBag<E> newInstance();
	}

	
	
	@Override
	public XImmutableBag<E> copy();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public XImmutableBag<E> immure();


}
