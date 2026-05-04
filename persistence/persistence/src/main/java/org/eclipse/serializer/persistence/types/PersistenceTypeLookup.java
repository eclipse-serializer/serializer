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

import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionConsistency;


/**
 * Bidirectional type lookup: extends the {@link Class}&rarr;{@code typeId} direction
 * ({@link PersistenceTypeIdLookup}) with the inverse {@code typeId}&rarr;{@link Class} direction and with
 * methods for validating that an existing registration matches a {@code (typeId, type)} pair.
 * <p>
 * The natural read-only counterpart to {@link PersistenceTypeRegistry}: registries implement this lookup, so
 * any code that only needs to read but not register can take a {@link PersistenceTypeLookup} instead.
 *
 * @see PersistenceTypeIdLookup
 * @see PersistenceTypeRegistry
 */
public interface PersistenceTypeLookup extends PersistenceTypeIdLookup
{
	@Override
	public long lookupTypeId(Class<?> type);

	/**
	 * Returns the {@link Class} registered for the passed type id, or {@code null} if the id is not known.
	 *
	 * @param <T>    the inferred type of the returned class literal.
	 * @param typeId the type id to look up.
	 *
	 * @return the registered class, or {@code null} if unknown.
	 */
	public <T> Class<T> lookupType(long typeId);

	/**
	 * Checks whether the passed {@code (typeId, type)} pair matches an existing registration.
	 * <p>
	 * Returns {@code true} if the mapping is already registered exactly as passed, {@code false} if neither
	 * id nor type are registered yet (i.e. the mapping would be a fresh registration), and throws a
	 * {@link PersistenceExceptionConsistency} subclass if a partial or conflicting mapping exists (e.g. the
	 * id is registered but to a different type).
	 *
	 * @param typeId the candidate type id.
	 * @param type   the candidate type.
	 *
	 * @return {@code true} if the mapping is already registered, {@code false} if it is unknown.
	 *
	 * @throws PersistenceExceptionConsistency if the registry contains a conflicting partial mapping.
	 */
	public boolean validateTypeMapping(long typeId, Class<?> type)
		throws PersistenceExceptionConsistency;

	/**
	 * Bulk variant of {@link #validateTypeMapping(long, Class)}: returns {@code true} only if every passed
	 * mapping is already registered, {@code false} if at least one is missing, and throws if any one is in
	 * conflict.
	 *
	 * @param mappings the mappings to validate.
	 *
	 * @return {@code true} if all mappings are already registered, {@code false} if any is unknown.
	 *
	 * @throws PersistenceExceptionConsistency if the registry contains a conflicting partial mapping.
	 */
	public boolean validateTypeMappings(Iterable<? extends PersistenceTypeLink> mappings)
		throws PersistenceExceptionConsistency;

}
