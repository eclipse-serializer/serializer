package org.eclipse.serializer.functional;

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

/**
 * Additionally to the element to accept, this class' {@link IndexedAcceptor#accept(Object, long)} method,
 * uses the coherent index of the given element.
 * 
 * @param <T> type of element to accept
 *
 */
public interface IndexedAcceptor<T>
{
	/**
	 * Expects the element and its coherent index.
	 * 
	 * @param e element which is expected at the given index
	 * @param index on which the element is expected
	 */
	public void accept(T e, long index);
}
