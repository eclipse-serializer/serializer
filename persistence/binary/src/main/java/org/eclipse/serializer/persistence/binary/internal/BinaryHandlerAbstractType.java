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

import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerAbstractType<T> extends AbstractBinaryHandlerTrivial<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <T> BinaryHandlerAbstractType<T> New(final Class<T> type)
	{
		return new BinaryHandlerAbstractType<>(
			notNull(type)
		);
	}
	
	public static <T> BinaryHandlerAbstractType<T> New(
		final Class<T> type,
		final String   typeName
	)
	{
		return new BinaryHandlerAbstractType<>(
			notNull(type)    ,
			notNull(typeName)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerAbstractType(final Class<T> type)
	{
		super(type);
	}
	
	BinaryHandlerAbstractType(final Class<T> type, final String typeName)
	{
		super(type, typeName);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}

	@Override
	public final T create(final Binary data, final PersistenceLoadHandler handler)
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}

	@Override
	public final void updateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}
	
	@Override
	public final void guaranteeSpecificInstanceViablity() throws PersistenceExceptionTypeNotPersistable
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}
	
	@Override
	public final boolean isSpecificInstanceViable()
	{
		return false;
	}

}
