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

public interface PersistenceStoreHandler<D> extends PersistenceFunction, SubStoring
{
	/**
	 * The "natural" way of handling an instance as defined by the implementation.
	 * 
	 * @param <T> the type of the instance
	 * @param instance the instance to store
	 * @return the assigned object id
	 */
	@Override
	public <T> long apply(T instance);
	
	/**
	 * A way to signal to the implementation that the passed instance is supposed to be handled eagerly,
	 * meaning it shall be handled even if the handling implementation does not deem it necessary.<br>
	 * This is needed, for example, to store composition pattern instances without breaking OOP encapsulation concepts.
	 * 
	 * @param <T> the type of the instance
	 * @param instance the instance to store
	 * @return the assigned object id
	 */
	public <T> long applyEager(T instance);
	
	public <T> long apply(T instance, PersistenceTypeHandler<D, T> localTypeHandler);
	
	public <T> long applyEager(T instance, PersistenceTypeHandler<D, T> localTypeHandler);
	
	public void registerCommitListener(PersistenceCommitListener listener);
	
	public ObjectSwizzling getObjectRetriever();
	
	public Persister getPersister();
	
}
