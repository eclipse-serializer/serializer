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
	private final transient LockedExecutor executor = LockedExecutor.New();
	
	protected LockScope()
	{
		super();
	}
	
	/**
	 * Executes an operation protected by a read lock.
	 *
	 * @param action the action to execute
	 */
	protected void read(final Action action)
	{
		this.executor.read(action);
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
		return this.executor.read(producer);
	}
	
	/**
	 * Executes an operation protected by a write lock.
	 *
	 * @param action the action to execute
	 */
	protected void write(final Action action)
	{
		this.executor.write(action);
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
		return this.executor.write(producer);
	}
	
}
