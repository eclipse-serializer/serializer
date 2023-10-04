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


/**
 * A wrapper for a mutable boolean type and convenience setting and getting methods.
 */
public interface Flag
{
	boolean set(boolean state);
	
	boolean on();

	boolean off();
	
	boolean isOn();
	
	boolean isOff();

	boolean toggle();

	
	
	static Flag New()
	{
		return New(false);
	}

	static Flag New(final boolean state)
	{
		return new Simple(state);
	}

	final class Simple implements Flag
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private boolean state;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Simple(final boolean state)
		{
			super();
			this.state = state;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public boolean set(final boolean state)
		{
			if(state)
			{
				this.on();
				return false;
			}
			
			this.off();
			return true;
		}

		@Override
		public boolean isOn()
		{
			return this.state;
		}

		@Override
		public boolean isOff()
		{
			return !this.state;
		}

		@Override
		public boolean on()
		{
			final boolean current = this.state;
			this.state = true;
			return current;
		}

		@Override
		public boolean off()
		{
			final boolean current = this.state;
			this.state = false;
			return current;
		}

		@Override
		public boolean toggle()
		{
			return !(this.state = !this.state); // extremely funny syntax
		}

	}

}
