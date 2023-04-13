package org.eclipse.serializer.memory.android;

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

import org.eclipse.serializer.functional.DefaultInstantiator;
import org.eclipse.serializer.memory.DirectBufferDeallocator;
import org.eclipse.serializer.memory.MemoryAccessorGeneric;
import org.eclipse.serializer.memory.XMemory;


/**
 * Trivial setup wrapping class to simplify and document the different setup possibilities.
 *
 */
public final class MicroStreamAndroidAdapter
{
	
	/**
	 * Sets up the memory accessing logic to use {@link MemoryAccessorGeneric}.
	 * <p>
	 * {@link AndroidInstantiatorBlank} ist used as the {@link DefaultInstantiator} implementation.
	 * <p>
	 * {@link AndroidDirectBufferDeallocator} is used as the {@link DirectBufferDeallocator}.
	 * 
	 */
	public static final void setupFull()
	{
		XMemory.setMemoryAccessor(
			MemoryAccessorGeneric.New(
				AndroidInternals.InstantiatorBlank(),
				AndroidInternals.DirectBufferDeallocator()
			)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private MicroStreamAndroidAdapter()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
