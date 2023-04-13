package org.eclipse.serializer.afs.types;

/*-
 * #%L
 * Eclipse Serializer Abstract File System
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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.serializer.chars.XChars;
import org.eclipse.serializer.collections.HashEnum;
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.memory.XMemory;

public class AFS
{
	public static XGettingEnum<AItem> listItems(
		final ADirectory               directory,
		final Predicate<? super AItem> selector
	)
	{
		return listFiles(directory, selector, HashEnum.New());
	}


	public static XGettingEnum<ADirectory> listDirectories(
		final ADirectory                    directory,
		final Predicate<? super ADirectory> selector
	)
	{
		return listDirectories(directory, selector, HashEnum.New());
	}

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

	public static XGettingEnum<AFile> listFiles(
		final ADirectory               directory,
		final Predicate<? super AFile> selector
	)
	{
		return listFiles(directory, selector, HashEnum.New());
	}

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

	public static String readString(final AFile file)
	{
		return readString(file, XChars.standardCharset());
	}

	public static String readString(final AFile file, final Charset charSet)
	{
		final byte[] bytes = read_bytes(file);

		return XChars.String(bytes, charSet);
	}

	public static byte[] read_bytes(final AFile file)
	{
		final ByteBuffer content = file.useReading().readBytes();
		final byte[]     bytes   = XMemory.toArray(content);
		XMemory.deallocateDirectByteBuffer(content);

		return bytes;
	}


	public static <R> R applyWriting(
		final AFile                              file ,
		final Function<? super AWritableFile, R> logic
	)
	{
		return applyWriting(file, file.defaultUser(), logic);
	}

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
