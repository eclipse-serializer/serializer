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

import org.eclipse.serializer.reference.ObjectSwizzling;

public interface PersistenceLoadHandler extends PersistenceObjectLookup
{
	@Override
	public Object lookupObject(long objectId);
	
	public default ObjectSwizzling getObjectRetriever()
	{
		return this.getPersister();
	}
	
	public Persister getPersister();
	
	public void validateType(Object object, long objectId);
	
	public void requireRoot(Object rootInstance, long rootObjectId);
	
	@Deprecated
	public void registerCustomRootRefactoring(Object rootInstance, long customRootObjectId);
	
	@Deprecated
	public void registerDefaultRootRefactoring(Object rootInstance, long defaultRootObjectId);
	
}
