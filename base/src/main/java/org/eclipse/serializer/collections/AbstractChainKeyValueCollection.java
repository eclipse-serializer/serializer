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

import org.eclipse.serializer.typing.KeyValue;

public abstract class AbstractChainKeyValueCollection<K, V, EN extends AbstractChainEntry<KeyValue<K, V>, K, V, EN>>
extends AbstractChainCollection<KeyValue<K, V>, K, V, EN>
{
	@Override
	protected abstract void internalRemoveEntry(EN chainEntry);

	@Override
	protected abstract int internalRemoveNullEntries();

	@Override
	protected abstract int internalClear();

}
