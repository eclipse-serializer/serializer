package org.eclipse.serializer.communication.types;

import static org.eclipse.serializer.util.X.notNull;

import org.eclipse.serializer.persistence.types.PersistenceObjectIdProvider;
import org.eclipse.serializer.persistence.types.PersistenceTypeIdProvider;

public final class ComCompositeIdProvider implements PersistenceObjectIdProvider, PersistenceTypeIdProvider
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static ComCompositeIdProvider New(
		final PersistenceTypeIdProvider   typeIdProvider  ,
		final PersistenceObjectIdProvider objectIdProvider
	)
	{
		return new ComCompositeIdProvider(
			notNull(typeIdProvider)  ,
			notNull(objectIdProvider)
		);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final PersistenceTypeIdProvider   typeIdProvider  ;
	private final PersistenceObjectIdProvider objectIdProvider;

	private transient boolean initialized;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	ComCompositeIdProvider(
		final PersistenceTypeIdProvider   typeIdProvider  ,
		final PersistenceObjectIdProvider objectIdProvider
	)
	{
		super();
		this.typeIdProvider   = typeIdProvider  ;
		this.objectIdProvider = objectIdProvider;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	private void markInitialized()
	{
		this.initialized = true;
	}

	private boolean isInitialized()
	{
		return this.initialized;
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public final synchronized ComCompositeIdProvider initialize()
	{
		if(!this.isInitialized())
		{
			this.typeIdProvider.initializeTypeId();
			this.objectIdProvider.initializeObjectId();
			this.markInitialized();
		}
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final ComCompositeIdProvider initializeTypeId()
	{
		return this.initialize();
	}

	@Override
	public final ComCompositeIdProvider initializeObjectId()
	{
		return this.initialize();
	}

	@Override
	public final long currentObjectId()
	{
		return this.objectIdProvider.currentObjectId();
	}

	@Override
	public final long currentTypeId()
	{
		return this.typeIdProvider.currentTypeId();
	}

	@Override
	public final long provideNextTypeId()
	{
		return this.typeIdProvider.provideNextTypeId();
	}

	@Override
	public final long provideNextObjectId()
	{
		return this.objectIdProvider.provideNextObjectId();
	}

	@Override
	public final ComCompositeIdProvider updateCurrentObjectId(final long currentObjectId)
	{
		this.objectIdProvider.updateCurrentObjectId(currentObjectId);
		return this;
	}

	@Override
	public final ComCompositeIdProvider updateCurrentTypeId(final long currentTypeId)
	{
		this.typeIdProvider.updateCurrentTypeId(currentTypeId);
		return this;
	}

}
