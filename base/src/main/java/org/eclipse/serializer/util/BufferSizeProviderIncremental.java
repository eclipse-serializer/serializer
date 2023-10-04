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

import org.eclipse.serializer.typing.Immutable;
import org.eclipse.serializer.typing.Stateless;
import org.eclipse.serializer.math.XMath;


public interface BufferSizeProviderIncremental extends BufferSizeProvider
{
	public default long provideIncrementalBufferSize()
	{
		return this.provideBufferSize();
	}



	public final class Default implements BufferSizeProviderIncremental, Stateless
	{
		// since default methods, java is missing interface instantiation
	}
	
	
	public static BufferSizeProviderIncremental New()
	{
		return new BufferSizeProviderIncremental.Default();
	}
	
	public static BufferSizeProviderIncremental New(final long bufferSize)
	{
		return New(bufferSize, bufferSize);
	}
	
	public static BufferSizeProviderIncremental New(final long initialBufferSize, final long incrementalBufferSize)
	{
		return new BufferSizeProviderIncremental.Sized(
			XMath.positive(initialBufferSize),
			XMath.positive(incrementalBufferSize)
		);
	}
	
	public final class Sized implements BufferSizeProviderIncremental, Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final long initialBufferSize    ;
		private final long incrementalBufferSize;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Sized(final long initialBufferSize, final long incrementalBufferSize)
		{
			super();
			this.initialBufferSize     = initialBufferSize    ;
			this.incrementalBufferSize = incrementalBufferSize;
		}


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long provideBufferSize()
		{
			return this.initialBufferSize;
		}

		@Override
		public final long provideIncrementalBufferSize()
		{
			return this.incrementalBufferSize;
		}

	}

}
