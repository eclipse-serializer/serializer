package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
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

import org.eclipse.serializer.reference.ObjectSwizzling;
import org.eclipse.serializer.util.BufferSizeProviderIncremental;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

/**
 * PeristenceStorer creator that creates {@link org.eclipse.serializer.persistence.types.PersistenceStorerDeactivatable#PersistenceStorerDeactivatable PersistenceStorerDeactivatable}
 * instances.
 * 
 */
public class PersistenceStorerCreatorDeactivatable<D> implements PersistenceStorer.Creator<D>
{
	private final static Logger logger = Logging.getLogger(PersistenceStorerCreatorDeactivatable.class);
	
	public static <D> PersistenceStorerCreatorDeactivatable<D> New(
		final PersistenceFoundation<D,?>             connectionFoundation,
		final PersistenceStorerDeactivatableRegistry storerRegistry
	)
	{
		return new PersistenceStorerCreatorDeactivatable<>(
			connectionFoundation.getStorerCreator(),
			storerRegistry
		);
	}


	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final PersistenceStorer.Creator<D>           creator;
	private final PersistenceStorerDeactivatableRegistry storerRegistry;

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceStorerCreatorDeactivatable(
		final PersistenceStorer.Creator<D>           creator,
		final PersistenceStorerDeactivatableRegistry storerModeController
	)
	{
		super();
		this.creator        = creator;
		this.storerRegistry = storerModeController;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public PersistenceStorer createLazyStorer(
		final PersistenceTypeHandlerManager<D> typeManager       ,
		final PersistenceObjectManager<D>      objectManager     ,
		final ObjectSwizzling                  objectRetriever   ,
		final PersistenceTarget<D>             target            ,
		final BufferSizeProviderIncremental    bufferSizeProvider,
		final Persister                        persister
	)
	{
		logger.debug("Creating lazy storer");
		
		return this.storerRegistry.register(
			new PersistenceStorerDeactivatable(
				this.creator.createLazyStorer(
					typeManager,
					objectManager,
					objectRetriever,
					target,
					bufferSizeProvider,
					persister
				)
			)
		);
	}

	@Override
	public PersistenceStorer createEagerStorer(
		final PersistenceTypeHandlerManager<D> typeManager       ,
		final PersistenceObjectManager<D>      objectManager     ,
		final ObjectSwizzling                  objectRetriever   ,
		final PersistenceTarget<D>             target            ,
		final BufferSizeProviderIncremental    bufferSizeProvider,
		final Persister                        persister
		)
	{
		logger.debug("Creating eager storer");
		
		return this.storerRegistry.register(
			new PersistenceStorerDeactivatable(
				this.creator.createEagerStorer(
					typeManager,
					objectManager,
					objectRetriever,
					target,
					bufferSizeProvider,
					persister
				)
			)
		);
	}

}
