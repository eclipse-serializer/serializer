package org.eclipse.serializer.persistence.types;

import static org.eclipse.serializer.util.X.notNull;

public interface PersistenceIdStrategy extends PersistenceObjectIdStrategy, PersistenceTypeIdStrategy
{
	public PersistenceObjectIdStrategy objectIdStragegy();
	
	public PersistenceTypeIdStrategy typeIdStragegy();
	
	@Override
	public default String strategyTypeNameObjectId()
	{
		return this.objectIdStragegy().strategyTypeNameObjectId();
	}
	
	@Override
	default String strategyTypeNameTypeId()
	{
		return this.typeIdStragegy().strategyTypeNameTypeId();
	}
	
	@Override
	public default PersistenceObjectIdProvider createObjectIdProvider()
	{
		return this.objectIdStragegy().createObjectIdProvider();
	}
	
	@Override
	public default PersistenceTypeIdProvider createTypeIdProvider()
	{
		return this.typeIdStragegy().createTypeIdProvider();
	}
				
	public static PersistenceIdStrategy New(
		final PersistenceObjectIdStrategy objectIdStrategy,
		final PersistenceTypeIdStrategy   typeIdStrategy
	)
	{
		return new PersistenceIdStrategy.Default(
			notNull(objectIdStrategy),
			notNull(typeIdStrategy)
		);
	}
	
	public class Default implements PersistenceIdStrategy
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceObjectIdStrategy objectIdStrategy;
		private final PersistenceTypeIdStrategy   typeIdStrategy  ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceObjectIdStrategy objectIdStrategy,
			final PersistenceTypeIdStrategy   typeIdStrategy
		)
		{
			super();
			this.objectIdStrategy = objectIdStrategy;
			this.typeIdStrategy   = typeIdStrategy  ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public PersistenceObjectIdStrategy objectIdStragegy()
		{
			return this.objectIdStrategy;
		}
		
		@Override
		public PersistenceTypeIdStrategy typeIdStragegy()
		{
			return this.typeIdStrategy;
		}
		
	}
	
}
