package org.eclipse.serializer.collections.lazy;

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

import java.util.List;

import org.eclipse.serializer.reference.Lazy;

/**
 * A {@link List}, which uses {@link Lazy} references internally,
 * to enable automatic partial loading of the list's content.
 *
 * @param <E> the type of elements in this collection
 */
public interface LazyList<E> extends List<E>, LazyCollection<E>
{
	// just a marker interface, for now
}
