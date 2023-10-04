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

public final class TraverserArray implements TypeTraverser<Object[]>
{
	@Override
	public final void traverseReferences(
		final Object[]          instance,
		final TraversalEnqueuer enqueuer
	)
	{
		final int length = instance.length;
		for(int i = 0; i < length; i++)
		{
			enqueuer.enqueue(instance[i]);
		}
	}
	
	@Override
	public final void traverseReferences(
		final Object[]          instance,
		final TraversalEnqueuer enqueuer,
		final TraversalAcceptor acceptor
	)
	{
		final int length = instance.length;
		try
		{
			for(int i = 0; i < length; i++)
			{
				if(acceptor.acceptReference(instance[i], instance))
				{
					enqueuer.enqueue(instance[i]);
				}
			}
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
	@Override
	public final void traverseReferences(
		final Object[]          instance        ,
		final TraversalEnqueuer enqueuer        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		final int length = instance.length;
		try
		{
			for(int i = 0; i < length; i++)
			{
				final Object current, returned;
				enqueuer.enqueue(current = instance[i]);
				if((returned = mutator.mutateReference(current, instance)) != current)
				{
					if(mutationListener != null)
					{
						if(mutationListener.registerChange(instance, current, returned))
						{
							enqueuer.enqueue(returned);
						}
					}
					// actual setting must occur at the end for consistency with collection handling
					instance[i] = returned;
				}
			}
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
	@Override
	public final void traverseReferences(
		final Object[]          instance        ,
		final TraversalEnqueuer enqueuer        ,
		final TraversalAcceptor acceptor        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		final int length = instance.length;
		try
		{
			for(int i = 0; i < length; i++)
			{
				final Object current, returned;
				if(acceptor.acceptReference(current = instance[i], instance))
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
					// actual setting must occur at the end for consistency with collection handling
					instance[i] = returned;
				}
			}
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
	@Override
	public final void traverseReferences(
		final Object[]          instance,
		final TraversalAcceptor acceptor
	)
	{
		final int length = instance.length;
		try
		{
			for(int i = 0; i < length; i++)
			{
				acceptor.acceptReference(instance[i], instance);
			}
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
	@Override
	public final void traverseReferences(
		final Object[]          instance        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		final int length = instance.length;
		try
		{
			for(int i = 0; i < length; i++)
			{
				final Object current = instance[i], returned;
				if((returned = mutator.mutateReference(current, instance)) != current)
				{
					if(mutationListener != null)
					{
						mutationListener.registerChange(instance, current, returned);
					}
					// actual setting must occur at the end for consistency with collection handling
					instance[i] = returned;
				}
			}
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
	@Override
	public final void traverseReferences(
		final Object[]          instance        ,
		final TraversalAcceptor acceptor        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		final int length = instance.length;
		try
		{
			for(int i = 0; i < length; i++)
			{
				final Object current, returned;
				acceptor.acceptReference(current = instance[i], instance);
				if((returned = mutator.mutateReference(current, instance)) != current)
				{
					if(mutationListener != null)
					{
						mutationListener.registerChange(instance, current, returned);
					}
					// actual setting must occur at the end for consistency with collection handling
					instance[i] = returned;
				}
			}
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
}
