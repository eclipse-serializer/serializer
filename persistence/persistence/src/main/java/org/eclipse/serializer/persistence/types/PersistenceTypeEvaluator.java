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
 * Predicate deciding whether a {@link Class} is eligible for persistence at all. Consulted by
 * {@link PersistenceTypeAnalyzer#isUnpersistable(Class)} early in handler discovery: if a type fails this
 * check, it never reaches field analysis or handler creation and is reported as unpersistable.
 * <p>
 * Distinct from {@link PersistenceFieldEvaluator}, which decides per-field rather than per-type.
 *
 * @see PersistenceTypeAnalyzer
 * @see PersistenceFieldEvaluator
 */
@FunctionalInterface
public interface PersistenceTypeEvaluator
{
	/**
	 * Whether instances of {@code type} can be persisted.
	 *
	 * @param type the type to evaluate.
	 *
	 * @return {@code true} if the type is persistable.
	 */
	public boolean isPersistableType(Class<?> type);
}
