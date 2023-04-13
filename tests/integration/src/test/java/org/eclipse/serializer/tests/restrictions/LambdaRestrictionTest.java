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
import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

public class LambdaRestrictionTest
{

    @Test
    void failure()
    {
        Function<Integer, Integer> func = (x) -> x * x;

        SerializerFoundation<?> foundation = SerializerFoundation.New();

        try (Serializer<byte[]> serializer = Serializer.Bytes(foundation))
        {
            Assertions.assertThatThrownBy(
                            () -> serializer.serialize(func)
                    )
                    .isInstanceOf(PersistenceException.class)
                    .hasMessage("Lambdas are not supported as they cannot be resolved during loading due to insufficient reflection mechanisms provided by the (current) JVM.");

        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    void failure2()
    {
        Function<Integer, Integer> func = new Function<Integer, Integer>()
        {
            @Override
            public Integer apply(Integer x)
            {
                return x * x;
            }
        };

        SerializerFoundation<?> foundation = SerializerFoundation.New();

        try (Serializer<byte[]> serializer = Serializer.Bytes(foundation))
        {
            Assertions.assertThatThrownBy(
                            () -> serializer.serialize(func)
                    )
                    .isInstanceOf(PersistenceException.class)
                    .hasMessage("Type not persistable: \"class org.eclipse.serializer.tests.restrictions.LambdaRestrictionTest$1\". Details: Synthetic classes ($1 etc.) are not reliably persistable since a simple reordering of source code elements would change the name identity of a class. For a type system that has to rely upon resolving types by their identifying name, this would silently cause a potentially fatal error. If handling synthetic classes (e.g. anonymous inner classes) is absolutely necessary, a custom org.eclipse.serializer.persistence.types.PersistenceTypeResolver can be used to remove the exception and assume  complete responsibility for correctly handling synthetic class names.");

        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    void success()
    {

        Function<Integer, Integer> func = new Square();

        SerializerFoundation<?> foundation = SerializerFoundation.New();

        try (Serializer<byte[]> serializer = Serializer.Bytes(foundation))
        {
            byte[] bytes = serializer.serialize(func);
            Function<Integer, Integer> newFunc = serializer.deserialize(bytes);
            Assertions.assertThat(newFunc.apply(2))
                    .isEqualTo(4);

        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }


    }

    private static class Square implements Function<Integer, Integer>
    {
        @Override
        public Integer apply(Integer x)
        {
            return x * x;
        }
    }
}
