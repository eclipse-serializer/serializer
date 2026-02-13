package org.eclipse.serializer.persistence.binary.java.sql;

import java.sql.Timestamp;

import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.binary.types.BinaryLegacyTypeHandler;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;

public class BinaryLegacyTypeHandlerSqlTimestamp extends BinaryLegacyTypeHandler.AbstractCustom<Timestamp>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryLegacyTypeHandlerSqlTimestamp New() 
	{
		return new BinaryLegacyTypeHandlerSqlTimestamp();
	}

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public BinaryLegacyTypeHandlerSqlTimestamp() 
	{
		super(
			Timestamp.class,
			CustomFields(
				CustomField(long.class, "timestamp")
			));
	}

	@Override
	public void iterateLoadableReferences(Binary data, PersistenceReferenceLoader iterator) 
	{
		//no-op
	}

	@Override
	public Timestamp create(Binary data, PersistenceLoadHandler handler) 
	{
		return new Timestamp(data.read_long(0));
	}

	@Override
	public void updateState(Binary data, Timestamp instance, PersistenceLoadHandler handler) 
	{
		instance.setTime(data.read_long(0));
	}
}
