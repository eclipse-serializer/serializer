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
 * Read-only lookup of {@link PersistenceTypeHandler}s by instance, by {@link Class}, or by typeId.
 * <p>
 * Returns {@code null} when no handler is registered for the requested key &mdash; this interface is
 * pure lookup and never causes new handlers to be created. For "ensure" semantics (lookup-or-create),
 * see {@link PersistenceTypeHandlerProvider} / {@link PersistenceTypeHandlerEnsurer}.
 *
 * @param <D> the data target type.
 */
public interface PersistenceTypeHandlerLookup<D> extends PersistenceTypeLookup
{
	/**
	 * Looks up the handler whose handled type matches (or is a super-type of) the runtime class of the
	 * passed instance.
	 *
	 * @param <T>      the static type of the instance.
	 * @param instance the instance to look up by.
	 *
	 * @return the matching handler, or {@code null} if none is registered.
	 */
	public <T> PersistenceTypeHandler<D, ? super T> lookupTypeHandler(T instance);

	/**
	 * Looks up the handler whose handled type is or is a super-type of the passed class.
	 *
	 * @param <T>  the type to look up.
	 * @param type the class to look up by.
	 *
	 * @return the matching handler, or {@code null} if none is registered.
	 */
	public <T> PersistenceTypeHandler<D, ? super T> lookupTypeHandler(Class<T> type);

	/**
	 * Looks up the handler bound to the passed typeId.
	 *
	 * @param typeId the typeId to look up by.
	 *
	 * @return the matching handler, or {@code null} if none is registered.
	 */
	public PersistenceTypeHandler<D, ?> lookupTypeHandler(long typeId);

}
