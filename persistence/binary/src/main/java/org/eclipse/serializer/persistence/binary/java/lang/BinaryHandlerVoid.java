package org.eclipse.serializer.persistence.binary.java.lang;

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

import org.eclipse.serializer.persistence.binary.internal.AbstractBinaryHandlerStateless;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerVoid extends AbstractBinaryHandlerStateless<Void>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerVoid New()
	{
		return new BinaryHandlerVoid();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerVoid()
	{
		super(Void.class);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                          data    ,
		final Void                            instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public final Void create(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
	)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateState(
		final Binary                 data    ,
		final Void                   instance,
		final PersistenceLoadHandler handler
	)
	{
		throw new UnsupportedOperationException();
	}

}
