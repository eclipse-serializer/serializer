package org.eclipse.serializer.communication.binarydynamic;

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
