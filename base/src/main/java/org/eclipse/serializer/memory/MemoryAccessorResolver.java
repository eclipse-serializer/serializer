package org.eclipse.serializer.memory;

import java.util.Iterator;

/*-
 * #%L
 * Eclipse Serializer Base
 * %%
 * Copyright (C) 2023 - 2025 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.util.ServiceLoader;


/**
 * Service loader for {@link MemoryAccessor}s.
 */
public final class MemoryAccessorResolver
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Get the first found implementation of the MemoryAccessor interface.
	 *
	 * @return MemoryAccessor instance
	 */
	public static MemoryAccessor resolve()
	{
		final MemoryAccessorProvider provider = resolveProvider();
		if(provider != null)
		{
			return provider.create();
		}

		return null;
	}

	public static MemoryAccessorProvider resolveProvider()
	{
		final ServiceLoader<MemoryAccessorProvider> serviceLoader =
			ServiceLoader.load(MemoryAccessorProvider.class);
		
		final Iterator<MemoryAccessorProvider> iterator = serviceLoader.iterator();
		return iterator.hasNext()
			? iterator.next()
			: null
		;
	}


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private MemoryAccessorResolver()
	{
		throw new Error();
	}
}
