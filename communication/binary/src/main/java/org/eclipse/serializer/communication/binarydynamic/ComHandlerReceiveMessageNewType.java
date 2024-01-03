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

import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.equality.Equalator;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionMember;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandler;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandlerEnsurer;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandlerManager;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

public class ComHandlerReceiveMessageNewType implements ComHandlerReceive<ComMessageNewType>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	private final static Logger logger = Logging.getLogger(ComHandlerReceiveMessageNewType.class);
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceTypeHandlerManager<Binary> typeHandlerManager  ;
	private final ComTypeDefinitionBuilder              typeDefintionBuilder;
	private final PersistenceTypeHandlerEnsurer<Binary> typeHandlerEnsurer  ;
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComHandlerReceiveMessageNewType(
		final PersistenceTypeHandlerManager<Binary> typeHandlerManager  ,
		final ComTypeDefinitionBuilder              typeDefintionBuilder,
		final PersistenceTypeHandlerEnsurer<Binary> typeHandlerEnsurer
	)
	{
		super();
		this.typeHandlerManager         = typeHandlerManager  ;
		this.typeDefintionBuilder       = typeDefintionBuilder;
		this.typeHandlerEnsurer         = typeHandlerEnsurer  ;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	final Equalator<PersistenceTypeDescriptionMember> memberValidator = (m1, m2) ->
	{
		if(m1 == null || m2 == null)
		{
			return false;
		}

		if(m1.equalsStructure(m2))
		{
			return true;
		}

		return false;
	};
	
	@Override
	public Void processMessage(final ComMessageNewType message)
	{
		final String typeEntry = message.typeEntry();
		logger.debug("received new type entry: \n {}", typeEntry);
		
		final XGettingSequence<PersistenceTypeDefinition> defs = this.typeDefintionBuilder.buildTypeDefinitions(typeEntry);
		for (final PersistenceTypeDefinition ptd : defs)
		{
			if(ptd.type() != null)
			{
				final PersistenceTypeHandler<Binary, ?> handler = this.typeHandlerManager.lookupTypeHandler(ptd.type());
				
				if(handler != null)
				{
					if(PersistenceTypeDescriptionMember.equalMembers(ptd.allMembers(), handler.allMembers(), this.memberValidator))
					{
						logger.trace("handler for type {}, typeId {} already registered",ptd.type(), ptd.typeId());
					}
					else
					{
						logger.trace("trying to create legacy type handler for type {}, typeId {}",ptd.type(), ptd.typeId());
						this.typeHandlerManager.updateCurrentHighestTypeId(ptd.typeId());
						this.typeHandlerManager.ensureLegacyTypeHandler(ptd, handler);
					}
				}
				else
				{
					final PersistenceTypeHandler<Binary, ?> th = this.typeHandlerEnsurer.ensureTypeHandler(ptd.type());
									
					if(PersistenceTypeDescriptionMember.equalMembers(ptd.allMembers(), th.allMembers(), this.memberValidator))
					{
						logger.trace("trying to create type handler for new type {}, typeId {}",ptd.type(), ptd.typeId());
						this.typeHandlerManager.ensureTypeHandler(ptd.type());
					}
					else
					{
						logger.trace("trying to create legacy type handler for new type {}, typeId {}",ptd.type(), ptd.typeId());
						this.typeHandlerManager.updateCurrentHighestTypeId(ptd.typeId());
						this.typeHandlerManager.ensureLegacyTypeHandler(ptd, th);
					}
				}
			}
			else
			{
				logger.error("Failed to resolve new type {}", ptd.typeName());
				throw new ComExceptionRemoteClassNotFound(ptd.typeName());
			}
		}
	
		return null;
	}
	
	@Override
	public Object processMessage(final Object messageObject)
	{
		final ComMessageNewType message = (ComMessageNewType)messageObject;
		return this.processMessage(message);
	}

	@Override
	public boolean continueReceiving()
	{
		return true;
	}
}
