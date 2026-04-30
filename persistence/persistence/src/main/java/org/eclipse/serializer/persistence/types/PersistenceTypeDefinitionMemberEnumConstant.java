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

import static org.eclipse.serializer.util.X.notNull;

/**
 * Runtime-bound counterpart to {@link PersistenceTypeDescriptionMemberEnumConstant}. Since enum
 * constants validate field names rather than carry a runtime field type, {@link #type()} always returns
 * {@code null}.
 */
public interface PersistenceTypeDefinitionMemberEnumConstant
extends PersistenceTypeDescriptionMemberEnumConstant, PersistenceTypeDefinitionMember
{

	/**
	 * Creates a new {@link PersistenceTypeDefinitionMemberEnumConstant} for the given persistent name.
	 *
	 * @param name the persistent enum constant name; must not be {@code null}.
	 *
	 * @return a new definition member.
	 */
	public static PersistenceTypeDefinitionMemberEnumConstant New(
		final String name
	)
	{
		return new PersistenceTypeDefinitionMemberEnumConstant.Default(
			notNull(name)
		);
	}

	public class Default
	extends PersistenceTypeDescriptionMemberEnumConstant.Default
	implements PersistenceTypeDefinitionMemberEnumConstant
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(final String enumName)
		{
			super(enumName);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		@Override
		public final Class<?> type()
		{
			// a enum constant does not have a defined type per se. It's just about validating the field names.
			return null;
		}
		
	}

}
