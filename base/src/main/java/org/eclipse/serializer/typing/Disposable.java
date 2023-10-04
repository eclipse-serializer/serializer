package org.eclipse.serializer.typing;

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

public interface Disposable 
{
	/**
	 * Release resources used by the implementing class
	 * that should be released before the garbage collector takes care of them.
	 * <p>
	 * After calling, the owning object may be in an inoperable state which it can't recover from! 
	 */
	public void dispose();
	
}
