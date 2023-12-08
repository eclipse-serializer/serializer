package org.eclipse.serializer.communication.binarydynamic;

import org.eclipse.serializer.communication.types.ComHost;
import org.eclipse.serializer.communication.types.ComHostChannel;
import org.eclipse.serializer.communication.types.ComProtocol;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceManager;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandlerEnsurer;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandlerManager;

public class ComHostChannelDynamic<C>
	extends ComChannelDynamic<C>
	implements ComHostChannel<C>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	protected final ComHost<C> parent;
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComHostChannelDynamic(
		final PersistenceManager<?>                 persistenceManager  ,
		final C                                     connection          ,
		final ComProtocol                           protocol            ,
		final ComHost<C>                            parent              ,
		final PersistenceTypeHandlerManager<Binary> typeHandlerManager  ,
		final ComTypeDefinitionBuilder              typeDefintionBuilder,
		final PersistenceTypeHandlerEnsurer<Binary> typeHandlerEnsurer
	)
	{
		super(persistenceManager, connection, protocol);
		this.parent = parent;
		
		final ComTypeDescriptionRegistrationObserver observer = new ComTypeDescriptionRegistrationObserver(this);
		this.persistenceManager.typeDictionary().setTypeDescriptionRegistrationObserver(observer);
		this.initalizeHandlersInternal(typeHandlerManager, typeDefintionBuilder, typeHandlerEnsurer);
		
	}

	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private void initalizeHandlersInternal(
		final PersistenceTypeHandlerManager<Binary> typeHandlerManager  ,
		final ComTypeDefinitionBuilder              typeDefintionBuilder,
		final PersistenceTypeHandlerEnsurer<Binary> typeHandlerEnsurer
	)
	{
		this.handlers.registerReceiveHandler(
			ComMessageNewType.class,
			new ComHandlerReceiveMessageNewType(
				typeHandlerManager,
				typeDefintionBuilder,
				typeHandlerEnsurer
			)
		);
		
		this.handlers.registerSendHandler(
			ComMessageNewType.class,
			new ComHandlerSendMessageNewType(this));
		
		this.handlers.registerReceiveHandler(
			ComMessageClientTypeMismatch.class,
			new ComHandlerReceiveMessageClientTypeMismatch(this));
		
		this.handlers.registerReceiveHandler(
			ComMessageStatus.class,
			new ComHandlerReceiveMessageStatus(this));
		
		this.handlers.registerSendHandler(
			ComMessageStatus.class,
			new ComHandlerReceiveMessageStatus(this));
		
		this.handlers.registerReceiveHandler(
			ComMessageData.class,
			new ComHandlerSendReceiveMessageData(this));
			
		this.handlers.registerSendHandler(
			ComMessageData.class,
			new ComHandlerSendReceiveMessageData(this));
	}
	
	@Override
	public final ComHost<C> parent()
	{
		return this.parent;
	}
	
	@Override
	public C connection()
	{
		return this.connection;
	}

	@Override
	public ComProtocol protocol()
	{
		return this.protocol;
	}

}
