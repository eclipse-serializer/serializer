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

public interface PersistenceFunction
{
	/**
	 * Applies any action on the passed instance (e.g.: simply looking up its object ID or
	 * storing its state to a storage medium) and returns the object ID that identifies the passed instance.
	 * The returned OID may be the existing one for the passed instance or a newly associated one.
	 * 
	 * @param <T> the instance's type
	 * @param instance the instance to which the function shall be applied.
	 * @return the object ID (OID) that is associated with the passed instance.
	 */
	public <T> long apply(T instance);

}
