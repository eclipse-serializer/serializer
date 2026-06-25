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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class XArraysTest {


    @Test
    void removeSelection_fromWholeArrayTest() {
        Integer[] integers = {1, 2, null, 4, null, 6, 7, 8, 9};

        XArrays.removeAllFromArray(integers, 2, integers.length, null);

        Integer[] expectedResult = {1, 2, 4, 6, 7, 8, 9, null, null};
        Assertions.assertArrayEquals(expectedResult, integers);
    }
}
