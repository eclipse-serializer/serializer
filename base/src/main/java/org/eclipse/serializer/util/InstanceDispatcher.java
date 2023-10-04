package org.eclipse.serializer.util;

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

import static org.eclipse.serializer.util.X.coalesce;

import org.eclipse.serializer.functional.InstanceDispatcherLogic;

public interface InstanceDispatcher
{
	public InstanceDispatcher setInstanceDispatcherLogic(InstanceDispatcherLogic logic);
	
	public InstanceDispatcherLogic getInstanceDispatcherLogic();
	
	
	
	public class Default implements InstanceDispatcher
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		private static final InstanceDispatcherLogic NO_OP = new InstanceDispatcherLogic()
		{
			@Override
			public <T> T apply(final T subject)
			{
				return subject;
			}
		};
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private InstanceDispatcherLogic logic = NO_OP;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////
		
		protected final <T> T dispatch(final T newInstance)
		{
			return this.logic.apply(newInstance);
		}
		
		@Override
		public InstanceDispatcher setInstanceDispatcherLogic(final InstanceDispatcherLogic instanceDispatcher)
		{
			this.logic = coalesce(instanceDispatcher, NO_OP);
			return this;
		}
		
		@Override
		public InstanceDispatcherLogic getInstanceDispatcherLogic()
		{
			return this.logic;
		}
		
	}

}
