package org.eclipse.serializer.wrapping;

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

/**
 * Generic interface for the wrapper (decorator) pattern.
 * 
 */
public interface Wrapper<W>
{
	public W wrapped();
	
	
	public abstract class Abstract<W> implements Wrapper<W>
	{
		private final W wrapped;

		protected Abstract(final W wrapped)
		{
			super();
			
			this.wrapped = wrapped;
		}
		
		@Override
		public final W wrapped()
		{
			return this.wrapped;
		}
		
	}
	
}
