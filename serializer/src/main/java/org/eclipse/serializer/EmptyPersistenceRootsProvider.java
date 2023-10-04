package org.eclipse.serializer;

/*-
 * #%L
 * Eclipse Serializer
 * %%
 * Copyright (C) 2023 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import org.eclipse.serializer.persistence.types.PersistenceCustomTypeHandlerRegistry;
import org.eclipse.serializer.persistence.types.PersistenceObjectRegistry;
import org.eclipse.serializer.persistence.types.PersistenceRoots;
import org.eclipse.serializer.persistence.types.PersistenceRootsProvider;

public class EmptyPersistenceRootsProvider<D> implements PersistenceRootsProvider<D>
{

    EmptyPersistenceRootsProvider()
    {
        super();
    }

    @Override
    public PersistenceRoots provideRoots()
    {
        //no-op
        return null;
    }

    @Override
    public PersistenceRoots peekRoots()
    {
        //no-op
        return null;
    }

    @Override
    public void updateRuntimeRoots(final PersistenceRoots runtimeRoots)
    {
        //no-op
    }

    @Override
    public void registerRootsTypeHandlerCreator(
            final PersistenceCustomTypeHandlerRegistry<D> typeHandlerRegistry,
            final PersistenceObjectRegistry objectRegistry
    )
    {
        //no-op
    }
}
