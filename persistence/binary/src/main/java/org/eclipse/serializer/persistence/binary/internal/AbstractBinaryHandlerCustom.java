package org.eclipse.serializer.persistence.binary.internal;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
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

import java.lang.reflect.Field;
import java.util.function.Consumer;

import org.eclipse.serializer.util.X;
import org.eclipse.serializer.collections.XArrays;
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.collections.types.XImmutableEnum;
import org.eclipse.serializer.collections.types.XImmutableSequence;
import org.eclipse.serializer.exceptions.NoSuchFieldRuntimeException;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.binary.types.BinaryPersistence;
import org.eclipse.serializer.persistence.binary.types.BinaryTypeHandler;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.persistence.types.PersistenceFunction;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMember;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldGeneric;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldGenericComplex;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldGenericSimple;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldGenericVariableLength;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionMember;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionMemberFieldGeneric;
import org.eclipse.serializer.reflect.XReflect;

/**
 * 
 *
 * @param <T> the handled type
 */
public abstract class AbstractBinaryHandlerCustom<T>
extends BinaryTypeHandler.Abstract<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final XImmutableSequence<PersistenceTypeDefinitionMemberFieldGeneric>
	defineValueType(final Class<?> valueType)
	{
		return X.Constant(CustomField(valueType, "value"));
	}
	
	public static final PersistenceTypeDefinitionMemberFieldGenericSimple CustomField(
		final Class<?> type,
		final String   name
	)
	{
		return CustomField(type, null, name);
	}
	
	public static final PersistenceTypeDefinitionMemberFieldGenericSimple CustomField(
		final Class<?> type     ,
		final String   qualifier,
		final String   name
	)
	{
		return PersistenceTypeDefinitionMemberFieldGenericSimple.New(
			type.getName(),
			qualifier,
			name,
			type,
			!type.isPrimitive(),
			BinaryPersistence.resolveFieldBinaryLength(type),
			BinaryPersistence.resolveFieldBinaryLength(type)
		);
	}
	
	public static final PersistenceTypeDefinitionMemberFieldGeneric chars(final String name)
	{
		return PersistenceTypeDefinitionMemberFieldGenericVariableLength.Chars(
			name,
			Binary.binaryListMinimumLength(),
			Binary.binaryListMaximumLength()
		);
	}

	public static final PersistenceTypeDefinitionMemberFieldGeneric bytes(final String name)
	{
		return PersistenceTypeDefinitionMemberFieldGenericVariableLength.Bytes(
			name,
			Binary.binaryListMinimumLength(),
			Binary.binaryListMaximumLength()
		);
	}

	public static final XImmutableSequence<PersistenceTypeDefinitionMemberFieldGeneric>
	CustomFields(final PersistenceTypeDefinitionMemberFieldGeneric... customFields)
	{
		return X.ConstList(customFields);
	}

	public static final PersistenceTypeDefinitionMemberFieldGenericComplex
	Complex(
		final String                                          name        ,
		final PersistenceTypeDescriptionMemberFieldGeneric... customFields
	)
	{
		return PersistenceTypeDefinitionMemberFieldGenericComplex.New(
			name,
			X.ConstList(customFields),
			Binary.binaryListMinimumLength(),
			Binary.binaryListMaximumLength()
		);
	}
	
	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberFieldGeneric> SizedArrayFields(
		final PersistenceTypeDefinitionMemberFieldGeneric... preHeaderFields
	)
	{
		return SimpleArrayFields(
			XArrays.add(
				preHeaderFields,
				AbstractBinaryHandlerCustom.CustomField(long.class, "capacity")
			)
		);
	}

	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberFieldGeneric> SimpleArrayFields(
		final PersistenceTypeDefinitionMemberFieldGeneric... preHeaderFields
	)
	{
		return AbstractBinaryHandlerCustom.CustomFields(
			XArrays.add(
				preHeaderFields,
				AbstractBinaryHandlerCustom.Complex("elements",
					AbstractBinaryHandlerCustom.CustomField(Object.class, "element")
				)
			)
		);
	}
	
	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberFieldGeneric> keyValuesFields(
		final PersistenceTypeDefinitionMemberFieldGeneric... preHeaderFields
	)
	{
		return AbstractBinaryHandlerCustom.CustomFields(
			XArrays.add(
				preHeaderFields,
				AbstractBinaryHandlerCustom.Complex("elements",
					AbstractBinaryHandlerCustom.CustomField(Object.class, "key"),
					AbstractBinaryHandlerCustom.CustomField(Object.class, "value")
				)
			)
		);
	}
		
	
	protected static final Field getInstanceFieldOfType(
		final Class<?> declaringType,
		final Class<?> fieldType
	)
		throws NoSuchFieldRuntimeException
	{
		return XReflect.setAccessible(
			XReflect.getInstanceFieldOfType(declaringType, fieldType)
		);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private XImmutableEnum<? extends PersistenceTypeDefinitionMember> members;
	
	private long binaryLengthMinimum, binaryLengthMaximum;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected AbstractBinaryHandlerCustom(final Class<T> type)
	{
		this(type, null);
	}

	protected AbstractBinaryHandlerCustom(
		final Class<T>                                                    type   ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
	)
	{
		this(type, deriveTypeName(type), members);
	}
	
	protected AbstractBinaryHandlerCustom(
		final Class<T>                                                    type    ,
		final String                                                      typeName,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
	)
	{
		super(type, typeName);
		this.members = validateAndImmure(members);
		this.calculcateBinaryLengths();
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	protected void calculcateBinaryLengths()
	{
		if(this.members == null)
		{
			// members may be null to allow delayed on-demand BinaryField initialization.
			return;
		}
		
		this.binaryLengthMinimum = PersistenceTypeDescriptionMember.calculatePersistentMinimumLength(0, this.members);
		this.binaryLengthMaximum = PersistenceTypeDescriptionMember.calculatePersistentMaximumLength(0, this.members);
	}
	
	@Override
	public boolean isPrimitiveType()
	{
		return false;
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
	{
		// Except some special types (primitive definition and enums), there are only instance members.
		return this.instanceMembers();
	}
	
	@Override
	public synchronized XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers()
	{
		this.ensureInitializeInstanceMembers();
		
		return this.members;
	}
	
	protected final void ensureInitializeInstanceMembers()
	{
		if(this.members != null)
		{
			return;
		}
		this.members = this.initializeInstanceMembers();
		this.calculcateBinaryLengths();
	}
	
	protected XImmutableEnum<? extends PersistenceTypeDefinitionMember> initializeInstanceMembers()
	{
		throw new PersistenceException(
			"type definition members may not be null for non-"
			+ CustomBinaryHandler.class.getSimpleName()
			+ "-implementations"
		);
	}
	
	@Override
	public long membersPersistedLengthMinimum()
	{
		return this.binaryLengthMinimum;
	}
	
	@Override
	public long membersPersistedLengthMaximum()
	{
		return this.binaryLengthMaximum;
	}

	@Override
	public abstract void store(Binary data, T instance, long objectId, PersistenceStoreHandler<Binary> handler);

	@Override
	public void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		// no-op, no references
	}

	@Override
	public abstract T create(Binary data, PersistenceLoadHandler handler);

	@Override
	public void complete(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		// no-op for normal implementation (see non-reference-hashing collections for other examples)
	}
	
	@Override
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		// native handling logic should normally not have any member types that have to be iterated here
		return logic;
	}

}
