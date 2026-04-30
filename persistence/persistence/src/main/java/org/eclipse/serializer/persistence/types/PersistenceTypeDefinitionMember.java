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
 * A {@link PersistenceTypeDescriptionMember} additionally bound to its runtime representation.
 * <p>
 * The runtime binding is one extra piece of information per member: the resolved {@link Class} for
 * {@linkplain PersistenceTypeDescriptionMemberField field-style} members (or {@code null} if it
 * cannot be resolved), and an optional {@link #runtimeQualifier()} that may differ from the dictionary
 * {@link #qualifier()} after the declaring class has been renamed.
 * <p>
 * This is the member-level counterpart to the type-level {@link PersistenceTypeDefinition} vs.
 * {@link PersistenceTypeDescription} distinction: descriptions live in dictionary text, definitions
 * live in the running JVM.
 *
 * @see PersistenceTypeDescriptionMember
 * @see PersistenceTypeDefinition
 */
public interface PersistenceTypeDefinitionMember extends PersistenceTypeDescriptionMember
{
	/**
	 * @return the runtime type used by this description member, if possible. Otherwise {@code null}.
	 */
	public Class<?> type();

	/**
	 * The current-runtime equivalent of {@link #qualifier()}: e.g. for reflective fields the runtime
	 * declaring-class name after a class rename has been applied via the refactoring mapping. May
	 * differ from {@link #qualifier()}, which always preserves the original dictionary value. The
	 * default implementation returns {@code null} for members that don't have a runtime qualifier.
	 *
	 * @return the runtime qualifier, or {@code null}.
	 */
	public default String runtimeQualifier()
	{
		return null;
	}

}
