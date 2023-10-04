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

import org.eclipse.serializer.functional.IndexedAcceptor;

public interface XIndexIterable<E> extends XIterable<E>
{
	/**
	 * Iterates over elements with the {@link IndexedAcceptor} to use
	 * not only the element itself but also its coherent index.
	 * @param <IP> type of procedure
	 * @param procedure which is executed when iterating
	 * @return Given procedure
	 */
	public <IP extends IndexedAcceptor<? super E>> IP iterateIndexed(IP procedure);
}
