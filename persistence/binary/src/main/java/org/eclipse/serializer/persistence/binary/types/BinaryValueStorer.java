package org.eclipse.serializer.persistence.binary.types;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
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

import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

/**
 * Reads a single value from a source instance's memory and writes it into the binary form at a target
 * memory address. One {@link BinaryValueStorer} is configured per persisted member; reference values are
 * funneled through the {@link PersistenceStoreHandler} so the persister can register and store unseen
 * referenced instances. Counterpart of {@link BinaryValueSetter} on the storing side.
 *
 * @see BinaryValueSetter
 * @see BinaryValueFunctions
 */
public interface BinaryValueStorer
{
	/**
	 * Reads the value at {@code source + sourceOffset} (or, when {@code source} is {@code null}, at the
	 * absolute address {@code sourceOffset}) and writes its persisted representation to
	 * {@code targetAddress}. Reference values are routed through {@code persister} for OID resolution.
	 *
	 * @param source        the source instance, or {@code null} for absolute-address access.
	 * @param sourceOffset  the field offset within {@code source} or an absolute source address.
	 * @param targetAddress the target memory address to write to.
	 * @param persister     the store handler used to resolve references.
	 *
	 * @return the address immediately after the written value.
	 */
	public long storeValueFromMemory(
		Object                          source       ,
		long                            sourceOffset ,
		long                            targetAddress,
		PersistenceStoreHandler<Binary> persister
	);
}
