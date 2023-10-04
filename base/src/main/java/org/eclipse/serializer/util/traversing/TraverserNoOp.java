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

public final class TraverserNoOp<T> implements TypeTraverser<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <T> TraverserNoOp<T> New(final Class<T> type)
	{
		return new TraverserNoOp<>(type);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final Class<T> type;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	TraverserNoOp(final Class<T> type)
	{
		super();
		this.type = type;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final Class<T> type()
	{
		return this.type;
	}
	
	@Override
	public final void traverseReferences(
		final T                 instance,
		final TraversalEnqueuer enqueuer
	)
	{
		// no-op
	}
	
	@Override
	public final void traverseReferences(
		final T                 instance,
		final TraversalEnqueuer enqueuer,
		final TraversalAcceptor acceptor
	)
	{
		// no-op
	}
	
	@Override
	public final void traverseReferences(
		final T                 instance        ,
		final TraversalEnqueuer enqueuer        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		// no-op
	}
	
	@Override
	public final void traverseReferences(
		final T                 instance        ,
		final TraversalEnqueuer enqueuer        ,
		final TraversalAcceptor acceptor        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		// no-op
	}
	

	@Override
	public final void traverseReferences(
		final T                 instance,
		final TraversalAcceptor acceptor
	)
	{
		// no-op
	}
	
	@Override
	public final void traverseReferences(
		final T                 instance        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		// no-op
	}
	
	@Override
	public final void traverseReferences(
		final T                 instance        ,
		final TraversalAcceptor acceptor        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		// no-op
	}
	
}
