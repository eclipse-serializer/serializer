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

import org.eclipse.serializer.math.XMath;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.typing.Immutable;
import org.eclipse.serializer.typing.Stateless;

public interface BufferSizeProvider
{
	public default long provideBufferSize()
	{
		return XMemory.defaultBufferSize();
	}
	
	public static BufferSizeProvider New()
	{
		return new BufferSizeProvider.Default();
	}
	
	public static BufferSizeProvider New(final long bufferSize)
	{
		return new BufferSizeProvider.Sized(
                XMath.positive(bufferSize)
		);
	}
	
	public final class Default implements BufferSizeProvider, Stateless
	{
		Default()
		{
			super();
		}
	}
	
	public final class Sized implements BufferSizeProviderIncremental, Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final long bufferSize;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Sized(final long bufferSize)
		{
			super();
			this.bufferSize = bufferSize;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long provideBufferSize()
		{
			return this.bufferSize;
		}

		@Override
		public final long provideIncrementalBufferSize()
		{
			return this.bufferSize;
		}

	}
	
}
