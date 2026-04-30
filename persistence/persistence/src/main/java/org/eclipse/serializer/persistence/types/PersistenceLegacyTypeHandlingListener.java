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
 * Listener notified whenever a legacy-handler-loaded instance is created during loading.
 * <p>
 * Receives the assigned objectId, the freshly created instance, the legacy type definition the data
 * was originally written under, and the current handler that legacy data was bound to. Useful for
 * diagnostic logging, migration auditing, or for triggering follow-up rewrites that store the loaded
 * instance through the current handler so its persisted form is upgraded on the next commit.
 * <p>
 * Only the basic "instance was created from legacy data" callback is provided here. More elaborate
 * listening (before-create / before-update / after-complete) is better expressed by wrapping the
 * legacy handler itself via a custom {@link PersistenceLegacyTypeHandlerCreator}.
 *
 * @param <D> the data target type.
 */
public interface PersistenceLegacyTypeHandlingListener<D>
{
	/**
	 * Invoked once per legacy-loaded instance, immediately after it is created from the persisted
	 * legacy form.
	 *
	 * @param <T>                   the runtime type.
	 * @param objectId              the objectId assigned to the instance.
	 * @param instance              the freshly created instance.
	 * @param legacyTypeDescription the legacy type definition the data was originally written under.
	 * @param currentTypeHandler    the current handler the legacy data was re-bound to.
	 */
	public <T> void registerLegacyTypeHandlingCreation(
		long                         objectId             ,
		T                            instance             ,
		PersistenceTypeDefinition    legacyTypeDescription,
		PersistenceTypeHandler<D, T> currentTypeHandler
	);

	/* note:
	 * further listening wishes (before creation, before/after update or completion, or whatever)
	 * can be implemented by using a LegacyTypeHandler wrapper implementing inserted by a
	 * wrapping LegacyTypeHandlerCreator. That approach makes this explicit listener almost redundant,
	 * but it is a little easier and nicer to have the basic creation listening in this concrete way.
	 * All beyond-basic needs are better implemented with the wrapping approach.
	 */
}
