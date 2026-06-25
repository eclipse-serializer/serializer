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


import java.time.LocalDateTime;

import org.eclipse.serializer.Serializer;
import org.eclipse.serializer.SerializerFoundation;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.reference.Lazy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import test.eclipse.serializer.fixtures.TypeEnum;

public class BasicSerializerTest {

    @Test
    public void basicSerializerTest() {
        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .registerEntityTypes(LocalDateTime.class);
        final Serializer<byte[]> serializer = Serializer.Bytes(foundation);

        final LocalDateTime ldt1 = LocalDateTime.now();
        final byte[] bytes = serializer.serialize(ldt1);
        final LocalDateTime ldt2 = serializer.deserialize(bytes);

        Assertions.assertEquals(ldt1, ldt2);
    }

    @ParameterizedTest
    @EnumSource(
            value = TypeEnum.class,
            names = {"HashTableMicroStream", "IdentityHashMap", "Lazy"},
            mode = EnumSource.Mode.EXCLUDE
    )
    public void typeParamsTest(TypeEnum type) {

        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .registerEntityTypes(type.getClass());
        final Serializer<byte[]> serializer = Serializer.Bytes(foundation);

        final byte[] bytes = serializer.serialize(type.getOriginal());
        Object copy;
        copy = serializer.deserialize(bytes);

        type.getOriginal().proveResults(copy);
    }

	@ParameterizedTest
	@EnabledForJreRange(min = JRE.JAVA_26)
	@EnumSource(
			value = TypeEnum.class,
			names = {"HashTableMicroStream", "IdentityHashMap", "Lazy",  "LocalDate", "LocalDateTime","Java_util_Calendar"},
			mode = EnumSource.Mode.EXCLUDE
	)
	public void typeParamsTestJava26(TypeEnum type) {

		final SerializerFoundation<?> foundation = SerializerFoundation.New()
				.registerEntityTypes(type.getClass());
		final Serializer<byte[]> serializer = Serializer.Bytes(foundation);

		final byte[] bytes = serializer.serialize(type.getOriginal());
		Object copy;
		copy = serializer.deserialize(bytes);

		type.getOriginal().proveResults(copy);
	}


	@Test
    public void throwUnsupportedLazyTest() {
        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .registerEntityTypes(Lazy.class);
        final Serializer<byte[]> serializer = Serializer.Bytes(foundation);

        Lazy<String> lazy = Lazy.Reference("ahoj");
        Assertions.assertThrows(UnsupportedOperationException.class, () -> serializer.serialize(lazy));
    }

    @Test
    public void binarySerializerTest() {
        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .registerEntityTypes(LocalDateTime.class);
        final Serializer<Binary> serializer = Serializer.Binary(foundation);

        final LocalDateTime ldt1 = LocalDateTime.now();
        final Binary binary = serializer.serialize(ldt1);
        final LocalDateTime ldt2 = serializer.deserialize(binary);

        Assertions.assertEquals(ldt1, ldt2);
    }

    @Test
    public void throwUnsupportedBinaryLazyTest() {
        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .registerEntityTypes(Lazy.class);
        final Serializer<Binary> serializer = Serializer.Binary(foundation);

        Lazy<String> lazy = Lazy.Reference("ahoj");
        Assertions.assertThrows(UnsupportedOperationException.class, () -> serializer.serialize(lazy));
    }
}
