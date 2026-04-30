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


/**
 * One-stop provider that combines type-id assignment ({@link PersistenceTypeManager}) with handler
 * creation / lookup ({@link PersistenceTypeHandlerEnsurer}).
 * <p>
 * {@link #provideTypeHandler(Class)} ensures both a typeId is allocated for the type <b>and</b> a
 * matching handler is initialized with that typeId &mdash; the canonical entry point used by the
 * persister when first encountering a runtime class. The inherited
 * {@link #ensureTypeHandler(Class)} is the pure "give me a handler, don't allocate a typeId" variant
 * needed during refactoring-driven type-mismatch checks.
 *
 * @param <D> the data target type.
 */
public interface PersistenceTypeHandlerProvider<D> extends PersistenceTypeManager, PersistenceTypeHandlerEnsurer<D>
{
	/**
	 * Ensures a typeId is allocated for {@code type} and returns a handler initialized with that
	 * typeId. For types handled via "abstract type" mappings (where the runtime class differs from the
	 * handler's own type), no new typeId is allocated.
	 *
	 * @param <T>  the type to provide a handler for.
	 * @param type the runtime class.
	 *
	 * @return an initialized handler for {@code type}.
	 *
	 * @throws PersistenceExceptionTypeNotPersistable if {@code type} cannot be persisted.
	 */
	public <T> PersistenceTypeHandler<D, ? super T> provideTypeHandler(Class<T> type) throws PersistenceExceptionTypeNotPersistable;

//	public PersistenceTypeHandler<D, ?> provideTypeHandler(long typeId);

	// must be able to act as a pure TypeHandlerEnsurer as well because of type refactoring type mismatch checks.
	@Override
	public <T> PersistenceTypeHandler<D, ? super T> ensureTypeHandler(Class<T> type)
		throws PersistenceExceptionTypeNotPersistable;

}
