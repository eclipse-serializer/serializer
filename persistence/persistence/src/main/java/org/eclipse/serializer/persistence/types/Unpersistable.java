package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
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
 * Marks a type as being unpersistence. Encountering such a type in the dynamic persistence type analysis will cause
 * a validation exception.<br>
 * This type is very useful as a safety net to prevent instances of types that may never end up in a
 * persistent context (database or serialized byte strem) from being persisted.
 * <br>
 * The naming (missing "Persistence" prefix) is intentional to support convenience on the application code level.
 * 
 *
 */
public interface Unpersistable
{
	// Marker interface
}
