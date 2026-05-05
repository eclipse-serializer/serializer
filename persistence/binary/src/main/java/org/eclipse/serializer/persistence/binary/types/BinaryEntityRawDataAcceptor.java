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

/**
 * Functional callback invoked by {@link BinaryEntityRawDataIterator} for each entity encountered while
 * scanning a raw memory range. Returning {@code false} aborts the iteration at the start of the current
 * entity, leaving the remaining bytes untouched for a later resumed scan.
 */
@FunctionalInterface
public interface BinaryEntityRawDataAcceptor
{
	/**
	 * @param entityStartAddress the start address of the current entity (header).
	 * @param dataBoundAddress   the exclusive bound address of the surrounding raw data range.
	 *
	 * @return {@code true} to continue iteration, {@code false} to abort at the current entity.
	 */
	public boolean acceptEntityData(long entityStartAddress, long dataBoundAddress);
}
