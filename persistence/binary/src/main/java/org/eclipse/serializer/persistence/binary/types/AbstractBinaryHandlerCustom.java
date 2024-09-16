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
import java.util.function.Consumer;

import org.eclipse.serializer.collections.XArrays;
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.collections.types.XImmutableEnum;
import org.eclipse.serializer.collections.types.XImmutableSequence;
import org.eclipse.serializer.collections.types.XList;
import org.eclipse.serializer.exceptions.NoSuchFieldRuntimeException;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.persistence.types.PersistenceFunction;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMember;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldGeneric;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldGenericComplex;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldGenericSimple;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldGenericVariableLength;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionMember;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionMemberFieldGeneric;
import org.eclipse.serializer.reflect.XReflect;
import org.eclipse.serializer.util.X;

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
	
	public static final XImmutableSequence<PersistenceTypeDefinitionMemberFieldGeneric>
	CustomFields(
		final PersistenceTypeDefinitionMemberFieldGeneric[] customFields          ,
		final PersistenceTypeDefinitionMemberFieldGeneric[] additionalCustomFields
	)
	{
		if(additionalCustomFields == null)
		{
			return CustomFields(customFields);
		}
		
		return X.List(customFields).addAll(additionalCustomFields).immure();
	}
	

	public static final XImmutableSequence<PersistenceTypeDefinitionMemberFieldGeneric>
	CustomFields(
		final PersistenceTypeDefinitionMemberFieldGeneric[]                   customFields          ,
		final Iterable<? extends PersistenceTypeDefinitionMemberFieldGeneric> additionalCustomFields
	)
	{
		if(additionalCustomFields == null)
		{
			return CustomFields(customFields);
		}
		
		final XList<PersistenceTypeDefinitionMemberFieldGeneric> merged = X.List(customFields);
		for(final PersistenceTypeDefinitionMemberFieldGeneric f : additionalCustomFields)
		{
			merged.add(f);
		}
		
		return merged.immure();
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
	private boolean hasPersistedReferences;
	private boolean hasVaryingPersistedLengthInstances;


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
		this.initializeFields(members);
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	protected void initializeFields(final XGettingSequence<? extends PersistenceTypeDefinitionMember> members)
	{
		if(members == null)
		{
			// null can happen with CustomHandler instances that initialize BinaryField instances per reflection.
			return;
		}

		this.members = validateAndImmure(members);
		this.hasPersistedReferences = PersistenceTypeDescriptionMember.determineHasReferences(members);
		this.hasVaryingPersistedLengthInstances = PersistenceTypeDefinition.determineVariableLength(members);
		this.calculcateBinaryLengths();
	}
	
	protected static boolean determineHasPersistedReferences(
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
	)
	{
		for(final PersistenceTypeDefinitionMember member : members)
		{
			if(member.hasReferences())
			{
				return true;
			}
		}

		return false;
	}

	protected static Field getClassDeclaredField(final Class<?> declaringClass, final String fieldName)
	{
		return XReflect.getDeclaredField(declaringClass, fieldName);
	}

	protected static long getClassDeclaredFieldOffset(final Class<?> declaringClass, final String fieldName)
	{
		return XMemory.objectFieldOffset(getClassDeclaredField(declaringClass, fieldName));
	}
	
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
	public boolean hasPersistedReferences()
	{
		return this.hasPersistedReferences;
	}

	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		return this.hasVaryingPersistedLengthInstances;
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
		this.initializeFields(this.initializeInstanceMembers());
	}
	
	protected XGettingSequence<? extends PersistenceTypeDefinitionMember> initializeInstanceMembers()
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
