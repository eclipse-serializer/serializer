package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
 * %%
 * Copyright (C) 2023 - 2025 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.time.Duration;

import static org.eclipse.serializer.util.X.notNull;

/**
 * A {@link Storer} designed for write-heavy operations that accumulates store operations
 * and commits them in batches rather than individually.
 * <p>
 * Each store call always re-serializes explicitly passed root instances even if they have
 * already been registered in the current storer lifecycle. This ensures that mutable objects
 * (e.g. collections whose content changes between calls) always capture their current state.
 * Child objects encountered during graph traversal use lazy semantics and are only stored if
 * they are not yet known to the persistence context.
 * <p>
 * Data is committed to the backing storage when the configured {@link Controller} determines
 * it is time — based on elapsed time, accumulated size, or both. A background daemon thread
 * periodically checks for pending flushes. Flushing can also be triggered manually via
 * {@link #flush()} or {@link #commit()}.
 * <p>
 * {@code BatchStorer} implements {@link AutoCloseable}. On close, it flushes any remaining
 * pending data and releases resources.
 * <p>
 * Usage:
 * <pre>
 * try (BatchStorer storer = storageManager.createBatchStorer(
 *     BatchStorer.Controller(Duration.ofSeconds(1)),
 *     Duration.ofMillis(200)
 * ))
 * {
 *     for (Object item : items)
 *     {
 *         list.add(item);
 *         storer.storeAll(list, item);
 *     }
 * }
 * </pre>
 */
public interface BatchStorer extends PersistenceStorer, AutoCloseable
{
	/**
	 * Flushes accumulated data to the underlying storage. This method ensures that all
	 * buffered data is written and made persistent.
	 */
	public void flush();

	/**
	 * Returns whether this {@code BatchStorer} has pending data that has not yet been flushed.
	 *
	 * @return {@code true} if there is unflushed data; {@code false} otherwise
	 */
	public boolean hasPendingData();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close();


	/**
	 * Creates a {@code Controller} that triggers a flush after a specified duration.
	 *
	 * @param flushCycle the time interval after which a flush should occur
	 * @return a time-based {@code Controller}
	 */
	public static Controller Controller(final Duration flushCycle)
	{
		final long flushCycleMillis = notNull(flushCycle).toMillis();
		if (flushCycleMillis <= 0L)
		{
			throw new IllegalArgumentException(
				"flushCycle must be > 0ms, was " + flushCycle
			);
		}
		return (size, millisSinceLastFlush) ->
			millisSinceLastFlush >= flushCycleMillis
		;
	}

	/**
	 * Creates a {@code Controller} that triggers a flush when the accumulated size
	 * reaches a specified threshold.
	 *
	 * @param maxSize the maximum batch size that triggers a flush
	 * @return a size-based {@code Controller}
	 */
	public static Controller Controller(final long maxSize)
	{
		if (maxSize <= 0L)
		{
			throw new IllegalArgumentException(
				"maxSize must be > 0, was " + maxSize
			);
		}
		return (size, millisSinceLastFlush) ->
			size >= maxSize
		;
	}

	/**
	 * Creates a {@code Controller} that triggers a flush based on size and time constraints.
	 *
	 * @param maxSize    the maximum batch size that triggers a flush
	 * @param flushCycle the time interval after which a flush should occur
	 * @return a combined {@code Controller}
	 */
	public static Controller Controller(final long maxSize, final Duration flushCycle)
	{
		if (maxSize <= 0L)
		{
			throw new IllegalArgumentException(
				"maxSize must be > 0, was " + maxSize
			);
		}
		final long flushCycleMillis = notNull(flushCycle).toMillis();
		if (flushCycleMillis <= 0L)
		{
			throw new IllegalArgumentException(
				"flushCycle must be > 0ms, was " + flushCycle
			);
		}
		return (size, millisSinceLastFlush) ->
			size >= maxSize
				|| millisSinceLastFlush >= flushCycleMillis
			;
	}

	/**
	 * Controls when a {@link BatchStorer} should flush its accumulated data.
	 */
	public static interface Controller
	{
		/**
		 * Determines whether a flush should be triggered.
		 *
		 * @param size                 the current number of accumulated objects
		 * @param millisSinceLastFlush milliseconds elapsed since the last flush
		 * @return {@code true} if a flush should be triggered
		 */
		public boolean shouldFlush(long size, long millisSinceLastFlush);
	}

}
