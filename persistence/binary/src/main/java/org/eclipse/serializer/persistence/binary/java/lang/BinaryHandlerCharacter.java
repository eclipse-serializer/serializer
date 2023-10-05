package org.eclipse.serializer.persistence.binary.java.lang;

import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustomValueFixedLength;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerCharacter extends AbstractBinaryHandlerCustomValueFixedLength<Character, Character>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerCharacter New()
	{
		return new BinaryHandlerCharacter();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerCharacter()
	{
		super(Character.class, defineValueType(char.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static char instanceState(final Character instance)
	{
		return instance.charValue();
	}
	
	private static char binaryState(final Binary data)
	{
		return data.read_char(0);
	}

	@Override
	public void store(
		final Binary                          data    ,
		final Character                       instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeCharacter(this.typeId(), objectId, instance.charValue());
	}

	@Override
	public Character create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.buildCharacter();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	// actually never called, just to satisfy the interface
	@Override
	public Character getValidationStateFromInstance(final Character instance)
	{
		return instance;
	}

	// actually never called, just to satisfy the interface
	@Override
	public Character getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}
	
	@Override
	public void validateState(
		final Binary                 data    ,
		final Character              instance,
		final PersistenceLoadHandler handler
	)
	{
		final char instanceState = instanceState(instance);
		final char binaryState   = binaryState(data);
		
		if(instanceState == binaryState)
		{
			return;
		}
		
		this.throwInconsistentStateException(instance, instanceState, binaryState);
	}

}
