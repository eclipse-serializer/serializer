package org.eclipse.serializer.util.traversing;

/*-
 * #%L
 * Eclipse Serializer Base
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

import static org.eclipse.serializer.util.X.notNull;

import java.util.function.Function;

import org.eclipse.serializer.collections.EqHashEnum;


public final class Deduplicator implements Function<Object, Object>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static Deduplicator New()
	{
		return New(
			EqHashEnum.New()
		);
	}
	
	public static Deduplicator New(final EqHashEnum<Object> registry)
	{
		return new Deduplicator(
			notNull(registry)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final EqHashEnum<Object> registry;
	
	
		
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	Deduplicator(final EqHashEnum<Object> registry)
	{
		super();
		this.registry = registry;
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final Object apply(final Object instance)
	{
		return this.registry.deduplicate(instance);
	}

}
