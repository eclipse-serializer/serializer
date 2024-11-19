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

import static java.lang.Math.abs;
import static org.eclipse.serializer.math.XMath.positive;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.eclipse.serializer.functional.Action;
import org.eclipse.serializer.functional.Producer;


/**
 * Facility to execute operations with a reentrant mutual exclusion for defined mutexes.
 * 
 * @see ReentrantLock
 * @see ReadWriteLock
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
	 * 
	 * @return a shared {@link StripeLockedExecutor} instance
	 */
	public static StripeLockedExecutor global()
	{
		return Static.sharedInstance();
	}
	
	
	
	/**
	 * Pseudo-constructor method to create a new {@link StripeLockedExecutor}.
	 * 
	 * @param stripeCount maximum number of stripes
	 * @return a newly created {@link StripeLockedExecutor}
	 */
	public static StripeLockedExecutor New(final int stripeCount)
	{
		return new StripeLockedExecutor.Default(
			positive(stripeCount)
		);
	}
	
	
	public static class Default implements StripeLockedExecutor
	{
		private transient volatile ReentrantReadWriteLock[] reentrantLocks;

		Default(final int stripeCount)
		{
			super();
			
			this.reentrantLocks = new ReentrantReadWriteLock[stripeCount];
		}
		
		private ReentrantReadWriteLock reentrantLock(final Object mutex)
		{
			/*
			 * Double-checked locking to reduce the overhead of acquiring a lock
			 * by testing the locking criterion.
			 * The field (this.reentrantLocks) has to be volatile.
			 */
			
			final int index = abs(mutex.hashCode()) % this.reentrantLocks.length;
			ReentrantReadWriteLock reentrantLock = this.reentrantLocks[index];
			if(reentrantLock == null)
			{
				synchronized(this)
				{
					if((reentrantLock = this.reentrantLocks[index]) == null)
					{
						reentrantLock = this.reentrantLocks[index] = new ReentrantReadWriteLock();
					}
				}
			}
			return reentrantLock;
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
