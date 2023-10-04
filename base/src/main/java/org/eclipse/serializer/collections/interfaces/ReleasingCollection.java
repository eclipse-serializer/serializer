package org.eclipse.serializer.collections.interfaces;

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
 * Marker type indicating that a collection releases references to its elements.
 * <p>
 * This mainly applies to removing, but also to setting, replacing and to all kinds of putting in set collections.
 *
 * @param <E> the type of elements in this collection
 */
public interface ReleasingCollection<E>
{
	// empty marker interface
}
