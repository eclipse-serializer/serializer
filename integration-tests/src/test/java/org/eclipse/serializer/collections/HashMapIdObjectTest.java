package org.eclipse.serializer.collections;

/*-
 * #%L
 * Eclipse Serializer Integration Tests
 * %%
 * Copyright (C) 2023 - 2026 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HashMapIdObjectTest
{
	@Test
	void toString_emptyMap()
	{
		// regression: threw StringIndexOutOfBoundsException
		assertEquals("{}", HashMapIdObject.New().toString());
	}

	@Test
	void toString_singleEntry()
	{
		final HashMapIdObject<String> map = HashMapIdObject.New();
		map.add(1L, "a");
		assertEquals("{1 -> a}", map.toString());
	}

	@Test
	void toString_afterClear()
	{
		final HashMapIdObject<String> map = HashMapIdObject.New();
		map.add(1L, "a");
		map.clear();
		assertEquals("{}", map.toString());
	}
}
