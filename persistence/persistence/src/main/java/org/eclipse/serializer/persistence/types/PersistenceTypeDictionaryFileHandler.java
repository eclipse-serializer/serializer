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

import org.eclipse.serializer.util.X;
import org.eclipse.serializer.afs.types.ADirectory;
import org.eclipse.serializer.afs.types.AFile;
import org.eclipse.serializer.afs.types.AReadableFile;
import org.eclipse.serializer.afs.types.AWritableFile;
import org.eclipse.serializer.chars.XChars;
import org.eclipse.serializer.io.XIO;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionSource;

/**
 * File-backed {@link PersistenceTypeDictionaryIoHandler} reading and writing the textual type dictionary
 * to a single {@link AFile} (typically located in the same directory as the persistent storage and named
 * after {@link Persistence#defaultFilenameTypeDictionary()}).
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
	 * Reads the textual type dictionary from {@code file}, returning {@code defaultString} if the file does
	 * not exist.
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
			if(!file.exists())
			{
				return defaultString;
			}
			
			final AReadableFile rFile = file.useReading();
			
			try
			{
				final ByteBuffer bb = rFile.readBytes();
				
				return XChars.String(bb, Persistence.standardCharset());
			}
			finally
			{
				rFile.release();
			}
		}
		catch(final Exception e)
		{
			throw new PersistenceExceptionSource(e);
		}
	}

	/**
	 * Writes the textual type dictionary to {@code file}, truncating any pre-existing content and creating the
	 * file if it does not exist yet. Bytes are encoded in {@link Persistence#standardCharset()}.
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
			final AWritableFile wFile = file.useWriting();
			if(wFile.exists())
			{
				wFile.truncate(0);
			}
			else
			{
				wFile.create();
			}
			
			try
			{
				final byte[] bytes = typeDictionaryString.getBytes(Persistence.standardCharset());
				final ByteBuffer dbb = XIO.wrapInDirectByteBuffer(bytes);
				wFile.writeBytes(X.List(dbb));
			}
			finally
			{
				wFile.release();
			}
		}
		catch(final Exception t)
		{
			throw new PersistenceException(t);
		}
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
