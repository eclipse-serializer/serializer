package org.eclipse.serializer.persistence.binary.java.time;

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

import static org.eclipse.serializer.util.X.Constant;

import java.time.ZoneOffset;

import org.eclipse.serializer.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

// custom type handler for zone offset, needed because of transient field ZoneOffset#id
public final class BinaryHandlerZoneOffset extends AbstractBinaryHandlerCustomValueFixedLength<ZoneOffset, Integer>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static BinaryHandlerZoneOffset New()
	{
		return new BinaryHandlerZoneOffset();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerZoneOffset()
	{
		super(
			ZoneOffset.class,
			Constant(
				CustomField(int.class, "totalSeconds")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	private static int instanceState(final ZoneOffset instance)
	{
		return instance.getTotalSeconds();
	}

	private static int binaryState(final Binary data)
	{
		return data.read_int(0L);
	}

	@Override
	public void store(
		final Binary                          data    ,
		final ZoneOffset                      instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeInteger(this.typeId(), objectId, instanceState(instance));
	}

	@Override
	public ZoneOffset create(final Binary data, final PersistenceLoadHandler handler)
	{
		return ZoneOffset.ofTotalSeconds(binaryState(data));
	}

	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////

	// actually never called, just to satisfy the interface
	@Override
	public Integer getValidationStateFromInstance(final ZoneOffset instance)
	{
		return instance.getTotalSeconds();
	}

	// actually never called, just to satisfy the interface
	@Override
	public Integer getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}

	@Override
	public void validateState(
		final Binary                 data    ,
		final ZoneOffset             instance,
		final PersistenceLoadHandler handler
	)
	{
		final int instanceState = instanceState(instance);
		final int binaryState   = binaryState(data);

		if(instanceState == binaryState)
		{
			return;
		}

		this.throwInconsistentStateException(instance, instanceState, binaryState);
	}

}
