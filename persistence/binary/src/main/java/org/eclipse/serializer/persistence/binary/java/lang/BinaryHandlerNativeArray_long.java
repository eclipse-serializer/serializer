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

import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeArray_long extends AbstractBinaryHandlerNativeArrayPrimitive<long[]>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerNativeArray_long New()
	{
		return new BinaryHandlerNativeArray_long();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerNativeArray_long()
	{
		super(long[].class, defineElementsType(long.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(
		final Binary                          data    ,
		final long[]                          array   ,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.store_longs(this.typeId(), objectId, array);
	}

	@Override
	public long[] create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.create_longs();
	}

	@Override
	public void updateState(final Binary data, final long[] instance, final PersistenceLoadHandler handler)
	{
		data.update_longs(instance);
	}

}
