package org.eclipse.serializer.persistence.binary.java.math;

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

import java.math.BigInteger;
import java.util.Arrays;

import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomValueVariableLength;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerBigInteger extends AbstractBinaryHandlerCustomValueVariableLength<BigInteger, byte[]>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerBigInteger New()
	{
		return new BinaryHandlerBigInteger();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerBigInteger()
	{
		super(
			BigInteger.class,
			CustomFields(
				bytes("value")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static byte[] instanceState(final BigInteger instance)
	{
		return instance.toByteArray();
	}
	
	private static byte[] binaryState(final Binary data)
	{
		return data.build_bytes();
	}

	@Override
	public void store(
		final Binary                          data    ,
		final BigInteger                      instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.store_bytes(this.typeId(), objectId, instance.toByteArray());
	}

	@Override
	public BigInteger create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new BigInteger(data.build_bytes());
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	// actually never called, just to satisfy the interface
	@Override
	public byte[] getValidationStateFromInstance(final BigInteger instance)
	{
		return instanceState(instance);
	}

	// actually never called, just to satisfy the interface
	@Override
	public byte[] getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}
	
	@Override
	public void validateState(
		final Binary                 data    ,
		final BigInteger             instance,
		final PersistenceLoadHandler handler
	)
	{
		final byte[] instanceState = instanceState(instance);
		final byte[] binaryState   = binaryState(data);
		
		if(Arrays.equals(instanceState, binaryState))
		{
			return;
		}
		
		this.throwInconsistentStateException(instance, Arrays.toString(instanceState), Arrays.toString(binaryState));
	}
	
}
