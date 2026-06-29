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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.serializer.collections.types.XGettingTable;
import org.eclipse.serializer.entity.Entity;
import org.eclipse.serializer.entity.EntityVersionContext;
import org.junit.jupiter.api.Test;

import test.eclipse.serializer.sampler.layeredentities._Human.HumanUpdater;

public class LayeredEntitiesTest
{

    @Test
    public void entities_version_test()
    {
        final Human human = EntityFactory.HumanCreator()
                .name("John Doe")
                .address(
                        EntityFactory.AddressCreator()
                                .street("Main Street")
                                .city("Springfield")
                                .create()
                )
                .create();

        HumanUpdater.setAddress(
                human,
                EntityFactory.AddressCreator()
                        .street("Rose Boulevard")
                        .city("Newtown")
                        .create()
        );

        checkVersion(human);
    }

    static void checkVersion(final Entity entity)
    {
        final EntityVersionContext<Integer> context = EntityVersionContext.lookup(entity);
        final XGettingTable<Integer, Entity> versions = context.versions(entity);
        versions.iterate(v ->
                System.out.println("Version " + v.key() + " = " + v.value())
        );
        assertTrue(versions.get(0).toString().contains("Springfield"));
        assertTrue(versions.get(1).toString().contains("Newtown"));
        assertFalse(versions.get(1).toString().contains("Springfield"));
    }

}
