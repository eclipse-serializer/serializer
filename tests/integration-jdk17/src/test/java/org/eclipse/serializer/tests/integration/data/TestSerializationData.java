package org.eclipse.serializer.tests.integration.data;

/*-
 * #%L
 * Eclipse Serializer Test on JDK 17
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

import java.util.StringJoiner;

public class TestSerializationData
{

    private final Object instance;
    private final String fileName;

    private final CompareInstances compareInstances;

    public TestSerializationData(Object instance, String fileName, CompareInstances compareInstances)
    {
        this.instance = instance;
        this.fileName = fileName;
        this.compareInstances = compareInstances;
    }

    public Object getInstance()
    {
        return instance;
    }

    public String getFileName()
    {
        return fileName;
    }

    public CompareInstances getCompareInstances()
    {
        return compareInstances;
    }

    @Override
    public String toString()
    {
        return new StringJoiner("Testing ")
                .add("fileName='" + fileName + "'")
                .toString();
    }
}
