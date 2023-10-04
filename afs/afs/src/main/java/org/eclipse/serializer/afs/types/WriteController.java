package org.eclipse.serializer.afs.types;

/*-
 * #%L
 * Eclipse Serializer Abstract File System
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

import org.eclipse.serializer.afs.exceptions.AfsExceptionReadOnly;

@FunctionalInterface
public interface WriteController
{
	public default void validateIsWritable()
	{
		if(this.isWritable())
		{
			return;
		}
		
		throw new AfsExceptionReadOnly("Writing is not enabled.");
	}
	
	public boolean isWritable();
	
	
	
	public static WriteController Enabled()
	{
		// Singleton is (usually) an anti pattern.
		return new WriteController.Enabled();
	}
	
	public final class Enabled implements WriteController
	{
		Enabled()
		{
			super();
		}
		
		@Override
		public final void validateIsWritable()
		{
			// no-op
		}

		@Override
		public final boolean isWritable()
		{
			return true;
		}
		
	}
	
	public static WriteController Disabled()
	{
		// Singleton is (usually) an anti pattern.
		return new WriteController.Disabled();
	}
	
	public final class Disabled implements WriteController
	{
		Disabled()
		{
			super();
		}

		@Override
		public final boolean isWritable()
		{
			return false;
		}
		
	}
	
}
