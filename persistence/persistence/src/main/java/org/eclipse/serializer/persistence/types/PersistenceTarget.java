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
	/**
	 * Writes the passed data to this target.
	 * <p>
	 * <b>Buffer contract:</b> implementations may consume the data buffers' <i>positions</i> while
	 * writing, but must not modify their <i>limits</i> or otherwise restructure them (no
	 * {@code flip()}, {@code clear()}, {@code limit(...)}). The storer relies on this for
	 * failure recovery: after a rejected write it may rewind the buffer positions and retry the
	 * byte-identical data (e.g. after healing dangling references). A target that alters limits
	 * would make such a retry silently write truncated or corrupted data.
	 *
	 * @param data the data to write.
	 *
	 * @throws PersistenceExceptionTransfer if the write fails.
	 */
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
