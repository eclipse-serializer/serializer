package org.eclipse.serializer.communication.binarydynamic;

/*-
 * #%L
 * Eclipse Serializer Communication Binary
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

import static org.eclipse.serializer.util.X.mayNull;
import static org.eclipse.serializer.util.X.notNull;

import java.nio.ByteOrder;
import java.util.function.Consumer;

import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.com.ComException;
import org.eclipse.serializer.communication.binary.types.ComBinaryPersistenceRootsProvider;
import org.eclipse.serializer.communication.binary.types.ComPersistenceChannelBinary;
import org.eclipse.serializer.communication.types.ComClient;
import org.eclipse.serializer.communication.types.ComClientChannel;
import org.eclipse.serializer.communication.types.ComConnection;
import org.eclipse.serializer.communication.types.ComHost;
import org.eclipse.serializer.communication.types.ComHostChannel;
import org.eclipse.serializer.communication.types.ComPersistenceAdaptor;
import org.eclipse.serializer.communication.types.ComPersistenceAdaptorCreator;
import org.eclipse.serializer.communication.types.ComProtocol;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.binary.types.BinaryPersistenceFoundation;
import org.eclipse.serializer.persistence.types.LoggingLegacyTypeMappingResultor;
import org.eclipse.serializer.persistence.types.Persistence;
import org.eclipse.serializer.persistence.types.PersistenceContextDispatcher;
import org.eclipse.serializer.persistence.types.PersistenceFoundation;
import org.eclipse.serializer.persistence.types.PersistenceIdStrategy;
import org.eclipse.serializer.persistence.types.PersistenceManager;
import org.eclipse.serializer.persistence.types.PersistenceSizedArrayLengthController;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionary;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryCompiler;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryLoader;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryManager;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryStorer;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryView;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandlerManager;
import org.eclipse.serializer.persistence.types.PersistenceWriteController;
import org.eclipse.serializer.typing.KeyValue;
import org.eclipse.serializer.util.BufferSizeProvider;


public class ComPersistenceAdaptorBinaryDynamic implements ComPersistenceAdaptor<ComConnection>
{
	private final BinaryPersistenceFoundation<?> foundation;
	private final BufferSizeProvider             bufferSizeProvider;
	
	private final PersistenceIdStrategy          hostInitIdStrategy;
	private final XGettingEnum<Class<?>>         entityTypes       ;
	private final ByteOrder                      hostByteOrder     ;
	private final PersistenceIdStrategy          hostIdStrategy    ;
	
	private transient PersistenceTypeDictionary  cachedTypeDictionary;
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected ComPersistenceAdaptorBinaryDynamic(
		final BinaryPersistenceFoundation<?> foundation        ,
		final BufferSizeProvider             bufferSizeProvider,
		final PersistenceIdStrategy          hostInitIdStrategy,
		final XGettingEnum<Class<?>>         entityTypes       ,
		final ByteOrder                      hostByteOrder     ,
		final PersistenceIdStrategy          hostIdStrategy
	)
	{
		super();
		this.foundation         = foundation;
		this.bufferSizeProvider = bufferSizeProvider;
		
		this.hostInitIdStrategy = hostInitIdStrategy;
		this.entityTypes        = entityTypes       ;
		this.hostByteOrder      = hostByteOrder     ;
		this.hostIdStrategy     = hostIdStrategy    ;
	}
	
	public static ComPersistenceAdaptorBinaryDynamic New(
			final BinaryPersistenceFoundation<?> foundation        ,
			final BufferSizeProvider             bufferSizeProvider,
			final PersistenceIdStrategy          hostInitIdStrategy,
			final XGettingEnum<Class<?>>         entityTypes       ,
			final ByteOrder                      hostByteOrder     ,
			final PersistenceIdStrategy          hostIdStrategy
		)
		{
			return new ComPersistenceAdaptorBinaryDynamic(
				notNull(foundation)        ,
				notNull(bufferSizeProvider),
				mayNull(hostInitIdStrategy), // null for client persistence. Checked for host persistence beforehand.
				mayNull(entityTypes)       , // null for client persistence. Checked for host persistence beforehand.
				mayNull(hostByteOrder)     , // null for client persistence. Checked for host persistence beforehand.
				mayNull(hostIdStrategy)      // null for client persistence. Checked for host persistence beforehand.
			);
		}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public PersistenceFoundation<?, ?> persistenceFoundation()
	{
		return this.foundation;
	}
	
	
	@Override
	public void iterateEntityTypes(final Consumer<? super Class<?>> iterator)
	{
		this.entityTypes.iterate(iterator);
	}

	@Override
	public PersistenceIdStrategy hostInitializationIdStrategy()
	{
		return this.hostInitIdStrategy;
	}

	@Override
	public PersistenceIdStrategy hostIdStrategy()
	{
		return this.hostIdStrategy;
	}

	@Override
	public ByteOrder hostByteOrder()
	{
		return this.hostByteOrder;
	}
	
	private BufferSizeProvider bufferSizeProvider()
	{
		return this.bufferSizeProvider;
	}
			
	@Override
	public PersistenceTypeDictionaryCompiler provideTypeDictionaryCompiler()
	{
		return this.createInitializationFoundation().getTypeDictionaryCompiler();
	}
	
	@Override
	public BinaryPersistenceFoundation<?> createInitializationFoundation()
	{
		final BinaryPersistenceFoundation<?> initFoundation = this.foundation.Clone();
		
		initFoundation.setContextDispatcher(
				PersistenceContextDispatcher.LocalObjectRegistration()
			)
			.setSizedArrayLengthController(
				PersistenceSizedArrayLengthController.Fitting()
			)
			.setTypeDictionaryLoader(
				createNoOpDictionaryLoader()
			)
			.setTypeDictionaryStorer(
				createNoOpTypDictionaryStorer()
			)
			.setRootsProvider(
				new ComBinaryPersistenceRootsProvider()
			)
			.setTargetByteOrder(this.hostByteOrder)
			.setLegacyTypeMappingResultor(
				LoggingLegacyTypeMappingResultor.New(initFoundation.getLegacyTypeMappingResultor())
			)
			;
			
		
		return initFoundation;
	}

	@Override
	public PersistenceFoundation<?, ?> provideHostPersistenceFoundation(final ComConnection connection)
	{
		if(connection != null)
		{
			return this.hostConnectionFoundation(connection);
		}
		
		return this.hostConnectionFoundation();
	}
	
	private BinaryPersistenceFoundation<?> hostConnectionFoundation()
	{
		final BinaryPersistenceFoundation<?> hostFoundation = this.createInitializationFoundation();
		
		hostFoundation.setTargetByteOrder      (this.hostByteOrder());
		hostFoundation.setObjectIdProvider     (this.hostIdStrategy().createObjectIdProvider());
		hostFoundation.setTypeIdProvider       (this.hostIdStrategy().createTypeIdProvider());
		hostFoundation.setTypeMismatchValidator(Persistence.typeMismatchValidatorFailing());
				
		final PersistenceTypeDictionaryManager typeDictionaryManager = PersistenceTypeDictionaryManager.Transient(
			hostFoundation.getTypeDictionaryCreator());
		
		final PersistenceTypeDictionaryView typeDictionaryView = this.provideTypeDictionary();
		typeDictionaryView.allTypeDefinitions().forEach(d -> typeDictionaryManager.registerTypeDefinition(d.value()));
				
		hostFoundation.setTypeDictionaryManager(typeDictionaryManager);
			
		return hostFoundation;
	}

	private PersistenceFoundation<?, ?> hostConnectionFoundation(final ComConnection connection)
	{
		final BinaryPersistenceFoundation<?> hostFoundation = this.hostConnectionFoundation();
				
		final PersistenceTypeHandlerManager<Binary> typeHandlerManager = hostFoundation.getTypeHandlerManager();
		typeHandlerManager.initialize();
		
		final ComPersistenceChannelBinary.Default channel = ComPersistenceChannelBinary.New(
				connection,
				this.bufferSizeProvider(),
				hostFoundation,
				this.comWriteController()
			);
		
		hostFoundation.setPersistenceChannel(channel);
		
		return hostFoundation;
	}
	
	private PersistenceWriteController comWriteController()
	{
		// (06.08.2020 TM)TODO: Com Layer WriteController
		return PersistenceWriteController.Enabled();
	}
	
	@Override
	public BinaryPersistenceFoundation<?> provideClientPersistenceFoundation(
		final ComConnection connection,
		final ComProtocol   protocol
	)
	{
		final BinaryPersistenceFoundation<?> clientFoundation = this.createInitializationFoundation();
		clientFoundation.setTargetByteOrder (protocol.byteOrder());
		clientFoundation.setObjectIdProvider(protocol.idStrategy().createObjectIdProvider());
		clientFoundation.setTypeIdProvider  (protocol.idStrategy().createTypeIdProvider());
				
		final PersistenceTypeDictionaryManager typeDictionaryManager = PersistenceTypeDictionaryManager.Transient(
			clientFoundation.getTypeDictionaryCreator());
		
		final PersistenceTypeDictionaryView typeDictionaryView = protocol.typeDictionary();
		typeDictionaryView.allTypeDefinitions().forEach(d -> typeDictionaryManager.registerTypeDefinition(d.value()));
				
		clientFoundation.setTypeDictionaryManager(typeDictionaryManager);
		
		final PersistenceTypeHandlerManager<Binary> typeHandlerManager = clientFoundation.getTypeHandlerManager();
		typeHandlerManager.initialize();
		
		this.ensureTypeHandlers(typeHandlerManager, protocol);
		
		final ComPersistenceChannelBinary.Default channel = ComPersistenceChannelBinary.New(
			connection,
			this.bufferSizeProvider(),
			clientFoundation,
			this.comWriteController()
		);
		
		clientFoundation.setPersistenceChannel(channel);
		
		return clientFoundation;
	}


	private void ensureTypeHandlers(final PersistenceTypeHandlerManager<Binary> typeHandlerManager, final ComProtocol protocol)
	{
		for (final KeyValue<Long, PersistenceTypeDefinition> entry : protocol.typeDictionary().allTypeDefinitions())
		{
			typeHandlerManager.ensureTypeHandler(entry.value());
		}
	}

	@Override
	public ComHostChannel<ComConnection> createHostChannel(
		final ComConnection          connection,
		final ComProtocol            protocol  ,
		final ComHost<ComConnection> parent
	)
	{
		final PersistenceFoundation<?, ?>          hf  = this.hostConnectionFoundation(connection);
		final PersistenceManager<?>                pm  = hf.createPersistenceManager();
		@SuppressWarnings("unchecked")
		final PersistenceTypeHandlerManager<Binary>thm = (PersistenceTypeHandlerManager<Binary>)hf.getTypeHandlerManager();
		
		final ComTypeDefinitionBuilder typeDefinitionBuilder = new ComTypeDefinitionBuilder(
			hf.getTypeDictionaryParser(),
			hf.getTypeDefinitionCreator(),
			hf.getTypeDescriptionResolverProvider());
				
		final ComHostChannelDynamic<ComConnection> channel = new ComHostChannelDynamic<>(
			pm,
			connection,
			protocol,
			parent,
			thm,
			typeDefinitionBuilder,
			this.foundation.getTypeHandlerEnsurer()
		);
		
		final ComTypeMappingResolver tmr = new ComTypeMappingResolver(
			hf.getTypeDictionaryAssembler(),
			connection,
			protocol.typeDictionary(),
			thm,
			typeDefinitionBuilder
		);
		tmr.resolveHost();
		
		return channel;
	}
	
	@Override
	public ComClientChannel<ComConnection> createClientChannel(
		final ComConnection            connection,
		final ComProtocol              protocol  ,
		final ComClient<ComConnection> parent
	)
	{
		final BinaryPersistenceFoundation<?> clientFoundation = this.provideClientPersistenceFoundation(connection, protocol);
		final PersistenceTypeHandlerManager<Binary> thm = clientFoundation.getTypeHandlerManager();
		
		final ComTypeDefinitionBuilder typeDefinitionBuilder = new ComTypeDefinitionBuilder(
			clientFoundation.getTypeDictionaryParser(),
			clientFoundation.getTypeDefinitionCreator(),
			clientFoundation.getTypeDescriptionResolverProvider()
		);
		
		final PersistenceManager<?> pm = clientFoundation.createPersistenceManager();
	
		final ComClientChannelDynamic<ComConnection> channel = new ComClientChannelDynamic<>(
			pm,
			connection,
			protocol,
			parent,
			thm,
			typeDefinitionBuilder,
			this.foundation.getTypeHandlerEnsurer()
		);
		
		final ComTypeMappingResolver tmr = new ComTypeMappingResolver
		(
			clientFoundation.getTypeDictionaryAssembler(),
			connection,
			protocol.typeDictionary(),
			thm,
			typeDefinitionBuilder
		);
		tmr.resolveClient();
		
		return channel;
	}
	

	public PersistenceTypeDictionary provideTypeDictionaryInternal()
	{
		final PersistenceFoundation<?, ?> initFoundation = this.createInitializationFoundation();
		initFoundation.setTargetByteOrder      (this.hostByteOrder());
		initFoundation.setObjectIdProvider     (this.hostIdStrategy().createObjectIdProvider());
		initFoundation.setTypeIdProvider       (this.hostIdStrategy().createTypeIdProvider());
		initFoundation.setTypeMismatchValidator(Persistence.typeMismatchValidatorFailing());
		
		initFoundation.setTypeDictionaryManager(
			PersistenceTypeDictionaryManager.Transient(
				initFoundation.getTypeDictionaryCreator()
			)
		);
			
		final PersistenceIdStrategy idStrategy = this.hostInitializationIdStrategy();
		initFoundation.setObjectIdProvider(idStrategy.createObjectIdProvider());
		initFoundation.setTypeIdProvider(idStrategy.createTypeIdProvider());

		final PersistenceTypeHandlerManager<?> typeHandlerManager = initFoundation.getTypeHandlerManager();
		typeHandlerManager.initialize();
		
		this.iterateEntityTypes(c ->
			typeHandlerManager.ensureTypeHandler(c)
		);
		
		typeHandlerManager.iteratePerIds((k,v) ->
		{
			if(!v.isPrimitive() && typeHandlerManager.lookupTypeHandler(v) == null)
			{
				try
				{
					typeHandlerManager.ensureTypeHandler(v);
				}
				catch (final Exception e)
				{
					throw new ComException("Failed to ensure type handler for type " + v.getName(), e);
				}
			}
		});
		
		return typeHandlerManager.typeDictionary();
	}
	
	@Override
	public PersistenceTypeDictionaryView provideTypeDictionary()
	{
		if(this.cachedTypeDictionary == null)
		{
			synchronized(this)
			{
				// recheck after synch
				if(this.cachedTypeDictionary == null)
				{
					this.cachedTypeDictionary = this.provideTypeDictionaryInternal();
				}
			}
		}
		
		return this.cachedTypeDictionary.view();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Creator methods //
	////////////////////
	
	public static ComPersistenceAdaptorCreator<ComConnection> Creator()
	{
		return Creator(
			BinaryPersistenceFoundation.New()
		);
	}
	
	public static ComPersistenceAdaptorCreator<ComConnection> Creator(
			final BinaryPersistenceFoundation<?> foundation
		)
	{
		return Creator(
			foundation,
			BufferSizeProvider.New()
		);
	}
		
	public static ComPersistenceAdaptorCreator<ComConnection> Creator(
		final BinaryPersistenceFoundation<?> foundation        ,
		final BufferSizeProvider             bufferSizeProvider
	)
	{
		return new ComPersistenceAdaptorBinaryDynamicCreator(
			notNull(foundation)        ,
			notNull(bufferSizeProvider)
		);
	}

	private static PersistenceTypeDictionaryLoader createNoOpDictionaryLoader()
	{
		return new PersistenceTypeDictionaryLoader()
		{
			@Override
			public String loadTypeDictionary()
			{
				//No OP
				return null;
			}
		};
	}
	
	private static PersistenceTypeDictionaryStorer createNoOpTypDictionaryStorer()
	{
		return new PersistenceTypeDictionaryStorer()
		{
			@Override
			public void storeTypeDictionary(final String typeDictionaryString)
			{
				// NO OP
			}
		};
	}
	
}
