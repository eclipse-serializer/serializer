package org.eclipse.serializer;

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

import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceContextDispatcher;

/**
 * Test fixture: a {@link SerializerFoundation} with the pass-through context dispatcher
 * instead of the serializer's default {@link PersistenceContextDispatcher.LocalObjectRegistration}.
 * <p>
 * With pass-through dispatching, loaders and storers operate directly on the manager's
 * shared global object registry — the wiring the embedded storage uses. This allows
 * integration-testing the loader's registry-visibility semantics without a storage backend.
 * ({@code SerializerFoundation#setContextDispatcher} is intentionally unsupported, hence
 * this same-package subclass overriding the {@code ensure~} default.)
 */
public final class PassThroughSerializerFoundation
extends SerializerFoundation.Default<PassThroughSerializerFoundation>
{
	public static PassThroughSerializerFoundation New()
	{
		return new PassThroughSerializerFoundation();
	}

	private PassThroughSerializerFoundation()
	{
		super();
	}

	@Override
	protected PersistenceContextDispatcher<Binary> ensureContextDispatcher()
	{
		return PersistenceContextDispatcher.PassThrough();
	}
}
