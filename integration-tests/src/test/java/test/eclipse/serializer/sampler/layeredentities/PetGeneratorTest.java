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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import test.eclipse.serializer.sampler.layeredentities._Pet.*;

public class PetGeneratorTest
{

    @Test
    void pet_appendable_present_test()
    {
        assertNotNull(PetAppendable.class);
    }

    @Test
    void pet_creator_present_test()
    {
        assertNotNull(PetCreator.class);
    }

    @Test
    void pet_data_present_test()
    {
        assertNotNull(PetData.class);
    }

    @Test
    void pet_entity_present_test()
    {
        assertNotNull(PetEntity.class);
    }

    @Test
    void pet_hash_equalator_present_test()
    {
        assertNotNull(PetHashEqualator.class);
    }

    @Test
    void pet_updater_present_test()
    {
        assertNotNull(PetUpdater.class);
        assertTrue(PetUpdater.class.getDeclaredMethods().length > 5);
    }


}
