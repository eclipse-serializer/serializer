package org.eclipse.serializer.tests.integration;

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

import org.assertj.core.api.Assertions;
import org.eclipse.serializer.Serializer;
import org.eclipse.serializer.SerializerFoundation;
import org.eclipse.serializer.persistence.binary.jdk17.types.BinaryHandlersJDK17;
import org.eclipse.serializer.tests.integration.data.TestSerializationData;
import org.eclipse.serializer.tests.integration.data.TestSerializationDataProviderJDK17;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

public class SerializationJDK17Test
{

    @ParameterizedTest
    @ArgumentsSource(TestSerializationDataProviderJDK17.class)
    void testSerialization(TestSerializationData serializationData)
    {
        SerializerFoundation<?> foundation = SerializerFoundation.New();
        BinaryHandlersJDK17.registerJDK17TypeHandlers(foundation);
        byte[] bytes;

        try (Serializer<byte[]> serializer = Serializer.Bytes(foundation))
        {

            bytes = serializer.serialize(serializationData.getInstance());

            URL url = getClass().getClassLoader()
                    .getResource(serializationData.getFileName());
            Assertions.assertThat(url)
                    .as(String.format("Cannot find file %s within classpath", serializationData.getFileName()))
                    .isNotNull();


            List<String> result = Files.readAllLines(Paths.get(url.toURI()));

            String encodedBytes = Base64.getEncoder()
                    .encodeToString(bytes);

            String encodedExpected = result.get(0);

            /*
            if (!encodedBytes.equals(encodedExpected)) {
                // FIXME Why is there a difference at position 9 value is 119 vs 124?
                System.out.printf("Difference in binary output for type: %s%n", serializationData.getInstance().getClass().getName());
                System.out.println("Expected");
                result.stream().skip(2).forEach(System.out::println);
                System.out.println("Found");
                System.out.println(PrettyPrint.bytesToHex(bytes));
            }
            Assertions.assertThat(encodedBytes).isEqualTo(encodedExpected);

             */
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
