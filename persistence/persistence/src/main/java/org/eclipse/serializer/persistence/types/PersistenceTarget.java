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

import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTransfer;

public interface PersistenceTarget<D> extends PersistenceWriteController
{
	public void write(D data) throws PersistenceExceptionTransfer;
	
	/**
	 * Prepare to write to this target. E.g. open a defined file.
	 * 
	 */
	public default void prepareTarget()
	{
		// no-op by default.
	}
	
	/**
	 * Take actions to deactivate/close/destroy the target because it won't be written to again.
	 */
	public default void closeTarget()
	{
		// no-op by default.
	}
}
