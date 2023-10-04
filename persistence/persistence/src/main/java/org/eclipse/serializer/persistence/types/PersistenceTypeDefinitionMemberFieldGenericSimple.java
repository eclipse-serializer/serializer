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

import static org.eclipse.serializer.util.X.mayNull;
import static org.eclipse.serializer.util.X.notNull;
import static org.eclipse.serializer.math.XMath.positive;

public interface PersistenceTypeDefinitionMemberFieldGenericSimple
extends PersistenceTypeDefinitionMemberFieldGeneric, PersistenceTypeDescriptionMemberFieldGenericSimple
{
	@Override
	public default PersistenceTypeDefinitionMemberFieldGenericSimple copyForName(final String name)
	{
		return this.copyForName(this.qualifier(), name);
	}
	
	@Override
	public PersistenceTypeDefinitionMemberFieldGenericSimple copyForName(String qualifier, String name);
	
	
	
	public static PersistenceTypeDefinitionMemberFieldGenericSimple New(
		final String   typeName               ,
		final String   qualifier              ,
		final String   name                   ,
		final Class<?> type                   ,
		final boolean  isReference            ,
		final long     persistentMinimumLength,
		final long     persistentMaximumLength
	)
	{
		return new PersistenceTypeDefinitionMemberFieldGenericSimple.Default(
			 notNull(typeName)               ,
			 mayNull(qualifier)              ,
			 notNull(name)                   ,
			 mayNull(type)                   ,
			         isReference             ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}
	
	public class Default
	extends PersistenceTypeDescriptionMemberFieldGeneric.Abstract
	implements PersistenceTypeDefinitionMemberFieldGenericSimple
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Class<?> type;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final String   typeName           ,
			final String   qualifier          ,
			final String   name               ,
			final Class<?> type               ,
			final boolean  isReference        ,
			final long     persistentMinLength,
			final long     persistentMaxLength
		)
		{
			super(typeName, qualifier, name, isReference, !isReference, isReference, persistentMinLength, persistentMaxLength);
			this.type = type;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
			
		@Override
		public final Class<?> type()
		{
			return this.type;
		}

		@Override
		public void assembleTypeDescription(final PersistenceTypeDescriptionMemberAppender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}

		@Override
		public PersistenceTypeDefinitionMemberFieldGenericSimple copyForName(final String qualifier, final String name)
		{
			return new PersistenceTypeDefinitionMemberFieldGenericSimple.Default(
				this.typeName()               ,
				qualifier                     ,
				name                          ,
				this.type                     ,
				this.isReference()            ,
				this.persistentMinimumLength(),
				this.persistentMaximumLength()
			);
		}

	}

}
