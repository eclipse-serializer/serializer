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

public interface PersistenceLocalObjectIdRegistry<D> extends PersistenceObjectIdRequestor<D>
{
	public PersistenceObjectManager<D> parentObjectManager();
	
	public <T> long lookupObjectId(
		T                               object           ,
		PersistenceObjectIdRequestor<D> objectIdRequestor,
		PersistenceTypeHandler<D, T>    optionalHandler
	);
	
	public void iterateMergeableEntries(PersistenceAcceptor iterator);
}
