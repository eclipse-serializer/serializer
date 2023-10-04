package org.eclipse.serializer.persistence.binary.android.java.time;

/*-
 * #%L
 * Eclipse Serializer Persistence Android
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

import java.time.Year;

import org.eclipse.serializer.persistence.binary.internal.AbstractBinaryHandlerCustomNonReferentialFixedLength;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerYear extends AbstractBinaryHandlerCustomNonReferentialFixedLength<Year>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerYear New()
	{
		return new BinaryHandlerYear();
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerYear()
	{
		super(
			Year.class,
			CustomFields(
				CustomField(int.class, "year")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	

	@Override
	public final void store(
		final Binary                          data    ,
		final Year                            instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(Integer.BYTES, this.typeId(), objectId);
		
		data.store_long(instance.getValue());
	}

	@Override
	public final Year create(final Binary data, final PersistenceLoadHandler handler)
	{
		return Year.of(data.read_int(0L));
	}

	@Override
	public final void updateState(final Binary data, final Year instance, final PersistenceLoadHandler handler)
	{
		// no-op
	}

}
