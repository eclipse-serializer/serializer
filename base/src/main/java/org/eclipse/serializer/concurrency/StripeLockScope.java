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

import org.eclipse.serializer.functional.Action;
import org.eclipse.serializer.functional.Producer;


/**
 * Abstract base class for types, which want to utilize a {@link StripeLockedExecutor}.
 * <p>
 * All the executor's methods are exposed by abstract equivalents inside this type hierarchy.
 */
public abstract class StripeLockScope
{
	private transient volatile StripeLockedExecutor executor;
	
	protected StripeLockScope()
	{
		super();
	}
	
	/**
	 * Lazy initializes the executor.
	 */
	private StripeLockedExecutor executor()
	{
		/*
		 * Double-checked locking to reduce the overhead of acquiring a lock
		 * by testing the locking criterion.
		 * The field (this.executor) has to be volatile.
		 */
		StripeLockedExecutor executor = this.executor;
		if(executor == null)
		{
			synchronized(this)
			{
				if((executor = this.executor) == null)
				{
					executor = this.executor = StripeLockedExecutor.New(this.stripeCount());
				}
			}
		}
		return executor;
	}
	
	/**
	 * Gets the maximum number of stripes used for the {@link StripeLockedExecutor}.
	 * 
	 * @return max number of stripes
	 */
	protected int stripeCount()
	{
		return Runtime.getRuntime().availableProcessors();
	}
	
	/**
	 * Executes an operation protected by a read lock.
	 *
	 * @param mutex the mutex to lock on, not <code>null</code>
	 * @param action the action to execute
	 */
	protected void read(final Object mutex, final Action action)
	{
		this.executor().read(mutex, action);
	}
	
	/**
	 * Executes an operation protected by a read lock.
	 *
	 * @param <R> the producer's return type
	 * @param mutex the mutex to lock on, not <code>null</code>
	 * @param producer the producer to execute
	 * @return the producer's result
	 */
	protected <R> R read(final Object mutex, final Producer<R> producer)
	{
		return this.executor().read(mutex, producer);
	}
	
	/**
	 * Executes an operation protected by a write lock.
	 *
	 * @param mutex the mutex to lock on, not <code>null</code>
	 * @param action the action to execute
	 */
	protected void write(final Object mutex, final Action action)
	{
		this.executor().write(mutex, action);
	}
	
	/**
	 * Executes an operation protected by a write lock.
	 *
	 * @param <R> the producer's return type
	 * @param mutex the mutex to lock on, not <code>null</code>
	 * @param producer the producer to execute
	 * @return the producer's result
	 */
	protected <R> R write(final Object mutex, final Producer<R> producer)
	{
		return this.executor().write(mutex, producer);
	}
	
}
