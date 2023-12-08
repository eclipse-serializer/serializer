package org.eclipse.serializer.communication.binarydynamic;

import org.eclipse.serializer.persistence.types.PersistenceIdStrategy;
import org.eclipse.serializer.persistence.types.PersistenceObjectIdStrategy;
import org.eclipse.serializer.persistence.types.PersistenceTypeIdStrategy;

public class ComDynamicIdStrategy implements PersistenceIdStrategy
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static ComDynamicIdStrategy New(final long startingObjectId)
	{
		return new ComDynamicIdStrategy(
			PersistenceTypeIdStrategy.Transient() ,
			PersistenceObjectIdStrategy.Transient(startingObjectId)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final PersistenceTypeIdStrategy.Transient   typeIdStrategy  ;
	private final PersistenceObjectIdStrategy.Transient objectIdStrategy;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	ComDynamicIdStrategy(
		final PersistenceTypeIdStrategy.Transient   typeIdStrategy  ,
		final PersistenceObjectIdStrategy.Transient objectIdStrategy
	)
	{
		super();
		this.typeIdStrategy   = typeIdStrategy  ;
		this.objectIdStrategy = objectIdStrategy;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public PersistenceObjectIdStrategy.Transient objectIdStragegy()
	{
		return this.objectIdStrategy;
	}

	@Override
	public PersistenceTypeIdStrategy.Transient typeIdStragegy()
	{
		return this.typeIdStrategy;
	}

}
