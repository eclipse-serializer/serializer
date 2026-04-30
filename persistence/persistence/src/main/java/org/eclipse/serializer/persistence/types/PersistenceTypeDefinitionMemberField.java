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

import java.lang.reflect.Field;

/**
 * Runtime-bound counterpart to {@link PersistenceTypeDescriptionMemberField}. Common supertype of the
 * reflective and generic flavors; only the reflective flavor returns a non-{@code null}
 * {@link #field()}.
 */
public interface PersistenceTypeDefinitionMemberField
extends PersistenceTypeDefinitionMember, PersistenceTypeDescriptionMemberField
{
	/**
	 * The underlying Java {@link Field} for {@linkplain PersistenceTypeDefinitionMemberFieldReflective
	 * reflective} fields, or {@code null} for generic fields and for reflective fields whose declaring
	 * class or field name could not be resolved on the current runtime.
	 * <p>
	 * The default implementation returns {@code null} so that callers do not need to distinguish between
	 * field members and generic field members when they only care about the reflective case.
	 *
	 * @return the underlying field, or {@code null} if none.
	 */
	public default Field field()
	{
		/*
		 * This is actually technically superfluous and just a mere usability helper for
		 * developers who don't want to distinct between field members and generic field members.
		 */
		return null;
	}

}
