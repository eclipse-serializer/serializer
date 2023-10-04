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

public interface PersistenceTypeDefinitionMember extends PersistenceTypeDescriptionMember
{
	/**
	 * @return the runtime type used by this description member, if possible. Otherwise {@code null}.
	 */
	public Class<?> type();
	
	public default String runtimeQualifier()
	{
		return null;
	}

}
