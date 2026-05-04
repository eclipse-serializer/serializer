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

import org.eclipse.serializer.io.BufferProvider;

/**
 * A handle representing a shared (reading) usage claim on an {@link AFile}, granted by the
 * {@link AccessManager} on behalf of a specific {@link AItem.Wrapper#user() user}.
 * <p>
 * The handle has two layered lifecycles:
 * <ul>
 *   <li>An <em>I/O</em> lifecycle ({@link #open()} / {@link #isOpen()} / {@link #close()})
 *       wrapping the underlying physical channel.</li>
 *   <li>An <em>AFS-management</em> lifecycle ({@link #release()} / {@link #retire()} /
 *       {@link #isRetired()}) that registers and unregisters the usage claim with the
 *       {@link AccessManager}.</li>
 * </ul>
 * {@link #release()} is the normal way to relinquish a handle: it closes the I/O channel and then
 * unregisters the claim. Once retired, the handle no longer represents a live access claim and
 * any further I/O operation throws {@link
 * org.eclipse.serializer.afs.exceptions.AfsExceptionRetiredFile}.
 *
 * @see AFile#useReading()
 * @see AccessManager
 * @see AWritableFile
 */
public interface AReadableFile extends AFile.Wrapper
{
	/* (31.05.2020 TM)NOTE: shortcut implementations for useReading?
	 * But beware:
	 * - Default user is defined in the accessmanager instance, so it must be used, anyway!
	 * - retired usage/wrapper instances might be used to create new, active ones. May not be suppressed!
	 * - More special cases? Thus: worth it?
	 */

