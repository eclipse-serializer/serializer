package org.eclipse.serializer.tests.integration;

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

import org.eclipse.serializer.Serializer;
import org.eclipse.serializer.SerializerFoundation;
import org.eclipse.serializer.util.X;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.binary.types.BinaryLegacyTypeHandler;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.assertj.core.api.Assertions;
import org.eclipse.serializer.tests.model.Address;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

class RestructuringTest
{

    @Test
    void loadOldStructure() throws URISyntaxException, IOException
    {
        URL url = getClass().getClassLoader()
                .getResource("OldAddress.txt");
        Assertions.assertThat(url)
                .as("Cannot find file 'OldAddress.txt' within classpath")
                .isNotNull();

        List<String> lines = Files.readAllLines(Paths.get(url.toURI()));
        byte[] bytes = Base64.getDecoder()
                .decode(lines.get(0));

        SerializerFoundation<?> foundation = SerializerFoundation.New();
        foundation.registerEntityType(Address.class);
        Address newAddress;
        try (Serializer<byte[]> serializer = Serializer.Bytes(foundation))
        {
            // It is able to deserialise the bytes but content end up in wrong fields !!
            // It is able to load it since we just gave moved/renamed String variables
            newAddress = serializer.deserialize(bytes);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        Assertions.assertThat(newAddress.getId())
                .isEqualTo(123);
        Assertions.assertThat(newAddress.getStreet())
                .isEqualTo("somewhere"); // should be 'to nowhere'
        Assertions.assertThat(newAddress.getCity())
                .isEqualTo("666"); // should be 'somewhere'
        Assertions.assertThat(newAddress.getPostalCode())
                .isEqualTo("to nowhere"); // should be '666'

    }

    @Test
    void loadOldStructure_corrected() throws URISyntaxException, IOException
    {
        URL url = getClass().getClassLoader()
                .getResource("OldAddress.txt");
        Assertions.assertThat(url)
                .as("Cannot find file 'OldAddress.txt' within classpath")
                .isNotNull();

        List<String> lines = Files.readAllLines(Paths.get(url.toURI()));
        byte[] bytes = Base64.getDecoder()
                .decode(lines.get(0));

        SerializerFoundation<?> foundation = SerializerFoundation.New();
        foundation.registerEntityType(Address.class);
        foundation.registerCustomTypeHandler(new AddressLegacyTypeMapper());

        Address newAddress;
        try (Serializer<byte[]> serializer = Serializer.Bytes(foundation))
        {
            newAddress = serializer.deserialize(bytes);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        Assertions.assertThat(newAddress.getId())
                .isEqualTo(123);
        Assertions.assertThat(newAddress.getStreet())
                .isEqualTo("to nowhere");
        Assertions.assertThat(newAddress.getCity())
                .isEqualTo("somewhere");
        Assertions.assertThat(newAddress.getPostalCode())
                .isEqualTo("666");

    }

    private static class AddressLegacyTypeMapper extends BinaryLegacyTypeHandler.AbstractCustom<Address>
    {

        // need to know the binary layout of the persisted legacy class (execute this on old class)
        // PersistenceTypeHandlerRegistry<Binary> registry = foundation.getTypeHandlerRegistry();
        // long typeId = registry.lookupTypeId(Address.class);
        // PersistenceTypeHandler<Binary, ?> handler = registry.lookupTypeHandler(typeId);
        // System.out.println(handler.allMembers());

        // This is the order of the old class in the binary format
        // city - postalCode - streetName - id

        private static final long BINARY_OFFSET_city = 0;
        private static final long BINARY_OFFSET_postalCode = BINARY_OFFSET_city + Binary.objectIdByteLength();
        private static final long BINARY_OFFSET_streetName = BINARY_OFFSET_postalCode + Binary.objectIdByteLength();
        private static final long BINARY_OFFSET_id = BINARY_OFFSET_streetName + Binary.objectIdByteLength();


        protected AddressLegacyTypeMapper()
        {
            super(Address.class,
                  X.List());  // No items needed here for this case
        }

        @Override
        public void iterateLoadableReferences(Binary data, PersistenceReferenceLoader iterator)
        {
            // No actions needed in this case
        }

        @Override
        public Address create(Binary data, PersistenceLoadHandler handler)
        {
            // the data is not available yet, this is, the other objects like Strings are not loaded.
            // See updateState
            return new Address();
        }

        @Override
        public void updateState(Binary data, Address instance, PersistenceLoadHandler handler)
        {

            Long id = data.read_long(BINARY_OFFSET_id);
            String city = (String) handler.lookupObject(data.read_long(BINARY_OFFSET_city));
            String postalCode = (String) handler.lookupObject(data.read_long(BINARY_OFFSET_postalCode));
            String streetName = (String) handler.lookupObject(data.read_long(BINARY_OFFSET_streetName));

            instance.setId(id);
            instance.setCity(city);
            instance.setPostalCode(postalCode);
            instance.setStreet(streetName);
        }

        @Override
        public boolean hasPersistedReferences()
        {
            return false;
        }

        @Override
        public boolean hasVaryingPersistedLengthInstances()
        {
            return false;
        }
    }
}
