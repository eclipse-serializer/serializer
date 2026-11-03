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

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.serializer.exceptions.NumberRangeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;


class StripeLockedExecutorTest
{
	@Test
	void New_stripeCountOnly_isNonFair()
	{
		assertFalse(((StripeLockedExecutor.Default)StripeLockedExecutor.New(4)).isFair());
	}

	@Test
	void New_fair_isFair()
	{
		assertTrue(((StripeLockedExecutor.Default)StripeLockedExecutor.New(4, true)).isFair());
	}

	@Test
	void global_isNonFair()
	{
		assertFalse(((StripeLockedExecutor.Default)StripeLockedExecutor.global()).isFair());
	}

	@Test
	void New_stripeCountIsZero_throwsNumberRangeException()
	{
		assertThrows(NumberRangeException.class, () -> StripeLockedExecutor.New(0));
	}

	@Test
	void New_stripeCountIsNegative_throwsNumberRangeException()
	{
		assertThrows(NumberRangeException.class, () -> StripeLockedExecutor.New(-1));
	}

	/**
	 * {@link Math#abs(int)} of {@link Integer#MIN_VALUE} is negative, so deriving the stripe index
	 * that way produces an {@link ArrayIndexOutOfBoundsException} for such a mutex.
	 * <p>
	 * The stripe count must not be a divisor of {@link Integer#MIN_VALUE}, since the remainder would
	 * then be zero and mask the defect. Hence 3 rather than a power of two.
	 */
	@Test
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	void read_mutexHashCodeIsIntegerMinValue_doesNotThrow()
	{
		final StripeLockedExecutor executor = StripeLockedExecutor.New(3);
		final AtomicBoolean        executed = new AtomicBoolean();

		executor.read(new FixedHashCode(Integer.MIN_VALUE), () -> executed.set(true));

		assertTrue(executed.get());
	}

	/**
	 * @see #read_mutexHashCodeIsIntegerMinValue_doesNotThrow()
	 */
	@Test
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	void write_mutexHashCodeIsIntegerMinValue_doesNotThrow()
	{
		final StripeLockedExecutor executor = StripeLockedExecutor.New(3);
		final AtomicBoolean        executed = new AtomicBoolean();

		executor.write(new FixedHashCode(Integer.MIN_VALUE), () -> executed.set(true));

		assertTrue(executed.get());
	}

	@Test
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	void read_mutexHashCodeIsNegative_doesNotThrow()
	{
		final StripeLockedExecutor executor = StripeLockedExecutor.New(3);
		final AtomicBoolean        executed = new AtomicBoolean();

		executor.read(new FixedHashCode(-7), () -> executed.set(true));

		assertTrue(executed.get());
	}

	/**
	 * Every stripe index has to stay within the array's bounds, for any hash code.
	 */
	@Test
	@Timeout(value = 30, unit = TimeUnit.SECONDS)
	void read_extremeHashCodes_stayWithinBounds()
	{
		final int[] hashCodes = {
			Integer.MIN_VALUE, Integer.MIN_VALUE + 1, -1234567, -3, -2, -1,
			0, 1, 2, 3, 1234567, Integer.MAX_VALUE - 1, Integer.MAX_VALUE
		};

		for(int stripeCount = 1; stripeCount <= 9; stripeCount++)
		{
			final StripeLockedExecutor executor = StripeLockedExecutor.New(stripeCount);
			for(final int hashCode : hashCodes)
			{
				final AtomicBoolean executed = new AtomicBoolean();
				executor.read(new FixedHashCode(hashCode), () -> executed.set(true));
				assertTrue(executed.get(), "hashCode " + hashCode + " / stripeCount " + stripeCount);
			}
		}
	}

	@Test
	void read_nullMutex_throwsNullPointerException()
	{
		final StripeLockedExecutor executor = StripeLockedExecutor.New(4);

		assertThrows(NullPointerException.class, () -> executor.read(null, () ->
		{
			// must not be reached
		}));
	}

