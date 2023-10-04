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


public interface XProcessingSet<E> extends XRemovingSet<E>, XGettingSet<E>, XProcessingCollection<E>
{
	public interface Factory<E>
	extends XRemovingSet.Factory<E>, XGettingSet.Creator<E>, XProcessingCollection.Factory<E>
	{
		@Override
		public XProcessingSet<E> newInstance();
	}

	@Override
	public XImmutableSet<E> immure();

	@Override
	public XProcessingSet<E> copy();

}
