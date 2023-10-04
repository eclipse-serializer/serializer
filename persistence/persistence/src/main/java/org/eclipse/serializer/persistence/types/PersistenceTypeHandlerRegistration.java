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

@FunctionalInterface
public interface PersistenceTypeHandlerRegistration<D>
{
	public void registerTypeHandlers(
		PersistenceCustomTypeHandlerRegistry<D> customTypeHandlerRegistry ,
		PersistenceSizedArrayLengthController   sizedArrayLengthController
	);
	
	
	@FunctionalInterface
	public static interface Executor<D>
	{
		/**
		 * Executes the passed {@link PersistenceTypeHandlerRegistration} logic while supplying this instance's
		 * {@link PersistenceCustomTypeHandlerRegistry} and {@link PersistenceSizedArrayLengthController} instances.
		 * The passed instance itself will not be referenced after the method exits.
		 * 
		 * @param typeHandlerRegistration the {@link PersistenceTypeHandlerRegistration} to be executed.
		 */
		public void executeTypeHandlerRegistration(PersistenceTypeHandlerRegistration<D> typeHandlerRegistration);
		
	}
	
}
