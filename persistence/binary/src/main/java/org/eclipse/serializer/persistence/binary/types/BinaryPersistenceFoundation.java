package org.eclipse.serializer.persistence.binary.types;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
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

import org.eclipse.serializer.collections.EqHashEnum;
import org.eclipse.serializer.collections.EqHashTable;
import org.eclipse.serializer.collections.types.XEnum;
import org.eclipse.serializer.collections.types.XTable;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.persistence.types.BinaryRootReferenceProvider;
import org.eclipse.serializer.persistence.types.PersistenceCustomTypeHandlerRegistry;
import org.eclipse.serializer.persistence.types.PersistenceCustomTypeHandlerRegistryEnsurer;
import org.eclipse.serializer.persistence.types.PersistenceFoundation;
import org.eclipse.serializer.persistence.types.PersistenceLegacyTypeHandlerCreator;
import org.eclipse.serializer.persistence.types.PersistenceManager;
import org.eclipse.serializer.persistence.types.PersistenceRootReferenceProvider;
import org.eclipse.serializer.persistence.types.PersistenceRootsProvider;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandlerCreator;


/**
 * Factory and master configuration type for assembling a binary persistence layer. Extends the generic
 * {@link PersistenceFoundation} with binary-specific component slots: custom value-translator lookup,
 * translator key builders, value-translator mapping/provider, and the {@link BinaryFieldHandlerProvider}.
 * Each slot is lazily ensured on first access and can be overridden via the matching setter.
 * <p>
 * The default implementation also wires the binary-specific storer/loader creators, type-handler creator,
 * legacy-type-handler creator, custom-type-handler registry, and roots/root-reference providers so a
 * caller only has to supply application-specific overrides.
 *
 * @param <F> the concrete foundation type (self-bound for fluent setters).
 *
 * @see PersistenceFoundation
 * @see BinaryPersistence
 */
