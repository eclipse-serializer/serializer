package org.eclipse.serializer.communication.types;

/*-
 * #%L
 * Eclipse Serializer Communication Parent
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

import java.nio.ByteOrder;

import org.eclipse.serializer.persistence.types.PersistenceIdStrategy;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryView;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryViewProvider;

public interface ComProtocolData extends PersistenceTypeDictionaryViewProvider
{
	public String name();
	
	public String version();
	
	public ByteOrder byteOrder();
	
	public PersistenceTypeDictionaryView typeDictionary();
	
	public PersistenceIdStrategy idStrategy();
	
	@Override
	public default PersistenceTypeDictionaryView provideTypeDictionary()
	{
		return this.typeDictionary();
	}

	public int inactivityTimeout();
	
}
