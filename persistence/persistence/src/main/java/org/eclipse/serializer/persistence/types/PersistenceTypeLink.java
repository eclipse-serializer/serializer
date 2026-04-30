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
 * Pairs a {@link PersistenceTypeIdOwner#typeId() typeId} with a runtime {@link Class}, i.e. the minimal
 * binding between the persistence-internal type identifier and the JVM-side type it represents.
 * <p>
 * This abstraction sits between {@link PersistenceTypeIdOwner} (typeId only) and the heavier
 * {@link PersistenceTypeDefinition} (typeId, type name, members, runtime class). Code paths that need
 * to associate a typeId with a runtime class &mdash; without caring about textual name, members or any
 * other dictionary-level detail &mdash; depend on this interface so they can accept lighter implementers
 * such as type-handler instances.
 * <p>
 * {@link #type()} may return {@code null} when the link points to a type that has been mapped as deleted
 * or is otherwise not loadable on the current runtime.
 *
 * @see PersistenceTypeIdOwner
 * @see PersistenceTypeDefinition
 */
public interface PersistenceTypeLink extends PersistenceTypeIdOwner
{
	@Override
	public long     typeId();

	/**
	 * The runtime {@link Class} this link is bound to, or {@code null} if no runtime counterpart exists.
	 *
	 * @return the runtime class, or {@code null}.
	 */
	public Class<?> type();

}
