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

import org.eclipse.serializer.reference.Referencing;


/**
 * Read-only handle on the application's user-defined root instance. Specializes {@link Referencing} with an
 * {@link #iterate(PersistenceFunction)} method so storers can apply a {@link PersistenceFunction} to the
 * referenced object without exposing an internal mutable reference.
 * <p>
 * Writable counterpart is {@link PersistenceRootReference}, which adds the setter side.
 *
 * @see PersistenceRootReference
 * @see PersistenceFunction
 */
public interface PersistenceRootReferencing extends Referencing<Object>
{
	@Override
	public Object get();

	/**
	 * Applies the passed {@link PersistenceFunction} to the currently referenced root, if any. Does
	 * nothing when no root is set.
	 *
	 * @param <F>      the iterator type, returned for fluent chaining.
	 * @param iterator the iterator to apply to the root.
	 *
	 * @return the same iterator that was passed in.
	 */
	public <F extends PersistenceFunction> F iterate(F iterator);

}
