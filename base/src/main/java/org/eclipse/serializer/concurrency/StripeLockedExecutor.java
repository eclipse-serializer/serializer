package org.eclipse.serializer.concurrency;

/*-
 * #%L
 * Eclipse Serializer Base
 * %%
 * Copyright (C) 2024 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import static java.lang.Math.floorMod;
import static org.eclipse.serializer.math.XMath.positive;
import static org.eclipse.serializer.util.X.notNull;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.eclipse.serializer.functional.Action;
import org.eclipse.serializer.functional.Producer;


/**
 * Facility to execute operations with a reentrant mutual exclusion for defined mutexes.
 * <p>
 * Mutexes are mapped onto a fixed number of stripes by their {@link Object#hashCode()}. Two distinct
 * mutexes that map onto the same stripe share a lock and therefore exclude each other, so the stripe
 * count trades memory for the amount of achievable parallelism.
 * <p>
 * <b>Reentrancy:</b> a read operation may be nested inside another read operation, a write operation
 * inside another write operation, and a read operation inside a write operation. Nesting a write
 * operation inside a read operation for the same stripe is <b>not</b> supported: a
 * {@link ReentrantReadWriteLock} cannot upgrade a read lock to a write lock, so such an attempt
 * deadlocks the calling thread. This applies to both fairness policies.
 * <p>
 * <b>Fairness:</b> instances created by {@link #New(int)}, and the instance returned by
 * {@link #global()}, use the <i>non-fair</i> policy. Arriving threads may barge ahead of threads that
 * are already waiting, so no acquisition order is guaranteed and an individual thread can be
 * overtaken an unbounded number of times. This maximizes throughput but permits starvation under
 * sustained contention. Use {@link #New(int, boolean)} with <code>true</code> to obtain a fair
 * instance, which serves waiting threads in approximate arrival order.
 *
 * @see ReentrantLock
 * @see ReadWriteLock
 * @see #New(int, boolean)
 */
public interface StripeLockedExecutor
{
	/**
	 * Executes an operation protected by a read lock.
	 *
	 * @param mutex the mutex to lock on, not <code>null</code>
	 * @param action the action to execute
	 */
	public void read(Object mutex, Action action);
	
	/**
	 * Executes an operation protected by a read lock.
	 *
	 * @param <R> the producer's return type
	 * @param mutex the mutex to lock on, not <code>null</code>
	 * @param producer the producer to execute
	 * @return the producer's result
	 */
	public <R> R read(Object mutex, Producer<R> producer);
	
	/**
	 * Executes an operation protected by a write lock.
	 *
	 * @param mutex the mutex to lock on, not <code>null</code>
	 * @param action the action to execute
	 */
	public void write(Object mutex, Action action);
	
	/**
	 * Executes an operation protected by a write lock.
	 *
	 * @param <R> the producer's return type
	 * @param mutex the mutex to lock on, not <code>null</code>
	 * @param producer the producer to execute
	 * @return the producer's result
	 */
	public <R> R write(Object mutex, Producer<R> producer);
	
	
	
	public static final class Static
	{
		private final static Object LOCK = new Object();
		private static volatile StripeLockedExecutor sharedInstance;
		
		public static StripeLockedExecutor sharedInstance()
		{
			/*
			 * Double-checked locking to reduce the overhead of acquiring a lock
			 * by testing the locking criterion.
			 * The field (Static.sharedInstance) has to be volatile.
			 */
			StripeLockedExecutor sharedInstance = Static.sharedInstance;
			if(sharedInstance == null)
			{
				synchronized(LOCK)
				{
					if((sharedInstance = Static.sharedInstance) == null)
					{
						sharedInstance = Static.sharedInstance = StripeLockedExecutor.New(
							Runtime.getRuntime().availableProcessors()
						);
					}
				}
			}
			return sharedInstance;
		}
		
		
		private Static()
		{
			// static only
			throw new UnsupportedOperationException();
		}
	}
	
	
	/**
	 * Provides a global {@link StripeLockedExecutor} instance.
	 * <p>
	 * Only a single one exists for the whole VM process, meaning it can be used to create VM-wide locks.
	 * <p>
	 * The shared instance uses the non-fair locking policy. Because it is shared by otherwise
	 * unrelated parts of the process, its contention - and therefore its exposure to starvation - is
	 * the sum of all its users. Prefer a dedicated instance from {@link #New(int, boolean)} where
	 * acquisition order matters.
	 *
	 * @return a shared {@link StripeLockedExecutor} instance
	 */
	public static StripeLockedExecutor global()
	{
		return Static.sharedInstance();
	}



	/**
	 * Pseudo-constructor method to create a new {@link StripeLockedExecutor} with the non-fair
	 * locking policy.
	 * <p>
	 * Equivalent to {@link #New(int, boolean) New(stripeCount, false)}.
	 *
	 * @param stripeCount maximum number of stripes, must be positive
	 * @return a newly created {@link StripeLockedExecutor}
	 * @see #New(int, boolean)
	 */
	public static StripeLockedExecutor New(final int stripeCount)
	{
		return New(stripeCount, false);
	}


