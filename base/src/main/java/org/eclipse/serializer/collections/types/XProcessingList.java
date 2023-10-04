
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





public interface XProcessingList<E> extends XRemovingList<E>, XGettingList<E>, XProcessingSequence<E>, XProcessingBag<E>
{
	public interface Factory<E>
	extends
	XRemovingList.Factory<E>,
	XGettingList.Factory<E>,
	XProcessingSequence.Factory<E>,
	XProcessingBag.Factory<E>
	{
		@Override
		public XProcessingList<E> newInstance();
	}



	@Override
	public XProcessingList<E> copy();

	/**
	 * Creates a new {@link XProcessingList} with the reversed order of elements.
	 * <p>
	 * This method creates a new collection and does <b>not</b> change the
	 * existing collection.
	 */
	@Override
	public XProcessingList<E> toReversed();

}
