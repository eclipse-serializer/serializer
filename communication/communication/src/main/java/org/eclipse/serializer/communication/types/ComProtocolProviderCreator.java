package org.eclipse.serializer.communication.types;

import java.nio.ByteOrder;

import org.eclipse.serializer.persistence.types.PersistenceIdStrategy;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryViewProvider;

/**
 * 
 * @param <C> the communication layer type
 */
@FunctionalInterface
public interface ComProtocolProviderCreator<C>
{
	public ComProtocolProvider<C> creatProtocolProvider(
		String                                name                  ,
		String                                version               ,
		ByteOrder                             byteOrder             ,
		int                                   inactivityTimeout     ,
		PersistenceIdStrategy                 idStrategy            ,
		PersistenceTypeDictionaryViewProvider typeDictionaryProvider,
		ComProtocolCreator                    protocolCreator
	);
	
	
	
	public static <C> ComProtocolProviderCreator<C> New()
	{
		return new ComProtocolProviderCreator.Default<>();
	}
	
	public final class Default<C> implements ComProtocolProviderCreator<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public ComProtocolProvider<C> creatProtocolProvider(
			final String                                name                  ,
			final String                                version               ,
			final ByteOrder                             byteOrder             ,
			final int                                   inactivityTimeout     ,
			final PersistenceIdStrategy                 idStrategy            ,
			final PersistenceTypeDictionaryViewProvider typeDictionaryProvider,
			final ComProtocolCreator                    protocolCreator
			
		)
		{
			return new ComProtocolProvider.Default<>(
				name                  ,
				version               ,
				byteOrder             ,
				inactivityTimeout     ,
				idStrategy            ,
				typeDictionaryProvider,
				protocolCreator
			);
		}
	}
	
}
