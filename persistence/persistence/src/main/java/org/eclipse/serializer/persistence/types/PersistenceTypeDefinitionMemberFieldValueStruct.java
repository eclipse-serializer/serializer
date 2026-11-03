package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
 * %%
 * Copyright (C) 2023 - 2026 MicroStream Software
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

import java.lang.reflect.Field;

import org.eclipse.serializer.collections.types.XGettingSequence;

/**
 * Runtime-bound counterpart of {@link PersistenceTypeDescriptionMemberFieldValueStruct}: it additionally
 * carries the owner's {@link Field} and the inlined type, and its {@link #members()} carry the inlined
 * type's own fields, which is what the binary handler needs to read the inlined content out of an instance
 * and to reconstruct one from it.
 * <p>
 * The runtime bindings are optional, exactly as for a plain reflective field: a definition derived from a
 * dictionary describes a layout that may no longer have a matching class or field.
 *
 * @see PersistenceTypeDescriptionMemberFieldValueStruct
 */
public interface PersistenceTypeDefinitionMemberFieldValueStruct
extends PersistenceTypeDescriptionMemberFieldValueStruct, PersistenceTypeDefinitionMemberFieldReflective
{
	@Override
	public XGettingSequence<? extends PersistenceTypeDefinitionMemberField> members();

	/**
	 * Full-control factory for an inlined field definition. Use {@link #New(Field, XGettingSequence)} for the
	 * common case where the field is known at runtime.
	 *
	 * @param runtimeDeclaringClass the runtime declaring-class name; may be {@code null}.
	 * @param declaringClass        the runtime declaring class; may be {@code null}.
	 * @param field                 the runtime {@link Field}; may be {@code null}.
	 * @param type                  the runtime field type; may be {@code null}.
	 * @param typeName              the dictionary type name; must not be {@code null}.
	 * @param name                  the simple field name; must not be {@code null}.
	 * @param declaringTypeName     the dictionary declaring-type name; must not be {@code null}.
	 * @param members               the inlined layout's members, in persistent order; must not be {@code null}.
	 *
	 * @return a new inlined field definition.
	 */
	public static PersistenceTypeDefinitionMemberFieldValueStruct New(
		final String                                                                    runtimeDeclaringClass,
		final Class<?>                                                                  declaringClass       ,
		final Field                                                                     field                ,
		final Class<?>                                                                  type                 ,
		final String                                                                    typeName             ,
		final String                                                                    name                 ,
		final String                                                                    declaringTypeName    ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMemberField> members
	)
	{
		return new PersistenceTypeDefinitionMemberFieldValueStruct.Default(
			mayNull(runtimeDeclaringClass),
			mayNull(declaringClass)       ,
			mayNull(field)                ,
			mayNull(type)                 ,
			notNull(typeName)             ,
			notNull(name)                 ,
			notNull(declaringTypeName)    ,
			notNull(members)
		);
	}

	/**
	 * Convenience factory deriving every name and class binding from the passed {@link Field}.
	 *
	 * @param field   the owner's field holding the inlined value; must not be {@code null}.
	 * @param members the inlined type's fields, in persistent order; must not be {@code null}.
	 *
	 * @return a new inlined field definition.
	 */
	public static PersistenceTypeDefinitionMemberFieldValueStruct New(
		final Field                                                                     field  ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMemberField> members
	)
	{
		return New(
			field.getDeclaringClass().getName(),
			field.getDeclaringClass()          ,
			field                              ,
			field.getType()                    ,
			field.getType().getName()          ,
			field.getName()                    ,
			field.getDeclaringClass().getName(),
			members
		);
	}

	public final class Default
	extends PersistenceTypeDescriptionMemberFieldValueStruct.Default
	implements PersistenceTypeDefinitionMemberFieldValueStruct
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String   runtimeDeclaringClassName;
		private final Class<?> declaringClass           ;
		private final Field    field                    ;
		private final Class<?> type                     ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final String                                                                    runtimeDeclClassName,
			final Class<?>                                                                  declaringClass      ,
			final Field                                                                     field               ,
			final Class<?>                                                                  type                ,
			final String                                                                    typeName            ,
			final String                                                                    name                ,
			final String                                                                    declaringTypeName   ,
			final XGettingSequence<? extends PersistenceTypeDefinitionMemberField> members
		)
		{
			super(typeName, declaringTypeName, name, members);

			this.runtimeDeclaringClassName = runtimeDeclClassName;
			this.declaringClass            = declaringClass      ;
			this.field                     = field               ;
			this.type                      = type                ;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@SuppressWarnings("unchecked")
		@Override
		public XGettingSequence<? extends PersistenceTypeDefinitionMemberField> members()
		{
			// the constructor accepts nothing but definition members, so the narrowing always holds
			return (XGettingSequence<? extends PersistenceTypeDefinitionMemberField>)super.members();
		}

		@Override
		public String runtimeDeclaringClassName()
		{
			return this.runtimeDeclaringClassName;
		}

		@Override
		public Class<?> declaringClass()
		{
			return this.declaringClass;
		}

		@Override
		public Field field()
		{
			return this.field;
		}

		@Override
		public Class<?> type()
		{
			return this.type;
		}

	}

}
