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
import org.eclipse.serializer.collections.Constant;
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.binary.types.BinaryPersistence;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMember;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberPrimitiveDefinition;


public final class BinaryHandlerPrimitive<T> extends AbstractBinaryHandlerTrivial<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <T> BinaryHandlerPrimitive<T> New(final Class<T> type)
	{
		return new BinaryHandlerPrimitive<>(
			notNull(type)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final Constant<PersistenceTypeDefinitionMemberPrimitiveDefinition> member;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerPrimitive(final Class<T> type)
	{
		super(type);

		final long primitiveBinaryLength = BinaryPersistence.resolvePrimitiveFieldBinaryLength(type);
		this.member = X.Constant(
			PersistenceTypeDefinitionMemberPrimitiveDefinition.New(
				type,
				primitiveBinaryLength
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
	{
		return this.member;
	}

	@Override
	public final XGettingEnum<? extends PersistenceTypeDefinitionMemberPrimitiveDefinition> instanceMembers()
	{
		return X.empty();
	}
	
	@Override
	public final long membersPersistedLengthMinimum()
	{
		return this.member.get().persistentMinimumLength();
	}
	
	@Override
	public final long membersPersistedLengthMaximum()
	{
		return this.member.get().persistentMaximumLength();
	}
	
	@Override
	public final boolean isPrimitiveType()
	{
		return true;
	}
	
	@Override
	public void store(
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public T create(final Binary data, final PersistenceLoadHandler handler)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		throw new UnsupportedOperationException();
	}


}
