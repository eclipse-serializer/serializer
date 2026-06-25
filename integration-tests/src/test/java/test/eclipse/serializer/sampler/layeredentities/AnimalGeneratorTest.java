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

import test.eclipse.serializer.sampler.layeredentities._Animal.*;

public class AnimalGeneratorTest
{

    @Test
    void animal_appendable_present_test()
    {
        assertNotNull(AnimalAppendable.class);
    }

    @Test
    void animal_creator_present_test()
    {
        assertNotNull(AnimalCreator.class);
    }

    @Test
    void animal_data_present_test()
    {
        assertNotNull(AnimalData.class);
    }

    @Test
    void animal_entity_present_test()
    {
        assertNotNull(AnimalEntity.class);
    }

    @Test
    void animal_hash_equalator_present_test()
    {
        assertNotNull(AnimalHashEqualator.class);
    }

    @Test
    void animal_updater_present_test()
    {
        assertNotNull(AnimalUpdater.class);
    }


}
