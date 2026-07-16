package test.eclipse.serializer.fixtures.types;

/*-
 * #%L
 * EclipseStore Integration Tests
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

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import org.eclipse.serializer.collections.EqConstList;
import org.eclipse.serializer.equality.Equalator;
import org.junit.jupiter.api.Assertions;

public class EqConstListData implements BinaryHandlerTestData
{

    private EqConstList<Integer> value;

    @Override
    public EqConstListData fillSampleData()
    {
        Integer first = 130;
        Integer second = 300;
        value = new EqConstList<>(new IntegerEquality(), first, second);
        return this;
    }

    EqConstList<Integer> getValue()
    {
        return value;
    }

    @Override
    public void proveResults(Object o)
    {
        Assertions.assertNotNull(o);
        EqConstListData copy = (EqConstListData) o;
        assertIterableEquals(this.getValue(), copy.getValue(), "EqConstList");
    }

    static class IntegerEquality implements Equalator<Integer>
    {
        IntegerEquality()
        {
        }

        @Override
        public boolean equal(Integer integer, Integer t1)
        {
            return integer.equals(t1);
        }
    }

}
