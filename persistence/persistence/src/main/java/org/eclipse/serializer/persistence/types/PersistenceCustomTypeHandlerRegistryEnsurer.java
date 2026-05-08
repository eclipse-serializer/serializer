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

import org.eclipse.serializer.reference.Reference;

/**
 * Pluggable factory the {@link PersistenceFoundation} consults to obtain its
 * {@link PersistenceCustomTypeHandlerRegistry}. The default implementation populates the registry with the
 * platform's bundled native handlers (e.g. the binary layer's per-primitive handlers); custom ensurers can
 * extend or replace that set without subclassing the foundation.
 *
 * @param <D> the persistence data type passed through to the registered handlers.
 *
 * @see PersistenceCustomTypeHandlerRegistry
 * @see PersistenceFoundation
 */
/* (16.10.2019 TM)NOTE:
 * Required to replace/modularize the calling of BinaryPersistence#createDefaultCustomTypeHandlerRegistry
 */
@FunctionalInterface
public interface PersistenceCustomTypeHandlerRegistryEnsurer<D>
{
	/**
	 * Builds and returns the custom type handler registry for the passed foundation. Implementations may
	 * consult the foundation for already-configured components and the (forward) reference to the type
	 * handler manager for handlers that need to call back into it.
	 *
	 * @param foundation                  the foundation requesting the registry.
	 * @param referenceTypeHandlerManager forward reference to the type handler manager that will own the
	 *                                    registered handlers; not yet initialized when this method is called.
	 *
	 * @return the populated custom type handler registry.
	 */
	public PersistenceCustomTypeHandlerRegistry<D> ensureCustomTypeHandlerRegistry(
		PersistenceFoundation<D, ? extends PersistenceFoundation<D, ?>> foundation,
		Reference<PersistenceTypeHandlerManager<D>>    referenceTypeHandlerManager
	);
}
