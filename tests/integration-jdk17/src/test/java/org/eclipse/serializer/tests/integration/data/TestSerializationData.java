package org.eclipse.serializer.tests.integration.data;

/*-
 * #%L
 * integration
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
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
