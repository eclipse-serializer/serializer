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

import static org.eclipse.serializer.util.X.notNull;

import org.eclipse.serializer.util.X;
import org.eclipse.serializer.collections.Singleton;
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.Persistence;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMember;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberEnumConstant;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandler;
import org.eclipse.serializer.reflect.XReflect;

public final class BinaryHandlerSingletonStatelessEnum<T> extends AbstractBinaryHandlerTrivial<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static boolean isSingletonEnumType(final Class<?> type)
	{
		return XReflect.isEnum(type) && type.getEnumConstants().length == 1;
	}
	
	public static <T> Class<T> validateIsSingletonEnumType(final Class<T> type)
	{
		if(isSingletonEnumType(type))
		{
			return type;
		}
		
		throw new IllegalArgumentException("Not a singleton Enum type: " + type);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> BinaryHandlerSingletonStatelessEnum<T> New(final Class<?> type)
	{
		return new BinaryHandlerSingletonStatelessEnum<>(
			(Class<T>)XReflect.validateIsEnum(type)
		);
	}
	
	private final Singleton<PersistenceTypeDefinitionMemberEnumConstant> enumConstantMember;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected BinaryHandlerSingletonStatelessEnum(final Class<T> type)
	{
		super(validateIsSingletonEnumType(type));
		
		// the notNull is very important to detect incompatibility issues with other JVMs.
		this.enumConstantMember = X.Singleton(
			notNull(BinaryHandlerGenericEnum.deriveEnumConstantMembers(type).get())
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final Object[] collectEnumConstants()
	{
		// single enum constant has already been validated by constructor logic
		return Persistence.collectEnumConstants(this);
	}
	
	@Override
	public final XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
	{
		return this.enumConstantMember;
	}

	@Override
	public final void store(
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(0, this.typeId(), objectId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final T create(final Binary data, final PersistenceLoadHandler handler)
	{
		return (T)XReflect.getDeclaredEnumClass(this.type()).getEnumConstants()[0];
	}
	
	@Override
	public final synchronized PersistenceTypeHandler<Binary, T> initialize(final long typeId)
	{
		// debug hook
		return super.initialize(typeId);
	}
	
}
