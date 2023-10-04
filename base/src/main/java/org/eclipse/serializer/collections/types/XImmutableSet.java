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


public interface XImmutableSet<E> extends XImmutableCollection<E>, XGettingSet<E>
{
	public interface Factory<E> extends XImmutableCollection.Factory<E>, XGettingSet.Creator<E>
	{
		@Override
		public XImmutableSet<E> newInstance();
	}



	@Override
	public XImmutableSet<E> copy();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public XImmutableSet<E> immure();

}
