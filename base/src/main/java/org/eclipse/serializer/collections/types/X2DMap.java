package org.eclipse.serializer.collections.types;

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

import java.util.function.Function;

public interface X2DMap<K1, K2, V> extends XGetting2DMap<K1, K2, V>
{
	public boolean add(K1 key1, K2 key2, V value);
	
	public boolean put(K1 key1, K2 key2, V value);
	
	public V ensure(K1 key1, K2 key2, Function<? super K2, V> valueSupplier);
	
}
