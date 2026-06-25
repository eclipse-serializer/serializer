package test.eclipse.serializer.sampler.layeredentities;

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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import test.eclipse.serializer.sampler.layeredentities._Address.*;

public class AddressGeneratorTest {

    @Test
    void address_appendable_present_test() {

        Address address = AddressCreator.New().create();

        AddressAppendable appendable = AddressAppendable.New(address);

        assertNotNull(appendable);
    }

    @Test
    void address_creator_present_test() {
        AddressCreator creator = AddressCreator.New();

        assertNotNull(creator);
    }

    @Test
    void address_data_present_test() {
        assertNotNull(AddressData.class);
    }

    @Test
    void address_entity_present_test() {
        assertNotNull(AddressEntity.class);
    }

    @Test
    void address_hash_equalator_present_test() {
        assertNotNull(AddressHashEqualator.class);
    }

    @Test
    void address_updater_present_test() {
        assertNotNull(AddressUpdater.class);
    }


}
