package org.eclipse.serializer.nativememory;

import org.eclipse.serializer.memory.MemoryAccessor;
import org.eclipse.serializer.memory.MemoryAccessorProvider;

public class NativeMemoryAccessorProvider implements MemoryAccessorProvider
{
	@Override
	public MemoryAccessor create() {
		return NativeMemoryAccessor.New();
	}
}
