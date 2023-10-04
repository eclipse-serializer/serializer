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
 * 
 * 
 *
 * @param <D> the data type
 */
/* (16.10.2019 TM)NOTE:
 * Required to replace/modularize the calling of BinaryPersistence#createDefaultCustomTypeHandlerRegistry
 */
@FunctionalInterface
public interface PersistenceCustomTypeHandlerRegistryEnsurer<D>
{
	public PersistenceCustomTypeHandlerRegistry<D> ensureCustomTypeHandlerRegistry(
		PersistenceFoundation<D, ? extends PersistenceFoundation<D, ?>> foundation,
		Reference<PersistenceTypeHandlerManager<D>>    referenceTypeHandlerManager
	);
}
