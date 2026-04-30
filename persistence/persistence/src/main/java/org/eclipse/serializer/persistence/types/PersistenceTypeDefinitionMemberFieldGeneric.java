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
 * Runtime-bound counterpart to {@link PersistenceTypeDescriptionMemberFieldGeneric}. Adds a
 * {@link #copyForName(String, String) copyForName} contract that lets callers obtain a renamed copy of
 * a generic member &mdash; this is what custom type handlers use when defining fields whose
 * dictionary names depend on context (e.g. when nesting members inside a complex generic field).
 */
public interface PersistenceTypeDefinitionMemberFieldGeneric
extends PersistenceTypeDefinitionMemberField, PersistenceTypeDescriptionMemberFieldGeneric
{
	/**
	 * Convenience overload of {@link #copyForName(String, String)} that keeps the existing qualifier.
	 *
	 * @param name the new simple name.
	 *
	 * @return a renamed copy of this member.
	 */
	public default PersistenceTypeDefinitionMemberFieldGeneric copyForName(final String name)
	{
		return this.copyForName(this.qualifier(), name);
	}

	/**
	 * Returns a copy of this member with the passed qualifier and simple name. All other attributes
	 * (type name, length range, runtime type binding) are preserved.
	 *
	 * @param qualifier the new qualifier; may be {@code null}.
	 * @param name      the new simple name.
	 *
	 * @return a renamed copy of this member.
	 */
	public PersistenceTypeDefinitionMemberFieldGeneric copyForName(String qualifier, String name);
}
