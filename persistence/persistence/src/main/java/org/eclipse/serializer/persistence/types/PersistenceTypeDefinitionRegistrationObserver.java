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
 * Callback invoked whenever a new {@link PersistenceTypeDefinition} is registered with the active
 * {@link PersistenceTypeDictionary}. Useful for keeping side-stores (logs, mirrored registries,
 * downstream caches) in sync with type-dictionary growth.
 * <p>
 * The observer is called synchronously from the registration call site, so implementations must be
 * cheap and must not block on locks held by the registry.
 */
@FunctionalInterface
public interface PersistenceTypeDefinitionRegistrationObserver
{
	/**
	 * Invoked once for every newly registered {@link PersistenceTypeDefinition}.
	 *
	 * @param typeDefinition the just-registered definition.
	 */
	public void observeTypeDefinitionRegistration(PersistenceTypeDefinition typeDefinition);

}
