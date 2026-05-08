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
 * Supplies the {@link PersistenceRootReference} used by the persistence layer together with the
 * {@link PersistenceTypeHandler} that knows how to read and write it. Pluggable so the platform binding
 * (e.g. the binary layer) can install its own custom handler for the reference type without subclassing the
 * foundation.
 *
 * @param <D> the persistence data type passed through to the handler.
 *
 * @see PersistenceRootReference
 * @see PersistenceTypeHandler
 */
public interface PersistenceRootReferenceProvider<D>
{
	/**
	 * Returns the {@link PersistenceRootReference} that should be installed as the application's root
	 * reference.
	 *
	 * @return the root reference.
	 */
	public PersistenceRootReference provideRootReference();

	/**
	 * Returns the {@link PersistenceTypeHandler} that knows how to read and write the supplied root
	 * reference type. The handler is registered against the passed object registry so it can resolve
	 * referenced ids during loading.
	 *
	 * @param globalRegistry the global object registry the handler will consult.
	 *
	 * @return the type handler for the root reference.
	 */
	public PersistenceTypeHandler<D, ? extends PersistenceRootReference> provideTypeHandler(
		PersistenceObjectRegistry globalRegistry
	);
}
