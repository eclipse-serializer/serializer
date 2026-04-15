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
 * try (BatchStorer storer = storageManager.batchStorerBuilder()
 *     .maxSize(100)
 *     .build())
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


	/**
	 * Pseudo-constructor method to create a new {@link Builder} bound to the given
	 * {@link Persister}.
	 *
	 * @param persister the persister the builder will create the {@link BatchStorer} from
	 * @return a new builder
	 */
	public static Builder Builder(final Persister persister)
	{
		return new Builder.Default(persister);
	}

	/**
	 * Fluent builder for {@link BatchStorer} instances. Promotes {@code maxSize},
	 * {@code flushCycle} and {@code checkInterval} to top-level methods and hides the
	 * {@link Controller} abstraction from user code.
	 * <p>
	 * At least one of {@link #maxSize(long)} and {@link #flushCycle(Duration)} must be
	 * configured before calling {@link #build()}. If both are set, a flush is
	 * triggered whenever either threshold is reached.
	 * <p>
	 * {@link #checkInterval(Duration)} defaults to {@code Duration.ofSeconds(1)}.
	 */
	public static interface Builder
	{
		/**
		 * Configures the maximum batch size that triggers a flush. Must be {@code > 0}.
		 *
		 * @param maxSize the maximum batch size that triggers a flush
		 * @return this builder
		 */
		public Builder maxSize(long maxSize);

		/**
		 * Configures the time interval after which a flush should occur. Must be {@code > 0ms}.
		 *
		 * @param flushCycle the time interval after which a flush should occur
		 * @return this builder
		 */
		public Builder flushCycle(Duration flushCycle);

		/**
		 * Configures the interval at which the background daemon thread checks for pending
		 * flushes. Must be {@code > 0ms}. Defaults to {@code Duration.ofSeconds(1)}.
		 *
		 * @param checkInterval the background flush check interval
		 * @return this builder
		 */
		public Builder checkInterval(Duration checkInterval);

		/**
		 * Builds the {@link BatchStorer}.
		 *
		 * @return the newly created {@link BatchStorer}
		 * @throws IllegalStateException if neither {@code maxSize} nor {@code flushCycle} was set
		 */
		public BatchStorer build();


		public static class Default implements Builder
		{
			private final Persister persister    ;
			private       long      maxSize      ; // 0 = not set
			private       Duration  flushCycle   ; // null = not set
			private       Duration  checkInterval = Duration.ofSeconds(1);

			Default(final Persister persister)
			{
				super();
				this.persister = notNull(persister);
			}

			@Override
			public Builder maxSize(final long maxSize)
			{
				this.maxSize = maxSize;
				return this;
			}

			@Override
			public Builder flushCycle(final Duration flushCycle)
			{
				this.flushCycle = flushCycle;
				return this;
			}

			@Override
			public Builder checkInterval(final Duration checkInterval)
			{
				this.checkInterval = checkInterval;
				return this;
			}

			@Override
			public BatchStorer build()
			{
				final Controller controller;
				if(this.maxSize > 0L && this.flushCycle != null)
				{
					controller = BatchStorer.Controller(this.maxSize, this.flushCycle);
				}
				else if(this.maxSize > 0L)
				{
					controller = BatchStorer.Controller(this.maxSize);
				}
				else if(this.flushCycle != null)
				{
					controller = BatchStorer.Controller(this.flushCycle);
				}
				else
				{
					throw new IllegalStateException(
						"At least one of maxSize or flushCycle must be set."
					);
				}

				return this.persister.createBatchStorer(controller, this.checkInterval);
			}
		}
	}

}
