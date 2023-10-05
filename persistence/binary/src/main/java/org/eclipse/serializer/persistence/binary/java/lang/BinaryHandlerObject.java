package org.eclipse.serializer.persistence.binary.java.lang;

import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerStateless;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;

public final class BinaryHandlerObject extends AbstractBinaryHandlerStateless<Object>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerObject New()
	{
		return new BinaryHandlerObject();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerObject()
	{
		super(Object.class);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final Object create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new Object(); // funny
	}

}
