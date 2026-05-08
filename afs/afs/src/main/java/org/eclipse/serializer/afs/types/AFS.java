package org.eclipse.serializer.afs.types;

/*-
 * #%L
 * Eclipse Serializer Abstract File System
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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.serializer.chars.XChars;
import org.eclipse.serializer.collections.HashEnum;
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.io.XIO;
import org.eclipse.serializer.memory.XMemory;

/**
 * Static utility class for the abstract file system: convenience helpers for listing directory
 * children, reading and writing file content, and bracketing I/O in
 * {@link AFile#useReading() acquire}/{@link AReadableFile#release() release} pairs.
 * <p>
 * Methods that take a {@link Function} or {@link Consumer} acquire the appropriate usage handle
 * through the file's {@link AccessManager}, run the supplied logic, and release the handle in a
 * {@code finally} block. They are the recommended entry points for one-shot reads and writes.
 *
 * @see AFile
 * @see AReadableFile
 * @see AWritableFile
 */
public class AFS
{
	/**
	 * Returns a snapshot of the items in {@code directory} matching {@code selector}.
	 *
	 * @param directory the directory to list.
	 * @param selector  the filter predicate.
	 *
	 * @return the matching items.
	 */
	public static XGettingEnum<AItem> listItems(
		final ADirectory               directory,
		final Predicate<? super AItem> selector
	)
	{
		return listFiles(directory, selector, HashEnum.New());
	}


	/**
	 * Returns a snapshot of the sub-directories of {@code directory} matching {@code selector}.
	 *
	 * @param directory the directory to list.
	 * @param selector  the filter predicate.
	 *
	 * @return the matching sub-directories.
	 */
	public static XGettingEnum<ADirectory> listDirectories(
		final ADirectory                    directory,
		final Predicate<? super ADirectory> selector
	)
	{
		return listDirectories(directory, selector, HashEnum.New());
	}

	/**
	 * Iterates the sub-directories of {@code directory}, passing each that matches {@code selector}
	 * to {@code collector}.
	 *
	 * @param <C>       the collector type.
	 * @param directory the directory to list.
	 * @param selector  the filter predicate.
	 * @param collector the collector to receive matching sub-directories.
	 *
	 * @return the passed collector for chaining.
	 */
	public static <C extends Consumer<? super ADirectory>> C listDirectories(
		final ADirectory                    directory,
		final Predicate<? super ADirectory> selector ,
		final C                             collector
	)
	{
		directory.iterateDirectories(f ->
		{
			if(selector.test(f))
			{
				collector.accept(f);
			}
		});

		return collector;
	}

	/**
	 * Returns a snapshot of the files in {@code directory} matching {@code selector}.
	 *
	 * @param directory the directory to list.
	 * @param selector  the filter predicate.
	 *
	 * @return the matching files.
	 */
	public static XGettingEnum<AFile> listFiles(
		final ADirectory               directory,
		final Predicate<? super AFile> selector
	)
	{
		return listFiles(directory, selector, HashEnum.New());
	}

	/**
	 * Iterates the files in {@code directory}, passing each that matches {@code selector} to
	 * {@code collector}.
	 *
	 * @param <C>       the collector type.
	 * @param directory the directory to list.
	 * @param selector  the filter predicate.
	 * @param collector the collector to receive matching files.
	 *
	 * @return the passed collector for chaining.
	 */
	public static <C extends Consumer<? super AFile>> C listFiles(
		final ADirectory               directory,
		final Predicate<? super AFile> selector ,
		final C                        collector
	)
	{
		directory.iterateFiles(f ->
		{
			if(selector.test(f))
			{
				collector.accept(f);
			}
		});

		return collector;
	}

	/**
	 * Reads the entire content of {@code file} as a string using the
	 * {@linkplain XChars#standardCharset() standard charset}.
	 *
	 * @param file the file to read.
	 *
	 * @return the file's content as a string.
	 */
	public static String readString(final AFile file)
	{
		return readString(file, XChars.standardCharset());
	}

	/**
	 * Reads the entire content of {@code file} as a string using the passed charset.
	 *
	 * @param file    the file to read.
	 * @param charSet the charset to decode with.
	 *
	 * @return the file's content as a string.
	 */
	public static String readString(final AFile file, final Charset charSet)
	{
		final byte[] bytes = read_bytes(file);

		return XChars.String(bytes, charSet);
	}

	/**
	 * Reads the entire content of {@code file} into a heap byte array.
	 *
	 * @param file the file to read.
	 *
	 * @return the file's content as a byte array.
	 */
	public static byte[] read_bytes(final AFile file)
	{
		final ByteBuffer content = file.useReading().readBytes();
		final byte[]     bytes   = XMemory.toArray(content);
		XMemory.deallocateDirectByteBuffer(content);

		return bytes;
	}


	/**
	 * Acquires an exclusive (writing) handle on {@code file} for its
	 * {@linkplain AFile#defaultUser() default user}, applies {@code logic} to it, and releases the
	 * handle in a {@code finally} block.
	 *
	 * @param <R>   the result type.
	 * @param file  the file to access.
	 * @param logic the function to apply.
	 *
	 * @return the value returned by {@code logic}.
	 */
	public static <R> R applyWriting(
		final AFile                              file ,
		final Function<? super AWritableFile, R> logic
	)
	{
		return applyWriting(file, file.defaultUser(), logic);
	}