	@Test
	void write_nullMutex_throwsNullPointerException()
	{
		final StripeLockedExecutor executor = StripeLockedExecutor.New(4);

		assertThrows(NullPointerException.class, () -> executor.write(null, () ->
		{
			// must not be reached
		}));
	}

	/**
	 * The locks are held in a transient field, which is {@code null} again after the instance has
	 * been deserialized. The executor has to re-create them instead of failing.
	 */
	@Test
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	void read_afterTransientLocksFieldCleared_reinitializesLocks() throws Exception
	{
		final StripeLockedExecutor executor = StripeLockedExecutor.New(4);
		final Object               mutex    = new Object();

		executor.read(mutex, () ->
		{
			// trigger the lazy initialization
		});

		final Field field = StripeLockedExecutor.Default.class.getDeclaredField("reentrantLocks");
		field.setAccessible(true);
		field.set(executor, null);

		final AtomicBoolean executed = new AtomicBoolean();
		executor.read(mutex, () -> executed.set(true));

		assertTrue(executed.get());
	}

	@Test
	@Timeout(value = 60, unit = TimeUnit.SECONDS)
	void write_distinctStripes_proceedConcurrently() throws InterruptedException
	{
		final StripeLockedExecutor executor = StripeLockedExecutor.New(4);

		// 0 and 1 map onto distinct stripes for a stripe count of 4
		final Object mutexA = new FixedHashCode(0);
		final Object mutexB = new FixedHashCode(1);

		final CyclicBarrier  barrier    = new CyclicBarrier(2);
		final CountDownLatch bothInside = new CountDownLatch(2);

		final Thread threadA = startDaemon("stripe-a", () -> executor.write(mutexA, () ->
		{
			awaitBarrier(barrier);
			bothInside.countDown();
		}));
		final Thread threadB = startDaemon("stripe-b", () -> executor.write(mutexB, () ->
		{
			awaitBarrier(barrier);
			bothInside.countDown();
		}));

		final boolean concurrent = bothInside.await(30, TimeUnit.SECONDS);

		threadA.join(TimeUnit.SECONDS.toMillis(10));
		threadB.join(TimeUnit.SECONDS.toMillis(10));

		assertTrue(concurrent, "writes on distinct stripes did not proceed concurrently");
	}

	@Test
	@Timeout(value = 60, unit = TimeUnit.SECONDS)
	void write_sameMutex_isMutuallyExclusive() throws InterruptedException
	{
		final int threadCount = 8;
		final int iterations  = 500;

		final StripeLockedExecutor executor      = StripeLockedExecutor.New(4);
		final Object               mutex         = new Object();
		final AtomicInteger        inside        = new AtomicInteger();
		final AtomicInteger        maxInside     = new AtomicInteger();
		final CountDownLatch       done          = new CountDownLatch(threadCount);

		for(int i = 0; i < threadCount; i++)
		{
			startDaemon("writer-" + i, () ->
			{
				for(int n = 0; n < iterations; n++)
				{
					executor.write(mutex, () ->
					{
						maxInside.accumulateAndGet(inside.incrementAndGet(), Math::max);
						Thread.onSpinWait();
						inside.decrementAndGet();
					});
				}
				done.countDown();
			});
		}

		assertTrue(done.await(30, TimeUnit.SECONDS), "the writers did not finish");
		assertEquals(1, maxInside.get(), "more than one thread held the same stripe's write lock");
	}

	private static Thread startDaemon(final String name, final Runnable runnable)
	{
		final Thread thread = new Thread(runnable, name);
		thread.setDaemon(true);
		thread.start();
		return thread;
	}

	private static void awaitBarrier(final CyclicBarrier barrier)
	{
		try
		{
			barrier.await(20, TimeUnit.SECONDS);
		}
		catch(final Exception e)
		{
			throw new RuntimeException(e);
		}
	}


	/**
	 * Mutex with a controllable hash code, to target a specific stripe.
	 */
	private static final class FixedHashCode
	{
		private final int hashCode;

		FixedHashCode(final int hashCode)
		{
			super();

			this.hashCode = hashCode;
		}

		@Override
		public int hashCode()
		{
			return this.hashCode;
		}

	}

}
