package org.eclipse.serializer.typing;

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
 * Marker interface that indicates that a type is a non-independent part of a higher-ranging {@link Composition} type.
 * Instances of this type should never be regarded as independent instances and should never be referenced directly.
 * 
 * @see Composition
 * 
 *
 */
public interface ComponentType
{
	// only typing interface so far.
}
