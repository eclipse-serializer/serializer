package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
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

public interface PersistenceChannel<D> extends PersistenceTarget<D>, PersistenceSource<D>
{
	// just a typing interface so far.
	
	public default void prepareChannel()
	{
		this.prepareSource();
		this.prepareTarget();
	}
	
	public default void closeChannel()
	{
		this.closeSource();
		this.closeTarget();
	}
	
}
