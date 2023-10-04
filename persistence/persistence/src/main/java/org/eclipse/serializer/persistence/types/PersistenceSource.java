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

import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTransfer;

public interface PersistenceSource<D>
{
	/**
	 * A general, unspecific read, e.g. to initially read data in general from the attached data source.
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>simply ALL data from a plain file.</li>
	 * <li>only root nodes (and all recursively referenced nodes) of a graph-based database.</li>
	 * <li>nothing at all if not applicable, resulting in {@code null} being returned.</li>
	 * </ul>
	 *
	 * @return data segments containing general data if applicable, otherwise {@code null}.
	 * @throws PersistenceExceptionTransfer if a transfer error occurs
	 */
	public XGettingCollection<? extends D> read() throws PersistenceExceptionTransfer;

	public XGettingCollection<? extends D> readByObjectIds(PersistenceIdSet[] oids) throws PersistenceExceptionTransfer;
	
	/**
	 * Prepare to read from this source. E.g. open a defined file.
	 * 
	 */
	public default void prepareSource()
	{
		// no-op by default.
	}
	
	/**
	 * Take actions to deactivate/close/destroy the source because it won't be read again.
	 */
	public default void closeSource()
	{
		// no-op by default.
	}

}
