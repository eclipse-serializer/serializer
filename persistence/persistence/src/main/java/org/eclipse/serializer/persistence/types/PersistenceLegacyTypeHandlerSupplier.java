package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
 * %%
 * Copyright (C) 2023 - 2026 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

/**
 * Classes implementing this interface must supply a PersistenceLegacyTypeHandler
 * 
 * @param <D> Persitence implementation type
 * @param <T> Class&lt;?&gt; of the legacy type
 */
public interface PersistenceLegacyTypeHandlerSupplier<D,T>
{
	PersistenceLegacyTypeHandler<D,T> getLegacyTypeHandler();
}
