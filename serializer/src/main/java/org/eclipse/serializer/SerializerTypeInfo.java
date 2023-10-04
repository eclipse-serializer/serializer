package org.eclipse.serializer;

/*-
 * #%L
 * Eclipse Serializer
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

import org.eclipse.serializer.collections.types.XGettingList;

public class SerializerTypeInfo
{
	private final XGettingList<String> serializedTypes;
	
	public SerializerTypeInfo(final XGettingList<String> newTypes)
	{
		this.serializedTypes = newTypes;
	}

	public XGettingList<String> getSerializedTypes()
	{
		return this.serializedTypes;
	}
}
