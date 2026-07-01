package test.eclipse.serializer.serializer;

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

import org.eclipse.serializer.Serializer;
import org.eclipse.serializer.SerializerFoundation;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import test.eclipse.serializer.fixtures.TypeEnum;

public class SerializerTypeTest
{


    @ParameterizedTest
    @EnumSource(value = TypeEnum.class, names = {"Lazy", "IdentityHashMap", "HashTableMicroStream"}, mode = EnumSource.Mode.EXCLUDE)
    public void serializerTest(TypeEnum type)
    {

        final SerializerFoundation<?> foundation = SerializerFoundation.New();

        final var original = type.getOriginal();
        final Serializer<byte[]> serializer = Serializer.Bytes(foundation);
        byte[] data = serializer.serialize(original);

        final Serializer<byte[]> deserializer = Serializer.Bytes(foundation);
        Object o = deserializer.deserialize(data);
        original.proveResults(o);
    }

    @ParameterizedTest
    @EnumSource(value = TypeEnum.class, names = {"Lazy", "IdentityHashMap", "HashTableMicroStream"}, mode = EnumSource.Mode.EXCLUDE)
    public void serializerTestBinary(TypeEnum type)
    {

        final SerializerFoundation<?> foundation = SerializerFoundation.New();

        final var original = type.getOriginal();
        final Serializer<Binary> serializer = Serializer.Binary(foundation);
        Binary data = serializer.serialize(original);

        final Serializer<Binary> deserializer = Serializer.Binary(foundation);
        Object o = deserializer.deserialize(data);
        original.proveResults(o);
    }

}
