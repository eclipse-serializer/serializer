package org.eclipse.serializer.communication.binarydynamic;

import java.net.InetSocketAddress;

import org.eclipse.serializer.communication.types.Com;
import org.eclipse.serializer.communication.types.ComClient;
import org.eclipse.serializer.communication.types.ComClientChannel;
import org.eclipse.serializer.communication.types.ComConnection;
import org.eclipse.serializer.communication.types.ComFoundation;
import org.eclipse.serializer.communication.types.ComHost;
import org.eclipse.serializer.communication.types.ComHostChannelAcceptor;
import org.eclipse.serializer.communication.types.ComPersistenceAdaptorCreator;

public class ComBinaryDynamic
{

	public static ComFoundation.Default<?> Foundation()
	{
		return ComFoundation.New()
			.setPersistenceAdaptorCreator(DefaultPersistenceAdaptorCreator())
			.setHostIdStrategy(ComDynamicIdStrategy.New(1_000_000_000_000_000_000L))
			.setClientIdStrategy(ComDynamicIdStrategy.New(4_100_000_000_000_000_000L))
			.registerEntityTypes(ComMessageNewType.class, ComMessageClientTypeMismatch.class, ComMessageStatus.class, ComMessageData.class)
		;
	}

	private static ComPersistenceAdaptorCreator<ComConnection> DefaultPersistenceAdaptorCreator()
	{
		return ComPersistenceAdaptorBinaryDynamic.Creator();
	}

	///////////////////////////////////////////////////////////////////////////
	// convenience methods //
	////////////////////////
		
	
	/////
	// host convenience methods
	////
	
	public static final ComHost<ComConnection> Host()
	{
		return Host(DefaultPersistenceAdaptorCreator(), null);
	}
	
	public static final ComHost<ComConnection> Host(
		final int localHostPort
	)
	{
		return Host(localHostPort, DefaultPersistenceAdaptorCreator(), null);
	}
	
	public static final ComHost<ComConnection> Host(
		final InetSocketAddress  targetAddress
	)
	{
		return Host(targetAddress, DefaultPersistenceAdaptorCreator(), null);
	}
	
	public static final ComHost<ComConnection> Host(
		final ComHostChannelAcceptor<ComConnection> channelAcceptor
	)
	{
		return Host(
			DefaultPersistenceAdaptorCreator(),
			channelAcceptor
		);
	}
	
	public static final ComHost<ComConnection> Host(
		final int                                   localHostPort  ,
		final ComHostChannelAcceptor<ComConnection> channelAcceptor
	)
	{
		return Host(
			DefaultPersistenceAdaptorCreator(),
			channelAcceptor
		);
	}
	
	public static final ComHost<ComConnection> Host(
		final InetSocketAddress                     targetAddress  ,
		final ComHostChannelAcceptor<ComConnection> channelAcceptor
	)
	{
		return Host(targetAddress, DefaultPersistenceAdaptorCreator(), channelAcceptor);
	}
	
	public static final ComHost<ComConnection> Host(
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator,
		final ComHostChannelAcceptor<ComConnection>       channelAcceptor
	)
	{
		return Host(
			Com.localHostSocketAddress(),
			persistenceAdaptorCreator   ,
			channelAcceptor
		);
	}
	
	public static final ComHost<ComConnection> Host(
		final InetSocketAddress                           targetAddress            ,
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator,
		final ComHostChannelAcceptor<ComConnection>       channelAcceptor
	)
	{
		final ComHost<ComConnection> host =
			Foundation()
			.setHostBindingAddress       (targetAddress)
			.setPersistenceAdaptorCreator(persistenceAdaptorCreator)
			.setHostChannelAcceptor      (channelAcceptor)
			.createHost()
		;
		
		return host;
	}
	
	public static final ComHost<ComConnection> Host(
			final int                                         localHostPort            ,
			final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator,
			final ComHostChannelAcceptor<ComConnection>       channelAcceptor
		)
	{
		return Host(
			Com.localHostSocketAddress(localHostPort),
			persistenceAdaptorCreator                ,
			channelAcceptor
		);
	}
		
	public static final void runHost()
	{
		runHost(null, null);
	}
	
	public static final void runHost(
		final int localHostPort
	)
	{
		runHost(localHostPort, null);
	}
	
	public static final void runHost(
		final InetSocketAddress targetAddress
	)
	{
		runHost(targetAddress, null);
	}
	
	public static final void runHost(
		final ComHostChannelAcceptor<ComConnection> channelAcceptor
	)
	{
		runHost(
			Com.localHostSocketAddress(),
			channelAcceptor
		);
	}
	
	public static final void runHost(
		final int                                   localHostPort  ,
		final ComHostChannelAcceptor<ComConnection> channelAcceptor
	)
	{
		runHost(
			Com.localHostSocketAddress(localHostPort),
			channelAcceptor
		);
	}
	
	public static final void runHost(
		final InetSocketAddress                     targetAddress  ,
		final ComHostChannelAcceptor<ComConnection> channelAcceptor
	)
	{
		final ComHost<ComConnection> host = Host(targetAddress, channelAcceptor);
		host.run();
	}
	
	/////
	// client convenience methods
	////
	
	public static final ComClient<ComConnection> Client()
	{
		return Client(
			DefaultPersistenceAdaptorCreator()
		);
	}
	
	public static final ComClient<ComConnection> Client(final int localHostPort)
	{
		return Client(
			localHostPort                     ,
			DefaultPersistenceAdaptorCreator()
		);
	}
		
	public static final ComClient<ComConnection> Client(
		final InetSocketAddress targetAddress
	)
	{
		return Client(
			targetAddress,
			DefaultPersistenceAdaptorCreator()
		);
	}
	
	public static final ComClient<ComConnection> Client(
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator
	)
	{
		return Client(
			Com.localHostSocketAddress(),
			persistenceAdaptorCreator
		);
	}
	
	public static final ComClient<ComConnection> Client(
			final int                                         localHostPort     ,
			final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator
	)
	{
		return Client(
			Com.localHostSocketAddress(localHostPort),
			persistenceAdaptorCreator
		);
	}
	
	public static final ComClient<ComConnection> Client(
		final InetSocketAddress                           targetAddress     ,
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator
	)
	{
		final ComClient<ComConnection> client = Foundation()
			.setClientTargetAddress(targetAddress)
			.setPersistenceAdaptorCreator(persistenceAdaptorCreator)
			.createClient()
		;
		
		return client;
	}
	
	
	public static final ComClientChannel<ComConnection> connect()
	{
		return Client()
			.connect()
		;
	}
	
	public static final ComClientChannel<ComConnection> connect(
		final int localHostPort
	)
	{
		return Client(localHostPort)
			.connect()
		;
	}
		
	public static final ComClientChannel<ComConnection> connect(
		final InetSocketAddress targetAddress
	)
	{
		return Client(targetAddress)
			.connect()
		;
	}

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private ComBinaryDynamic()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}

