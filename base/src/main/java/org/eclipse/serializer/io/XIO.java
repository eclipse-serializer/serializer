package org.eclipse.serializer.io;

/*-
 * #%L
 * Eclipse Serializer Base
 * %%
 * Copyright (C) 2023 Eclipse Foundation
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.serializer.chars.XChars;
import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.collections.XArrays;
import org.eclipse.serializer.exceptions.IORuntimeException;
import org.eclipse.serializer.functional.XFunc;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.util.UtilStackTrace;
import org.eclipse.serializer.util.X;

public final class XIO
{
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public static char fileSuffixSeparator()
	{
		return '.';
	}
	
	public static char filePathSeparator()
	{
		return '/';
	}
	public static String getFileSuffix(final Path file)
	{
		return getFileSuffix(getFileName(file));
	}
	
	public static String getFileSuffix(final String fileName)
	{
		if(XChars.hasNoContent(fileName))
		{
			return null;
		}
		
		final int fileSuffixSeparatorIndex = fileName.lastIndexOf(fileSuffixSeparator());
		if(fileSuffixSeparatorIndex < 0)
		{
			return null;
		}
		
		return fileName.substring(fileSuffixSeparatorIndex + 1);
	}
	
	public static <T> T unchecked(final IoOperationR<T> operation)
		throws IORuntimeException
	{
		try
		{
			return operation.executeR();
		}
		catch(final IOException e)
		{
			throw UtilStackTrace.cutStacktraceByOne(new IORuntimeException(e));
		}
	}
	
	/* (19.11.2019 TM)NOTE:
	 * "Path" is not the greatest idea on earth for a name to represent a file or a directory.
	 * "Path" is way too generic.
	 * It's explicitly not a generic "can-be-anything-Path", it is designed to represent a FileSystem file.
	 *
	 */

	public static final Path Path(final String... items)
	{
		return Path(FileSystems.getDefault(), items);
	}
	
	public static final Path Path(final FileSystem fileSystem, final String... items)
	{
		if(items == null)
		{
			// (07.03.2022 TM)NOTE: not sure what to do here in that case.
			throw new NullPointerException();
		}
		
		/*
		 * To work around the JDK behavior of conveniently ignoring empty strings in the path items.
		 * This is a critical bug if a leading separator is used to define an absolut path.
		 * Consider:
		 * - "/mydir" gets parsed to the separator-independent path items {"", "mydir"}.
		 * - that array is passed here and on to Paths#get
		 */
		if(items.length > 0 && "".equals(items[0]))
		{
			return fileSystem.getPath(Character.toString(XIO.filePathSeparator()), items);
		}
		
		/* (07.03.2022 TM)XXX: Explaining comment missing
		 * Why did this become necessary?
		 * The previous version...
		 * return fileSystem.getPath("", notNull(items));
		 * ... worked fine in tests.
		 * Also potential null pointer access warning.
		 * Since this is more complex code than the previous version, I added an explicit null check above.
		 */
		return items.length == 1
			? fileSystem.getPath(items[0])
			: fileSystem.getPath(items[0], Arrays.copyOfRange(items, 1, items.length))
		;
	}

	/**
	 * Creates a sub-path under the passed {@code parent} {@link Path} inside the same {@link FileSystem}.
	 * <p>
	 * Note that this is fundamentally different to {@link #Path(String...)} or {@link Paths#get(String, String...)}
	 * since those two end up using {@code FileSystems.getDefault()}, no matter the {@link FileSystem} that the passed
	 * parent {@link Path} is associated with.
	 * 
	 * @param  parent the {@code parent} {@link Path} of the new sub-path.
	 * @param  items the path items defining the sub-path under the passed {@code parent} {@link Path}.
	 * @return a sub-path under the passed {@code parent} {@link Path}.
	 */
	public static final Path Path(final Path parent, final String... items)
	{
		if(parent == null)
		{
			return Path(items);
		}
		
		return parent.getFileSystem().getPath(parent.toString(), items);
	}
	
	public static String getFileName(final Path file)
	{
		// because lol.
		return file != null
			? file.getFileName().toString()
			: null
		;
	}
	
	public static boolean isDirectory(final Path path) throws IOException
	{
		// file or directory
		return Files.isDirectory(path);
	}

	public static boolean exists(final Path path) throws IOException
	{
		// file or directory
		return Files.exists(path);
	}
	
	public static long size(final Path file) throws IOException
	{
		// file only
		return Files.size(file);
	}
	
	public static <C extends Consumer<? super Path>> C listEntries(
		final Path directory,
		final C    target
	)
		throws IOException
	{
		return iterateEntries(directory, target);
	}

	public static final <C extends Consumer<? super Path>> C listEntries(
			final Path                    directory,
			final C                       target   ,
			final Predicate<? super Path> selector
	)
			throws IOException
	{
		return iterateEntries(directory, target, selector);
	}

	public static Path[] listEntries(
			final Path                    directory,
			final Predicate<? super Path> selector
	)
			throws IOException
	{
		return listEntries(directory, BulkList.New(), selector).toArray(Path.class);
	}

	/**
	 * Warning: this (because of using Files.newDirectoryStream) does some weird file opening/locking stuff.
	 * <p>
	 * Also see: https://stackoverflow.com/questions/48311252/a-bit-strange-behaviour-of-files-delete-and-files-deleteifexists
	 * 
	 * @param <C> the consumer type
	 * @param directory the directory to iterate
	 * @param logic the itaration logic
	 * @return the given logic
	 * @throws IOException if an IO error occurs
	 */
	public static <C extends Consumer<? super Path>> C iterateEntries(
		final Path directory,
		final C    logic
	)
		throws IOException
	{
		return iterateEntries(directory, logic, XFunc.all());
	}
	
	/**
	 * Warning: this (because of using Files.newDirectoryStream) does some weird file opening/locking stuff.
	 * <p>
	 * Also see: https://stackoverflow.com/questions/48311252/a-bit-strange-behaviour-of-files-delete-and-files-deleteifexists
	 * 
	 * @param <C> the consumer type
	 * @param directory the directory to iterate
	 * @param logic the itaration logic
	 * @param selector filter predicate
	 * @return the given logic
	 * @throws IOException if an IO error occurs
	 */
	public static <C extends Consumer<? super Path>> C iterateEntries(
		final Path                    directory,
		final C                       logic    ,
		final Predicate<? super Path> selector
	)
		throws IOException
	{
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(directory))
		{
			for(final Path p : stream)
			{
				if(!selector.test(p))
				{
					continue;
				}
				logic.accept(p);
			}
		}
		
		return logic;
	}
	
	
	public static final <P extends Path> P ensureDirectory(final P directory) throws IOException
	{
		// Let's hope calling this on an already existing directory is not too much overhead ...
		Files.createDirectories(directory);

		return directory;
	}
	
	public static FileChannel openFileChannelReading(final Path file)
		throws IOException
	{
		return FileChannel.open(file, READ);
	}
	
	public static FileChannel openFileChannelWriting(final Path file, final OpenOption... options)
		throws IOException
	{
		return openFileChannel(file, XArrays.ensureContained(options, WRITE));
	}
	
	public static FileChannel openFileChannel(final Path file, final OpenOption... options)
		throws IOException
	{
		return FileChannel.open(file, options);
	}
	
	
	public static final <T> T readOneShot(final Path file, final IoOperationSR<FileChannel, T> operation)
		throws IOException
	{
		return XIO.performClosingOperation(
			openFileChannelReading(file),
			operation
		);
	}
	
	
	public static String readString(final Path file)
		throws IOException
	{
		return readString(file, XChars.standardCharset());
	}
	
	public static String readString(final Path file, final Charset charSet)
		throws IOException
	{
		final byte[] bytes = read_bytes(file);
		
		return XChars.String(bytes, charSet);
	}
	
	public static byte[] read_bytes(final Path file)
		throws IOException
	{
		final ByteBuffer content = read(file);
		final byte[]     bytes   = XMemory.toArray(content);
		XMemory.deallocateDirectByteBuffer(content);
		
		return bytes;
	}
	
	public static ByteBuffer read(final Path file) throws IOException
	{
		return readOneShot(file, XIO::read);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// java.nio.channels.FileChannel //
	//////////////////////////////////
	
	public static final ByteBuffer wrapInDirectByteBuffer(final byte[] bytes)
	{
		final ByteBuffer dbb = ByteBuffer.allocateDirect(bytes.length);
		dbb.put(bytes);
		dbb.flip();
		
		return dbb;
	}
	
	public static final <T> T performClosingOperation(
		final FileChannel                   fileChannel,
		final IoOperationSR<FileChannel, T> operation
	)
		throws IOException
	{
		try
		{
			return operation.executeSR(fileChannel);
		}
		finally
		{
			fileChannel.close();
		}
	}
		
	public static ByteBuffer read(final FileChannel fileChannel)
		throws IOException
	{
		return read(fileChannel, 0);
	}
	
	public static ByteBuffer read(
		final FileChannel fileChannel ,
		final long        filePosition
	)
		throws IOException
	{
		return read(fileChannel, filePosition, fileChannel.size());
	}
	
	public static ByteBuffer read(
		final FileChannel fileChannel ,
		final long        filePosition,
		final long        length
	)
		throws IOException
	{
		// always hilarious to see that a low-level IO-tool has a int size limitation. Geniuses.
		final ByteBuffer dbb = ByteBuffer.allocateDirect(X.checkArrayRange(length));
		
		read(fileChannel, dbb, filePosition, dbb.limit());
		
		dbb.flip();
		
		return dbb;
	}
	
	public static long read(
		final FileChannel fileChannel ,
		final ByteBuffer  targetBuffer,
		final long        filePosition,
		final long        length
	)
		throws IOException
	{
		if(targetBuffer.remaining() < length)
		{
			throw new IllegalArgumentException(
				"Provided target buffer has not enough space remaining to load the file content: "
				+ targetBuffer.remaining() + " < " + length
			);
		}
		
		return internalRead(fileChannel, targetBuffer, filePosition, length);
	}
	
	private static long internalRead(
		final FileChannel fileChannel ,
		final ByteBuffer  targetBuffer,
		final long        filePosition,
		final long        effectiveLength
	)
		throws IOException
	{
		if(effectiveLength == 0L)
		{
			/*
			 * no-op
			 */
			return 0L;
		}
		
		final int  targetLimit = X.checkArrayRange(targetBuffer.position() + effectiveLength);
		final long fileLength  = fileChannel.size();
		
		X.validateRange(fileLength, filePosition, effectiveLength);
		long fileOffset = filePosition;
		targetBuffer.limit(targetLimit);
		
		// reading should be done in one fell swoop, but better be sure
		long readCount = 0;
		while(targetBuffer.hasRemaining())
		{
			readCount += fileChannel.read(targetBuffer, fileOffset);
			fileOffset = filePosition + readCount;
		}

		return readCount;
	}
	
	
	/**
	 * Uses {@link #openFileChannelReading(Path)}, {@link #openFileChannelWriting(Path, OpenOption...)}
	 * and {@link #copyFile(FileChannel, FileChannel)} to copy the contents of the specified {@code sourceFile}
	 * to the specified {@code targetFile}.<br>
	 * <b>Important note</b>:<br>
	 * This method is a fix for the JDK method {@link Files#copy(Path, Path, java.nio.file.CopyOption...)},
	 * which throws an exception about another process having locked "the file" (without specifying
	 * which one it means) if the process owns a lock on the source file. Since this means the process locks
	 * itself out of using the source file if it has secured the source file for its exclusive use.
	 * As a consequence, the JDK method cannot be used if a file is locked and should generally not be
	 * trusted.
	 * <p>
	 * For any special needs like copying from and/or to a position and/or only a part of the file and/or using
	 * custom OpenOptions and/or modifying file timestamps and or performing pre- or post-actions, it is strongly
	 * suggested to write a custom tailored version of a copying method. Covering all conceivable cases would result
	 * in an overly complicated one-size-fits-all attempt and we all know how well those work in practice.
	 * 
	 * @param sourceFile the source file whose content shall be copied.
	 * @param targetFile the target file that shall receive the copied content. Must already exist!
	 * @param targetChannelOpenOptions the {@link OpenOption}s (see {@link StandardOpenOption}) to be passed to
	 *        {@link #openFileChannelWriting(Path, OpenOption...)}. May be null / empty.
	 * 
	 * @return the number of bytes written by {@link FileChannel#transferFrom(java.nio.channels.ReadableByteChannel, long, long)}.
	 * 
	 * @throws IOException if an IO error occurs
	 * 
	 * @see StandardOpenOption
	 * @see #openFileChannelReading(Path)
	 * @see #copyFile(FileChannel, FileChannel)
	 * @see FileChannel#transferFrom(java.nio.channels.ReadableByteChannel, long, long)
	 */
	public static long copyFile(
		final Path          sourceFile              ,
		final Path          targetFile              ,
		final OpenOption... targetChannelOpenOptions
	)
		throws IOException
	{
		
		try(
			final FileChannel sourceChannel = openFileChannelReading(sourceFile);
			final FileChannel targetChannel = openFileChannelWriting(targetFile, targetChannelOpenOptions);
		)
		{
			return copyFile(sourceChannel, targetChannel);
		}
	}
	
	/**
	 * Alias for {@code targetChannel.transferFrom(sourceChannel, 0, sourceChannel.size())}.<br>
	 * 
	 * @param sourceChannel an open and readable channel to the source file whose content shall be copied.
	 * @param targetChannel an open and writeable channel to the target file that shall receive the copied content.
	 * 
	 * @return The number of bytes, possibly zero, that were actually transferred.
	 * 
	 * @throws IOException as specified by {@link FileChannel#transferFrom(java.nio.channels.ReadableByteChannel, long, long)}
	 * 
	 * @see FileChannel#transferFrom(java.nio.channels.ReadableByteChannel, long, long)
	 * @see #copyFile(Path, Path, OpenOption...)
	 */
	public static long copyFile(
		final FileChannel sourceChannel,
		final FileChannel targetChannel
	)
		throws IOException
	{
		return copyFile(sourceChannel, 0, targetChannel);
	}

	
	public static long copyFile(
		final FileChannel sourceChannel ,
		final long        sourcePosition,
		final FileChannel targetChannel
	)
		throws IOException
	{
		return copyFile(sourceChannel, sourcePosition, sourceChannel.size() - sourcePosition, targetChannel);
	}
	
	public static long copyFile(
		final FileChannel sourceChannel ,
		final long        sourcePosition,
		final long        length        ,
		final FileChannel targetChannel
	)
		throws IOException
	{
		return sourceChannel.transferTo(sourcePosition, length, targetChannel);
	}

	public static final boolean hasNoFiles(final Path directory) throws IOException
	{
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(directory))
		{
			return !stream.iterator().hasNext();
		}
		catch(final IOException e)
		{
			throw e;
		}
	}

	public static final <C extends Closeable> C close(
			final C         closable  ,
			final Throwable suppressed
	)
			throws IOException
	{
		if(closable == null)
		{
			return null;
		}

		try
		{
			closable.close();
		}
		catch(final IOException e)
		{
			if(suppressed != null)
			{
				e.addSuppressed(suppressed);
			}
			throw e;
		}

		return closable;
	}

	// breaks naming conventions intentionally to indicate a modification of called methods instead of a type
	public static final class unchecked
	{

		public static final long size(final FileChannel fileChannel) throws IORuntimeException
		{
			try
			{
				return fileChannel.size();
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		public static boolean isDirectory(final Path path) throws IORuntimeException
		{
			try
			{
				return XIO.isDirectory(path);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		public static final boolean exists(final Path path) throws IORuntimeException
		{
			try
			{
				return XIO.exists(path);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		public static final long size(final Path file) throws IORuntimeException
		{
			try
			{
				return XIO.size(file);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
		public static final <C extends Consumer<? super Path>> C listEntries(
			final Path directory,
			final C    target
		)
			throws IORuntimeException
		{
			try
			{
				return XIO.listEntries(directory, target);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}

		public static final <C extends Consumer<? super Path>> C listEntries(
				final Path                    directory,
				final C                       target   ,
				final Predicate<? super Path> selector
		)
				throws IORuntimeException
		{
			try
			{
				return XIO.listEntries(directory, target, selector);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}

		public static final <P extends Path> P ensureDirectory(final P directory) throws IORuntimeException
		{
			try
			{
				return XIO.ensureDirectory(directory);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}

		public static final boolean hasNoFiles(final Path directory) throws IORuntimeException
		{
			try
			{
				return XIO.hasNoFiles(directory);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}

		public static final <C extends Closeable> C close(
				final C         closable  ,
				final Throwable suppressed
		)
				throws IORuntimeException
		{
			try
			{
				return XIO.close(closable, suppressed);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}


		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		/**
		 * Dummy constructor to prevent instantiation of this static-only utility class.
		 * 
		 * @throws UnsupportedOperationException when called
		 */
		private unchecked()
		{
			// static only
			throw new UnsupportedOperationException();
		}
		
	}
		
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private XIO()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
