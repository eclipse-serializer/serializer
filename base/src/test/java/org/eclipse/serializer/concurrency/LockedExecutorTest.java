package org.eclipse.serializer.concurrency;

/*-
 * #%L
 * Eclipse Serializer Base
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.serializer.functional.Action;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


class LockedExecutorTest
{
	@Test
	void New_noArguments_isNonFair()
	{
		assertFalse(((LockedExecutor.Default)LockedExecutor.New()).isFair());
	}

	@Test
	void New_fair_isFair()
	{
		assertTrue(((LockedExecutor.Default)LockedExecutor.New(true)).isFair());
	}

	@Test
	void New_nonFair_isNonFair()
	{
		assertFalse(((LockedExecutor.Default)LockedExecutor.New(false)).isFair());
	}

	@Test
	void global_isNonFair()
	{
		assertFalse(((LockedExecutor.Default)LockedExecutor.global()).isFair());
	}

	@Test
	void global_calledTwice_returnsSameInstance()
	{
		assertEquals(LockedExecutor.global(), LockedExecutor.global());
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	void read_nestedRead_doesNotDeadlock(final boolean fair)
	{
		final LockedExecutor executor = LockedExecutor.New(fair);
		final AtomicInteger  depth    = new AtomicInteger();

		executor.read(() ->
		{
			executor.read(() ->
			{
				depth.incrementAndGet();
			});
		});

		assertEquals(1, depth.get());
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	void write_nestedWrite_doesNotDeadlock(final boolean fair)
	{
		final LockedExecutor executor = LockedExecutor.New(fair);
		final AtomicInteger  depth    = new AtomicInteger();

		executor.write(() ->
		{
			executor.write(() ->
			{
				depth.incrementAndGet();
			});
		});

		assertEquals(1, depth.get());
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	void write_nestedRead_doesNotDeadlock(final boolean fair)
	{
		final LockedExecutor executor = LockedExecutor.New(fair);
		final AtomicInteger  depth    = new AtomicInteger();

		executor.write(() ->
		{
			executor.read(() ->
			{
				depth.incrementAndGet();
			});
		});

		assertEquals(1, depth.get());
	}

	@Test
	void read_producer_returnsProducersResult()
	{
		final String result = LockedExecutor.New().read(() -> "read");

		assertEquals("read", result);
	}

	@Test
	void write_producer_returnsProducersResult()
	{
		final String result = LockedExecutor.New().write(() -> "write");

		assertEquals("write", result);
	}

	@Test
	@Timeout(value = 30, unit = TimeUnit.SECONDS)
	void write_actionThrows_releasesLock() throws InterruptedException
	{
		final LockedExecutor executor = LockedExecutor.New();

		assertThrows(IllegalStateException.class, () -> executor.write((Action)() ->
		{
			throw new IllegalStateException("expected");
		}));

		assertTrue(
			this.acquiresWriteLockWithin(executor, 10, TimeUnit.SECONDS),
			"the write lock was not released after the action threw"
		);
	}

	@Test
	@Timeout(value = 30, unit = TimeUnit.SECONDS)
	void read_actionThrows_releasesLock() throws InterruptedException
	{
		final LockedExecutor executor = LockedExecutor.New();

		assertThrows(IllegalStateException.class, () -> executor.read((Action)() ->
		{
			throw new IllegalStateException("expected");
		}));

		assertTrue(
			this.acquiresWriteLockWithin(executor, 10, TimeUnit.SECONDS),
			"the read lock was not released after the action threw"
		);
	}

	/**
	 * A fair executor must hand the write lock over to a waiting writer even while readers keep
	 * arriving. With the non-fair policy this is not guaranteed, which is why only the fair
	 * direction is asserted here.
	 */
	@Test
	@Timeout(value = 120, unit = TimeUnit.SECONDS)
	void write_fairExecutorUnderSustainedReadLoad_isNotStarved() throws InterruptedException
	{
		final int readerCount = Math.max(4, Runtime.getRuntime().availableProcessors());

		final LockedExecutor executor    = LockedExecutor.New(true);
		final AtomicBoolean  keepReading = new AtomicBoolean(true);
		final CountDownLatch readersUp   = new CountDownLatch(readerCount);
		final CountDownLatch writerDone  = new CountDownLatch(1);

		final Thread[] readers = new Thread[readerCount];
		for(int i = 0; i < readerCount; i++)
		{
			readers[i] = new Thread(() ->
			{
				readersUp.countDown();
				while(keepReading.get())
				{
					executor.read(() -> Thread.onSpinWait());
				}
			}, "reader-" + i);
			readers[i].setDaemon(true);
			readers[i].start();
		}

		final boolean acquired;
		try
		{
			assertTrue(readersUp.await(30, TimeUnit.SECONDS), "the readers did not start");

			final Thread writer = new Thread(
				() -> executor.write(() -> writerDone.countDown()),
				"writer"
			);
			writer.setDaemon(true);
			writer.start();

			acquired = writerDone.await(30, TimeUnit.SECONDS);
		}
		finally
		{
			keepReading.set(false);
			for(final Thread reader : readers)
			{
				reader.join(TimeUnit.SECONDS.toMillis(10));
			}
		}

		assertTrue(acquired, "the fair write lock was starved by the sustained read load");
	}

	private boolean acquiresWriteLockWithin(
		final LockedExecutor executor,
		final long           timeout,
		final TimeUnit       unit
	)
		throws InterruptedException
	{
		final CountDownLatch acquired = new CountDownLatch(1);

		final Thread thread = new Thread(() -> executor.write(() -> acquired.countDown()), "acquirer");
		thread.setDaemon(true);
		thread.start();

		final boolean result = acquired.await(timeout, unit);
		thread.join(unit.toMillis(timeout));
		return result;
	}

}
