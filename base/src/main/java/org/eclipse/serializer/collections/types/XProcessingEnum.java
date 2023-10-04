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


public interface XProcessingEnum<E> extends XRemovingEnum<E>, XGettingEnum<E>, XProcessingSet<E>, XProcessingSequence<E>
{
	public interface Creator<E>
	extends XGettingEnum.Creator<E>, XProcessingSet.Factory<E>, XProcessingSequence.Factory<E>
	{
		@Override
		public XProcessingEnum<E> newInstance();
	}



	@Override
	public XProcessingEnum<E> copy();

	@Override
	public XProcessingEnum<E> toReversed();

}
