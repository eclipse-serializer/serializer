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

import org.eclipse.serializer.util.X;

/**
 * A handle representing an exclusive (writing) usage claim on an {@link AFile}, granted by the
 * {@link AccessManager} on behalf of a specific {@link AItem.Wrapper#user() user}. Extends
 * {@link AReadableFile} because exclusive access also permits reading.
 * <p>
 * In addition to the readable handle's lifecycle, a writable handle exposes content-mutating
 * operations: {@link #writeBytes(ByteBuffer)}, {@link #copyFrom(AReadableFile)},
 * {@link #create()}, {@link #delete()}, {@link #moveTo(ADirectory)}, {@link #truncate(long)}.
 * Use {@link #downgrade()} to convert this handle into a shared readable one without releasing
 * and re-acquiring; like its {@link AReadableFile} parent, the handle must be
 * {@link #release() released} when no longer needed.
 * <p>
 * Note that {@link #moveTo(AWritableFile)} and {@link #delete()} affect the underlying physical
 * file but leave this {@link AWritableFile} (the abstract notion of the file) valid; callers are
 * responsible for releasing the handle.
 *
 * @see AFile#useWriting()
 * @see AccessManager
 */
public interface AWritableFile extends AReadableFile
{
	@Override
	public default boolean open()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().openWriting(this);
	}

	/* (31.05.2020 TM)NOTE: shortcut implementations for useReading and useWriting?
	 * But beware:
	 * - Default user is defined in the accessmanager instance, so it must be used, anyway!
	 * - retired usage/wrapper instances might be used to create new, active ones. May not be suppressed!
	 * - More special cases? Thus: worth it?
	 */

	@Override
	public default AWritableFile useWriting(final Object user)
	{
		return this.fileSystem().accessManager().useWriting(this, user);
	}

	@Override
	public default AWritableFile useWriting()
	{
		return this.fileSystem().accessManager().useWriting(this);
	}

	@Override
	public default long copyTo(final AWritableFile target)
	{
		return this.actual().fileSystem().ioHandler().copyTo(this, target);
	}

	@Override
	public default long copyTo(final AWritableFile target, final long sourcePosition)
	{
		return this.actual().fileSystem().ioHandler().copyTo(this, sourcePosition, target);
	}

	@Override
	public default long copyTo(final AWritableFile target, final long sourcePosition, final long length)
	{
		return this.actual().fileSystem().ioHandler().copyTo(this, sourcePosition, length, target);
	}

	/**
	 * Copies the entire content of {@code source} into this file.
	 *
	 * @param source the readable source.
	 *
	 * @return the number of bytes copied.
	 */
	public default long copyFrom(final AReadableFile source)
	{
		return this.actual().fileSystem().ioHandler().copyFrom(source, this);
	}

	/**
	 * Copies the content of {@code source} from {@code sourcePosition} to its end into this file.
	 *
	 * @param source         the readable source.
	 * @param sourcePosition the byte offset to start copying from.
	 *
	 * @return the number of bytes copied.
	 */
	public default long copyFrom(final AReadableFile source, final long sourcePosition)
	{
		return this.actual().fileSystem().ioHandler().copyFrom(source, sourcePosition, this);
	}

	/**
	 * Copies up to {@code length} bytes from {@code source} starting at {@code sourcePosition}
	 * into this file.
	 *
	 * @param source         the readable source.
	 * @param sourcePosition the byte offset to start copying from.
	 * @param length         the maximum number of bytes to copy.
	 *
	 * @return the number of bytes copied.
	 */
	public default long copyFrom(final AReadableFile source, final long sourcePosition, final long length)
	{
		return this.actual().fileSystem().ioHandler().copyFrom(source, sourcePosition, length, this);
	}

	/**
	 * Writes the remaining bytes of {@code source} to this file.
	 *
	 * @param source the buffer holding the bytes to write.
	 *
	 * @return the number of bytes written.
	 */
	public default long writeBytes(final ByteBuffer source)
	{
		return this.writeBytes(X.Constant(source));
	}

	/**
	 * Writes the remaining bytes of every buffer in {@code sources} to this file in iteration
	 * order.
	 *
	 * @param sources the buffers holding the bytes to write.
	 *
	 * @return the number of bytes written in total.
	 */
	public default long writeBytes(final Iterable<? extends ByteBuffer> sources)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().writeBytes(this, sources);
	}

	/**
	 * Forces all previously written bytes of this file (and its length metadata) to physical
	 * storage, i.e. an fsync/{@link java.nio.channels.FileChannel#force(boolean) force} barrier.
	 * Backends whose write acknowledgement is already durable may implement this as a no-op.
	 */
	public default void synchronize()
	{
		// synchronization handled by IoHandler.
		this.actual().fileSystem().ioHandler().synchronize(this);
	}

	/**
	 * Creates the underlying physical file. The file must not already exist; use
	 * {@link #ensureExists()} for create-if-missing semantics.
	 */
	public default void create()
	{
		// synchronization handled by IoHandler.
		this.actual().fileSystem().ioHandler().create(this);
	}

	@Override
	public default boolean ensureExists()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().ensureExists(this);
	}

	/**
	 * Deletes the underlying physical file. The {@link AFile} this handle wraps remains valid as
	 * the abstract notion of the file and may be recreated.
	 *
	 * @return {@code true} if the physical file was deleted by this call.
	 */
	public default boolean delete()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().deleteFile(this);

		/* note:
		 * no release since an abstract file represents "the notion of a physical file".
		 * It can remain valid even after the physical file was removed.
		 */
	}

	/**
	 * Moves the underlying physical file into {@code targetDirectory}, preserving identifier,
	 * name and type. A target writable handle is acquired and released internally.
	 *
	 * @param targetDirectory the destination directory.
	 */
	public default void moveTo(final ADirectory targetDirectory)
	{
		final AFile targetFile = targetDirectory.ensureFile(this.identifier(), this.name(), this.type());

		final AWritableFile wFile = targetFile.useWriting();
		try
		{
			this.moveTo(wFile);
		}
		finally
		{
			wFile.release();
		}
	}

	/**
	 * Moves the underlying physical file to the file represented by {@code targetFile}. The
	 * {@link AFile} this handle wraps remains valid as the abstract notion of the file.
	 *
	 * @param targetFile a writable handle on the move destination.
	 */
	public default void moveTo(final AWritableFile targetFile)
	{
		// synchronization handled by IoHandler.
		this.actual().fileSystem().ioHandler().moveFile(this, targetFile);

		/* note:
		 * no release since an abstract file represents "the notion of a physical file".
		 * It can remain valid even after the physical file was removed.
		 */
	}

	/**
	 * Downgrades this exclusive (writing) usage claim into a shared (reading) one.
	 * <p>
	 * The returned readable handle takes over the access claim; the original writable handle is
	 * effectively replaced and must not be used for further I/O.
	 *
	 * @return a readable handle on the same file.
	 */
	public default AReadableFile downgrade()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().accessManager().downgrade(this);
	}

	/**
	 * Truncates the file to the passed size. If {@code newSize} exceeds the current size, the
	 * behavior depends on the underlying I/O implementation.
	 *
	 * @param newSize the new size in bytes.
	 */
	public default void truncate(final long newSize)
	{
		// synchronization handled by IoHandler.
		this.actual().fileSystem().ioHandler().truncate(this, newSize);
	}

}
