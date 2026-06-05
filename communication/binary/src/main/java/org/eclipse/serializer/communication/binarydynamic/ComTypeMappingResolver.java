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

import java.nio.ByteBuffer;

import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.chars.XChars;
import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.com.ComException;
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

	/*
	 * Hardening limits for the dynamic type-mapping handshake. The host reads the length and count
	 * header fields from an unauthenticated peer and would otherwise allocate a direct buffer of the
	 * declared size before validating anything (CWE-770 / CWE-400). These bounds reject malformed,
	 * negative-sized and excessively large messages before any allocation takes place.
	 */

	// the two header fields (length + count) are the minimum a well-formed message can contain
	protected static final int MIN_MESSAGE_LENGTH = LENGTH_CHAR_COUNT + LENGTH_CHAR_COUNT;

	// generous upper bound on a single type-mapping message; far larger than any realistic type dictionary
	protected static final int MAX_MESSAGE_LENGTH = 64 * 1024 * 1024;

	// upper bound on the number of declared type definitions in a single message
	protected static final int MAX_TYPE_COUNT     = 1_000_000;
		
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

		final int length = this.readHeaderField("length");
		final int count  = this.readHeaderField("count");

		this.validateTypeMappingHeader(length, count);

		if(count > 0 )
		{
			final int bodyLength = length - LENGTH_CHAR_COUNT - LENGTH_CHAR_COUNT;
			final ByteBuffer typeDefinitionsBuffer = XMemory.allocateDirectNative(bodyLength);
			this.connection.read(typeDefinitionsBuffer, bodyLength);
			return typeDefinitionsBuffer;
		}

		return null;
	}

	/**
	 * Reads a single fixed-width {@link #LENGTH_CHAR_COUNT}-character ASCII decimal header field
	 * and parses it. The decoded characters are verified to be decimal digits before parsing so
	 * that malformed (non-numeric or signed) handshake data is rejected with a clear
	 * {@link ComException} instead of relying on {@link NumberFormatException} or producing a
	 * negative value.
	 */
	private int readHeaderField(final String fieldName)
	{
		final ByteBuffer buffer = XMemory.allocateDirectNative(LENGTH_CHAR_COUNT);
		this.connection.read(buffer, LENGTH_CHAR_COUNT);

		buffer.position(0);
		final String digits = XChars.standardCharset().decode(buffer).toString();

		for(int i = 0; i < digits.length(); i++)
		{
			final char c = digits.charAt(i);
			if(c < '0' || c > '9')
			{
				throw new ComException("Invalid type mapping " + fieldName + " field: non-numeric handshake data");
			}
		}

		return Integer.parseInt(digits);
	}

	/**
	 * Validates the type-mapping message header before any body buffer is allocated. Guards against
	 * negative-sized allocations (length below the header size), excessively large allocations
	 * (length above {@link #MAX_MESSAGE_LENGTH}) and inconsistent or oversized type counts.
	 */
	private void validateTypeMappingHeader(final int length, final int count)
	{
		if(length < MIN_MESSAGE_LENGTH)
		{
			throw new ComException("Type mapping message length " + length
				+ " is below the minimum of " + MIN_MESSAGE_LENGTH);
		}
		if(length > MAX_MESSAGE_LENGTH)
		{
			throw new ComException("Type mapping message length " + length
				+ " exceeds the maximum of " + MAX_MESSAGE_LENGTH);
		}
		if(count > MAX_TYPE_COUNT)
		{
			throw new ComException("Type mapping type count " + count
				+ " exceeds the maximum of " + MAX_TYPE_COUNT);
		}

		// every declared type definition requires at least one body byte; reject inconsistent headers
		final int bodyLength = length - LENGTH_CHAR_COUNT - LENGTH_CHAR_COUNT;
		if(count > bodyLength)
		{
			throw new ComException("Type mapping type count " + count
				+ " is inconsistent with the declared body length " + bodyLength);
		}
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
