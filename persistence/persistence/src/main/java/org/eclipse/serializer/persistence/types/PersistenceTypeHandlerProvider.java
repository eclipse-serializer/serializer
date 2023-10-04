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

import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTypeNotPersistable;


public interface PersistenceTypeHandlerProvider<D> extends PersistenceTypeManager, PersistenceTypeHandlerEnsurer<D>
{
	public <T> PersistenceTypeHandler<D, ? super T> provideTypeHandler(Class<T> type) throws PersistenceExceptionTypeNotPersistable;

//	public PersistenceTypeHandler<D, ?> provideTypeHandler(long typeId);
	
	// must be able to act as a pure TypeHandlerEnsurer as well because of type refactoring type mismatch checks.
	@Override
	public <T> PersistenceTypeHandler<D, ? super T> ensureTypeHandler(Class<T> type)
		throws PersistenceExceptionTypeNotPersistable;

}
