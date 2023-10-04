package org.eclipse.serializer.memory.sun;

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

import java.nio.ByteBuffer;

import org.eclipse.serializer.memory.DirectBufferDeallocator;

final class JdkDirectBufferDeallocator implements DirectBufferDeallocator
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final JdkDirectBufferDeallocator New()
	{
		return new JdkDirectBufferDeallocator();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	JdkDirectBufferDeallocator()
	{
		super();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public boolean deallocateDirectBuffer(final ByteBuffer directBuffer)
	{
		return JdkInternals.internalDeallocateDirectBuffer(directBuffer);
	}
	
}
