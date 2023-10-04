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



public interface XProcessingBag<E> extends XRemovingBag<E>, XGettingBag<E>, XProcessingCollection<E>
{
	public interface Factory<E>
	extends XRemovingBag.Factory<E>, XGettingBag.Factory<E>, XProcessingCollection.Factory<E>
	{
		@Override
		public XProcessingBag<E> newInstance();
	}



	@Override
	public XProcessingBag<E> copy();

	@Override
	public XGettingBag<E> view();


	/**
	 * Provides an instance of an immutable collection type with equal behavior and data as this instance.
	 * <p>
	 * If this instance already is of an immutable collection type, it returns itself.
	 *
	 * @return an immutable copy of this collection instance.
	 */
	@Override
	public XImmutableBag<E> immure();

}
