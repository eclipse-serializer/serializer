package org.eclipse.serializer.collections.sorting;

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

import org.eclipse.serializer.collections.types.XProcessingSequence;

public interface XProcessingSortation<E> extends XGettingSortation<E>, XRemovingSortation<E>, XProcessingSequence<E>
{
	public interface Factory<E> extends XGettingSortation.Factory<E>, XProcessingSequence.Factory<E>
	{
		@Override
		public XProcessingSortation<E> newInstance();

	}



	@Override
	public XProcessingSortation<E> copy();

	@Override
	public XProcessingSortation<E> toReversed();

}
