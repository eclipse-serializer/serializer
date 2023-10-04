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



public interface PersistenceTypeDefinitionMemberFieldGeneric
extends PersistenceTypeDefinitionMemberField, PersistenceTypeDescriptionMemberFieldGeneric
{
	public default PersistenceTypeDefinitionMemberFieldGeneric copyForName(final String name)
	{
		return this.copyForName(this.qualifier(), name);
	}
	
	public PersistenceTypeDefinitionMemberFieldGeneric copyForName(String qualifier, String name);
}