public interface BinaryPersistenceFoundation<F extends BinaryPersistenceFoundation<?>>
extends PersistenceFoundation<Binary, F>
{

	@Override
	public BinaryPersistenceFoundation<F> Clone();

	/**
	 * @return the table of registered custom value translators keyed by lookup key.
	 */
	public XTable<String, BinaryValueSetter> getCustomTranslatorLookup();

	/**
	 * @return the registered {@link BinaryValueTranslatorKeyBuilder}s consulted in order during legacy mapping.
	 */
	public XEnum<BinaryValueTranslatorKeyBuilder> getTranslatorKeyBuilders();

	/**
	 * @return the {@link BinaryValueTranslatorMappingProvider} backing the translator lookup table.
	 */
	public BinaryValueTranslatorMappingProvider getValueTranslatorMappingProvider();

	/**
	 * @return the {@link BinaryValueTranslatorProvider} used during legacy type mapping.
	 */
	public BinaryValueTranslatorProvider getValueTranslatorProvider();

	/**
	 * @return the {@link BinaryFieldHandlerProvider} consulted by reflective handlers for per-field overrides.
	 */
	public BinaryFieldHandlerProvider getFieldHandlerProvider();


	/**
	 * @param customTranslatorLookup the custom translator lookup table to use.
	 *
	 * @return this foundation for fluent chaining.
	 */
	public F setCustomTranslatorLookup(
		XTable<String, BinaryValueSetter> customTranslatorLookup
	);

	/**
	 * @param translatorKeyBuilders the translator key builders to use.
	 *
	 * @return this foundation for fluent chaining.
	 */
	public F setTranslatorKeyBuilders(
		XEnum<BinaryValueTranslatorKeyBuilder> translatorKeyBuilders
	);

	/**
	 * @param valueTranslatorProvider the value-translator provider to use.
	 *
	 * @return this foundation for fluent chaining.
	 */
	public F setValueTranslatorProvider(
		BinaryValueTranslatorProvider valueTranslatorProvider
	);

	/**
	 * @param valueTranslatorMappingProvider the translator mapping provider to use.
	 *
	 * @return this foundation for fluent chaining.
	 */
	public F setValueTranslatorMappingProvider(
		BinaryValueTranslatorMappingProvider valueTranslatorMappingProvider
	);

	/**
	 * @param fieldHandlerProvider the field-handler provider to use.
	 *
	 * @return this foundation for fluent chaining.
	 */
	public F setFieldHandlerProvider(
		BinaryFieldHandlerProvider fieldHandlerProvider
	);

	@Override
	public PersistenceManager<Binary> createPersistenceManager();



	/**
	 * @return a new default {@link BinaryPersistenceFoundation}.
	 */
	public static BinaryPersistenceFoundation<?> New()
	{
		return new BinaryPersistenceFoundation.Default<>();
	}

	/**
	 * Default {@link BinaryPersistenceFoundation} implementation. Each binary-specific slot is lazily
	 * ensured on first access via the matching {@code ensureXxx} method and dispatched through the
	 * configured {@link org.eclipse.serializer.functional.InstanceDispatcherLogic}; the {@code ensureXxx} methods are protected so that
	 * subclasses can swap in alternate components without losing the lazy/dispatch wiring.
	 *
	 * @param <F> the concrete subclass type.
	 */
	public class Default<F extends BinaryPersistenceFoundation.Default<?>>
	extends PersistenceFoundation.Default<Binary, F>
	implements BinaryPersistenceFoundation<F>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private XTable<String, BinaryValueSetter>      customTranslatorLookup ;
		private XEnum<BinaryValueTranslatorKeyBuilder> translatorKeyBuilders  ;
		private BinaryValueTranslatorMappingProvider   valueTranslatorMapping ;
		private BinaryValueTranslatorProvider          valueTranslatorProvider;
		private BinaryFieldHandlerProvider             fieldHandlerProvider   ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default()
		{
			super(Binary.class);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public BinaryPersistenceFoundation.Default<F> Clone()
		{
			return new BinaryPersistenceFoundation.Default<>();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// getters //
		////////////
		
		@Override
		public XTable<String, BinaryValueSetter> getCustomTranslatorLookup()
		{
			if(this.customTranslatorLookup == null)
			{
				this.customTranslatorLookup = this.dispatch(this.ensureCustomTranslatorLookup());
			}
			
			return this.customTranslatorLookup;
		}
		
		@Override
		public XEnum<BinaryValueTranslatorKeyBuilder> getTranslatorKeyBuilders()
		{
			if(this.translatorKeyBuilders == null)
			{
				this.translatorKeyBuilders = this.dispatch(this.ensureTranslatorKeyBuilders());
			}
			
			return this.translatorKeyBuilders;
		}
		
		@Override
		public BinaryValueTranslatorMappingProvider getValueTranslatorMappingProvider()
		{
			if(this.valueTranslatorMapping == null)
			{
				this.valueTranslatorMapping = this.dispatch(this.ensureValueTranslatorMappingProvider());
			}
			
			return this.valueTranslatorMapping;
		}
		
		@Override
		public BinaryValueTranslatorProvider getValueTranslatorProvider()
		{
			if(this.valueTranslatorProvider == null)
			{
				this.valueTranslatorProvider = this.dispatch(this.ensureValueTranslatorProvider());
			}
			
			return this.valueTranslatorProvider;
		}
		
		@Override
		public BinaryFieldHandlerProvider getFieldHandlerProvider()
		{
			if(this.fieldHandlerProvider == null)
			{
				this.fieldHandlerProvider = this.dispatch(this.ensureFieldHandlerProvider());
			}
			
			return this.fieldHandlerProvider;
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// setters //
		////////////
		
		@Override
		public F setCustomTranslatorLookup(final XTable<String, BinaryValueSetter> customTranslatorLookup)
		{
			this.customTranslatorLookup = customTranslatorLookup;
			return this.$();
		}
		
		@Override
		public F setTranslatorKeyBuilders(final XEnum<BinaryValueTranslatorKeyBuilder> translatorKeyBuilders)
		{
			this.translatorKeyBuilders = translatorKeyBuilders;
			return this.$();
		}
		
		@Override
		public F setValueTranslatorProvider(final BinaryValueTranslatorProvider valueTranslatorProvider)
		{
			this.valueTranslatorProvider = valueTranslatorProvider;
			return this.$();
		}
		
		@Override
		public F setValueTranslatorMappingProvider(final BinaryValueTranslatorMappingProvider valueTranslatorMapping)
		{
			this.valueTranslatorMapping = valueTranslatorMapping;
			return this.$();
		}
		
		@Override
		public F setFieldHandlerProvider(final BinaryFieldHandlerProvider fieldHandlerProvider)
		{
			this.fieldHandlerProvider = fieldHandlerProvider;
			return this.$();
		}
	

		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected BinaryStorer.Creator ensureStorerCreator()
		{
			return BinaryStorer.Creator(
				() -> 1,
				this.isByteOrderMismatch()
			);
		}

		@Override
		protected BinaryLoader.Creator ensureBuilderCreator()
		{
			return new BinaryLoader.CreatorSimple(
				this.isByteOrderMismatch()
			);
		}

		@Override
		protected PersistenceTypeHandlerCreator<Binary> ensureTypeHandlerCreator()
		{
			return new BinaryTypeHandlerCreator.Default(
				this.getTypeAnalyzer(),
				this.getTypeResolver(),
				this.getFieldFixedLengthResolver(),
				this.getReferenceFieldEagerEvaluator(),
				this.getInstantiatorProvider(),
				this.referenceTypeHandlerManager(),
				this.getFieldHandlerProvider(),
				this.isByteOrderMismatch()
			);
		}
		
		@Override
		protected PersistenceCustomTypeHandlerRegistryEnsurer<Binary> ensureCustomTypeHandlerRegistryEnsurer(
			final F foundation
		)
		{
			return (f, rthm) ->
			{
				return BinaryPersistence.createDefaultCustomTypeHandlerRegistry(
					rthm,
					f.getSizedArrayLengthController(),
					f.getTypeHandlerCreator(),
					f.customTypeHandlers().values()
				);
			};
		}

		@Override
		protected synchronized PersistenceCustomTypeHandlerRegistry<Binary> ensureCustomTypeHandlerRegistry()
		{
			return this.getCustomTypeHandlerRegistryEnsurer().ensureCustomTypeHandlerRegistry(
				this,
				this.referenceTypeHandlerManager()
			);
		}

		@Override
		protected BinaryFieldLengthResolver ensureFieldFixedLengthResolver()
		{
			return BinaryPersistence.createFieldLengthResolver();
		}
		
		@Override
		protected PersistenceRootsProvider<Binary> ensureRootsProviderInternal()
		{
			return BinaryPersistenceRootsProvider.New(
				this.getRootResolverProvider(),
				this.getRootReferenceProvider()
			);
		}
		
		@Override
		protected PersistenceRootReferenceProvider<Binary> ensureRootReferenceProvider()
		{
			return BinaryRootReferenceProvider.New();
		}
		
		@Override
		protected PersistenceLegacyTypeHandlerCreator<Binary> ensureLegacyTypeHandlerCreator()
		{
			return BinaryLegacyTypeHandlerCreator.New(
				this.ensureValueTranslatorProvider(),
				this.getLegacyTypeHandlingListener(),
				this.isByteOrderMismatch()
			);
		}
		
		protected XTable<String, BinaryValueSetter> ensureCustomTranslatorLookup()
		{
			return EqHashTable.New();
		}
		
		protected XEnum<BinaryValueTranslatorKeyBuilder> ensureTranslatorKeyBuilders()
		{
			return EqHashEnum.New();
		}
		
		protected BinaryValueTranslatorMappingProvider ensureValueTranslatorMappingProvider()
		{
			return BinaryValueTranslatorMappingProvider.New();
		}
		
		protected BinaryValueTranslatorProvider ensureValueTranslatorProvider()
		{
			return BinaryValueTranslatorProvider.New(
				this.getCustomTranslatorLookup(),
				this.getTranslatorKeyBuilders(),
				this.getValueTranslatorMappingProvider(),
				this.isByteOrderMismatch()
			);
		}
		
		protected BinaryFieldHandlerProvider ensureFieldHandlerProvider()
		{
			return BinaryFieldHandlerProvider.New();
		}
	}

}
