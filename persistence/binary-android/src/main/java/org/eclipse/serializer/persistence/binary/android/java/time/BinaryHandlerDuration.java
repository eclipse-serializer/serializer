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

import java.time.Duration;

import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomNonReferentialFixedLength;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerDuration extends AbstractBinaryHandlerCustomNonReferentialFixedLength<Duration>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerDuration New()
	{
		return new BinaryHandlerDuration();
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerDuration()
	{
		super(
			Duration.class,
			CustomFields(
				CustomField(long.class, "nanos")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	

	@Override
	public final void store(
		final Binary                          data    ,
		final Duration                        instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(Long.BYTES, this.typeId(), objectId);
		
		data.store_long(instance.toNanos());
	}

	@Override
	public final Duration create(final Binary data, final PersistenceLoadHandler handler)
	{
		return Duration.ofNanos(data.read_long(0L));
	}

	@Override
	public final void updateState(final Binary data, final Duration instance, final PersistenceLoadHandler handler)
	{
		// no-op
	}

}
