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

/**
 * Bidirectional handle for the persisted type dictionary &mdash; reads it via
 * {@link PersistenceTypeDictionaryLoader} and writes it via {@link PersistenceTypeDictionaryStorer}.
 * <p>
 * Currently a pure typing interface combining both halves: implementations supply both reading and writing for
 * the same underlying dictionary location (typically a file).
 *
 * @see PersistenceTypeDictionaryLoader
 * @see PersistenceTypeDictionaryStorer
 * @see PersistenceTypeDictionaryFileHandler
 */
public interface PersistenceTypeDictionaryIoHandler
extends PersistenceTypeDictionaryLoader, PersistenceTypeDictionaryStorer
{
	// just a typing interface so far

	/**
	 * Factory for {@link PersistenceTypeDictionaryIoHandler} instances. Indirection so that the underlying
	 * dictionary location (e.g. the {@link AFile} to use) can be resolved lazily, after the surrounding
	 * persistence foundation has finished bootstrapping.
	 */
	public interface Provider
	{
		/**
		 * Provides an I/O handler with no additional write listener; equivalent to
		 * {@link #provideTypeDictionaryIoHandler(PersistenceTypeDictionaryStorer) provideTypeDictionaryIoHandler(null)}.
		 *
		 * @return the I/O handler.
		 */
		public default PersistenceTypeDictionaryIoHandler provideTypeDictionaryIoHandler()
		{
			return this.provideTypeDictionaryIoHandler(null);
		}

		/**
		 * Provides an I/O handler that additionally forwards every successful
		 * {@link #storeTypeDictionary(String) store} to the passed listener, e.g. for backup or replication
		 * scenarios.
		 *
		 * @param writeListener an optional listener notified after every successful write, or {@code null}.
		 *
		 * @return the I/O handler.
		 */
		public PersistenceTypeDictionaryIoHandler provideTypeDictionaryIoHandler(
			PersistenceTypeDictionaryStorer writeListener
		);


		/**
		 * Skeleton {@link Provider} that delegates the choice of dictionary file to subclasses
		 * (via {@link #defineTypeDictionaryFile()}) and constructs file-backed I/O handlers through a
		 * supplied {@link PersistenceTypeDictionaryFileHandler.Creator}.
		 */
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
