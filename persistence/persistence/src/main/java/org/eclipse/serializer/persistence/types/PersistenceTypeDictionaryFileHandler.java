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

import static org.eclipse.serializer.util.X.mayNull;
import static org.eclipse.serializer.util.X.notNull;

import java.nio.ByteBuffer;

import org.eclipse.serializer.afs.types.ADirectory;
import org.eclipse.serializer.afs.types.AFS;
import org.eclipse.serializer.afs.types.AFile;
import org.eclipse.serializer.afs.types.AWritableFile;
import org.eclipse.serializer.chars.XChars;
import org.eclipse.serializer.io.XIO;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionSource;

/**
 * File-backed {@link PersistenceTypeDictionaryIoHandler} reading and writing the textual type dictionary
 * to a single {@link AFile} (typically located in the same directory as the persistent storage and named
 * after {@link Persistence#defaultFilenameTypeDictionary()}). Writes are crash-safe via a temporary
 * sibling file, see {@link #writeTypeDictionary(AFile, String)}.
 * <p>
 * Optionally forwards every successful write to a delegate {@link PersistenceTypeDictionaryStorer} for backup
 * or replication purposes.
 *
 * @see PersistenceTypeDictionaryIoHandler
 * @see Persistence#defaultFilenameTypeDictionary()
 */
