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
 * Container for shared logging context tokens used by the persistence layer's logging adapters. The token
 * itself is opaque &mdash; callers compare it by identity to decide whether a log event originates from a
 * given subsystem.
 */
public interface PersistenceLogging
{
	/**
	 * Logging context marker for storer-related events. Adapters compare this token by identity.
	 */
	public final static Object STORER_CONTEXT = Storer.class;
}
