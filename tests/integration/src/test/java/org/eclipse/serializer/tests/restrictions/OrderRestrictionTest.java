package org.eclipse.serializer.tests.restrictions;

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

import org.eclipse.serializer.Serializer;
import org.eclipse.serializer.SerializerFoundation;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import org.assertj.core.api.Assertions;
import org.eclipse.serializer.tests.model.Address;
import org.eclipse.serializer.tests.model.Employee;
import org.junit.jupiter.api.Test;

class OrderRestrictionTest
{

    @Test
    void failure() throws Exception
    {

        SerializerFoundation<?> foundation = SerializerFoundation.New();

        byte[] bytesEmployee;
        byte[] bytesAddress;

        try (Serializer<byte[]> serializer = Serializer.Bytes(foundation))
        {

            bytesEmployee = serializer.serialize(createCircular());

            bytesAddress = serializer.serialize(testAddress());

        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        foundation = SerializerFoundation.New();
        // Serialization fails because the binary for Address was created after one for Employee
        // was created. This means that the  id assignment doesn't match.
        // use registerEntityType or TypedSerializer.
        try (Serializer<byte[]> serializer = Serializer.Bytes(foundation))
        {
            Assertions.assertThatThrownBy(
                            () -> serializer.deserialize(bytesAddress)
                    )
                    .isInstanceOf(PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId.class)
                    .hasMessage("No type handler found for type id \"1000057\".");
        }


    }

    @Test
    void failure_wrongOrder() throws Exception
    {

        SerializerFoundation<?> foundation = SerializerFoundation.New();

        byte[] bytesEmployee;
        byte[] bytesAddress;

        try (Serializer<byte[]> serializer = Serializer.Bytes(foundation))
        {

            bytesEmployee = serializer.serialize(createCircular());

            bytesAddress = serializer.serialize(testAddress());

        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        foundation = SerializerFoundation.New();
        // Wrong order
        foundation.registerEntityType(Address.class);
        foundation.registerEntityType(Employee.class);
        try (Serializer<byte[]> serializer = Serializer.Bytes(foundation))
        {
            Assertions.assertThatThrownBy(
                            () -> serializer.deserialize(bytesAddress)
                    )
                    .isInstanceOf(PersistenceExceptionTypeNotPersistable.class)
                    .hasMessage("Type not persistable: \"interface java.util.List\".");
        }


    }

    @Test
    void success() throws Exception
    {

        SerializerFoundation<?> foundation = SerializerFoundation.New();

        byte[] bytesEmployee;
        byte[] bytesAddress;

        try (Serializer<byte[]> serializer = Serializer.Bytes(foundation))
        {

            bytesEmployee = serializer.serialize(createCircular());

            bytesAddress = serializer.serialize(testAddress());

        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }


        foundation = SerializerFoundation.New();
        // As this is what happens under the hood when a non registered is type is encountered by the serialize.
        foundation.registerEntityType(Employee.class);
        foundation.registerEntityType(Address.class);

        Address address;
        try (Serializer<byte[]> serializer = Serializer.Bytes(foundation))
        {
            address = serializer.deserialize(bytesAddress);
        }

        Assertions.assertThat(address.toString())
                .isEqualTo("Address[id=123, street='to nowhere', city='somewhere', postalCode='666']");
    }


    private static Address testAddress()
    {
        return new Address(123, "to nowhere", "somewhere", "666");
    }

    private static Employee createCircular()
    {
        Employee theBoss = new Employee(1L, "The boss");

        Employee employee1 = new Employee(2L, "Person X");
        Employee employee2 = new Employee(3L, "Person Y");
        Employee employee3 = new Employee(4L, "Person Z");

        employee3.setManager(employee2);

        employee1.setManager(theBoss);
        employee2.setManager(theBoss);

        return theBoss;
    }
}
