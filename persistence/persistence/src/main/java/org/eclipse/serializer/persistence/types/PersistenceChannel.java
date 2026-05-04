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
 * Bidirectional combination of {@link PersistenceSource} (read) and {@link PersistenceTarget} (write) into a
 * single component. Used where a single persistence endpoint speaks both directions, e.g. a network channel
 * or an in-memory buffer that backs both loading and storing.
 * <p>
 * The default {@link #prepareChannel()} and {@link #closeChannel()} methods sequence the source and target
 * lifecycle hooks so callers don't have to invoke each side individually.
 *
 * @param <D> the persistence data type.
 *
 * @see PersistenceSource
 * @see PersistenceTarget
 */
public interface PersistenceChannel<D> extends PersistenceTarget<D>, PersistenceSource<D>
{
	// just a typing interface so far.

	/**
	 * Prepares both the source and the target side of the channel by calling
	 * {@link PersistenceSource#prepareSource()} followed by {@link PersistenceTarget#prepareTarget()}.
	 */
	public default void prepareChannel()
	{
		this.prepareSource();
		this.prepareTarget();
	}

	/**
	 * Closes both the source and the target side of the channel by calling
	 * {@link PersistenceSource#closeSource()} followed by {@link PersistenceTarget#closeTarget()}.
	 */
	public default void closeChannel()
	{
		this.closeSource();
		this.closeTarget();
	}

}
