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

public class ArrayViewTest
{

    @Test
    void ArrayView_defaultConstructor()
    {
        ArrayView arrayview = new ArrayView();
        Assertions.assertTrue(arrayview.isEmpty());
    }

    @Test
    void ArrayView_original()
    {
        ArrayView original = new ArrayView();
        original.size = 2;
        original.data = new Object[0];
        ArrayView arrayview = new ArrayView(original);
        Assertions.assertEquals(original.size, arrayview.size);
        Assertions.assertEquals(original.data, arrayview.data);
    }
}
