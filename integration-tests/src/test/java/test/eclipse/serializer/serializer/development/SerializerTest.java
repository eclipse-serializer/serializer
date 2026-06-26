package test.eclipse.serializer.serializer.development;

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

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.util.stream.Stream;

import org.eclipse.serializer.Serializer;
import org.eclipse.serializer.SerializerFoundation;
import org.eclipse.serializer.SerializerTypeInfoStrategyCreator;
import org.eclipse.serializer.TypedSerializer;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class SerializerTest
{

    @ParameterizedTest(name = "serializeSameDeserializerTest {index}")
    @MethodSource("supplySerializers")
    <S> void serializeSameDeserializerTest(final Serializer<S> serializer)
    {
        final S data = serializer.serialize("Hello World");
        final String result = serializer.deserialize(data);

        assertEquals("Hello World", result);
    }

    @ParameterizedTest(name = "serializeDifferentDeserializerTest {index}")
    @MethodSource("supplySerializersPairs")
    <S> void serializeDifferentDeserializerTest(final Serializer<S> serializer, final Serializer<S> deserializer)
    {
        final S data = serializer.serialize("Hello World");
        final String result = deserializer.deserialize(data);

        assertEquals("Hello World", result);
    }

    @ParameterizedTest(name = "serializeTest {index}")
    @MethodSource("supplySerializersPairs")
    <S> void serializeTest(final Serializer<S> serializer, final Serializer<S> deserializer)
    {
        S data = serializer.serialize("Hello World");
        String result = deserializer.deserialize(data);
        assertEquals("Hello World", result);

        data = serializer.serialize("Just a test");
        result = deserializer.deserialize(data);
        assertEquals("Just a test", result);
    }

    @ParameterizedTest(name = "serializeNewTypesTest {index}")
    @MethodSource("supplySerializersPairs")
    <S> void serializeNewTypesTest(final Serializer<S> serializer, final Serializer<S> deserializer)
    {

        final A originalA = new A();
        S data = serializer.serialize(originalA);
        final A resultA = deserializer.deserialize(data);
        assertEquals(originalA, resultA);

        final B originalB = new B();
        data = serializer.serialize(new B());
        final B resultB = deserializer.deserialize(data);
        assertEquals(originalB, resultB);
    }

    @ParameterizedTest(name = "serializeOnceSizeTest {index}")
    @MethodSource("supplyOnceSerializersPairs")
    <S> void serializeOnceSizeTest(final Serializer<S> serializer, final Serializer<S> deserializer)
    {

        final A originalA = new A();
        final S data = serializer.serialize(originalA);
        final A resultA = deserializer.deserialize(data);
        assertEquals(originalA, resultA);

        final A originalA2 = new A();
        final S data2 = serializer.serialize(originalA2);
        final A resultA2 = deserializer.deserialize(data2);
        assertEquals(originalA2, resultA2);

        if (data instanceof byte[]) {
            final byte[] tmp1 = (byte[]) data;
            final byte[] tmp2 = (byte[]) data2;
            assertTrue(tmp1.length > tmp2.length);

        } else if (data instanceof Binary) {
            final Binary tmp1 = (Binary) data;
            final Binary tmp2 = (Binary) data2;

            long size1 = 0;
            for (final ByteBuffer buffer : tmp1.buffers()) {
                size1 += buffer.limit();
            }

            long size2 = 0;
            for (final ByteBuffer buffer : tmp2.buffers()) {
                size2 += buffer.limit();
            }
            assertTrue(size1 > size2);
        } else {
            fail();
        }
    }

    @ParameterizedTest(name = "serializeAlwaysSizeTest {index}")
    @MethodSource("supplyAlwaysSerializersPairs")
    <S> void serializeAlwaysSizeTest(final Serializer<S> serializer, final Serializer<S> deserializer)
    {

        final A originalA = new A();
        final S data = serializer.serialize(originalA);
        final A resultA = deserializer.deserialize(data);
        assertEquals(originalA, resultA);

        final A originalA2 = new A();
        final S data2 = serializer.serialize(originalA2);
        final A resultA2 = deserializer.deserialize(data2);
        assertEquals(originalA2, resultA2);

        if (data instanceof byte[]) {
            final byte[] tmp1 = (byte[]) data;
            final byte[] tmp2 = (byte[]) data2;
            assertEquals(tmp1.length, tmp2.length);

        } else if (data instanceof Binary) {
            final Binary tmp1 = (Binary) data;
            final Binary tmp2 = (Binary) data2;

            long size1 = 0;
            for (final ByteBuffer buffer : tmp1.buffers()) {
                size1 += buffer.limit();
            }

            long size2 = 0;
            for (final ByteBuffer buffer : tmp2.buffers()) {
                size2 += buffer.limit();
            }
            assertEquals(size1, size2);
        } else {
            fail();
        }
    }

    @Test
    void incompatibleTest()
    {

        final Serializer<Binary> serializer = createBinaryDiffOnce();
        final Serializer<Binary> deserializer = createBinaryDiffOnce();

        serializer.serialize(new A());
        final Binary data = serializer.serialize(new A());

        assertThrows(PersistenceExceptionTypeHandlerConsistencyUnhandledTypeId.class, () -> deserializer.deserialize(data));


    }

    /*
     * Binary
     */
    static Serializer<Binary> createBinaryIncrementalDiffOnce()
    {
        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .setSerializerTypeInfoStrategyCreator(
                        new SerializerTypeInfoStrategyCreator.IncrementalDiff(true));

        return TypedSerializer.Binary(foundation);
    }

    static Serializer<Binary> createBinaryIncrementalDiffAlways()
    {
        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .setSerializerTypeInfoStrategyCreator(
                        new SerializerTypeInfoStrategyCreator.IncrementalDiff(false));
        return TypedSerializer.Binary(foundation);
    }

    static Serializer<Binary> createBinaryDiffOnce()
    {
        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .setSerializerTypeInfoStrategyCreator(
                        new SerializerTypeInfoStrategyCreator.Diff(true));

        return TypedSerializer.Binary(foundation);
    }

    static Serializer<Binary> createBinaryDiffAlways()
    {
        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .setSerializerTypeInfoStrategyCreator(
                        new SerializerTypeInfoStrategyCreator.Diff(false));
        return TypedSerializer.Binary(foundation);
    }

    static Serializer<Binary> createBinaryTypeDictionaryOnce()
    {
        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .setSerializerTypeInfoStrategyCreator(
                        new SerializerTypeInfoStrategyCreator.TypeDictionary(true));

        return TypedSerializer.Binary(foundation);
    }

    static Serializer<Binary> createBinaryTypeDictionaryAlways()
    {
        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .setSerializerTypeInfoStrategyCreator(
                        new SerializerTypeInfoStrategyCreator.TypeDictionary(false));
        return TypedSerializer.Binary(foundation);
    }

    static Serializer<Binary> createBinarySimpleSerializer()
    {
        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .registerEntityTypes(A.class, B.class);

        return Serializer.Binary(foundation);
    }

    /*
     * byte[]
     */
    static Serializer<byte[]> createBytesIncrementalDiffOnce()
    {
        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .setSerializerTypeInfoStrategyCreator(
                        new SerializerTypeInfoStrategyCreator.IncrementalDiff(true));

        return TypedSerializer.Bytes(foundation);
    }

    static Serializer<byte[]> createBytesIncrementalDiffAlways()
    {
        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .setSerializerTypeInfoStrategyCreator(
                        new SerializerTypeInfoStrategyCreator.IncrementalDiff(false));
        return TypedSerializer.Bytes(foundation);
    }

    static Serializer<byte[]> createBytesDiffOnce()
    {
        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .setSerializerTypeInfoStrategyCreator(
                        new SerializerTypeInfoStrategyCreator.Diff(true));

        return TypedSerializer.Bytes(foundation);
    }

    static Serializer<byte[]> createBytesDiffAlways()
    {
        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .setSerializerTypeInfoStrategyCreator(
                        new SerializerTypeInfoStrategyCreator.Diff(false));
        return TypedSerializer.Bytes(foundation);
    }

    static Serializer<byte[]> createBytesTypeDictionaryOnce()
    {
        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .setSerializerTypeInfoStrategyCreator(
                        new SerializerTypeInfoStrategyCreator.TypeDictionary(true));

        return TypedSerializer.Bytes(foundation);
    }

    static Serializer<byte[]> createBytesTypeDictionaryAlways()
    {
        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .setSerializerTypeInfoStrategyCreator(
                        new SerializerTypeInfoStrategyCreator.TypeDictionary(false));
        return TypedSerializer.Bytes(foundation);
    }

    static Serializer<byte[]> createBytesSimpleSerializer()
    {
        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .registerEntityTypes(A.class, B.class);

        return Serializer.Bytes(foundation);
    }


    static Stream<Serializer<?>> supplySerializers()
    {
        return Stream.of(
                createBinarySimpleSerializer(),
                createBinaryIncrementalDiffOnce(),
                createBinaryIncrementalDiffAlways(),
                createBinaryDiffOnce(),
                createBinaryDiffAlways(),
                createBinaryTypeDictionaryOnce(),
                createBinaryTypeDictionaryAlways(),

                createBytesSimpleSerializer(),
                createBytesIncrementalDiffOnce(),
                createBytesIncrementalDiffAlways(),
                createBytesDiffOnce(),
                createBytesDiffAlways(),
                createBytesTypeDictionaryOnce(),
                createBytesTypeDictionaryAlways()
        );
    }

    static Stream<Arguments> supplySerializersPairs()
    {
        return Stream.of(
                Arguments.of(createBinarySimpleSerializer(), createBinarySimpleSerializer()),
                Arguments.of(createBinaryIncrementalDiffOnce(), createBinaryIncrementalDiffOnce()),
                Arguments.of(createBinaryIncrementalDiffAlways(), createBinaryIncrementalDiffAlways()),
                Arguments.of(createBinaryDiffOnce(), createBinaryDiffOnce()),
                Arguments.of(createBinaryDiffAlways(), createBinaryDiffAlways()),
                Arguments.of(createBinaryTypeDictionaryOnce(), createBinaryTypeDictionaryOnce()),
                Arguments.of(createBinaryTypeDictionaryAlways(), createBinaryTypeDictionaryAlways()),

                Arguments.of(createBytesSimpleSerializer(), createBytesSimpleSerializer()),
                Arguments.of(createBytesIncrementalDiffOnce(), createBytesIncrementalDiffOnce()),
                Arguments.of(createBytesIncrementalDiffAlways(), createBytesIncrementalDiffAlways()),
                Arguments.of(createBytesDiffOnce(), createBytesDiffOnce()),
                Arguments.of(createBytesDiffAlways(), createBytesDiffAlways()),
                Arguments.of(createBytesTypeDictionaryOnce(), createBytesTypeDictionaryOnce()),
                Arguments.of(createBytesTypeDictionaryAlways(), createBytesTypeDictionaryAlways())
        );
    }

    static Stream<Arguments> supplyTypedSerializersPairs()
    {
        return Stream.of(
                //Arguments.of(createBinarySimpleSerializer(),      createBinarySimpleSerializer()),
                Arguments.of(createBinaryIncrementalDiffOnce(), createBinaryIncrementalDiffOnce()),
                Arguments.of(createBinaryIncrementalDiffAlways(), createBinaryIncrementalDiffAlways()),
                Arguments.of(createBinaryDiffOnce(), createBinaryDiffOnce()),
                Arguments.of(createBinaryDiffAlways(), createBinaryDiffAlways()),
                Arguments.of(createBinaryTypeDictionaryOnce(), createBinaryTypeDictionaryOnce()),
                Arguments.of(createBinaryTypeDictionaryAlways(), createBinaryTypeDictionaryAlways()),

                //Arguments.of(createBytesSimpleSerializer(),      createBytesSimpleSerializer()),
                Arguments.of(createBytesIncrementalDiffOnce(), createBytesIncrementalDiffOnce()),
                Arguments.of(createBytesIncrementalDiffAlways(), createBytesIncrementalDiffAlways()),
                Arguments.of(createBytesDiffOnce(), createBytesDiffOnce()),
                Arguments.of(createBytesDiffAlways(), createBytesDiffAlways()),
                Arguments.of(createBytesTypeDictionaryOnce(), createBytesTypeDictionaryOnce()),
                Arguments.of(createBytesTypeDictionaryAlways(), createBytesTypeDictionaryAlways())
        );
    }

    static Stream<Arguments> supplyOnceSerializersPairs()
    {
        return Stream.of(
                Arguments.of(createBinaryIncrementalDiffOnce(), createBinaryIncrementalDiffOnce()),
                Arguments.of(createBinaryDiffOnce(), createBinaryDiffOnce()),
                Arguments.of(createBinaryTypeDictionaryOnce(), createBinaryTypeDictionaryOnce()),

                Arguments.of(createBytesIncrementalDiffOnce(), createBytesIncrementalDiffOnce()),
                Arguments.of(createBytesDiffOnce(), createBytesDiffOnce()),
                Arguments.of(createBytesTypeDictionaryOnce(), createBytesTypeDictionaryOnce())
        );
    }

    static Stream<Arguments> supplyAlwaysSerializersPairs()
    {
        return Stream.of(
                Arguments.of(createBinaryIncrementalDiffAlways(), createBinaryIncrementalDiffAlways()),
                Arguments.of(createBinaryDiffAlways(), createBinaryDiffAlways()),
                Arguments.of(createBinaryTypeDictionaryAlways(), createBinaryTypeDictionaryAlways()),

                Arguments.of(createBytesIncrementalDiffAlways(), createBytesIncrementalDiffAlways()),
                Arguments.of(createBytesDiffAlways(), createBytesDiffAlways()),
                Arguments.of(createBytesTypeDictionaryAlways(), createBytesTypeDictionaryAlways())
        );
    }
}
