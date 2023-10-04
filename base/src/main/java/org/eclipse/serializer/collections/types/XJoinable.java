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

import java.util.function.BiConsumer;

/**
 * 
 * @param <E> type of data to join
 */
public interface XJoinable<E>
{
	/**
	 * Iterates over all elements of the collections and calls the joiner
	 * with each element and the aggregate.
	 * 
	 * @param joiner is the actual function to do the joining
	 * @param aggregate where to join into
	 * @param <A> type of aggregate
	 * @return the joined aggregate
	 */
	public <A> A join(BiConsumer<? super E, ? super A> joiner, A aggregate);
}
