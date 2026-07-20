package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
 * %%
 * Copyright (C) 2026 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

/**
 * Marks a persistent instance that holds resources which must be released when the owning storage is
 * shut down — for example background threads, off-heap buffers or auxiliary on-disk files that are not
 * part of the storage's own data files and therefore are not reclaimed by shutting the storage down.
 * <p>
 * On shutdown the storage manager enumerates its object registry (already-loaded instances only, so no
 * {@code Lazy} subgraph is force-loaded) and invokes {@link #releaseOnShutdown()} on every registered
 * instance that implements this interface. This is an opt-in participation contract: a plain
 * {@code AutoCloseable} is deliberately <i>not</i> triggered, so implementing this interface is an
 * explicit statement that release-on-shutdown is desired.
 * <p>
 * Implementations must be idempotent (shutdown may follow an explicit release) and must not throw for
 * conditions the caller cannot act on; the storage manager treats the release pass as best-effort and
 * continues shutting down regardless.
 */
@FunctionalInterface
public interface PersistenceShutdownReleasable
{
	/**
	 * Releases resources held by this instance because its owning storage is being shut down.
	 * Must be idempotent and should not throw on already-released state.
	 */
	public void releaseOnShutdown();

}
