package org.eclipse.serializer.math;

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

public interface _longRange
{
	public long start();
	
	public long bound();
	
	public default long length()
	{
		return this.bound() - this.start();
	}
	
	
	
	public static _longRange New(final long start, final long bound)
	{
		return new _longRange.Default(start, bound);
	}
	
	public final class Default implements _longRange
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final long
			start,
			bound
		;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final long start, final long bound)
		{
			super();
			this.start = start;
			this.bound = bound;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final long start()
		{
			return this.start;
		}
		
		@Override
		public final long bound()
		{
			return this.bound;
		}
		
	}
	
}
