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

import test.eclipse.serializer.sampler.layeredentities._Human.*;

public class HumanGeneratorTest
{

    @Test
    void human_appendable_present_test()
    {
        assertNotNull(HumanAppendable.class);
    }

    @Test
    void human_creator_present_test()
    {
        assertNotNull(HumanCreator.class);
    }

    @Test
    void human_data_present_test()
    {
        assertNotNull(HumanData.class);
    }

    @Test
    void human_entity_present_test()
    {
        assertNotNull(HumanEntity.class);
    }

    @Test
    void human_hash_equalator_present_test()
    {
        assertNotNull(HumanHashEqualator.class);
    }

    @Test
    void human_updater_present_test()
    {
        assertNotNull(HumanUpdater.class);
    }


}
