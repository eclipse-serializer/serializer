package org.eclipse.serializer.communication.binary.types;

/*-
 * #%L
 * Eclipse Serializer Communication Binary
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

import static org.eclipse.serializer.util.X.mayNull;
import static org.eclipse.serializer.util.X.notNull;

import java.nio.ByteOrder;

import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.communication.types.ComConnection;
import org.eclipse.serializer.communication.types.ComPersistenceAdaptor;
import org.eclipse.serializer.communication.types.ComPersistenceAdaptorCreator;
import org.eclipse.serializer.communication.types.ComProtocol;
import org.eclipse.serializer.persistence.binary.types.BinaryPersistenceFoundation;
import org.eclipse.serializer.persistence.types.PersistenceContextDispatcher;
import org.eclipse.serializer.persistence.types.PersistenceFoundation;
import org.eclipse.serializer.persistence.types.PersistenceIdStrategy;
import org.eclipse.serializer.persistence.types.PersistenceSizedArrayLengthController;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryViewProvider;
import org.eclipse.serializer.util.BufferSizeProvider;


public interface ComPersistenceAdaptorBinary<C> extends ComPersistenceAdaptor<C>
{
	@Override
	public default BinaryPersistenceFoundation<?> initializePersistenceFoundation(
		final PersistenceTypeDictionaryViewProvider typeDictionaryProvider,
		final ByteOrder                             hostByteOrder,
		final PersistenceIdStrategy                 idStrategy
	)
	{
		ComPersistenceAdaptor.super.initializePersistenceFoundation(typeDictionaryProvider, hostByteOrder, idStrategy);
		return this.persistenceFoundation();
	}
	
	@Override
	public BinaryPersistenceFoundation<?> persistenceFoundation();
		
	
	
	public static ComPersistenceAdaptorBinary.Default New(
		final BinaryPersistenceFoundation<?> foundation        ,
		final BufferSizeProvider             bufferSizeProvider,
		final PersistenceIdStrategy          hostInitIdStrategy,
		final XGettingEnum<Class<?>>         entityTypes       ,
		final ByteOrder                      hostByteOrder,
		final PersistenceIdStrategy          hostIdStrategy
	)
	{
		return new ComPersistenceAdaptorBinary.Default(
			notNull(foundation)        ,
			notNull(bufferSizeProvider),
			mayNull(hostInitIdStrategy), // null for client persistence. Checked for host persistence beforehand.
			mayNull(entityTypes)       , // null for client persistence. Checked for host persistence beforehand.
			mayNull(hostByteOrder)     , // null for client persistence. Checked for host persistence beforehand.
			mayNull(hostIdStrategy)      // null for client persistence. Checked for host persistence beforehand.
		);
	}
	
	public abstract class Abstract<C>
	extends ComPersistenceAdaptor.Abstract<C>
	implements ComPersistenceAdaptorBinary<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final BinaryPersistenceFoundation<?> foundation        ;
		private final BufferSizeProvider             bufferSizeProvider;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(
			final BinaryPersistenceFoundation<?> foundation        ,
			final BufferSizeProvider             bufferSizeProvider,
			final PersistenceIdStrategy          hostInitIdStrategy,
			final XGettingEnum<Class<?>>         entityTypes       ,
			final ByteOrder                      hostByteOrder     ,
			final PersistenceIdStrategy          hostIdStrategy
		)
		{
			super(hostInitIdStrategy, entityTypes, hostByteOrder, hostIdStrategy);
			this.foundation         = foundation        ;
			this.bufferSizeProvider = bufferSizeProvider;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final BinaryPersistenceFoundation<?> persistenceFoundation()
		{
			return this.foundation;
		}
		
		@Override
		public final BinaryPersistenceFoundation<?> createInitializationFoundation()
		{
			return this.foundation.Clone();
		}
		
		public BufferSizeProvider bufferSizeProvider()
		{
			return this.bufferSizeProvider;
		}
		
	}

	public final class Default extends ComPersistenceAdaptorBinary.Abstract<ComConnection>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final BinaryPersistenceFoundation<?> foundation        ,
			final BufferSizeProvider             bufferSizeProvider,
			final PersistenceIdStrategy          hostInitIdStrategy,
			final XGettingEnum<Class<?>>         entityTypes       ,
			final ByteOrder                      hostByteOrder     ,
			final PersistenceIdStrategy          hostIdStrategy
		)
		{
			super(foundation, bufferSizeProvider, hostInitIdStrategy, entityTypes, hostByteOrder, hostIdStrategy);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public PersistenceFoundation<?, ?> provideHostPersistenceFoundation(
			final ComConnection connection
		)
		{
			this.initializeHostPersistenceFoundation();
			
			final BinaryPersistenceFoundation<?> foundation = this.persistenceFoundation();
			
			if(connection != null)
			{
				final ComPersistenceChannelBinary.Default channel = ComPersistenceChannelBinary.New(
					connection,
					this.bufferSizeProvider(),
					foundation,
					this.comWriteController()
				);
				foundation.setPersistenceChannel(channel);
			}
			
			return foundation;
		}
		
		@Override
		public BinaryPersistenceFoundation<?> provideClientPersistenceFoundation(
			final ComConnection connection,
			final ComProtocol   protocol
		)
		{
			this.initializeClientPersistenceFoundation(protocol);
			
			final BinaryPersistenceFoundation<?> foundation = this.persistenceFoundation();
			
			final ComPersistenceChannelBinary.Default channel = ComPersistenceChannelBinary.New(
				connection,
				this.bufferSizeProvider(),
				foundation,
				this.comWriteController()
			);
			foundation.setPersistenceChannel(channel);
			
			return foundation;
		}
				
	}
	
	/**
	 * Calls {@link ComPersistenceAdaptorBinary#Creator(BinaryPersistenceFoundation)} with the Com-specific
	 * modifications set:<br>
	 * PersistenceContextDispatcher.LocalObjectRegistration()<br>
	 * PersistenceSizedArrayLengthController.Fitting()<br>
	 * based on the following rationale:<br>
	 * <p>
	 * PersistenceContextDispatcher.LocalObjectRegistration:<br>
	 * Communication normally doesn't update a central/global object registry (= object graph) directly,
	 * but uses a local one that is discarded after every message.
	 * In case this shall change, a custom-configured foundation can be passed instead.
	 * <p>
	 * PersistenceSizedArrayLengthController.Fitting:<br>
	 * Sized arrays shouldn't be unrestricted for use in comm. in order to prevent array bombs.
	 * <p>
	 * These aspects should be considered to be replicated when calling
	 * {@link ComPersistenceAdaptorBinary#Creator(BinaryPersistenceFoundation)} directly to pass an externally defined
	 * {@link BinaryPersistenceFoundation} instance.
	 * 
	 * @return a new creator
	 */
	public static ComPersistenceAdaptorBinary.Creator.Default Creator()
	{
		/*
		 * Communication normally doesn't update a central/global object registry (= object graph) directly,
		 * but uses a local one that is discarded after every message.
		 * In case this shall change, a custom-configured foundation can be passed instead.
		 * 
		 * Also, sized arrays shouldn't be unrestricted for use in comm. in order to prevent array bombs.
		 */
		return Creator(
			BinaryPersistenceFoundation.New()
				.setContextDispatcher(
					PersistenceContextDispatcher.LocalObjectRegistration()
				)
				.setSizedArrayLengthController(
					PersistenceSizedArrayLengthController.Fitting()
				)
		);
	}
	
	public static ComPersistenceAdaptorBinary.Creator.Default Creator(
		final BinaryPersistenceFoundation<?> foundation
	)
	{
		return Creator(
			foundation,
			BufferSizeProvider.New()
		);
	}
	
	public static ComPersistenceAdaptorBinary.Creator.Default Creator(
		final BinaryPersistenceFoundation<?> foundation        ,
		final BufferSizeProvider             bufferSizeProvider
	)
	{
		return new ComPersistenceAdaptorBinary.Creator.Default(
			notNull(foundation)        ,
			notNull(bufferSizeProvider)
		);
	}
	
	public interface Creator<C> extends ComPersistenceAdaptorCreator<C>
	{
		public abstract class Abstract<C> implements ComPersistenceAdaptorBinary.Creator<C>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			private final BinaryPersistenceFoundation<?> foundation        ;
			private final BufferSizeProvider             bufferSizeProvider;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			protected Abstract(
				final BinaryPersistenceFoundation<?> foundation        ,
				final BufferSizeProvider             bufferSizeProvider
			)
			{
				super();
				this.foundation         = foundation        ;
				this.bufferSizeProvider = bufferSizeProvider;
			}
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			public BinaryPersistenceFoundation<?> foundation()
			{
				return this.foundation;
			}
			
			public BufferSizeProvider bufferSizeProvider()
			{
				return this.bufferSizeProvider;
			}
			
		}
		
		
		public final class Default extends ComPersistenceAdaptorBinary.Creator.Abstract<ComConnection>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			protected Default(
				final BinaryPersistenceFoundation<?> foundation        ,
				final BufferSizeProvider             bufferSizeProvider
			)
			{
				super(foundation, bufferSizeProvider);
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////

			@Override
			public ComPersistenceAdaptor<ComConnection> createPersistenceAdaptor(
				final PersistenceIdStrategy  hostIdStrategyInitialization,
				final XGettingEnum<Class<?>> entityTypes                 ,
				final ByteOrder              hostByteOrder               ,
				final PersistenceIdStrategy  hostIdStrategy
			)
			{
				return ComPersistenceAdaptorBinary.New(
					this.foundation()           ,
					this.bufferSizeProvider()   ,
					hostIdStrategyInitialization,
					entityTypes                 ,
					hostByteOrder               ,
					hostIdStrategy
				);
			}
			
		}
		
	}
	
}
