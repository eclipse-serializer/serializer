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

import java.util.function.Consumer;

public interface PersistenceTypeHandlerIterable<D>
{
	public <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateTypeHandlers(C iterator);
	
	public <C extends Consumer<? super PersistenceLegacyTypeHandler<D, ?>>> C iterateLegacyTypeHandlers(C iterator);
	
	public default <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateAllTypeHandlers(final C iterator)
	{
		this.iterateTypeHandlers(iterator);
		this.iterateLegacyTypeHandlers(iterator);
		
		return iterator;
	}
}
