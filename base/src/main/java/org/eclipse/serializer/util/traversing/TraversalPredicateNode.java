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

import java.util.function.Predicate;

import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.collections.types.XGettingSet;

@FunctionalInterface
public interface TraversalPredicateNode extends TraversalPredicate
{
	public boolean isNode(Object instance);

	

	public static TraversalPredicateNode New(
		final XGettingSet<Object>        explicitInstances,
		final Predicate<Object>          customPredicate  ,
		final XGettingSet<Class<?>>      positiveTypes    ,
		final XGettingSequence<Class<?>> typesPolymorphic
	)
	{
		return new TraversalPredicateNode.Default(
			explicitInstances,
			customPredicate  ,
			positiveTypes    ,
			typesPolymorphic
		);
	}
	
	public final class Default extends AbstractHandlingPredicate implements TraversalPredicateNode
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final XGettingSet<Object> explicitInstances;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final XGettingSet<Object>        explicitInstances,
			final Predicate<Object>          customPredicate  ,
			final XGettingSet<Class<?>>      positiveTypes    ,
			final XGettingSequence<Class<?>> typesPolymorphic
		)
		{
			super(customPredicate, positiveTypes, typesPolymorphic);
			this.explicitInstances = explicitInstances;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final boolean isNode(final Object instance)
		{
			if(this.explicitInstances != null && this.explicitInstances.contains(instance))
			{
				return true;
			}
			return this.test(instance);
		}
		
	}
	
}
