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

/**
 * Field-style member derived from a Java {@link java.lang.reflect.Field} via reflection.
 * <p>
 * The {@link #qualifier()} carries the declaring class's fully qualified name, which is what
 * disambiguates same-named private fields inherited along a class hierarchy: e.g. a {@code Person}
 * sub-class that has its own private {@code name} alongside a {@code Person.name} field will produce
 * two reflective members with identical {@link #name()} but distinct {@link #declaringTypeName()}s,
 * yielding distinct {@link #identifier()}s of the form {@code com.example.Person#name} and
 * {@code com.example.Employee#name}.
 *
 * @see PersistenceTypeDescriptionMemberFieldGeneric
 */
public interface PersistenceTypeDescriptionMemberFieldReflective
extends PersistenceTypeDescriptionMemberField
{
	@Override
	public String identifier();

	/**
	 * The fully qualified name of the class on which the underlying {@link java.lang.reflect.Field} is
	 * declared. This is an alias for {@link #qualifier()} expressed in domain terms.
	 *
	 * @return the declaring class's fully qualified name.
	 */
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
	

	
	/**
	 * Type-specific overload of
	 * {@link PersistenceTypeDescriptionMember#equalDescription(PersistenceTypeDescriptionMember,
	 * PersistenceTypeDescriptionMember)}.
	 *
	 * @param m1 the first reflective field.
	 * @param m2 the second reflective field.
	 *
	 * @return {@code true} if both have equal description.
	 */
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMemberFieldReflective m1,
		final PersistenceTypeDescriptionMemberFieldReflective m2
	)
	{
		return PersistenceTypeDescriptionMember.equalDescription(m1, m2);
	}

	/**
	 * Type-specific overload of
	 * {@link PersistenceTypeDescriptionMember#equalStructure(PersistenceTypeDescriptionMember,
	 * PersistenceTypeDescriptionMember)}.
	 *
	 * @param m1 the first reflective field.
	 * @param m2 the second reflective field.
	 *
	 * @return {@code true} if both have equal structure.
	 */
	public static boolean equalStructure(
		final PersistenceTypeDescriptionMemberFieldReflective m1,
		final PersistenceTypeDescriptionMemberFieldReflective m2
	)
	{
		return PersistenceTypeDescriptionMember.equalStructure(m1, m2);
	}
	

	// (14.08.2015 TM)TODO: include Generics, Field#getGenericType
//	public String typeParameterString();
	
	
	
	/**
	 * Creates a reflective field description.
	 *
	 * @param typeName                the textual type name of the field; must not be {@code null}.
	 * @param declaringTypeName       the fully qualified name of the declaring class; must not be {@code null}.
	 * @param name                    the field's simple name; must not be {@code null}.
	 * @param isReference             whether the field is a reference (otherwise primitive).
	 * @param persistentMinimumLength the persistent length lower bound; must be positive.
	 * @param persistentMaximumLength the persistent length upper bound; must be positive.
	 *
	 * @return a new reflective field description.
	 */
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
