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

import java.util.function.Consumer;

import org.eclipse.serializer.util.X;
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.binary.types.BinaryTypeHandler;
import org.eclipse.serializer.persistence.types.PersistenceFunction;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMember;

public abstract class AbstractBinaryHandlerTrivial<T> extends BinaryTypeHandler.Abstract<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractBinaryHandlerTrivial(final Class<T> type)
	{
		super(type);
	}
	
	protected AbstractBinaryHandlerTrivial(final Class<T> type, final String typeName)
	{
		super(type, typeName);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void updateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		// no-op, no state to update
	}
	
	@Override
	public final void complete(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		/* any "trival" implementation cannot have the need for a completion step
		 * (see non-reference-hashing collections for other examples)
		 */
	}

	@Override
	public final void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		// no-op, no references
	}

	@Override
	public final void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		// no-op, no references
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
	{
		return X.empty();
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers()
	{
		return X.empty();
	}
	
	@Override
	public long membersPersistedLengthMinimum()
	{
		return 0;
	}
	
	@Override
	public long membersPersistedLengthMaximum()
	{
		return 0;
	}
	
	@Override
	public boolean isPrimitiveType()
	{
		return false;
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return false;
	}
	
	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}
	
	@Override
	public final <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		// no member types to iterate in a trivial handler implementation
		return logic;
	}
	
}
