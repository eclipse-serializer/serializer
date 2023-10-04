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

import java.util.function.Consumer;


/**
 * @param <E> type of contained elements
 * 
 *
 */
public interface XGettingSet<E> extends XGettingCollection<E>
{
	public interface Creator<E> extends XGettingCollection.Creator<E>
	{
		@Override
		public XGettingSet<E> newInstance();
	}



	@Override
	public XImmutableSet<E> immure();

	@Override
	public XGettingSet<E> copy();

	@Override
	public <P extends Consumer<? super E>> P iterate(P procedure);

}
