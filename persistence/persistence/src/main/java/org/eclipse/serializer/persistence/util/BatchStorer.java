package org.eclipse.serializer.persistence.util;

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

import static org.eclipse.serializer.util.X.notNull;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.serializer.persistence.types.PersistenceCommitListener;
import org.eclipse.serializer.persistence.types.PersistenceObjectRegistrationListener;
import org.eclipse.serializer.persistence.types.Storer;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

/**
 * A batching {@link Storer} decorator designed for write-heavy operations.
 * It accumulates store operations and commits them in batches rather than individually,
 * significantly improving throughput when storing large numbers of objects.
 * <p>
 * Instead of committing each store operation immediately, a {@code BatchStorer} delegates
 * to an underlying {@link Storer} and only flushes when the configured {@link Controller}
 * determines it is time — based on elapsed time, accumulated size, or both.
 * A background daemon thread periodically checks for pending flushes at the configured
 * check interval, ensuring data is flushed even when no new store operations occur.
 * Flushing can also be triggered manually by calling {@link #flush()}.
 * <p>
 * <b>Persistence guarantee:</b> a {@code store} call alone does not fully persist its data —
 * it only buffers the operation in the underlying {@link Storer}. Data is only committed to
 * the backing storage once a flush is triggered, either by the {@link Controller}, the
 * background scheduler, an explicit {@link #flush()} or {@link #commit()} call, or on
 * {@link #close()}. Callers that need a hard persistence guarantee at a specific point in
 * time must invoke {@link #flush()} or {@link #commit()} explicitly.
 * <p>
 * {@code BatchStorer} implements {@link AutoCloseable}. On close, it flushes any remaining
 * pending data and releases resources.
 * <p>
 * Usage:
 * <pre>
 * try (BatchStorer storer = BatchStorer.New(
 *     storageManager.createLazyStorer(),
 *     BatchStorer.Controller(Duration.ofSeconds(1)),
 *     Duration.ofMillis(200)
 * ))
 * {
 *     storer.storeAll(instances);
 * }
 * </pre>
 */
public interface BatchStorer extends Storer, AutoCloseable
{
	/**
	 * Flushes accumulated data to the underlying storage or delegate. This method
	 * ensures that all buffered or batched data is written and made persistent.
	 * It may be called explicitly to force a flush at a specific point in time,
	 * regardless of the configured flush cycle or size thresholds.
	 * <p>
	 * This is effectively a synonym for {@link #commit()} and simply delegates to it;
	 * the only difference is that {@code flush()} discards the commit result. Use
	 * {@link #commit()} if the underlying {@link Storer}'s commit status is needed.
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
	 * Creates a {@code Controller} instance configured to trigger a flush operation
	 * when a specified duration of time has elapsed since the last flush.
	 *
	 * @param flushCycle the duration representing the time interval after which a flush
	 *                   should occur
	 * @return a {@code Controller} that triggers a flush based on the elapsed time
	 * since the last flush
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
	 * Creates a {@code Controller} instance configured to trigger a flush operation
	 * when the accumulated size reaches a specified threshold.
	 *
	 * @param maxSize the maximum size of the batch or buffer that triggers a flush
	 * @return a {@code Controller} that determines when a flush operation should be triggered
	 * based on the given size threshold
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
	 * Creates a {@code Controller} instance configured to trigger a flush operation
	 * based on size and time constraints.
	 *
	 * @param maxSize    the maximum size of the batch or buffer that triggers a flush
	 * @param flushCycle the duration representing the time interval after which a flush
	 *                   should occur if no flush has been triggered by size
	 * @return a {@code Controller} that determines when a flush operation should be triggered
	 * based on the given size and time thresholds
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
	 * The Controller interface defines a contract for determining whether a flush
	 * operation should be triggered based on size and time constraints.
	 */
	public static interface Controller
	{
		/**
		 * Determines whether a flush operation should be triggered based on the
		 * provided size and time since the last flush.
		 *
		 * @param size                 the current size of the batch or buffer
		 * @param millisSinceLastFlush the time in milliseconds that has elapsed since the last flush operation
		 * @return {@code true} if a flush operation should be triggered; {@code false} otherwise
		 */
		public boolean shouldFlush(long size, long millisSinceLastFlush);
	}


	/**
	 * Constructs a new instance of {@code BatchStorer} with the specified delegate, controller,
	 * and background check interval.
	 * <p>
	 * A daemon thread periodically invokes the flush check at the given interval,
	 * ensuring pending data is flushed even when no new store operations occur.
	 *
	 * @param delegate      the underlying {@code Storer} implementation that handles storage operations
	 * @param controller    the {@code Controller} instance that manages flushing behavior and triggers
	 *                      based on time or size constraints
	 * @param checkInterval the interval at which the background thread checks for pending flushes
	 * @return a new {@code BatchStorer} instance configured with the provided delegate and controller
	 */
	public static BatchStorer New(final Storer delegate, final Controller controller, final Duration checkInterval)
	{
		final long millis = notNull(checkInterval).toMillis();
		if (millis <= 0L)
		{
			throw new IllegalArgumentException(
				"checkInterval must be > 0ms, was " + checkInterval
			);
		}

		return new Default(
			notNull(delegate),
			notNull(controller),
			checkInterval
		);
	}


