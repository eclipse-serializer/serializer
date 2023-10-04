package org.eclipse.serializer.util.traversing;

/*-
 * #%L
 * Eclipse Serializer Base
 * %%
 * Copyright (C) 2023 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

/**
 * This type should actually extend {@link Throwable}, not {@link Runtime}. But sadly, the prior is checked,
 * which is a flawed concept in Java and prevent proper functional programming.
 * Also, this type should be an interface instead of a class.
 */
public abstract class AbstractTraversalSignal extends RuntimeException
{

	protected AbstractTraversalSignal()
	{
		super();
	}
	
	@Override
	public synchronized AbstractTraversalSignal fillInStackTrace()
	{
		// signals are branching mechanisms, not debugging tools. Hence no stack trace is needed or wanted.
		return this;
	}
	
}
