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
 * Mix-in interface implemented by a current {@link PersistenceTypeHandler} that ships its own
 * matching {@link PersistenceLegacyTypeHandler}.
 * <p>
 * When {@link PersistenceLegacyTypeMapper} ensures a legacy handler for a previously persisted type,
 * it first checks whether the current handler implements this interface; if so, the supplied legacy
 * handler is used (after structural validation and typeId initialization) and no automatic mapping is
 * performed. This is the escape hatch for handlers that need full manual control over how legacy data
 * is read &mdash; e.g. when a structurally non-trivial migration must be performed.
 *
 * @param <D> the data target (persistence implementation) type.
 * @param <T> the runtime type the legacy data should be re-bound to.
 *
 * @see PersistenceLegacyTypeMapper
 */
public interface PersistenceLegacyTypeHandlerSupplier<D,T>
{
	/**
	 * @return the legacy handler shipped alongside this current handler.
	 */
	PersistenceLegacyTypeHandler<D,T> getLegacyTypeHandler();
}
