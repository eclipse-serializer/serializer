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

import java.util.function.Consumer;

/**
 * Read-only iteration contract over a registry of {@link PersistenceTypeHandler}s and
 * {@link PersistenceLegacyTypeHandler}s. The two are kept separate because legacy handlers are bound to
 * outdated type definitions and are only relevant for reading older data.
 *
 * @param <D> the data target type.
 */
public interface PersistenceTypeHandlerIterable<D>
{
	/**
	 * Iterates all current (non-legacy) type handlers.
	 *
	 * @param <C>      the iterator type.
	 * @param iterator receives each handler.
	 *
	 * @return the same iterator, for fluent chaining.
	 */
	public <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateTypeHandlers(C iterator);

	/**
	 * Iterates all registered legacy type handlers.
	 *
	 * @param <C>      the iterator type.
	 * @param iterator receives each legacy handler.
	 *
	 * @return the same iterator, for fluent chaining.
	 */
	public <C extends Consumer<? super PersistenceLegacyTypeHandler<D, ?>>> C iterateLegacyTypeHandlers(C iterator);

	/**
	 * Convenience iteration over both current and legacy handlers.
	 *
	 * @param <C>      the iterator type.
	 * @param iterator receives each handler.
	 *
	 * @return the same iterator, for fluent chaining.
	 */
	public default <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateAllTypeHandlers(final C iterator)
	{
		this.iterateTypeHandlers(iterator);
		this.iterateLegacyTypeHandlers(iterator);

		return iterator;
	}
}
