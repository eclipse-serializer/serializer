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

import org.eclipse.serializer.collections.types.XCollection;


public final class TraverserXCollectionReplacing implements TypeTraverser<XCollection<Object>>
{
	@Override
	public final void traverseReferences(
		final XCollection<Object> instance,
		final TraversalEnqueuer     enqueuer
	)
	{
		instance.iterate(enqueuer::enqueue);
	}
	
	@Override
	public final void traverseReferences(
		final XCollection<Object> instance,
		final TraversalEnqueuer     enqueuer,
		final TraversalAcceptor     acceptor
	)
	{
		try
		{
			instance.iterate(current ->
			{
				if(acceptor.acceptReference(current, instance))
				{
					enqueuer.enqueue(current);
				}
			});
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
	@Override
	public final void traverseReferences(
		final XCollection<Object> instance        ,
		final TraversalEnqueuer     enqueuer        ,
		final TraversalMutator      mutator         ,
		final MutationListener      mutationListener
	)
	{
		try
		{
			instance.substitute(current ->
			{
				final Object returned;
				enqueuer.enqueue(current);
				if((returned = mutator.mutateReference(current, instance)) != current)
				{
					if(mutationListener != null)
					{
						if(mutationListener.registerChange(instance, current, returned))
						{
							enqueuer.enqueue(returned);
						}
					}
				}
				return returned;
			});
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
	@Override
	public final void traverseReferences(
		final XCollection<Object> instance        ,
		final TraversalEnqueuer     enqueuer        ,
		final TraversalAcceptor     acceptor        ,
		final TraversalMutator      mutator         ,
		final MutationListener      mutationListener
	)
	{
		try
		{
			instance.substitute(current ->
			{
				final Object returned;
				if(acceptor.acceptReference(current, instance))
				{
					enqueuer.enqueue(current);
				}
				if((returned = mutator.mutateReference(current, instance)) != current)
				{
					if(mutationListener != null)
					{
						if(mutationListener.registerChange(instance, current, returned))
						{
							enqueuer.enqueue(returned);
						}
					}
				}
				return returned;
			});
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}

	
	@Override
	public final void traverseReferences(
		final XCollection<Object> instance,
		final TraversalAcceptor     acceptor
	)
	{
		try
		{
			instance.iterate(current ->
			{
				acceptor.acceptReference(current, instance);
			});
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
	@Override
	public final void traverseReferences(
		final XCollection<Object> instance        ,
		final TraversalMutator      mutator         ,
		final MutationListener      mutationListener
	)
	{
		try
		{
			instance.substitute(current ->
			{
				final Object returned;
				if((returned = mutator.mutateReference(current, instance)) != current)
				{
					if(mutationListener != null)
					{
						mutationListener.registerChange(instance, current, returned);
					}
				}
				return returned;
			});
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
	@Override
	public final void traverseReferences(
		final XCollection<Object> instance        ,
		final TraversalAcceptor     acceptor        ,
		final TraversalMutator      mutator         ,
		final MutationListener      mutationListener
	)
	{
		try
		{
			instance.substitute(current ->
			{
				final Object returned;
				acceptor.acceptReference(current, instance);
				if((returned = mutator.mutateReference(current, instance)) != current)
				{
					if(mutationListener != null)
					{
						mutationListener.registerChange(instance, current, returned);
					}
				}
				return returned;
			});
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
}
