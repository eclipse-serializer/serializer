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

import org.eclipse.serializer.Serializer;
import org.eclipse.serializer.SerializerFoundation;
import org.eclipse.serializer.persistence.types.PersistenceFieldEvaluator;
import org.eclipse.serializer.tests.model.FieldEvaluatorExample;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldEvaluatorTest
{

    @Test
    void standard() throws Exception
    {

        SerializerFoundation<?> foundation = SerializerFoundation.New();
        foundation.registerEntityType(FieldEvaluatorExample.class);
        byte[] bytes;

        try (Serializer<byte[]> serializer = Serializer.Bytes(foundation))
        {

            bytes = serializer.serialize(new FieldEvaluatorExample("Foo", 321, "anotherValue"));

        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        foundation = SerializerFoundation.New();
        foundation.registerEntityType(FieldEvaluatorExample.class);

        FieldEvaluatorExample example;
        try (Serializer<byte[]> serializer = Serializer.Bytes(foundation))
        {
            example = serializer.deserialize(bytes);
        }

        Assertions.assertThat(example.getValue())
                .isEqualTo("Foo");
        Assertions.assertThat(example.getIgnored())
                .isEqualTo(0);  //Because we don't have standard constructor and class creation
        Assertions.assertThat(example.get_internalField())
                .isEqualTo("anotherValue");

    }

    @Test
    void customEvaluator() throws Exception
    {

        SerializerFoundation<?> foundation = SerializerFoundation.New();
        foundation.registerEntityType(FieldEvaluatorExample.class);
        foundation.setFieldEvaluatorPersistable(new CustomFieldEvaluator());
        byte[] bytes;

        try (Serializer<byte[]> serializer = Serializer.Bytes(foundation))
        {

            bytes = serializer.serialize(new FieldEvaluatorExample("Foo", 321, "anotherValue"));

        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        foundation = SerializerFoundation.New();
        foundation.registerEntityType(FieldEvaluatorExample.class);
        foundation.setFieldEvaluatorPersistable(new CustomFieldEvaluator());

        FieldEvaluatorExample example;
        try (Serializer<byte[]> serializer = Serializer.Bytes(foundation))
        {
            example = serializer.deserialize(bytes);
        }

        Assertions.assertThat(example.getValue())
                .isEqualTo("Foo");
        Assertions.assertThat(example.getIgnored())
                .isEqualTo(0);  //Because we don't have standard constructor and class creation
        Assertions.assertThat(example.get_internalField())
                .isNull();  //Because we don't have standard constructor and class creation

    }

    private static class CustomFieldEvaluator implements PersistenceFieldEvaluator
    {

        @Override
        public boolean applies(Class<?> entityType, Field field)
        {
            boolean result = true;
            if (Modifier.isTransient(field.getModifiers()))
            {
                result = false;
            }
            if (field.getName()
                    .startsWith("_"))
            {
                result = false;
            }
            return result;
        }
    }
}
