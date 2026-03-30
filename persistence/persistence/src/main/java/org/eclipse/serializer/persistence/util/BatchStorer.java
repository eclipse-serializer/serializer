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

import org.eclipse.serializer.persistence.types.PersistenceCommitListener;
import org.eclipse.serializer.persistence.types.PersistenceObjectRegistrationListener;
import org.eclipse.serializer.persistence.types.Storer;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.eclipse.serializer.util.X.notNull;

/**
 * Utility to auto-batch store operations.
 * <p>
 * It wraps another storer but only commits in cycles, depending on the controller's input.
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
 * It commits automatically when the controller decides to flush.
 * A background daemon thread periodically checks for pending flushes
 * at the configured check interval, ensuring data is flushed even
 * when no new store operations occur.
 * Or manually, by calling {@link #flush()}.
 */
public interface BatchStorer extends Storer, AutoCloseable
{
    /**
     * Flushes accumulated data to the underlying storage or delegate. This method
     * ensures that all buffered or batched data is written and made persistent.
     * It may be called explicitly to force a flush at a specific point in time,
     * regardless of the configured flush cycle or size thresholds.
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
     *         since the last flush
     */
    public static Controller Controller(final Duration flushCycle)
    {
        final long flushCycleMillis = flushCycle.toMillis();
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
     *         based on the given size threshold
     */
    public static Controller Controller(final long maxSize)
    {
        return (size, millisSinceLastFlush) ->
            size >= maxSize
        ;
    }


    /**
     * Creates a {@code Controller} instance configured to trigger a flush operation
     * based on size and time constraints.
     *
     * @param maxSize the maximum size of the batch or buffer that triggers a flush
     * @param flushCycle the duration representing the time interval after which a flush
     *                   should occur if no flush has been triggered by size
     * @return a {@code Controller} that determines when a flush operation should be triggered
     *         based on the given size and time thresholds
     */
    public static Controller Controller(final long maxSize, final Duration flushCycle)
    {
        final long flushCycleMillis = flushCycle.toMillis();
        return (size, millisSinceLastFlush) ->
            size >= maxSize
        ||  millisSinceLastFlush >= flushCycleMillis
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
         * @param size the current size of the batch or buffer
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
     * @param delegate the underlying {@code Storer} implementation that handles storage operations
     * @param controller the {@code Controller} instance that manages flushing behavior and triggers
     *                   based on time or size constraints
     * @param checkInterval the interval at which the background thread checks for pending flushes
     * @return a new {@code BatchStorer} instance configured with the provided delegate and controller
     */
    public static BatchStorer New(final Storer delegate, final Controller controller, final Duration checkInterval)
    {
        return new Default(
            notNull(delegate     ),
            notNull(controller   ),
            notNull(checkInterval)
        );
    }


    public static class Default implements BatchStorer
    {
        private final static Logger logger = Logging.getLogger(BatchStorer.class);

        private final Storer                    delegate  ;
        private final Controller                controller;
        private final ScheduledExecutorService  scheduler ;
        private       long                      lastFlush = -1;

        Default(final Storer delegate, final Controller controller, final Duration checkInterval)
        {
            super();

            this.delegate   = delegate  ;
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
        public long store(final Object instance)
        {
            final long id = this.delegate.store(instance);

            this.optFlush();

            return id;
        }

        @Override
        public long[] storeAll(final Object... instances)
        {
            final long[] ids = this.delegate.storeAll(instances);

            this.optFlush();

            return ids;
        }

        @Override
        public void storeAll(final Iterable<?> instances)
        {
            this.delegate.storeAll(instances);

            this.optFlush();
        }

        @Override
        public long store(final Object instance, final long objectId)
        {
            final long id = this.delegate.store(instance, objectId);

            this.optFlush();

            return id;
        }

        @Override
        public synchronized void flush()
        {
            this.internalFlush(System.currentTimeMillis());
        }

        @Override
        public boolean hasPendingData()
        {
            return !this.delegate.isEmpty();
        }

        @Override
        public synchronized void close()
        {
            this.scheduler.shutdown();

            if (!this.delegate.isEmpty())
            {
                this.internalFlush(System.currentTimeMillis());
            }
            this.delegate.clear();
        }

        @Override
        public synchronized Object commit()
        {
            this.flush();
            return null;
        }

        @Override
        public synchronized void clear()
        {
            this.delegate.clear();
            this.lastFlush = -1;
        }

        @Override
        public boolean skip(final Object instance)
        {
            return this.delegate.skip(instance);
        }

        @Override
        public boolean skipNulled(final Object instance)
        {
            return this.delegate.skipNulled(instance);
        }

        @Override
        public boolean skipMapped(final Object instance, final long objectId)
        {
            return this.delegate.skipMapped(instance, objectId);
        }

        @Override
        public long size()
        {
            return this.delegate.size();
        }

        @Override
        public boolean isEmpty()
        {
            return this.delegate.isEmpty();
        }

        @Override
        public long currentCapacity()
        {
            return this.delegate.currentCapacity();
        }

        @Override
        public long maximumCapacity()
        {
            return this.delegate.maximumCapacity();
        }

        @Override
        public synchronized Storer reinitialize()
        {
            this.delegate.reinitialize();
            this.lastFlush = -1;
            return this;
        }

        @Override
        public synchronized Storer reinitialize(final long initialCapacity)
        {
            this.delegate.reinitialize(initialCapacity);
            this.lastFlush = -1;
            return this;
        }

        @Override
        public synchronized Storer ensureCapacity(final long desiredCapacity)
        {
            this.delegate.ensureCapacity(desiredCapacity);
            this.lastFlush = -1;
            return this;
        }

        @Override
        public void registerCommitListener(final PersistenceCommitListener listener)
        {
            this.delegate.registerCommitListener(listener);
        }

        @Override
        public void registerRegistrationListener(final PersistenceObjectRegistrationListener listener)
        {
            this.delegate.registerRegistrationListener(listener);
        }

        private void backgroundFlush()
        {
            try
            {
                this.optFlush();
            }
            catch (final Exception e)
            {
                logger.error("Background flush failed", e);
            }
        }

        private synchronized void optFlush()
        {
            if (this.delegate.isEmpty())
            {
                return;
            }

            final long now = System.currentTimeMillis();

            if (this.lastFlush == -1)
            {
                this.lastFlush = now;
                return;
            }

            if (this.controller.shouldFlush(
                this.delegate.size(),
                now - this.lastFlush
            ))
            {
                this.internalFlush(now);
            }
        }

        private void internalFlush(final long timestamp)
        {
            logger.debug("Flushing batch storer with size = {}", this.delegate.size());

            this.delegate.commit();
            this.lastFlush = timestamp;
        }

    }

}
