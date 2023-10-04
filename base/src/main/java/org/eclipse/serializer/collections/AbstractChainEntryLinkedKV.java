package org.eclipse.serializer.collections;

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

import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.typing.KeyValue;


public abstract class AbstractChainEntryLinkedKV<K, V, EN extends AbstractChainEntryLinkedKV<K, V, EN>>
extends AbstractChainEntryLinked<KeyValue<K, V>, K, V, EN>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractChainEntryLinkedKV(final EN link)
	{
		super(link);
	}

	@Override
	public String toString()
	{
		// only for debug
		return VarString.New().append('(').add(this.key()).append('=').add(this.value()).append(')').toString();
	}

}
