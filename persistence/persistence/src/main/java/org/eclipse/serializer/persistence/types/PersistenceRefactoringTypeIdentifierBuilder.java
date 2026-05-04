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

import org.eclipse.serializer.collections.HashEnum;
import org.eclipse.serializer.collections.types.XEnum;

/**
 * Strategy for turning a {@link PersistenceTypeDescription} into the textual identifier used as a key in the
 * user-supplied refactoring map ({@link PersistenceRefactoringMapping}). Several builders are typically
 * tried in priority order: a refactoring rule matches as soon as one builder produces the same identifier on
 * both sides of the rename.
 * <p>
 * Bundled builders, in descending priority:
 * <ul>
 * <li>{@link PersistenceTypeDescription#buildTypeIdentifier(PersistenceTypeDescription)} &mdash;
 * {@code <typeId>:<typeName>}; unambiguous because it includes the type id.</li>
 * <li>{@link PersistenceTypeDescription#typeName()} &mdash; the bare type name; ambiguous if multiple
 * historical definitions share it.</li>
 * </ul>
 *
 * @see PersistenceRefactoringMemberIdentifierBuilder
 * @see PersistenceRefactoringMapping
 */
public interface PersistenceRefactoringTypeIdentifierBuilder
{
	/**
	 * Builds the textual identifier for the passed type description.
	 *
	 * @param typeDescription the description to identify.
	 *
	 * @return the textual identifier.
	 */
	public String buildTypeIdentifier(PersistenceTypeDescription typeDescription);



	/**
	 * Default builder set for refactoring lookups, in descending priority: type-id-qualified name first
	 * (unambiguous), bare type name second.
	 *
	 * @return the default type identifier builders, in priority order.
	 */
	public static XEnum<? extends PersistenceRefactoringTypeIdentifierBuilder> createDefaultRefactoringLegacyTypeIdentifierBuilders()
	{
		/* Identifier builders in descending order of priority:
		 * - [TypeId]:[TypeName]
		 * - [TypeName]
		 * Note that the first one is the only one that is unambiguous for all cases.
		 */
		return HashEnum.New(
			PersistenceTypeDescription::buildTypeIdentifier,
			PersistenceTypeDescription::typeName
		);
	}

}
