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

public final class BinaryHandlerNativeArray_short extends AbstractBinaryHandlerNativeArrayPrimitive<short[]>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerNativeArray_short New()
	{
		return new BinaryHandlerNativeArray_short();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerNativeArray_short()
	{
		super(short[].class, defineElementsType(short.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(
		final Binary                          data    ,
		final short[]                         array   ,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.store_shorts(this.typeId(), objectId, array);
	}

	@Override
	public short[] create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.create_shorts();
	}

	@Override
	public void updateState(final Binary data, final short[] instance, final PersistenceLoadHandler handler)
	{
		data.update_shorts(instance);
	}
}
