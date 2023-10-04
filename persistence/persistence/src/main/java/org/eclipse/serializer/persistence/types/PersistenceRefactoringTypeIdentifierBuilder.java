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

public interface PersistenceRefactoringTypeIdentifierBuilder
{
	public String buildTypeIdentifier(PersistenceTypeDescription typeDescription);
	
	
	
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
