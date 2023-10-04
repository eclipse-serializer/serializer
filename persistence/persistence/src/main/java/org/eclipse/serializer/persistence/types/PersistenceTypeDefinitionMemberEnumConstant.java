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

public interface PersistenceTypeDefinitionMemberEnumConstant
extends PersistenceTypeDescriptionMemberEnumConstant, PersistenceTypeDefinitionMember
{
	
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
