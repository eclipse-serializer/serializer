package org.eclipse.serializer.persistence.exceptions;

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

public class PersistenceExceptionStoringDisabled extends PersistenceException
{
	@Override
	public String getMessage()
	{
		return "Storing is not enabled! " +
			(super.getMessage() != null ? " Details: " + super.getMessage() : "");
	}
	
}
