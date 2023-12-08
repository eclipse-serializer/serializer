package org.eclipse.serializer.communication.binarydynamic;

import java.nio.ByteBuffer;

import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.chars.XChars;
import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.communication.types.ComConnection;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescription;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryAssembler;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryView;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandler;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandlerManager;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

/**
 * This class handles the matching of types that have been modified on either the client or the host side
 * Including the necessary data transfer during the initialization of the ComChannels.
 *
 */
public class ComTypeMappingResolver
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	protected static final int LENGTH_CHAR_COUNT = 8;
		
	private final static Logger logger = Logging.getLogger(ComHandlerSendMessageNewType.class);
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	protected final PersistenceTypeDictionaryAssembler    typeDictionaryAssembler;
	protected final ComConnection                         connection;
	protected final PersistenceTypeDictionaryView         hostTypeDictionary;
	protected final PersistenceTypeHandlerManager<Binary> typeHandlerManager;
	protected final ComTypeDefinitionBuilder              typeDefinitionBuilder;
		
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Constructs a ComTypeMappingResolver instance
	 * 
	 * @param typeDictionaryAssembler PersistenceTypeDictionaryAssembler
	 * @param connection              connection
	 * @param hostTypeDictionary      PersistenceTypeDictionaryView
	 * @param typeHandlerManager      PersistenceTypeHandlerManager
	 * @param typeDefinitionBuilder   ComTypeDefinitionBuilder
	 */
	public ComTypeMappingResolver(
		final PersistenceTypeDictionaryAssembler    typeDictionaryAssembler,
		final ComConnection                         connection             ,
		final PersistenceTypeDictionaryView         hostTypeDictionary     ,
		final PersistenceTypeHandlerManager<Binary> typeHandlerManager     ,
		final ComTypeDefinitionBuilder              typeDefinitionBuilder
	)
	{
		super();
		this.typeDictionaryAssembler = typeDictionaryAssembler;
		this.connection              = connection             ;
		this.hostTypeDictionary      = hostTypeDictionary     ;
		this.typeHandlerManager      = typeHandlerManager     ;
		this.typeDefinitionBuilder   = typeDefinitionBuilder  ;
	}

	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	/**
	 * Handle the client's side of the communication type mapping during connection initialization phase.
	 * This is collection all type definition that belong to the clients classes that needs to be mapped by the host
	 * and transferring those to the host.
	 */
	public void resolveClient()
	{
		logger.debug("resolving client type mappings ");
		
		this.sendNewTypeDefintionsToHost(
			this.assembleTypeDefinitions(
				this.findHostTypeDefinitions()));
	}
	
	/**
	 * Handle the host's side of the communication type mapping during connection initialization phase.
	 * This is receiving the client's type definitions and creating the required legacy type handlers.
	 */
	public void resolveHost()
	{
		logger.debug("resolving host type mappings ");
		
		this.applyHostTypeMapping(
			this.parseClientTypeDefinitions(
				this.receiveUpdatedDefintionsfromClient()));
	}
	
	private void sendNewTypeDefintionsToHost(final byte[] assembledTypeDefinitions)
	{
		logger.trace("transfering new type defintions to host");
		
		final ByteBuffer dbb = XMemory.allocateDirectNative(assembledTypeDefinitions.length);
		final long dbbAddress = XMemory.getDirectByteBufferAddress(dbb);
		XMemory.copyArrayToAddress(assembledTypeDefinitions, dbbAddress);
		
		this.connection.writeCompletely(dbb);
	}

	private byte[] assembleTypeDefinitions(final BulkList<PersistenceTypeDescription> newDefinitions)
	{
		final VarString vs = VarString.New(10_000);
		
		vs
		.reset()
		.repeat(LENGTH_CHAR_COUNT, '0')
		.add(String.format("%08d", newDefinitions.intSize()));
		
		newDefinitions.forEach(definition -> {
			vs.add(this.assembleTypeDefintion(definition));
		});
		
		final char[] lengthString = XChars.readChars(XChars.String(vs.length()));
		vs.setChars(LENGTH_CHAR_COUNT - lengthString.length, lengthString);
		
		return vs.encode();
	}

	private VarString assembleTypeDefintion(final PersistenceTypeDescription definition)
	{
		final VarString vc = VarString.New();
		this.typeDictionaryAssembler.assembleTypeDescription(vc, definition);
		return vc;
	}

	private BulkList<PersistenceTypeDescription> findHostTypeDefinitions()
	{
		final BulkList<PersistenceTypeDescription> newTypeDescriptions = BulkList.New();
		
		this.typeHandlerManager.iterateLegacyTypeHandlers(legacyTypeHandler -> {
			final PersistenceTypeHandler<Binary, ?> currentHandler = this.typeHandlerManager.lookupTypeHandler(legacyTypeHandler.type());
			if(this.hostTypeDictionary.lookupTypeById(currentHandler.typeId()) == null)
			{
				newTypeDescriptions.add(currentHandler);
				logger.trace("new type found for id {}", currentHandler.typeId());
			}
		});
		
		logger.debug("{} new types found", newTypeDescriptions.size());
		return newTypeDescriptions;
	}
	
	private XGettingSequence<PersistenceTypeDefinition> parseClientTypeDefinitions(final ByteBuffer buffer)
	{
		if(buffer != null)
		{
			buffer.position(1);
			final char[] typeDefinitionsChars = XChars.standardCharset().decode(buffer).array();
		
			final String typeDefintions = XChars.String(typeDefinitionsChars);
			final XGettingSequence<PersistenceTypeDefinition> newTypeDescriptions = this.typeDefinitionBuilder.buildTypeDefinitions(typeDefintions);
			
			logger.debug("received {} types from client", newTypeDescriptions.size());
			return newTypeDescriptions;
		}
		
		logger.debug("received 0 types from client");
		return BulkList.New();
	}

	private ByteBuffer receiveUpdatedDefintionsfromClient()
	{
		logger.trace("receiving new type defintions from client");
		
		final ByteBuffer lengthBuffer = XMemory.allocateDirectNative(LENGTH_CHAR_COUNT);
		this.connection.read(lengthBuffer, LENGTH_CHAR_COUNT);
		
		lengthBuffer.position(0);
		final String lengthDigits = XChars.standardCharset().decode(lengthBuffer).toString();
		final int    length       = Integer.parseInt(lengthDigits);
		
		final ByteBuffer countBuffer = XMemory.allocateDirectNative(LENGTH_CHAR_COUNT);
		this.connection.read(countBuffer, LENGTH_CHAR_COUNT);
		countBuffer.position(0);
		final String countDigits = XChars.standardCharset().decode(countBuffer).toString();
		final int    count       = Integer.parseInt(countDigits);
		
		if(count > 0 )
		{
			final ByteBuffer typeDefinitionsBuffer = XMemory.allocateDirectNative(length - LENGTH_CHAR_COUNT - LENGTH_CHAR_COUNT);
			this.connection.read(typeDefinitionsBuffer, length - LENGTH_CHAR_COUNT - LENGTH_CHAR_COUNT);
			return typeDefinitionsBuffer;
		}
		
		return null;
	}
	
	private void applyHostTypeMapping(final XGettingSequence<PersistenceTypeDefinition> typeDefinitions)
	{
		if(typeDefinitions != null)
		{
			typeDefinitions.forEach( typeDefinition -> {
				final PersistenceTypeHandler<Binary, ?> currentHandler = this.typeHandlerManager.lookupTypeHandler(typeDefinition.type());
				this.typeHandlerManager.ensureLegacyTypeHandler(typeDefinition, currentHandler);
				this.typeHandlerManager.updateCurrentHighestTypeId(typeDefinition.typeId());
				
				logger.trace("type mapping applied for typeId {}", typeDefinition.typeId());
			});
		}
	}
}
