package org.eclipse.serializer.reference;

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

@FunctionalInterface
public interface _booleanReference
{
	boolean get();
	
	
	
	static _booleanReference True()
	{
		// Singleton is an anti-pattern.
		return new True();
	}
	
	static _booleanReference False()
	{
		// Singleton is an anti-pattern.
		return new False();
	}
	
	static _booleanReference New(final boolean value)
	{
		return new Default(value);
	}
		
	final class Default implements _booleanReference
	{
		final boolean value;

		Default(final boolean value)
		{
			super();
			this.value = value;
		}

		@Override
		public boolean get()
		{
			return this.value;
		}
		
	}
	
	final class True implements _booleanReference
	{

		@Override
		public boolean get()
		{
			return true;
		}
		
	}
	
	final class False implements _booleanReference
	{

		@Override
		public boolean get()
		{
			return false;
		}
		
	}
	
}
