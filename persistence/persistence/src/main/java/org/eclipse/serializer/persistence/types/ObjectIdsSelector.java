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
 * Drives an {@link ObjectIdsProcessor} session: implementations decide which subset of live object ids the
 * processor should see and orchestrate the actual {@link ObjectIdsProcessor#processObjectIdsByFilter} or
 * {@link ObjectIdsProcessor#provideObjectIdsBaseSet} calls accordingly.
 *
 * @see ObjectIdsProcessor
 */
public interface ObjectIdsSelector
{
	/**
	 * Drives a processing pass against {@code processor}, returning {@code true} on success and
	 * {@code false} if the underlying source declined the request (e.g. a registry that refused to lock
	 * for the operation).
	 *
	 * @param processor the processor to drive.
	 *
	 * @return {@code true} if processing completed, {@code false} if it was declined.
	 */
	public boolean processSelected(ObjectIdsProcessor processor);

}
