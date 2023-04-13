package org.eclipse.serializer.tests.integration.data;

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
import org.eclipse.serializer.tests.model.Employee;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.stream.Stream;

public class TestSerializationDataProviderJDK8 implements ArgumentsProvider
{

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception
    {
        CompareInstances defaultCompare = (i1, i2) -> Assertions.assertThat(i1)
                .isEqualTo(i2);
        return Stream.of(

                Arguments.of(new TestSerializationData(testArrayList(), "jdk8/ArrayList.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(testHashMap(), "jdk8/HashMap.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(testHashSet(), "jdk8/HashSet.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(testHashtable(), "jdk8/Hashtable.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(testLinkedHashMap(), "jdk8/LinkedHashMap.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(testLinkedHashSet(), "jdk8/LinkedHashSet.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(testStack(), "jdk8/Stack.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(testVector(), "jdk8/Vector.test.txt", defaultCompare))
                , Arguments.of(new TestSerializationData(testProperties(), "jdk8/Properties.test.txt", defaultCompare))

                , Arguments.of(new TestSerializationData(createCircular(), "jdk8/circular.test.txt", defaultCompare))
        );
    }

    private Properties testProperties()
    {
        Properties result = new Properties();
        result.setProperty("key1", "value1");
        result.setProperty("key2", "value3");
        result.setProperty("key3", "value3");
        return result;
    }

    private Vector<String> testVector()
    {
        Vector<String> result = new Vector<>();
        result.add("Item1");
        result.add("Item2");
        result.add("Item3");

        return result;

    }

    private Stack<String> testStack()
    {
        Stack<String> result = new Stack<>();
        result.add("Item1");
        result.add("Item2");
        result.add("Item3");

        return result;
    }


    private LinkedHashSet<String> testLinkedHashSet()
    {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        // Not using the constructor with collection as that not seems to guarantee the same order.
        result.add("Item1");
        result.add("Item2");
        result.add("Item3");
        return result;
    }

    private LinkedHashMap<String, String> testLinkedHashMap()
    {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        result.put("key1", "value1");
        result.put("key2", "value2");
        result.put("key3", "value3");
        return result;

    }

    private Hashtable<String, String> testHashtable()
    {
        Hashtable<String, String> result = new Hashtable<>();
        result.put("key1", "value1");
        result.put("key2", "value2");
        result.put("key3", "value3");
        return result;
    }

    private Set<String> testHashSet()
    {
        return new HashSet<>(Set.of("Item1", "Item2", "Item3"));

    }

    private Map<String, String> testHashMap()
    {
        Map<String, String> result = new HashMap<>();
        result.put("key1", "value1");
        result.put("key2", "value2");
        result.put("key3", "value3");
        return result;
    }

    private List<String> testArrayList()
    {
        return new ArrayList<>(List.of("Item1", "Item2", "Item3"));
    }

    private Employee createCircular()
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
