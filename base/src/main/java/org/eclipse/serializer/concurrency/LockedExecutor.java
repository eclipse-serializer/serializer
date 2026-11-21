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

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.eclipse.serializer.functional.Action;
import org.eclipse.serializer.functional.Producer;


/**
 * Facility to execute operations with a reentrant mutual exclusion.
 * <p>
 * <b>Reentrancy:</b> a read operation may be nested inside another read operation, a write operation
 * inside another write operation, and a read operation inside a write operation. Nesting a write
 * operation inside a read operation is <b>not</b> supported: a {@link ReentrantReadWriteLock} cannot
 * upgrade a read lock to a write lock, so such an attempt deadlocks the calling thread. This applies
 * to both fairness policies.
 * <p>
 * <b>Fairness:</b> instances created by {@link #New()}, and the instance returned by
 * {@link #global()}, use the <i>non-fair</i> policy. Arriving threads may barge ahead of threads
 * that are already waiting, so no acquisition order is guaranteed and an individual thread can be
 * overtaken an unbounded number of times. This maximizes throughput but permits starvation under
 * sustained contention. Use {@link #New(boolean)} with <code>true</code> to obtain a fair instance,
 * which serves waiting threads in approximate arrival order.
 *
 * @see ReentrantLock
 * @see ReadWriteLock
 * @see #New(boolean)
 */
public interface LockedExecutor
{
	/**
	 * Executes an operation protected by a read lock.
	 *
	 * @param action the action to execute
	 */
	public void read(Action action);
	
	/**
	 * Executes an operation protected by a read lock.
	 *
	 * @param <R> the producer's return type
	 * @param producer the producer to execute
	 * @return the producer's result
	 */
	public <R> R read(Producer<R> producer);
	
	/**
	 * Executes an operation protected by a write lock.
	 *
	 * @param action the action to execute
	 */
	public void write(Action action);
	
	/**
	 * Executes an operation protected by a write lock.
	 *
	 * @param <R> the producer's return type
	 * @param producer the producer to execute
	 * @return the producer's result
	 */
	public <R> R write(Producer<R> producer);
	
	
	
	public static final class Static
	{
		private final static Object LOCK = new Object();
		private static volatile LockedExecutor sharedInstance;
		
		public static LockedExecutor sharedInstance()
		{
			/*
			 * Double-checked locking to reduce the overhead of acquiring a lock
			 * by testing the locking criterion.
			 * The field (Static.sharedInstance) has to be volatile.
			 */
			LockedExecutor sharedInstance = Static.sharedInstance;
			if(sharedInstance == null)
			{
				synchronized(LOCK)
				{
					if((sharedInstance = Static.sharedInstance) == null)
					{
						sharedInstance = Static.sharedInstance = LockedExecutor.New();
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
	 * Provides a global {@link LockedExecutor} instance.
	 * <p>
	 * Only a single one exists for the whole VM process, meaning it can be used to create VM-wide locks.
	 * <p>
	 * The shared instance uses the non-fair locking policy. Because it is shared by otherwise
	 * unrelated parts of the process, its contention - and therefore its exposure to starvation - is
	 * the sum of all its users. Prefer a dedicated instance from {@link #New(boolean)} where
	 * acquisition order matters.
	 *
	 * @return a shared {@link LockedExecutor} instance
	 */
	public static LockedExecutor global()
	{
		return Static.sharedInstance();
	}


	/**
	 * Pseudo-constructor method to create a new {@link LockedExecutor} with the non-fair locking policy.
	 * <p>
	 * Equivalent to {@link #New(boolean) New(false)}.
	 *
	 * @return a newly created {@link LockedExecutor}
	 * @see #New(boolean)
	 */
	public static LockedExecutor New()
	{
		return New(false);
	}


	/**
	 * Pseudo-constructor method to create a new {@link LockedExecutor} with the given locking policy.
	 * <p>
	 * A fair executor hands the lock over in approximate arrival order, so a waiting thread is not
	 * overtaken indefinitely, but its throughput is considerably lower because every hand-over has to
	 * unpark the next thread instead of letting an already running one barge in. A non-fair executor
	 * lets arriving threads barge ahead of waiting ones, which yields a much higher throughput but
	 * provides no ordering guarantee at all.
	 * <p>
	 * Note that the fairness of a lock does not extend to the scheduling of threads, as documented on
	 * {@link ReentrantLock}. A fair policy orders the hand-over of the lock itself; it cannot prevent
	 * the JVM or the operating system from scheduling the contending threads unevenly.
	 * <p>
	 * Reentrancy is unaffected by this choice: a thread that already holds the lock is always let
	 * through, in both policies.
	 *
	 * @param fair <code>true</code> to use a fair locking policy, <code>false</code> for a non-fair one
	 * @return a newly created {@link LockedExecutor}
	 */
	public static LockedExecutor New(final boolean fair)
	{
		return new LockedExecutor.Default(fair);
	}


	public static class Default implements LockedExecutor
	{
		private final boolean fair;

		private transient volatile ReentrantReadWriteLock reentrantLock;

		Default(final boolean fair)
		{
			super();

			this.fair = fair;
		}

		/**
		 * Tells whether this executor's lock uses the fair policy.
		 *
		 * @return <code>true</code> if the locking policy is fair
		 */
		boolean isFair()
		{
			return this.fair;
		}

		private ReentrantReadWriteLock reentrantLock()
		{
			/*
			 * Double-checked locking to reduce the overhead of acquiring a lock
			 * by testing the locking criterion.
			 * The field (this.reentrantLock) has to be volatile.
			 */
			ReentrantReadWriteLock reentrantLock = this.reentrantLock;
			if(reentrantLock == null)
			{
				synchronized(this)
				{
					if((reentrantLock = this.reentrantLock) == null)
					{
						reentrantLock = this.reentrantLock = new ReentrantReadWriteLock(this.fair);
					}
				}
			}
			return reentrantLock;
		}

		@Override
		public void read(final Action action)
		{
			final ReadLock readLock = this.reentrantLock().readLock();
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
		public <T> T read(final Producer<T> producer)
		{
			final ReadLock readLock = this.reentrantLock().readLock();
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
		public void write(final Action action)
		{
			final WriteLock writeLock = this.reentrantLock().writeLock();
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
		public <R> R write(final Producer<R> producer)
		{
			final WriteLock writeLock = this.reentrantLock().writeLock();
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
