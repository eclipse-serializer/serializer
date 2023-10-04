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

public interface TraversalMode
{
	public void handle(Object[] instances, TraversalReferenceHandler referenceHandler);
	
	
	public final class Full implements TraversalMode
	{
		@Override
		public final void handle(final Object[] instances, final TraversalReferenceHandler referenceHandler)
		{
			referenceHandler.handleAsFull(instances);
		}
		
	}
	
	public final class Node implements TraversalMode
	{
		@Override
		public final void handle(final Object[] instances, final TraversalReferenceHandler referenceHandler)
		{
			referenceHandler.handleAsNode(instances);
		}
		
	}
	
	public final class Leaf implements TraversalMode
	{
		@Override
		public final void handle(final Object[] instances, final TraversalReferenceHandler referenceHandler)
		{
			referenceHandler.handleAsLeaf(instances);
		}
		
	}
}
