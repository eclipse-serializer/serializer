package org.eclipse.serializer.persistence.binary.java.lang;

import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomValueFixedLength;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerShort extends AbstractBinaryHandlerCustomValueFixedLength<Short, Short>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerShort New()
	{
		return new BinaryHandlerShort();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerShort()
	{
		super(Short.class, defineValueType(short.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static short instanceState(final Short instance)
	{
		return instance.shortValue();
	}
	
	private static short binaryState(final Binary data)
	{
		return data.read_short(0);
	}

	@Override
	public void store(
		final Binary                          data    ,
		final Short                           instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeShort(this.typeId(), objectId, instance.shortValue());
	}

	@Override
	public Short create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.buildShort();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	// actually never called, just to satisfy the interface
	@Override
	public Short getValidationStateFromInstance(final Short instance)
	{
		// well, lol
		return instance;
	}

	// actually never called, just to satisfy the interface
	@Override
	public Short getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}
	
	@Override
	public void validateState(
		final Binary                 data    ,
		final Short                  instance,
		final PersistenceLoadHandler handler
	)
	{
		final short instanceState = instanceState(instance);
		final short binaryState   = binaryState(data);
		
		if(instanceState == binaryState)
		{
			return;
		}
		
		this.throwInconsistentStateException(instance, instanceState, binaryState);
	}

}
