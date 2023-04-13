package org.eclipse.serializer.tests.model;

/*-
 * #%L
 * integration-jdk17
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
