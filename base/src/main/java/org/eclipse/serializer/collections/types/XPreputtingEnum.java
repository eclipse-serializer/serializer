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

import org.eclipse.serializer.collections.interfaces.ReleasingCollection;


public interface XPreputtingEnum<E> extends XPreputtingSequence<E>, ReleasingCollection<E>
{
	public interface Creator<E> extends XPreputtingSequence.Creator<E>
	{
		@Override
		public XPreputtingEnum<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XPreputtingEnum<E> prependAll(E... elements);

	@Override
	public XPreputtingEnum<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPreputtingEnum<E> prependAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XPreputtingEnum<E> preputAll(E... elements);

	@Override
	public XPreputtingEnum<E> preputAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPreputtingEnum<E> preputAll(XGettingCollection<? extends E> elements);

}
