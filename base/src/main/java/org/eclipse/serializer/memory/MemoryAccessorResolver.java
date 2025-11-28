package org.eclipse.serializer.memory;

import java.util.Iterator;
import java.util.ServiceLoader;


/**
 * Service loader for {@link MemoryAccessor}s.
 *
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

		throw new RuntimeException("No MemoryAccessor implementation found");
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
