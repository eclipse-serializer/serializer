package org.eclipse.serializer.collections.interfaces;

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

public interface OptimizableCollection extends Sized
{
	/**
	 * Optimizes the internal storage of this collection and returns the storage size of the collection after the
	 * process is complete.
	 *
	 * @return the storage size of the collection after the optimization.
	 */
	public long optimize();

}
