package org.eclipse.serializer.communication.types;

import java.nio.ByteOrder;

import org.eclipse.serializer.persistence.types.PersistenceIdStrategy;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryView;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryViewProvider;

public interface ComProtocolData extends PersistenceTypeDictionaryViewProvider
{
	public String name();
	
	public String version();
	
	public ByteOrder byteOrder();
	
	public PersistenceTypeDictionaryView typeDictionary();
	
	public PersistenceIdStrategy idStrategy();
	
	@Override
	public default PersistenceTypeDictionaryView provideTypeDictionary()
	{
		return this.typeDictionary();
	}

	public int inactivityTimeout();
	
}
