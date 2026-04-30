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
 * Pluggable hook used by foundations to register custom {@link PersistenceTypeHandler}s into a
 * {@link PersistenceCustomTypeHandlerRegistry} during setup. The registration is invoked once per
 * foundation initialization with the registry and a {@link PersistenceSizedArrayLengthController}
 * (so that handlers can apply the configured sized-array length policy).
 *
 * @param <D> the data target type.
 *
 * @see Executor
 */
@FunctionalInterface
public interface PersistenceTypeHandlerRegistration<D>
{
	/**
	 * Adds custom type handlers to the passed registry, configuring them with the given
	 * {@link PersistenceSizedArrayLengthController} where applicable.
	 *
	 * @param customTypeHandlerRegistry  the target registry.
	 * @param sizedArrayLengthController the length controller to pass to length-aware handlers.
	 */
	public void registerTypeHandlers(
		PersistenceCustomTypeHandlerRegistry<D> customTypeHandlerRegistry ,
		PersistenceSizedArrayLengthController   sizedArrayLengthController
	);


	/**
	 * Executes a {@link PersistenceTypeHandlerRegistration} against an executor-owned registry and
	 * length controller. Implementations of this interface bridge "I have a registration to run" with
	 * "I own the registry and the length controller" without forcing the caller to know about either.
	 *
	 * @param <D> the data target type.
	 */
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
