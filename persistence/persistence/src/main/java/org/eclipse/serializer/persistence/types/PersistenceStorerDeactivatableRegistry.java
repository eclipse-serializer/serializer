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

public interface PersistenceStorerDeactivatableRegistry
{
	/**
	 * Register a PersistenceStorerDeactivatable instance to the StorerModeController.
	 * 
	 * @param deactivatableStorer the PersistenceStorerDeactivatable to be registered.
	 * 
	 * @return the registered instance.
	 */
	public PersistenceStorerDeactivatable register(PersistenceStorerDeactivatable deactivatableStorer);

	/**
	 * Returns true if there are any registered PersistenceStorerDeactivatable instances.
	 * 
	 * @return true or false.
	 */
	public boolean hasRegisteredStorers();

	/**
	 * Cleanup all no more valid (garbage collected) Storer instances.
	 */
	public void clean();
}
