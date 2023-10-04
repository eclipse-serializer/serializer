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
 * Alias type to concretely identify the task of evaluating a {@link Field}'s persistability
 *
 * 
 */
@FunctionalInterface
public interface PersistenceFieldEvaluator
{
	public boolean applies(Class<?> entityType, Field field);

}
