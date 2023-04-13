package org.eclipse.serializer.tests.integration.data;

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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class TestSerializationDataProviderJDK17 implements ArgumentsProvider
{

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception
    {
        CompareInstances defaultCompare = (i1, i2) -> Assertions.assertThat(i1)
                .isEqualTo(i2);
        return Stream.of(

                Arguments.of(new TestSerializationData(Set.of("Item1", "Item2", "Item3"), "jdk17/SetOf.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(List.of("Item1", "Item2", "Item3"), "jdk17/ListOf.test.txt", defaultCompare))

                , Arguments.of(new TestSerializationData(new MyRecord(42, "Serializer"), "jdk17/Record.test.txt", defaultCompare))
        );
    }

    public record MyRecord(int id, String name)
    {
    }
}
