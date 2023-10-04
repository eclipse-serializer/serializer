package org.eclipse.serializer.persistence.binary.types;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
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

import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldReflective;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandler;
import org.eclipse.serializer.reflect.XReflect;

public interface BinaryTypeHandler<T> extends PersistenceTypeHandler<Binary, T>
{
	@Override
	public default Class<Binary> dataType()
	{
		return Binary.class;
	}
	
	public abstract class Abstract<T>
	extends PersistenceTypeHandler.Abstract<Binary, T>
	implements BinaryTypeHandler<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static final PersistenceTypeDefinitionMemberFieldReflective declaredField(
			final Class<?> declaringClass,
			final String   fieldName
		)
		{
			final Field field = XReflect.getDeclaredField(declaringClass, fieldName);
			return declaredField(field, BinaryPersistence.createFieldLengthResolver());
		}
		
		public static final PersistenceTypeDefinitionMemberFieldReflective declaredField(final Field field)
		{
			return declaredField(field, BinaryPersistence.createFieldLengthResolver());
		}
		
			
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(final Class<T> type)
		{
			super(type);
		}
		
		protected Abstract(final Class<T> type, final String typeName)
		{
			super(type, typeName);
		}
		
	}

}
