package org.eclipse.serializer.reference;

/*-
 * #%L
 * Eclipse Serializer Base
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

public interface ObjectSwizzling
{
	/**
	 * Retrieves the instance associated with the passed {@literal objectId}. Retrieving means guaranteeing that
	 * the associated instance is returned. If it does not yet exist, it will be created from persisted data,
	 * including all non-lazily referenced objects it is connected to.
	 * 
	 * @param objectId the {@literal objectId} defining which instance to return.
	 * 
	 * @return the instance associated with the passed {@literal objectId}.
	 */
	public Object getObject(long objectId);
}
