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
import static org.eclipse.serializer.math.XMath.positive;

public interface PersistenceTypeDescriptionMemberFieldReflective
extends PersistenceTypeDescriptionMemberField
{
	@Override
	public String identifier();
	
	public default String declaringTypeName()
	{
		return this.qualifier();
	}
	
	@Override
	public default PersistenceTypeDefinitionMemberFieldReflective createDefinitionMember(
		final PersistenceTypeDefinitionMemberCreator creator
	)
	{
		return creator.createDefinitionMember(this);
	}
	

	
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMemberFieldReflective m1,
		final PersistenceTypeDescriptionMemberFieldReflective m2
	)
	{
		return PersistenceTypeDescriptionMember.equalDescription(m1, m2);
	}
	
	public static boolean equalStructure(
		final PersistenceTypeDescriptionMemberFieldReflective m1,
		final PersistenceTypeDescriptionMemberFieldReflective m2
	)
	{
		return PersistenceTypeDescriptionMember.equalStructure(m1, m2);
	}
	

	// (14.08.2015 TM)TODO: include Generics, Field#getGenericType
//	public String typeParameterString();
	
	
	
	public static PersistenceTypeDescriptionMemberFieldReflective New(
		final String  typeName               ,
		final String  declaringTypeName      ,
		final String  name                   ,
		final boolean isReference            ,
		final long    persistentMinimumLength,
		final long    persistentMaximumLength
	)
	{
		return new PersistenceTypeDescriptionMemberFieldReflective.Default(
			 notNull(typeName)               ,
			 notNull(declaringTypeName)      ,
			 notNull(name)                   ,
			         isReference             ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}
	
	public class Default
	extends PersistenceTypeDescriptionMemberField.Abstract
	implements PersistenceTypeDescriptionMemberFieldReflective
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final String  typeName           ,
			final String  declaringTypeName  ,
			final String  name               ,
			final boolean isReference        ,
			final long    persistentMinLength,
			final long    persistentMaxLength
		)
		{
			super(
				typeName           ,
				declaringTypeName  ,
				name               ,
				isReference        ,
				!isReference       ,
				isReference        ,
				persistentMinLength,
				persistentMaxLength
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