	/**
	 * Pseudo-constructor method to create a new {@link StripeLockedExecutor} with the given locking
	 * policy.
	 * <p>
	 * A fair executor hands a stripe's lock over in approximate arrival order, so a waiting thread is
	 * not overtaken indefinitely, but its throughput is considerably lower because every hand-over has
	 * to unpark the next thread instead of letting an already running one barge in. A non-fair
	 * executor lets arriving threads barge ahead of waiting ones, which yields a much higher
	 * throughput but provides no ordering guarantee at all.
	 * <p>
	 * Note that the fairness of a lock does not extend to the scheduling of threads, as documented on
	 * {@link ReentrantLock}. A fair policy orders the hand-over of the lock itself; it cannot prevent
	 * the JVM or the operating system from scheduling the contending threads unevenly.
	 * <p>
	 * Reentrancy is unaffected by this choice: a thread that already holds a stripe's lock is always
	 * let through, in both policies.
	 * <p>
	 * The locks of all stripes are created together, the first time the executor is used. The stripe
	 * count should therefore be sized like a degree of parallelism - the default of
	 * {@link Runtime#availableProcessors()} used by {@link StripeLockScope} is a good yardstick - and
	 * not like the number of mutexes the executor will ever see.
	 *
	 * @param stripeCount maximum number of stripes, must be positive
	 * @param fair <code>true</code> to use a fair locking policy, <code>false</code> for a non-fair one
	 * @return a newly created {@link StripeLockedExecutor}
	 */
	public static StripeLockedExecutor New(final int stripeCount, final boolean fair)
	{
		return new StripeLockedExecutor.Default(
			positive(stripeCount),
			fair
		);
	}
	
	
	public static class Default implements StripeLockedExecutor
	{
		private final int     stripeCount;
		private final boolean fair;

		private transient volatile ReentrantReadWriteLock[] reentrantLocks;

		Default(final int stripeCount, final boolean fair)
		{
			super();

			this.stripeCount = stripeCount;
			this.fair        = fair;
		}

		/**
		 * Tells whether this executor's locks use the fair policy.
		 *
		 * @return <code>true</code> if the locking policy is fair
		 */
		boolean isFair()
		{
			return this.fair;
		}

		private ReentrantReadWriteLock[] reentrantLocks()
		{
			/*
			 * Double-checked locking to reduce the overhead of acquiring a lock
			 * by testing the locking criterion.
			 * The field (this.reentrantLocks) has to be volatile.
			 *
			 * The array is populated completely before it is published via the volatile write, so a
			 * thread that reads a non-null array is guaranteed to see all of its elements as well.
			 * Initializing the elements individually would not be safe, since the volatility of the
			 * field applies to the array reference only, not to its elements.
			 *
			 * The array cannot be created in the constructor because the field is transient and is
			 * therefore null again after the instance has been deserialized.
			 */
			ReentrantReadWriteLock[] reentrantLocks = this.reentrantLocks;
			if(reentrantLocks == null)
			{
				synchronized(this)
				{
					if((reentrantLocks = this.reentrantLocks) == null)
					{
						reentrantLocks = new ReentrantReadWriteLock[this.stripeCount];
						for(int i = 0; i < reentrantLocks.length; i++)
						{
							reentrantLocks[i] = new ReentrantReadWriteLock(this.fair);
						}
						this.reentrantLocks = reentrantLocks;
					}
				}
			}
			return reentrantLocks;
		}

		private ReentrantReadWriteLock reentrantLock(final Object mutex)
		{
			notNull(mutex);

			/*
			 * floorMod instead of abs(...) % length: abs(Integer.MIN_VALUE) is negative,
			 * which would yield a negative index for a mutex with that hash code.
			 */
			final ReentrantReadWriteLock[] reentrantLocks = this.reentrantLocks();
			return reentrantLocks[floorMod(mutex.hashCode(), reentrantLocks.length)];
		}

		@Override
		public void read(final Object mutex, final Action action)
		{
			final ReadLock readLock = this.reentrantLock(mutex).readLock();
			readLock.lock();

			try
			{
				action.execute();
			}
			finally
			{
				readLock.unlock();
			}
		}
		
		@Override
		public <T> T read(final Object mutex, final Producer<T> producer)
		{
			final ReadLock readLock = this.reentrantLock(mutex).readLock();
			readLock.lock();

			try
			{
				return producer.produce();
			}
			finally
			{
				readLock.unlock();
			}
		}
		
		@Override
		public void write(final Object mutex, final Action action)
		{
			final WriteLock writeLock = this.reentrantLock(mutex).writeLock();
			writeLock.lock();

			try
			{
				action.execute();
			}
			finally
			{
				writeLock.unlock();
			}
		}
		
		@Override
		public <R> R write(final Object mutex, final Producer<R> producer)
		{
			final WriteLock writeLock = this.reentrantLock(mutex).writeLock();
			writeLock.lock();

			try
			{
				return producer.produce();
			}
			finally
			{
				writeLock.unlock();
			}
		}
		
	}
	
}
