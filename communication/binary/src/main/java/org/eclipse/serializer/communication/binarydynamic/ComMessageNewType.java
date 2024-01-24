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

import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryAssembler;

public class ComMessageNewType implements ComMessage
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private String typeEntry;
	private transient PersistenceTypeDefinition typeDefinition;
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComMessageNewType(final PersistenceTypeDefinition typeDefinition)
	{
		this.typeDefinition = typeDefinition;
		this.typeEntry = "";
		final PersistenceTypeDictionaryAssembler assembler = PersistenceTypeDictionaryAssembler.New();
		
		final VarString vc = VarString.New();
		assembler.assembleTypeDescription(vc, typeDefinition);
		this.typeEntry = vc.toString();
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public String typeEntry()
	{
		return this.typeEntry;
	}


	public PersistenceTypeDefinition typeDefinition()
	{
		return this.typeDefinition;
	}
	
	
}
