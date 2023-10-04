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


public final class TraversalSignalSkipInstance extends AbstractTraversalSkipSignal
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	// can be thrown any number of times, so a singleton instead of constant instantiation is the better approach
	static final TraversalSignalSkipInstance SINGLETON = new TraversalSignalSkipInstance();
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	/**
	 * Should actually be called "throw", but that is a keyword.
	 */
	public static void fire() throws TraversalSignalSkipInstance
	{
		throw SINGLETON;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private TraversalSignalSkipInstance()
	{
		super();
	}
	
}
