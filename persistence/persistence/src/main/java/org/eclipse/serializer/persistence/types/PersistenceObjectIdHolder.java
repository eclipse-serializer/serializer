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
 * Carrier of the highest object id that has so far been handed out for the persistent object graph. Unlike
 * {@link PersistenceTypeIdHolder} (read-only), this holder is also writable so that the highest assigned
 * object id can be restored from persistent state after a restart.
 * <p>
 * Object ids form a separate, monotonically increasing identifier space from type ids
 * (see {@link PersistenceTypeIdHolder}) and constant ids; the configured starting value lies above the type-id
 * range so that the spaces never overlap. Each persisted instance permanently occupies one id in this space,
 * and the "current" value is the highest object id assigned so far, from which the next object id is
 * generated.
 * <p>
 * Implemented by {@link PersistenceObjectIdProvider} (which adds id generation) and by
 * {@link PersistenceObjectManager} (which combines a registry with a provider).
 *
 * @see PersistenceObjectIdProvider
 * @see PersistenceTypeIdHolder
 */
public interface PersistenceObjectIdHolder
{
	/**
	 * The highest object id known to this holder. Implementations that also generate new object ids
	 * (see {@link PersistenceObjectIdProvider}) advance this value as ids are handed out.
	 *
	 * @return the current (highest) object id.
	 */
	public long currentObjectId();

	/**
	 * Overrides the current highest assigned object id. Typically called when the persistence layer is
	 * restored from disk and the holder must be advanced past every id already in use.
	 *
	 * @param currentObjectId the new highest object id.
	 *
	 * @return this holder, for fluent chaining.
	 */
	public PersistenceObjectIdHolder updateCurrentObjectId(long currentObjectId);

}
