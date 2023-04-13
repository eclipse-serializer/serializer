package org.eclipse.serializer.util;

/*-
 * #%L
 * Eclipse Serializer Base
 * %%
 * Copyright (C) 2023 Eclipse Foundation
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
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
