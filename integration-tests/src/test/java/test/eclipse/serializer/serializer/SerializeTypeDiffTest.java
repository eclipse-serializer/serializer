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
import org.eclipse.serializer.SerializerTypeInfoStrategyCreator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import test.eclipse.serializer.fixtures.TypeEnum;

public class SerializeTypeDiffTest {

    static Serializer<byte[]> serializer;
    static Serializer<byte[]> deserializer;

    @BeforeAll
    static void setup(){
        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .setSerializerTypeInfoStrategyCreator(
                        new SerializerTypeInfoStrategyCreator.Diff(true));

        serializer = Serializer.Bytes(foundation);
        deserializer = Serializer.Bytes(foundation);
    }

    @ParameterizedTest
    @EnumSource(value = TypeEnum.class, names = {"Lazy", "IdentityHashMap", "HashTableMicroStream"}, mode = EnumSource.Mode.EXCLUDE)
    public void serializerTest(TypeEnum type) {

        byte[] data = serializer.serialize(type.getOriginal());

        Object o = deserializer.deserialize(data);
        type.getOriginal().proveResults(o);
    }

	@ParameterizedTest
	@EnabledForJreRange(min = JRE.JAVA_26)
	@EnumSource(value = TypeEnum.class, names = {"Lazy", "IdentityHashMap", "HashTableMicroStream", "LocalDate", "LocalDateTime","Java_util_Calendar"}, mode = EnumSource.Mode.EXCLUDE)
	public void serializerTestJava26(TypeEnum type) {

		byte[] data = serializer.serialize(type.getOriginal());

		Object o = deserializer.deserialize(data);
		type.getOriginal().proveResults(o);
	}
}
