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

import org.eclipse.serializer.afs.types.AFile;

public interface PersistenceTypeDictionaryIoHandler
extends PersistenceTypeDictionaryLoader, PersistenceTypeDictionaryStorer
{
	// just a typing interface so far
		
	public interface Provider
	{
		public default PersistenceTypeDictionaryIoHandler provideTypeDictionaryIoHandler()
		{
			return this.provideTypeDictionaryIoHandler(null);
		}
		
		public PersistenceTypeDictionaryIoHandler provideTypeDictionaryIoHandler(
			PersistenceTypeDictionaryStorer writeListener
		);
		
		
		public abstract class Abstract implements PersistenceTypeDictionaryIoHandler.Provider
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			protected Abstract(final PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator)
			{
				super();
				this.fileHandlerCreator = fileHandlerCreator;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////

			protected abstract AFile defineTypeDictionaryFile();

			@Override
			public PersistenceTypeDictionaryIoHandler provideTypeDictionaryIoHandler(
				final PersistenceTypeDictionaryStorer writeListener
			)
			{
				/*
				 * (04.03.2019 TM)TODO: forced delegating API is not a clean solution.
				 * This is only a temporary solution. See the task containing "PersistenceDataFile".
				 */
				final AFile file = this.defineTypeDictionaryFile();
				
				return this.fileHandlerCreator.createTypeDictionaryIoHandler(file, writeListener);
			}
			
		}
		
	}
		
}
