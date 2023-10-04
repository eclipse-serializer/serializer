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
 * This type extends the {@link PersistenceTypeHandler} type only by the following reflection contract:
 * <p>
 * A class implementing this type must solely handle actual class {@link Field}s and their values.
 * It may not use any custom persistent state or logic like e.g. {@link PersistenceTypeHandlerCustom}.
 *
 * 
 * @param <D> the data type
 * @param <T> the handled type
 * 
 * @see PersistenceTypeHandlerCustom
 */
public interface PersistenceTypeHandlerGeneric<D, T> extends PersistenceTypeHandler<D, T>
{
	// typing interface only (so far)
}