	/**
	 * Default implementation of {@link BatchStorer}.
	 * <p>
	 * All store operations are synchronized to ensure thread safety.
	 * A background daemon thread periodically checks for pending data
	 * and flushes it according to the configured {@link Controller}.
	 */
	public static class Default implements BatchStorer
	{
		private final static Logger logger = Logging.getLogger(BatchStorer.class);

		private final Storer                   delegate         ;
		private final Controller               controller       ;
		private final ScheduledExecutorService scheduler        ;
		private long                           pendingSinceNanos;
		private volatile boolean               closed           ;

		Default(final Storer delegate, final Controller controller, final Duration checkInterval)
		{
			super();

			this.delegate   = delegate;
			this.controller = controller;
			this.scheduler  = Executors.newSingleThreadScheduledExecutor(r ->
			{
				final Thread t = new Thread(r, "batch-storer-flush");
				t.setDaemon(true);
				return t;
			});

			final long millis = checkInterval.toMillis();
			this.scheduler.scheduleAtFixedRate(
				this::backgroundFlush,
				millis,
				millis,
				TimeUnit.MILLISECONDS
			);
		}

		@Override
		public synchronized long store(final Object instance)
		{
			final long id = this.delegate.store(instance);

			this.optFlush();

			return id;
		}

		@Override
		public synchronized long[] storeAll(final Object... instances)
		{
			final long[] ids = this.delegate.storeAll(instances);

			this.optFlush();

			return ids;
		}

		@Override
		public synchronized void storeAll(final Iterable<?> instances)
		{
			this.delegate.storeAll(instances);

			this.optFlush();
		}

		@Override
		public synchronized long store(final Object instance, final long objectId)
		{
			final long id = this.delegate.store(instance, objectId);

			this.optFlush();

			return id;
		}

		@Override
		public synchronized boolean hasPendingData()
		{
			return !this.delegate.isEmpty();
		}

		@Override
		public synchronized void flush()
		{
			this.commit();
		}

		@Override
		public synchronized Object commit()
		{
			return this.internalFlush();
		}

		@Override
		public synchronized void clear()
		{
			this.delegate.clear();
			this.pendingSinceNanos = 0L;
		}

		@Override
		public synchronized boolean skip(final Object instance)
		{
			return this.delegate.skip(instance);
		}

		@Override
		public synchronized boolean skipNulled(final Object instance)
		{
			return this.delegate.skipNulled(instance);
		}

		@Override
		public synchronized boolean skipMapped(final Object instance, final long objectId)
		{
			return this.delegate.skipMapped(instance, objectId);
		}

		@Override
		public synchronized long size()
		{
			return this.delegate.size();
		}

		@Override
		public synchronized boolean isEmpty()
		{
			return this.delegate.isEmpty();
		}

		@Override
		public synchronized long currentCapacity()
		{
			return this.delegate.currentCapacity();
		}

		@Override
		public synchronized long maximumCapacity()
		{
			return this.delegate.maximumCapacity();
		}

		@Override
		public synchronized Storer reinitialize()
		{
			this.delegate.reinitialize();
			this.pendingSinceNanos = 0L;
			return this;
		}

		@Override
		public synchronized Storer reinitialize(final long initialCapacity)
		{
			this.delegate.reinitialize(initialCapacity);
			this.pendingSinceNanos = 0L;
			return this;
		}

		@Override
		public synchronized Storer ensureCapacity(final long desiredCapacity)
		{
			this.delegate.ensureCapacity(desiredCapacity);
			return this;
		}

		@Override
		public synchronized void registerCommitListener(final PersistenceCommitListener listener)
		{
			this.delegate.registerCommitListener(listener);
		}

		@Override
		public synchronized void registerRegistrationListener(final PersistenceObjectRegistrationListener listener)
		{
			this.delegate.registerRegistrationListener(listener);
		}

		@Override
		public void close()
		{
			synchronized (this)
			{
				if (this.closed)
				{
					return;
				}
				this.closed = true;
			}

			this.scheduler.shutdown();
			try
			{
				if (!this.scheduler.awaitTermination(5, TimeUnit.SECONDS))
				{
					this.scheduler.shutdownNow();
					if (!this.scheduler.awaitTermination(5, TimeUnit.SECONDS))
					{
						logger.warn("Background flush thread did not terminate within timeout");
					}
				}
			}
			catch (final InterruptedException e)
			{
				this.scheduler.shutdownNow();
				Thread.currentThread().interrupt();
			}

			synchronized (this)
			{
				if (!this.delegate.isEmpty())
				{
					this.internalFlush();
				}
				this.delegate.clear();
			}
		}

		private void backgroundFlush()
		{
			try
			{
				this.optFlush();
			} catch (final Exception e)
			{
				logger.error("Background flush failed", e);
			}
		}

		private synchronized void optFlush()
		{
			if (this.closed || this.delegate.isEmpty())
			{
				return;
			}

			final long nowNanos = System.nanoTime();
			if (this.pendingSinceNanos == 0L)
			{
				this.pendingSinceNanos = nowNanos;
			}

			if (this.controller.shouldFlush(
				this.delegate.size(),
				TimeUnit.NANOSECONDS.toMillis(nowNanos - this.pendingSinceNanos)
			))
			{
				this.internalFlush();
			}
		}

		private Object internalFlush()
		{
			logger.debug("Flushing batch storer with size = {}", this.delegate.size());

			final Object result = this.delegate.commit();
			this.pendingSinceNanos = 0L;
			return result;
		}

	}

}
