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

import java.lang.reflect.Field;

/**
 * Predicate marking fields whose referenced graph should be stored <em>eagerly</em>, i.e. always written along
 * with the enclosing instance even when a lazy storer would otherwise leave them unchanged. Used by storers
 * to override their default lazy traversal on a per-field basis.
 *
 * @see PersistenceFieldEvaluator
 */
@FunctionalInterface
public interface PersistenceEagerStoringFieldEvaluator
{
	/**
	 * Whether the passed field of {@code t} should be treated as eagerly stored.
	 *
	 * @param t the declaring (or owning) type.
	 * @param u the field to evaluate.
	 *
	 * @return {@code true} if the field is eagerly stored.
	 */
	public boolean isEagerStoring(Class<?> t, Field u);

}
