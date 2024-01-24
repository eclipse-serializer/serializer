package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * microstream-persistence
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

/**
 * This type extends the {@link PersistenceTypeHandler} type only by the following reflection contract:<p>
 * A class implementing this type can use arbitrary logic to translate instances of the handled type to
 * their persistent form and back.
 *
 * 
 * @param <D> the data type
 * @param <T> the handled type
 * 
 * @see PersistenceTypeHandlerGeneric
 */
public interface PersistenceTypeHandlerCustom<D, T> extends PersistenceTypeHandler<D, T>
{
	// typing interface only (so far)
}
