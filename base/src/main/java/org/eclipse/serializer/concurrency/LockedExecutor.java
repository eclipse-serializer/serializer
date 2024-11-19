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
 * 
 * @see ReentrantLock
 * @see ReadWriteLock
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
	 * 
	 * @return a shared {@link LockedExecutor} instance
	 */
	public static LockedExecutor global()
	{
		return Static.sharedInstance();
	}
	
	
	/**
	 * Pseudo-constructor method to create a new {@link LockedExecutor}.
	 * 
	 * @return a newly created {@link LockedExecutor}
	 */
	public static LockedExecutor New()
	{
		return new LockedExecutor.Default();
	}
	
	
	public static class Default implements LockedExecutor
	{
		private transient volatile ReentrantReadWriteLock reentrantLock;

		Default()
		{
			super();
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
						reentrantLock = this.reentrantLock = new ReentrantReadWriteLock();
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
