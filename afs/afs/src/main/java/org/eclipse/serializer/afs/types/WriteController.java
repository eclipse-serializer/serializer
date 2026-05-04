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

import org.eclipse.serializer.afs.exceptions.AfsExceptionReadOnly;

/**
 * Writability gate consulted by {@link AFileSystem} and {@link AIoHandler} before performing any
 * write-side operation.
 * <p>
 * An {@link AFileSystem} that is gated by a non-writable {@link WriteController} rejects every
 * write attempt with {@link AfsExceptionReadOnly}. The interface itself is a single-method
 * functional interface ({@link #isWritable()}); {@link #validateIsWritable()} is the throw-on-fail
 * variant called from the I/O paths.
 *
 * @see AfsExceptionReadOnly
 * @see #Enabled()
 * @see #Disabled()
 */
@FunctionalInterface
public interface WriteController
{
	/**
	 * Throws an {@link AfsExceptionReadOnly} if writing is currently not permitted.
	 *
	 * @throws AfsExceptionReadOnly if {@link #isWritable()} returns {@code false}.
	 */
	public default void validateIsWritable()
	{
		if(this.isWritable())
		{
			return;
		}

		throw new AfsExceptionReadOnly("Writing is not enabled.");
	}

	/**
	 * Whether write-side operations are currently permitted.
	 *
	 * @return {@code true} if writing is enabled.
	 */
	public boolean isWritable();



	/**
	 * Returns a {@link WriteController} that always permits writing. {@link #validateIsWritable()}
	 * is a no-op on the returned instance.
	 *
	 * @return an enabled {@link WriteController}.
	 */
	public static WriteController Enabled()
	{
		// Singleton is (usually) an anti pattern.
		return new WriteController.Enabled();
	}

	/**
	 * {@link WriteController} that always permits writing.
	 */
	public final class Enabled implements WriteController
	{
		Enabled()
		{
			super();
		}

		@Override
		public final void validateIsWritable()
		{
			// no-op
		}

		@Override
		public final boolean isWritable()
		{
			return true;
		}

	}

	/**
	 * Returns a {@link WriteController} that rejects every write attempt with
	 * {@link AfsExceptionReadOnly}.
	 *
	 * @return a disabled {@link WriteController}.
	 */
	public static WriteController Disabled()
	{
		// Singleton is (usually) an anti pattern.
		return new WriteController.Disabled();
	}

	/**
	 * {@link WriteController} that rejects every write attempt.
	 */
	public final class Disabled implements WriteController
	{
		Disabled()
		{
			super();
		}

		@Override
		public final boolean isWritable()
		{
			return false;
		}

	}

}
