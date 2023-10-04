package org.eclipse.serializer.tests.integration;

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

import org.eclipse.serializer.ObjectCopier;
import org.eclipse.serializer.SerializerFoundation;
import org.eclipse.serializer.persistence.binary.jdk8.types.BinaryHandlersJDK8;
import org.eclipse.serializer.tests.integration.data.TestSerializationData;
import org.eclipse.serializer.tests.integration.data.TestSerializationDataProviderJDK8;
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
