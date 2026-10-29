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
 * Abstract base class for types, which want to utilize a {@link LockedExecutor}.
 * <p>
 * All the executor's methods are exposed by abstract equivalents inside this type hierarchy.
 */
public abstract class LockScope
{
	private transient volatile LockedExecutor executor;
	
	protected LockScope()
	{
		super();
	}
	
	/**
	 * Lazy initializes the executor.
	 */
	private LockedExecutor executor()
	{
		/*
		 * Double-checked locking to reduce the overhead of acquiring a lock
		 * by testing the locking criterion.
		 * The field (this.executor) has to be volatile.
		 */
		LockedExecutor executor = this.executor;
		if(executor == null)
		{
			synchronized(this)
			{
				if((executor = this.executor) == null)
				{
					executor = this.executor = this.createExecutor();
				}
			}
		}
		return executor;
	}

	/**
	 * Creates the {@link LockedExecutor} used by this scope.
	 * <p>
	 * Override to control the executor's configuration, e.g. to request a fair locking policy via
	 * {@link LockedExecutor#New(boolean)}.
	 * <p>
	 * This method is called while the monitor of this instance is held. It must therefore return
	 * quickly and must not acquire any other lock, otherwise it can deadlock the scope.
	 *
	 * @return a newly created {@link LockedExecutor}
	 */
	protected LockedExecutor createExecutor()
	{
		return LockedExecutor.New();
	}

	/**
	 * Executes an operation protected by a read lock.
	 *
	 * @param action the action to execute
	 */
	protected void read(final Action action)
	{
		this.executor().read(action);
	}
	
	/**
	 * Executes an operation protected by a read lock.
	 *
	 * @param <R> the producer's return type
	 * @param producer the producer to execute
	 * @return the producer's result
	 */
	protected <R> R read(final Producer<R> producer)
	{
		return this.executor().read(producer);
	}
	
	/**
	 * Executes an operation protected by a write lock.
	 *
	 * @param action the action to execute
	 */
	protected void write(final Action action)
	{
		this.executor().write(action);
	}
	
	/**
	 * Executes an operation protected by a write lock.
	 *
	 * @param <R> the producer's return type
	 * @param producer the producer to execute
	 * @return the producer's result
	 */
	protected <R> R write(final Producer<R> producer)
	{
		return this.executor().write(producer);
	}
	
}