public class PersistenceTypeDictionaryFileHandler implements PersistenceTypeDictionaryIoHandler
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Reads the textual type dictionary from {@code file}, returning {@code null} if the file does not exist.
	 *
	 * @param file the dictionary file.
	 *
	 * @return the dictionary text, or {@code null} if the file does not exist.
	 *
	 * @throws PersistenceExceptionSource if reading fails.
	 */
	public static final String readTypeDictionary(final AFile file)
	{
		return readTypeDictionary(file, null);
	}

	/**
	 * Suffix of the temporary sibling file (same parent directory, named
	 * {@code <live file identifier> + suffix}) used by {@link #writeTypeDictionary(AFile, String)} to
	 * write the dictionary crash-safely before swapping it in place of the live file.
	 *
	 * @return the temporary dictionary file suffix.
	 */
	public static String temporaryFileSuffix()
	{
		return ".tmp";
	}

	private static AFile temporaryFile(final AFile file)
	{
		return file.parent().ensureFile(file.identifier() + temporaryFileSuffix());
	}

	/**
	 * Reads the textual type dictionary from {@code file}, returning {@code defaultString} if the file does
	 * not exist.
	 * <p>
	 * If {@code file} does not exist but its temporary sibling does, the sibling is read instead: it is a
	 * complete dictionary export whose swap was interrupted before the move, see
	 * {@link #writeTypeDictionary(AFile, String)}. A - possibly torn - temporary file alongside an
	 * existing live file is ignored.
	 *
	 * @param file          the dictionary file.
	 * @param defaultString the value to return when the file does not exist.
	 *
	 * @return the dictionary text or {@code defaultString}.
	 *
	 * @throws PersistenceExceptionSource if reading fails.
	 */
	public static final String readTypeDictionary(final AFile file, final String defaultString)
	{
		try
		{
			if(file.exists())
			{
				return readFileContent(file);
			}

			final AFile temporaryFile = temporaryFile(file);
			if(temporaryFile.exists())
			{
				return readFileContent(temporaryFile);
			}

			return defaultString;
		}
		catch(final Exception e)
		{
			throw new PersistenceExceptionSource(e);
		}
	}

	private static String readFileContent(final AFile file)
	{
		return AFS.apply(file, rFile ->
			XChars.String(rFile.readBytes(), Persistence.standardCharset())
		);
	}

	/**
	 * Writes the textual type dictionary to {@code file}, creating it if it does not exist yet.
	 * Bytes are encoded in {@link Persistence#standardCharset()}.
	 * <p>
	 * The write is crash-safe: an existing dictionary file is never modified in place. The new content is
	 * first written and synchronized to the temporary sibling file (see {@link #temporaryFileSuffix()}),
	 * which then replaces the live file (delete + move). A process or power failure at any point leaves
	 * either the previous or the complete new dictionary readable via
	 * {@link #readTypeDictionary(AFile, String)} - never a truncated dictionary as the only copy of a
	 * dictionary that data was committed against.
	 * <p>
	 * The very first write (neither a live nor a temporary file exists yet) goes directly to the live
	 * file and can be left torn by a crash. This is deliberate: it keeps the invariant that a SOLE
	 * temporary file is always a complete export (which the crash healing above relies on), and it is
	 * harmless - the export runs before the data commit that introduces the types, so at that point no
	 * data has ever been committed against any dictionary; a restart fails loudly on an empty storage.
	 * <p>
	 * Residual limitation: the swap's file system metadata (delete + move) cannot be explicitly forced on
	 * every backend, so a power loss may revert to the previous, complete dictionary.
	 *
	 * @param file                 the dictionary file.
	 * @param typeDictionaryString the dictionary text to write.
	 *
	 * @throws PersistenceException if writing fails.
	 */
	public static final void writeTypeDictionary(final AFile file, final String typeDictionaryString)
	{
		try
		{
			AFS.executeWriting(file, wFile ->
				AFS.executeWriting(temporaryFile(file), wTemporaryFile ->
					writeTypeDictionary(wFile, wTemporaryFile, typeDictionaryString)
				)
			);
		}
		catch(final PersistenceException e)
		{
			// pass through untranslated instead of wrapping again (e.g. the swap's delete failure)
			throw e;
		}
		catch(final Exception t)
		{
			throw new PersistenceException(t);
		}
	}

	private static void writeTypeDictionary(
		final AWritableFile wFile         ,
		final AWritableFile wTemporaryFile,
		final String        typeDictionaryString
	)
	{
		if(!wFile.exists())
		{
			if(!wTemporaryFile.exists())
			{
				// initial write: no previous dictionary to protect yet, write directly.
				writeContent(wFile, typeDictionaryString);

				return;
			}

			/*
			 * Crash healing: a sole temporary file is a completely written export whose swap was
			 * interrupted before the move. It must become the live file before being overwritten,
			 * or the rewrite below would destroy the only copy of the dictionary.
			 */
			wTemporaryFile.moveTo(wFile);
		}

		// the previous dictionary stays intact until the replacement is durably complete on the medium
		writeContent(wTemporaryFile, typeDictionaryString);

		if(!wFile.delete())
		{
			throw new PersistenceException(
				"Could not delete type dictionary file " + wFile.toPathString() + " to swap in its replacement."
			);
		}
		wTemporaryFile.moveTo(wFile);

		// commits the swap's file system metadata (delete + move) where the backend supports it; without
		// this, a power loss could revert to the previous dictionary although a later data commit survived.
		wFile.synchronize();
	}

	private static void writeContent(final AWritableFile wFile, final String typeDictionaryString)
	{
		if(!wFile.ensureExists())
		{
			wFile.truncate(0);
		}

		final ByteBuffer dbb = XIO.wrapInDirectByteBuffer(
			typeDictionaryString.getBytes(Persistence.standardCharset())
		);
		try
		{
			wFile.writeBytes(dbb);
		}
		finally
		{
			XMemory.deallocateDirectByteBuffer(dbb);
		}

		wFile.synchronize();
	}
	
	/**
	 * @deprecated use {@link #New(ADirectory)} instead.
	 *
	 * @param directory the directory to place the default-named dictionary file in.
	 *
	 * @return the new handler.
	 */
	@Deprecated
	public static PersistenceTypeDictionaryFileHandler NewInDirectory(final ADirectory directory)
	{
		return New(directory);
	}

	/**
	 * Creates a handler bound to the
	 * {@linkplain Persistence#defaultFilenameTypeDictionary() default-named} dictionary file in
	 * {@code directory}.
	 *
	 * @param directory the directory to place the dictionary file in.
	 *
	 * @return the new handler.
	 */
	public static PersistenceTypeDictionaryFileHandler New(final ADirectory directory)
	{
		return New(directory, null);
	}

	/**
	 * Creates a handler bound to the passed dictionary file.
	 *
	 * @param file the dictionary file; must not be {@code null}.
	 *
	 * @return the new handler.
	 */
	public static PersistenceTypeDictionaryFileHandler New(final AFile file)
	{
		return New(file, null);
	}

	/**
	 * @deprecated use {@link #New(ADirectory, PersistenceTypeDictionaryStorer)} instead.
	 *
	 * @param directory     the directory to place the default-named dictionary file in.
	 * @param writeListener optional listener notified after every successful write, or {@code null}.
	 *
	 * @return the new handler.
	 */
	@Deprecated
	public static PersistenceTypeDictionaryFileHandler NewInDirectory(
		final ADirectory                      directory    ,
		final PersistenceTypeDictionaryStorer writeListener
	)
	{
		return New(directory, writeListener);
	}


	/**
	 * Creates a handler bound to the
	 * {@linkplain Persistence#defaultFilenameTypeDictionary() default-named} dictionary file in
	 * {@code directory} and an optional write listener.
	 *
	 * @param directory     the directory to place the dictionary file in.
	 * @param writeListener optional listener notified after every successful write, or {@code null}.
	 *
	 * @return the new handler.
	 */
	public static PersistenceTypeDictionaryFileHandler New(
		final ADirectory                      directory    ,
		final PersistenceTypeDictionaryStorer writeListener
	)
	{
		return new PersistenceTypeDictionaryFileHandler(
			directory.ensureFile(Persistence.defaultFilenameTypeDictionary()),
			mayNull(writeListener)
		);
	}

	/**
	 * Creates a handler bound to the passed dictionary file and an optional write listener.
	 *
	 * @param file          the dictionary file; must not be {@code null}.
	 * @param writeListener optional listener notified after every successful write, or {@code null}.
	 *
	 * @return the new handler.
	 */
	public static PersistenceTypeDictionaryFileHandler New(
		final AFile                           file         ,
		final PersistenceTypeDictionaryStorer writeListener
	)
	{
		return new PersistenceTypeDictionaryFileHandler(
			notNull(file)         ,
			mayNull(writeListener)
		);
	}

	

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final AFile                           file         ;
	private final PersistenceTypeDictionaryStorer writeListener;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	PersistenceTypeDictionaryFileHandler(
		final AFile                           file         ,
		final PersistenceTypeDictionaryStorer writeListener
	)
	{
		super();
		this.file          = file         ;
		this.writeListener = writeListener;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////
	
	protected AFile file()
	{
		return this.file;
	}

	@Override
	public final synchronized String loadTypeDictionary()
	{
		return readTypeDictionary(this.file);
	}
	
	protected synchronized void writeTypeDictionary(final String typeDictionaryString)
	{
		writeTypeDictionary(this.file, typeDictionaryString);
	}

	@Override
	public final synchronized void storeTypeDictionary(final String typeDictionaryString)
	{
		this.writeTypeDictionary(typeDictionaryString);
		if(this.writeListener != null)
		{
			this.writeListener.storeTypeDictionary(typeDictionaryString);
		}
	}
	
	
	/**
	 * Functional factory used by {@link PersistenceTypeDictionaryIoHandler.Provider.Abstract} to construct
	 * the handler given a resolved {@link AFile} and an optional write listener.
	 */
	@FunctionalInterface
	public interface Creator
	{
		/**
		 * Creates a {@link PersistenceTypeDictionaryIoHandler} bound to {@code file} and {@code writeListener}.
		 *
		 * @param file          the dictionary file.
		 * @param writeListener optional listener forwarded each successful write, or {@code null}.
		 *
		 * @return the new I/O handler.
		 */
		public PersistenceTypeDictionaryIoHandler createTypeDictionaryIoHandler(
			AFile                           file         ,
			PersistenceTypeDictionaryStorer writeListener
		);

	}


	/**
	 * Creates a {@link Provider} for the default-named dictionary file in {@code directory}.
	 *
	 * @param directory the directory to place the dictionary file in.
	 *
	 * @return the new provider.
	 */
	public static PersistenceTypeDictionaryFileHandler.Provider ProviderInDirectory(final ADirectory directory)
	{
		return new PersistenceTypeDictionaryFileHandler.Provider(
			directory.ensureFile(Persistence.defaultFilenameTypeDictionary())
		);
	}

	/**
	 * Creates a {@link Provider} bound to the passed dictionary file.
	 *
	 * @param file the dictionary file; must not be {@code null}.
	 *
	 * @return the new provider.
	 */
	public static PersistenceTypeDictionaryFileHandler.Provider Provider(final AFile file)
	{
		return new PersistenceTypeDictionaryFileHandler.Provider(
			notNull(file)
		);
	}

	/**
	 * {@link PersistenceTypeDictionaryIoHandler.Provider} that hands out file-backed handlers bound to a
	 * fixed {@link AFile}.
	 */
	public static final class Provider implements PersistenceTypeDictionaryIoHandler.Provider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final AFile file;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Provider(final AFile file)
		{
			super();
			this.file = file;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public PersistenceTypeDictionaryFileHandler provideTypeDictionaryIoHandler(
			final PersistenceTypeDictionaryStorer writeListener
		)
		{
			return PersistenceTypeDictionaryFileHandler.New(this.file, writeListener);
		}
		
	}

}
