package org.eclipse.serializer.equality;

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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EqualatorTest
{

    @Test
    void wrapTest()
    {
        String s1 = "This is a String";
        String s2 = "aaaa just string to compare";

        ExampleComparator exampleComparator = new ExampleComparator();

        Equalator<String> stringEqualator = Equalator.Wrap(exampleComparator);
        Assertions.assertFalse(stringEqualator.equal(s1, s2));
    }


    @Test
    void wrapTestCompareSuccessful()
    {
        String s1 = new String("This is a String"); // new String() I just need another instance
        String s2 = new String("This is a String"); // new String() I just need another instance

        ExampleComparator exampleComparator = new ExampleComparator();

        Equalator<String> stringEqualator = Equalator.Wrap(exampleComparator);
        Assertions.assertTrue(stringEqualator.equal(s1, s2));
    }

    @Test
    void compareSuccessfulChain()
    {
        String s1 = new String("This is a String"); // new String() I just need another instance
        String s2 = new String("This is a String"); // new String() I just need another instance

        ExampleComparator exampleComparator = new ExampleComparator();

        Equalator<String> stringEqualator = Equalator.Wrap(exampleComparator);
        Equalator<String> stringEqualator2 = Equalator.Wrap(exampleComparator);
        Equalator<String> chain = Equalator.Chain(stringEqualator, stringEqualator2);
        Assertions.assertTrue(chain.equal(s1, s2));
    }

    @Test
    void compareOneFailedInhain()
    {
        String s1 = new String("This is a String"); // new String() I just need another instance
        String s2 = new String("This is a String"); // new String() I just need another instance

        ExampleComparator exampleComparator = new ExampleComparator();
        StillFalseComparator falseComparator = new StillFalseComparator();


        Equalator<String> stringEqualator = Equalator.Wrap(exampleComparator);
        Equalator<String> falseEqualator = Equalator.Wrap(falseComparator);

        Equalator<String> chain = Equalator.Chain(stringEqualator, falseEqualator);
        Assertions.assertFalse(chain.equal(s1, s2));
    }

    @Test
    void equalityIdentifyNotEquals()
    {
        String s1 = new String("This is a String"); // new String() I just need another instance
        String s2 = new String("This is a String"); // new String() I just need another instance

        ExampleComparator exampleComparator = new ExampleComparator();

        Equalator<String> identity = Equalator.identity();
        Assertions.assertFalse(identity.equal(s1, s2));
    }

    @Test
    void equalityIdentifyEquals()
    {
        String s1 = "This is a String";
        String s2 = "This is a String";

        ExampleComparator exampleComparator = new ExampleComparator();

        Equalator<String> identity = Equalator.identity();
        Assertions.assertTrue(identity.equal(s1, s2));
    }

    @Test
    void equalityValueEquals()
    {
        String s1 = "This is a String";
        String s2 = "This is a String";

        Equalator<String> identity = Equalator.value();
        Assertions.assertTrue(identity.equal(s1, s2));
    }

    @Test
    void equalitySampleEquals()
    {
        String s1 = "This is a String";
        String s2 = "This is a String";
        ArrayList<String> strings = Stream.generate(() -> "This is a String")
                .limit(100)
                .collect(Collectors.toCollection(ArrayList::new));

        ExampleComparator exampleComparator = new ExampleComparator();

        Equalator<String> identity = Equalator.value();
        Predicate<String> sample = identity.sample(s1);

        ArrayList<String> filteredStrings = strings.stream()
                .filter(sample)
                .collect(Collectors.toCollection(ArrayList::new));
        Assertions.assertIterableEquals(strings, filteredStrings);
    }

    @Test
    void equalitySampleNotEquals()
    {
        String s1 = "This is a String";
        String s2 = "This is a String";
        ArrayList<String> strings = Stream.generate(() -> "This is a String")
                .limit(100)
                .collect(Collectors.toCollection(ArrayList::new));
        strings.add("another string");

        ExampleComparator exampleComparator = new ExampleComparator();

        Equalator<String> identity = Equalator.value();
        Predicate<String> sample = identity.sample(s1);

        ArrayList<String> filteredStrings = strings.stream()
                .filter(sample)
                .collect(Collectors.toCollection(ArrayList::new));
        Assertions.assertNotEquals(filteredStrings.size(), strings.size());
    }

    static class ExampleComparator implements Comparator<String>
    {
        public int compare(String obj1, String obj2)
        {
            if (obj1 == obj2) {
                return 0;
            }
            if (obj1 == null) {
                return -1;
            }
            if (obj2 == null) {
                return 1;
            }
            return obj1.compareTo(obj2);
        }
    }

    static class StillFalseComparator implements Comparator<String>
    {
        public int compare(String obj1, String obj2)
        {
            return -1;
        }
    }
}
