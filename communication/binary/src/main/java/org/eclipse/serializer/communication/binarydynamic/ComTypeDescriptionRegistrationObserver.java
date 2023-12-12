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

import org.eclipse.serializer.communication.types.ComChannel;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionRegistrationObserver;


public class ComTypeDescriptionRegistrationObserver implements PersistenceTypeDefinitionRegistrationObserver
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
		
	private final ComChannel comChannel;
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComTypeDescriptionRegistrationObserver(final ComChannel comChannel)
	{
		super();
		this.comChannel = comChannel;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
				
	@Override
	public void observeTypeDefinitionRegistration(final PersistenceTypeDefinition typeDefinition)
	{
		this.comChannel.send(new ComMessageNewType(typeDefinition));
	}
}
