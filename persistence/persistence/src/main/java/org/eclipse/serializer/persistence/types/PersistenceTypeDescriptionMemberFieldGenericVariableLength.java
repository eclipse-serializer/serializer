package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
 * %%
 * Copyright (C) 2023 Eclipse Foundation
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static org.eclipse.serializer.util.X.mayNull;
import static org.eclipse.serializer.util.X.notNull;
import static org.eclipse.serializer.math.XMath.positive;

public interface PersistenceTypeDescriptionMemberFieldGenericVariableLength
extends PersistenceTypeDescriptionMemberFieldGeneric
{
	@Override
	public default boolean isVariableLength()
	{
		return true;
	}
	
	@Override
	public default boolean equalsStructure(final PersistenceTypeDescriptionMember other)
	{
		// the type check is the only specific thing here.
		return other instanceof PersistenceTypeDescriptionMemberFieldGenericVariableLength
			&& PersistenceTypeDescriptionMemberFieldGeneric.super.equalsStructure(other)
		;
	}
	
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMemberFieldGenericVariableLength m1,
		final PersistenceTypeDescriptionMemberFieldGenericVariableLength m2
	)
	{
		return PersistenceTypeDescriptionMember.equalDescription(m1, m2);
	}
	
	public static boolean equalStructure(
		final PersistenceTypeDescriptionMemberFieldGenericVariableLength m1,
		final PersistenceTypeDescriptionMemberFieldGenericVariableLength m2
	)
	{
		return PersistenceTypeDescriptionMember.equalStructure(m1, m2);
	}
	
	@Override
	public default PersistenceTypeDefinitionMemberFieldGenericVariableLength createDefinitionMember(
		final PersistenceTypeDefinitionMemberCreator creator
	)
	{
		return creator.createDefinitionMember(this);
	}

	

	public static PersistenceTypeDescriptionMemberFieldGenericVariableLength.Default New(
		final String typeName               ,
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return New(typeName, null, name, persistentMinimumLength, persistentMaximumLength);
	}

	public static PersistenceTypeDescriptionMemberFieldGenericVariableLength.Default New(
		final String typeName               ,
		final String qualifier              ,
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return new PersistenceTypeDescriptionMemberFieldGenericVariableLength.Default(
			 notNull(typeName),
			 mayNull(qualifier),
			 notNull(name),
			         false,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}

	public class Default
	extends PersistenceTypeDescriptionMemberFieldGeneric.Abstract
	implements PersistenceTypeDescriptionMemberFieldGenericVariableLength
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final String  typeName               ,
			final String  qualifier              ,
			final String  name                   ,
			final boolean hasReferences          ,
			final long    persistentMinimumLength,
			final long    persistentMaximumLength
		)
		{
			super(
				typeName               ,
				qualifier              ,
				name                   ,
				false                  ,
				false                  ,
				hasReferences          ,
				persistentMinimumLength,
				persistentMaximumLength
			);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void assembleTypeDescription(final PersistenceTypeDescriptionMemberAppender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}

	}

}
