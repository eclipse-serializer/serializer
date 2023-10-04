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

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface TraversalAcceptor extends TraversalHandler
{
	
	public boolean acceptReference(Object instance, Object parent);
	
	
	public static TraversalAcceptor New(final Consumer<Object> logic)
	{
		return new TraversalAcceptor.Default(logic);
	}
	
	public static TraversalAcceptor New(final Predicate<Object> condition, final Consumer<Object> logic)
	{
		return new TraversalAcceptor.Conditional(condition, logic);
	}
		
	public final class Default implements TraversalAcceptor
	{
		private final Consumer<Object> logic;

		Default(final Consumer<Object> logic)
		{
			super();
			this.logic = logic;
		}

		@Override
		public final boolean acceptReference(final Object instance, final Object parent)
		{
			this.logic.accept(instance);
			return true;
		}
		
	}
	
	public final class Conditional implements TraversalAcceptor
	{
		private final Predicate<Object> condition;
		private final Consumer<Object>  logic    ;

		Conditional(final Predicate<Object> condition, final Consumer<Object> logic)
		{
			super();
			this.condition = condition;
			this.logic     = logic    ;
		}

		@Override
		public final boolean acceptReference(final Object instance, final Object parent)
		{
			if(this.condition.test(instance))
			{
				this.logic.accept(instance);
			}
			return true;
		}
		
	}
		
}
