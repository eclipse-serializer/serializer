package org.eclipse.serializer.util.cql;

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

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.serializer.functional.Aggregator;

public final class CqlWrapperCollectorLinkingFinalizing<O, R> implements Aggregator<O, R>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	final R                   target   ;
	final BiConsumer<O, R>   linker   ;
	final Consumer<? super R> finalizer;

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	CqlWrapperCollectorLinkingFinalizing(
		final R                   target   ,
		final BiConsumer<O, R>   linker   ,
		final Consumer<? super R> finalizer
	)
	{
		super();
		this.target    = target   ;
		this.linker    = linker   ;
		this.finalizer = finalizer;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final O element)
	{
		this.linker.accept(element, this.target);
	}

	@Override
	public final R yield()
	{
		this.finalizer.accept(this.target);
		return this.target;
	}

}