	// ONLY the IO-Aspect, not the AFS-management-level aspect
	/**
	 * Opens the underlying physical channel for reading. Affects only the I/O lifecycle, not the
	 * usage claim with the {@link AccessManager}.
	 *
	 * @return {@code true} if the channel was opened by this call, {@code false} if it was already open.
	 */
	public default boolean open()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().openReading(this);
	}

	// ONLY the IO-Aspect, not the AFS-management-level aspect
	/**
	 * Whether the underlying physical channel is currently open.
	 *
	 * @return {@code true} if the channel is open.
	 */
	public default boolean isOpen()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().isOpen(this);
	}

	// ONLY the IO-Aspect, not the AFS-management-level aspect
	/**
	 * Closes the underlying physical channel. Affects only the I/O lifecycle; the usage claim with
	 * the {@link AccessManager} remains in place. Use {@link #release()} to relinquish the claim.
	 *
	 * @return {@code true} if the channel was closed by this call, {@code false} if it was already closed.
	 */
	public default boolean close()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().close(this);
	}

	// implicitly #close PLUS the AFS-management-level aspect
	/**
	 * Closes the underlying channel and unregisters this handle's usage claim with the
	 * {@link AccessManager}. After {@code release}, the handle is retired and must not be used for
	 * further I/O.
	 *
	 * @return {@code true} if the channel was open and got closed by this call.
	 */
	public default boolean release()
	{
		final boolean wasClosed = this.close();

		this.fileSystem().accessManager().unregister(this);

		return wasClosed;
	}

	@Override
	public default long size()
	{
		// synchronization handled by IoHandler.
		return this.fileSystem().ioHandler().size(this);
	}



	/**
	 * Reads the entire content of the file into a newly allocated direct {@link ByteBuffer}.
	 *
	 * @return a buffer containing the file's content.
	 */
	public default ByteBuffer readBytes()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this);
	}

	/**
	 * Reads the file's content from {@code position} to the end into a newly allocated direct
	 * {@link ByteBuffer}.
	 *
	 * @param position the byte offset to start reading from.
	 *
	 * @return a buffer containing the read bytes.
	 */
	public default ByteBuffer readBytes(final long position)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, position);
	}

	/**
	 * Reads {@code length} bytes starting at {@code position} into a newly allocated direct
	 * {@link ByteBuffer}.
	 *
	 * @param position the byte offset to start reading from.
	 * @param length   the number of bytes to read.
	 *
	 * @return a buffer containing the read bytes.
	 */
	public default ByteBuffer readBytes(final long position, final long length)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, position, length);
	}


	/**
	 * Reads as many bytes as fit into {@code targetBuffer} starting at the file's beginning.
	 *
	 * @param targetBuffer the buffer to read into.
	 *
	 * @return the number of bytes actually read.
	 */
	public default long readBytes(final ByteBuffer targetBuffer)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, targetBuffer);
	}

	/**
	 * Reads as many bytes as fit into {@code targetBuffer} starting at the given {@code position}.
	 *
	 * @param targetBuffer the buffer to read into.
	 * @param position     the byte offset to start reading from.
	 *
	 * @return the number of bytes actually read.
	 */
	public default long readBytes(final ByteBuffer targetBuffer, final long position)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, targetBuffer, position);
	}

	/**
	 * Reads up to {@code length} bytes starting at {@code position} into {@code targetBuffer}.
	 *
	 * @param targetBuffer the buffer to read into.
	 * @param position     the byte offset to start reading from.
	 * @param length       the maximum number of bytes to read.
	 *
	 * @return the number of bytes actually read.
	 */
	public default long readBytes(final ByteBuffer targetBuffer, final long position, final long length)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, targetBuffer, position, length);
	}


	/**
	 * Reads the file's content into buffers obtained from the passed {@link BufferProvider}.
	 *
	 * @param bufferProvider the provider supplying target buffers.
	 *
	 * @return the number of bytes read in total.
	 */
	public default long readBytes(final BufferProvider bufferProvider)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, bufferProvider);
	}

	/**
	 * Reads the file's content from {@code position} into buffers obtained from the passed
	 * {@link BufferProvider}.
	 *
	 * @param bufferProvider the provider supplying target buffers.
	 * @param position       the byte offset to start reading from.
	 *
	 * @return the number of bytes read in total.
	 */
	public default long readBytes(final BufferProvider bufferProvider, final long position)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, bufferProvider, position);
	}

	/**
	 * Reads up to {@code length} bytes from {@code position} into buffers obtained from the passed
	 * {@link BufferProvider}.
	 *
	 * @param bufferProvider the provider supplying target buffers.
	 * @param position       the byte offset to start reading from.
	 * @param length         the maximum number of bytes to read.
	 *
	 * @return the number of bytes read in total.
	 */
	public default long readBytes(final BufferProvider bufferProvider, final long position, final long length)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, bufferProvider, position, length);
	}



	/**
	 * Copies the entire content of this file to {@code target}.
	 *
	 * @param target the writable destination.
	 *
	 * @return the number of bytes copied.
	 */
	public default long copyTo(final AWritableFile target)
	{
		return this.actual().fileSystem().ioHandler().copyTo(this, target);
	}

	/**
	 * Copies the content of this file from {@code sourcePosition} to the end into {@code target}.
	 *
	 * @param target         the writable destination.
	 * @param sourcePosition the byte offset to start copying from.
	 *
	 * @return the number of bytes copied.
	 */
	public default long copyTo(final AWritableFile target, final long sourcePosition)
	{
		return this.actual().fileSystem().ioHandler().copyTo(this, sourcePosition, target);
	}

	/**
	 * Copies up to {@code length} bytes from {@code sourcePosition} of this file into {@code target}.
	 *
	 * @param target         the writable destination.
	 * @param sourcePosition the byte offset to start copying from.
	 * @param length         the maximum number of bytes to copy.
	 *
	 * @return the number of bytes copied.
	 */
	public default long copyTo(final AWritableFile target, final long sourcePosition, final long length)
	{
		return this.actual().fileSystem().ioHandler().copyTo(this, sourcePosition, length, target);
	}

	/**
	 * Marks this handle as retired. Called by the {@link AccessManager} during unregistration;
	 * normal client code should use {@link #release()} instead.
	 *
	 * @return {@code true} if the handle transitioned to retired by this call.
	 */
	public boolean retire();

	/**
	 * Whether this handle is retired and therefore no longer usable for I/O.
	 *
	 * @return {@code true} if the handle is retired.
	 */
	public boolean isRetired();

	/**
	 * Throws an {@link org.eclipse.serializer.afs.exceptions.AfsExceptionRetiredFile} if this
	 * handle is retired. Used by I/O implementations to fail fast on stale handles.
	 *
	 * @throws org.eclipse.serializer.afs.exceptions.AfsExceptionRetiredFile if {@link #isRetired()} returns {@code true}.
	 */
	public void validateIsNotRetired();

}
