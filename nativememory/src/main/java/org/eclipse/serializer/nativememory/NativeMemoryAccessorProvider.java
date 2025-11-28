package org.eclipse.serializer.nativememory;

/*-
 * #%L
 * Eclipse Serializer NativeMemory
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

import org.eclipse.serializer.memory.MemoryAccessor;
import org.eclipse.serializer.memory.MemoryAccessorProvider;

public class NativeMemoryAccessorProvider implements MemoryAccessorProvider
{
	@Override
	public MemoryAccessor create() {
		return NativeMemoryAccessor.New();
	}
}
