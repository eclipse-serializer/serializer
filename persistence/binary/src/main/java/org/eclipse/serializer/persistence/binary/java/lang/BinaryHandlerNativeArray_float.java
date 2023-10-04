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

public final class BinaryHandlerNativeArray_float extends AbstractBinaryHandlerNativeArrayPrimitive<float[]>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerNativeArray_float New()
	{
		return new BinaryHandlerNativeArray_float();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerNativeArray_float()
	{
		super(float[].class, defineElementsType(float.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(
		final Binary                          data    ,
		final float[]                         array   ,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.store_floats(this.typeId(), objectId, array);
	}

	@Override
	public float[] create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.create_floats();
	}

	@Override
	public void updateState(final Binary data, final float[] instance, final PersistenceLoadHandler handler)
	{
		data.update_floats(instance);
	}

}
