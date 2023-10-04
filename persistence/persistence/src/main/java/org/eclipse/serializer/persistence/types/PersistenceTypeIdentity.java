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


public interface PersistenceTypeIdentity extends PersistenceTypeIdOwner
{
	@Override
	public long typeId();

	public String typeName();


	public static int hashCode(final PersistenceTypeIdentity typeIdentity)
	{
		return Long.hashCode(typeIdentity.typeId()) & typeIdentity.typeName().hashCode();
	}

	public static boolean equals(
		final PersistenceTypeIdentity ti1,
		final PersistenceTypeIdentity ti2
	)
	{
		return ti1 == ti2
			|| ti1 != null && ti2 != null
			&& ti1.typeId() == ti2.typeId()
			&& ti1.typeName().equals(ti2.typeName())
		;
	}

}
