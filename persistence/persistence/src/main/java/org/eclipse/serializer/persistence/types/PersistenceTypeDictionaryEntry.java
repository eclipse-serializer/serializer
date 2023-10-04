package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
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
import org.eclipse.serializer.collections.types.XGettingSequence;

/**
 * Data that ties a {@link PersistenceTypeDescription} to a biunique type id, aka a {@link PersistenceTypeIdentity}.
 * 
 * 
 *
 */
public interface PersistenceTypeDictionaryEntry extends PersistenceTypeDescription
{
	@Override
	public long   typeId();

	@Override
	public String typeName();

	@Override
	public XGettingSequence<? extends PersistenceTypeDescriptionMember> instanceMembers();

	
	
	public abstract class Abstract implements PersistenceTypeDictionaryEntry
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public String toString()
		{
			return PersistenceTypeDictionaryAssembler.New()
				.assembleTypeDescription(VarString.New(), this)
				.toString()
			;
		}

	}
	
}