	/**
	 * Acquires an exclusive (writing) handle on {@code file} for the passed user, applies
	 * {@code logic} to it, and releases the handle in a {@code finally} block.
	 *
	 * @param <R>   the result type.
	 * @param file  the file to access.
	 * @param user  the user identity.
	 * @param logic the function to apply.
	 *
	 * @return the value returned by {@code logic}.
	 */
	public static <R> R applyWriting(
		final AFile                              file ,
		final Object                             user ,
		final Function<? super AWritableFile, R> logic
	)
	{
		// no locking needed, here since the implementation of #useWriting has to cover that
		final AWritableFile writableFile = file.useWriting(user);
		try
		{
			return logic.apply(writableFile);
		}
		finally
		{
			writableFile.release();
		}
	}

	/**
	 * Acquires an exclusive (writing) handle on {@code file} for its
	 * {@linkplain AFile#defaultUser() default user}, runs {@code logic}, and releases the handle
	 * in a {@code finally} block.
	 *
	 * @param file  the file to access.
	 * @param logic the action to run.
	 */
	public static void executeWriting(
        final AFile file ,
        final Consumer<? super AWritableFile> logic
	)
	{
	    executeWriting(file, file.defaultUser(), logic);
	}

	/**
	 * Acquires an exclusive (writing) handle on {@code file} for the passed user, runs
	 * {@code logic}, and releases the handle in a {@code finally} block.
	 *
	 * @param file  the file to access.
	 * @param user  the user identity.
	 * @param logic the action to run.
	 */
	public static void executeWriting(
	        final AFile                           file ,
	        final Object                          user ,
	        final Consumer<? super AWritableFile> logic
	)
	{
	    // no locking needed, here since the implementation of #useWriting has to cover that
	    final AWritableFile writableFile = file.useWriting(user);
	    try
	    {
	        logic.accept(writableFile);
	    }
	    finally
	    {
	        writableFile.release();
	    }
	}


	/**
	 * Closes {@code file} if it is non-{@code null}, attaching {@code cause} as a suppressed
	 * exception on any thrown failure. Useful in catch blocks where the original failure must
	 * survive a secondary close failure.
	 *
	 * @param file  the readable handle to close, or {@code null}.
	 * @param cause the original cause to suppress with, or {@code null}.
	 */
	public static void close(final AReadableFile file, final Throwable cause)
	{
	    if(file == null)
	    {
	        return;
	    }

	    try
	    {
	        file.close();
	    }
	    catch(final Throwable t)
	    {
	        if(cause != null)
	        {
	            t.addSuppressed(cause);
	        }
	        throw t;
	    }
	}

	/**
	 * Acquires a shared (reading) handle on {@code file} for its
	 * {@linkplain AFile#defaultUser() default user}, runs {@code logic}, and releases the handle
	 * in a {@code finally} block.
	 *
	 * @param file  the file to access.
	 * @param logic the action to run.
	 */
	public static void execute(
	        final AFile                           file ,
	        final Consumer<? super AReadableFile> logic
	)
	{
	    final AReadableFile rFile = file.useReading();
	    try
	    {
	        logic.accept(rFile);
	    }
	    finally
	    {
	        rFile.release();
	    }
	}

	/**
	 * Writes {@code string} to {@code file} using the {@linkplain XChars#standardCharset()
	 * standard charset}.
	 *
	 * @param file   the file to write to.
	 * @param string the content to write.
	 *
	 * @return the number of bytes written.
	 */
	public static final long writeString(final AFile file, final String string)
	{
	    return writeString(file, string, XChars.standardCharset());
	}

	/**
	 * Writes {@code string} to {@code file} using the passed charset.
	 *
	 * @param file    the file to write to.
	 * @param string  the content to write.
	 * @param charset the charset to encode with.
	 *
	 * @return the number of bytes written.
	 */
	public static final long writeString(final AFile file, final String string, final Charset charset)
	{
	    final byte[] bytes = string.getBytes(charset);

	    return write_bytes(file, bytes);
	}

	/**
	 * Writes the remaining bytes of {@code bytes} to {@code file}, acquiring and releasing a
	 * writable handle internally.
	 *
	 * @param file  the file to write to.
	 * @param bytes the buffer holding the bytes to write.
	 *
	 * @return the number of bytes written.
	 */
	public static long writeBytes(
	        final AFile      file ,
	        final ByteBuffer bytes
	)
	{
	    final AWritableFile wFile = file.useWriting();
	    try
	    {
	        return wFile.writeBytes(bytes);
	    }
	    finally
	    {
	        wFile.release();
	    }
	}

	/**
	 * Writes the passed byte array to {@code file}, wrapping it in a temporary direct
	 * {@link ByteBuffer} that is deallocated before returning.
	 *
	 * @param file  the file to write to.
	 * @param bytes the bytes to write.
	 *
	 * @return the number of bytes written.
	 */
	public static final long write_bytes(final AFile file, final byte[] bytes)
	{
	    final ByteBuffer dbb = XIO.wrapInDirectByteBuffer(bytes);
	    final Long writeCount = writeBytes(file, dbb);
	    XMemory.deallocateDirectByteBuffer(dbb);

	    return writeCount;
	}

	/**
	 * Acquires a shared (reading) handle on {@code file} for its
	 * {@linkplain AFile#defaultUser() default user}, applies {@code logic} to it, and releases the
	 * handle in a {@code finally} block.
	 *
	 * @param <R>   the result type.
	 * @param file  the file to access.
	 * @param logic the function to apply.
	 *
	 * @return the value returned by {@code logic}.
	 */
	public static <R> R apply(
	        final AFile                              file ,
	        final Function<? super AReadableFile, R> logic
	)
	{
	    final AReadableFile rFile = file.useReading();
	    try
	    {
	        return logic.apply(rFile);
	    }
	    finally
	    {
	        rFile.release();
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
	private AFS()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
