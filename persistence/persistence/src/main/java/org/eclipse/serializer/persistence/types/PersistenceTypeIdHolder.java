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
 * Carrier of the highest type id that has so far been handed out for the persistent {@link PersistenceTypeDictionary}.
 * Read-only counterpart to the writing side of {@link PersistenceTypeIdProvider}: the holder only exposes the current
 * value, while the provider also advances and overrides it.
 * <p>
 * Type ids form a separate, monotonically increasing identifier space from object ids
 * (see {@link PersistenceObjectIdHolder}). Each persisted type permanently occupies one id in this space, and the
 * "current" value is the highest id known so far &mdash; the watermark from which the next type id is generated.
 *
 * @see PersistenceTypeIdProvider
 * @see PersistenceObjectIdHolder
 */
public interface PersistenceTypeIdHolder
{
	/**
	 * The highest type id known to this holder. Implementations that also generate new type ids
	 * (see {@link PersistenceTypeIdProvider}) advance this value as ids are handed out.
	 *
	 * @return the current (highest) type id.
	 */
	public long currentTypeId();
}
