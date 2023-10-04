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

public interface TypeTraverser<T>
{
	public void traverseReferences(
		T                 instance,
		TraversalEnqueuer enqueuer
	);
	
	public void traverseReferences(
		T                 instance,
		TraversalEnqueuer enqueuer,
		TraversalAcceptor acceptor
	);
	
	public void traverseReferences(
		T                 instance        ,
		TraversalEnqueuer enqueuer        ,
		TraversalMutator  mutator         ,
		MutationListener  mutationListener
	);

	public void traverseReferences(
		T                 instance        ,
		TraversalEnqueuer enqueuer        ,
		TraversalAcceptor acceptor        ,
		TraversalMutator  mutator         ,
		MutationListener  mutationListener
	);
	
	public void traverseReferences(
		T                 instance,
		TraversalAcceptor acceptor
	);
	
	public void traverseReferences(
		T                instance        ,
		TraversalMutator mutator         ,
		MutationListener mutationListener
	);

	public void traverseReferences(
		T                 instance        ,
		TraversalAcceptor acceptor        ,
		TraversalMutator  mutator         ,
		MutationListener  mutationListener
	);
	

	
	public interface Creator
	{
		public <T> TypeTraverser<T> createTraverser(Class<T> type);
						
	}
	
}
