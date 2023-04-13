package org.eclipse.serializer.tests.integration;

/*-
 * #%L
 * Eclipse Serializer Test on JDK 8
 * %%
 * Copyright (C) 2023 Eclipse Foundation
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

import org.eclipse.serializer.SerializerFoundation;
import org.eclipse.serializer.persistence.binary.jdk8.types.BinaryHandlersJDK8;
import org.eclipse.serializer.tests.integration.data.TestSerializationDataProviderJDK8;
import org.eclipse.serializer.ObjectCopier;
import org.eclipse.serializer.tests.integration.data.TestSerializationData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class DeepCopyJDK8Test
{

    @ParameterizedTest
    @ArgumentsSource(TestSerializationDataProviderJDK8.class)
    void testCopy(TestSerializationData serializationData)
    {
        SerializerFoundation<?> foundation = SerializerFoundation.New();
        BinaryHandlersJDK8.registerJDK8TypeHandlers(foundation);

        try (ObjectCopier objectCopier = ObjectCopier.New(foundation))
        {

            Object cloned = objectCopier.copy(serializationData.getInstance());

            serializationData.getCompareInstances()
                    .doCheckEquality(cloned, serializationData.getInstance());
        }
    }
}
