package org.eclipse.serializer.communication.types;

import static org.eclipse.serializer.util.X.notNull;

import java.nio.ByteOrder;

import org.eclipse.serializer.persistence.types.PersistenceIdStrategy;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryView;
import org.eclipse.serializer.typing.Immutable;

public interface ComProtocol extends ComProtocolData
{
	public static String protocolName()
	{
		return "ECLIPSE-SERIALIZER-COMCHANNEL";
	}
	
	public static String protocolVersion()
	{
		return "1.0";
	}
				
	public static ComProtocolCreator Creator()
	{
		return ComProtocolCreator.New();
	}
	
	public static ComProtocol New(
		final String                        name             ,
		final String                        version          ,
		final ByteOrder                     byteOrder        ,
		final int                           inactivityTimeout,
		final PersistenceIdStrategy         idStrategy       ,
		final PersistenceTypeDictionaryView persistenceTypeDictionaryView
	)
	{
		return new ComProtocol.Default(
			notNull(name)      ,
			notNull(version)   ,
			notNull(byteOrder) ,
			inactivityTimeout  ,
			notNull(idStrategy),
			persistenceTypeDictionaryView
		);
	}
	
	public final class Default implements ComProtocol, Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String                        name             ;
		private final String                        version          ;
		private final ByteOrder                     byteOrder        ;
		private final int                           inactivityTimeOut;
		private final PersistenceIdStrategy         idStrategy       ;
		private final PersistenceTypeDictionaryView typeDictionary   ;
		
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final String                        name             ,
			final String                        version          ,
			final ByteOrder                     byteOrder        ,
			final int                           inactivityTimeOut,
			final PersistenceIdStrategy         idStrategy       ,
			final PersistenceTypeDictionaryView typeDictionary
			
		)
		{
			super();
			this.name              = name             ;
			this.version           = version          ;
			this.byteOrder         = byteOrder        ;
			this.inactivityTimeOut = inactivityTimeOut;
			this.idStrategy        = idStrategy       ;
			this.typeDictionary    = typeDictionary   ;
			
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final String name()
		{
			return this.name;
		}

		@Override
		public final String version()
		{
			return this.version;
		}

		@Override
		public final ByteOrder byteOrder()
		{
			return this.byteOrder;
		}

		@Override
		public final PersistenceIdStrategy idStrategy()
		{
			return this.idStrategy;
		}
		
		@Override
		public final PersistenceTypeDictionaryView typeDictionary()
		{
			return this.typeDictionary;
		}
		
		@Override
		public final int inactivityTimeout()
		{
			return this.inactivityTimeOut;
		}
		
	}
		
}
