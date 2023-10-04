package org.eclipse.serializer.tests.model;

/*-
 * #%L
 * Eclipse Serializer Test on JDK 8
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

public class FieldEvaluatorExample
{

    private String value;
    private transient int ignored = 123;

    private String _internalField;

    public FieldEvaluatorExample(String value, int ignored, String _internalField)
    {
        this.value = value;
        this.ignored = ignored;
        this._internalField = _internalField;
    }

    public String getValue()
    {
        return value;
    }

    public int getIgnored()
    {
        return ignored;
    }

    public String get_internalField()
    {
        return _internalField;
    }
}
