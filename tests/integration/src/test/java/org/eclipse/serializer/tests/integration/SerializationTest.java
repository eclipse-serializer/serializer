package org.eclipse.serializer.tests.integration;

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

import org.eclipse.serializer.tests.integration.util.PrettyPrint;
import org.eclipse.serializer.Serializer;
import org.eclipse.serializer.SerializerFoundation;
import org.assertj.core.api.Assertions;
import org.eclipse.serializer.tests.integration.data.TestSerializationData;
import org.eclipse.serializer.tests.integration.data.TestSerializationDataProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

public class SerializationTest
{

    @ParameterizedTest
    @ArgumentsSource(TestSerializationDataProvider.class)
    void testSerialization(TestSerializationData serializationData)
    {
        SerializerFoundation<?> foundation = SerializerFoundation.New();

        byte[] bytes;

        try (Serializer<byte[]> serializer = Serializer.Bytes(foundation))
        {

            bytes = serializer.serialize(serializationData.getInstance());

            URL url = getClass().getClassLoader()
                    .getResource(serializationData.getFileName());
            Assertions.assertThat(url)
                    .as(String.format("Cannot find file %s within classpath", serializationData.getFileName()))
                    .isNotNull();

            if (!"IdentityHashMap.test.txt".equals(serializationData.getFileName()))
            {
                // The binary output is not unique for a IdentityHashMap with the same entries
                // So we only tst restored instance equality later on in the method

                List<String> result = Files.readAllLines(Paths.get(url.toURI()));

                String encodedBytes = Base64.getEncoder()
                        .encodeToString(bytes);

                String encodedExpected = result.get(0);

                if (!encodedBytes.equals(encodedExpected))
                {
                    System.out.printf("Difference in binary output for type: %s%n", serializationData.getInstance()
                            .getClass()
                            .getName());
                    System.out.println("Expected");
                    result.stream()
                            .skip(2)
                            .forEach(System.out::println);
                    System.out.println("Found");
                    System.out.println(PrettyPrint.bytesToHex(bytes));
                }
                Assertions.assertThat(encodedBytes)
                        .isEqualTo(encodedExpected);
            }
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        try (Serializer<byte[]> serializer = Serializer.Bytes(foundation))
        {
            Object reconstructed = serializer.deserialize(bytes);
            // Makes use of the equals to compare instances.
            // For Arrays, make use of Arrays.equals (arrays of primitives) or Arrays.deepEquals
            serializationData.getCompareInstances()
                    .doCheckEquality(reconstructed, serializationData.getInstance());
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }


    }
}
