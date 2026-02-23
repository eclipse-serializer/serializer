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

import org.eclipse.serializer.persistence.types.PersistenceStoring;
import org.eclipse.serializer.persistence.types.Storer;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.eclipse.serializer.util.X.notNull;

/**
 * Utility to auto-batch store operations.
 * <p>
 * It wraps another storer but only commits in cycles, depending on the controller's input.
 * <p>
 * Usage:
 * <pre>
 * BatchStorer storer = BatchStorer.New(
 *     storageManager.createLazyStorer(),
 *     BatchStorer.Controller(Duration.ofSeconds(1))
 * );
 *
 * void bulkUpdate(Object... instances)
 * {
 *     storer.storeAll(instances);
 * }
 * </pre>
 * It commits automatically when the controller decides to flush.
 * Or manually, by calling {@link #flush()}.
 */
public interface BatchStorer extends PersistenceStoring
{
    /**
     * Flushes accumulated data to the underlying storage or delegate. This method
     * ensures that all buffered or batched data is written and made persistent.
     * It may be called explicitly to force a flush at a specific point in time,
     * regardless of the configured flush cycle or size thresholds.
     */
    public void flush();


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
     * Constructs a new instance of {@code BatchStorer} with the specified delegate and controller.
     *
     * @param delegate the underlying {@code Storer} implementation that handles storage operations
     * @param controller the {@code Controller} instance that manages flushing behavior and triggers
     *                   based on time or size constraints
     * @return a new {@code BatchStorer} instance configured with the provided delegate and controller
     */
    public static BatchStorer New(final Storer delegate, final Controller controller)
    {
        return new Default(
            notNull(delegate  ),
            notNull(controller)
        );
    }


    public static class Default implements BatchStorer
    {
        private final static Logger logger = Logging.getLogger(BatchStorer.class);

        private final Storer     delegate  ;
        private final Controller controller;
        private final AtomicLong lastFlush = new AtomicLong(0);

        Default(final Storer delegate, final Controller controller)
        {
            super();

            this.delegate   = delegate  ;
            this.controller = controller;
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
        public void flush()
        {
            this.internalFlush(System.currentTimeMillis());
        }

        private void optFlush()
        {
            final long now = System.currentTimeMillis();
            if (this.controller.shouldFlush(
                this.delegate.size(),
                now - this.lastFlush.get()
            ))
            {
                this.internalFlush(now);
            }
        }

        private void internalFlush(final long timestamp)
        {
            logger.debug("Flushing batch storer with size = {}", this.delegate.size());

            this.delegate.commit();
            this.lastFlush.set(timestamp);
        }

    }

}
