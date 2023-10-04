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

import org.eclipse.serializer.collections.interfaces.ChainKeyValueStorage;
import org.eclipse.serializer.typing.KeyValue;


public abstract class AbstractChainKeyValueStorage<K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>>
extends ChainStorageStrong<KeyValue<K, V>, K, V, EN>
implements ChainKeyValueStorage<K, V, EN>
{
	public AbstractChainKeyValueStorage(final AbstractChainCollection<KeyValue<K, V>, K, V, EN> parent, final EN head)
	{
		super(parent, head);
	}

}
