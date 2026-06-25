package test.eclipse.serializer.communication.tls;

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

import java.io.File;

import test.eclipse.serializer.communication.AbstractCommunicationTest;

public class AbstractSecurityComTest extends AbstractCommunicationTest
{

    protected char[] password = new char[]{'m', 'i', 'c', 'r', 'o', 's', 't', 'r', 'e', 'a', 'm'};
    //protected char[] password2 = new char[]{'m','i','c','r','o','s','t','r','e','a','m'};
    //protected final char[] password = "microstream".toCharArray();

    public String findPksPath()
    {
        String store =
                this.getClass().getClassLoader().getResource("example_store.p12").getFile();
        File file = new File(store);

        return file.getAbsolutePath();
    }
}
