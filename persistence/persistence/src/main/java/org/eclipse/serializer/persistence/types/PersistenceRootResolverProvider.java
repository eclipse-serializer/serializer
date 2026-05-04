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

import static org.eclipse.serializer.util.X.notNull;

import java.util.function.Supplier;

import org.eclipse.serializer.collections.EqHashTable;
import org.eclipse.serializer.collections.types.XGettingTable;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.reference.Reference;
import org.eclipse.serializer.typing.KeyValue;


/**
 * Builder/factory for {@link PersistenceRootResolver}s. Lets the application register named root entries,
 * configure refactoring rules, and supply the type handler manager that the resolver will fall back to for
 * enum-constants resolution. Calling {@link #provideRootResolver()} produces (and then caches) the
 * configured resolver.
 * <p>
 * The user-defined root reference must already be supplied at construction time, since the system constants
 * registered during initialization include a supplier wrapping it. After
 * {@link #provideRootResolver()} has been called once, every subsequent call returns the cached resolver
 * &mdash; further mutations to the provider are not reflected.
 *
 * @see PersistenceRootResolver
 * @see PersistenceRootReference
 */
public interface PersistenceRootResolverProvider
{
	/**
	 * The {@link PersistenceRootReference} that the produced resolver will expose as the user-defined root.
	 *
	 * @return the root reference.
	 */
	public PersistenceRootReference rootReference();

	/**
	 * The identifier under which the user-defined root will be registered. Defaults to
	 * {@link Persistence#rootIdentifier()}.
	 *
	 * @return the root identifier.
	 */
	public default String rootIdentifier()
	{
		return Persistence.rootIdentifier();
	}

	/**
	 * Whether a non-{@code null} root has already been set on the {@link #rootReference()}.
	 *
	 * @return {@code true} if a root is registered.
	 */
	public default boolean hasRootRegistered()
	{
		final PersistenceRootReference rootReference = this.rootReference();

		return rootReference != null && rootReference.get() != null;
	}

	/**
	 * Sets the user-defined root instance on the {@link #rootReference()}.
	 *
	 * @param root the new root instance; may be {@code null}.
	 *
	 * @return this provider, for fluent chaining.
	 */
	public PersistenceRootResolverProvider setRoot(Object root);

	/**
	 * Registers a named root entry that returns {@code instance} on lookup. Convenience wrapper around
	 * {@link #registerRootSupplier(String, Supplier)} for eager-construction roots.
	 *
	 * @param identifier the identifier to register the entry under.
	 * @param instance   the root instance.
	 *
	 * @return this provider, for fluent chaining.
	 */
	public default PersistenceRootResolverProvider registerRoot(
		final String identifier,
		final Object instance
	)
	{
		return this.registerRootSupplier(identifier, () -> instance);
	}


	/**
	 * Registers a supplier for the user-defined root under {@link #rootIdentifier()}. Fails if an entry is
	 * already registered for that identifier.
	 *
	 * @param instanceSupplier the supplier producing the root instance on demand.
	 *
	 * @return this provider, for fluent chaining.
	 */
	public default PersistenceRootResolverProvider registerRootSupplier(final Supplier<?> instanceSupplier)
	{
		return this.registerRootSupplier(this.rootIdentifier(), instanceSupplier);
	}

	/**
	 * Registers a named root entry whose instance is resolved lazily by the passed supplier. Fails if an
	 * entry is already registered for {@code identifier}.
	 *
	 * @param identifier       the identifier to register the entry under.
	 * @param instanceSupplier the supplier producing the root instance on demand.
	 *
	 * @return this provider, for fluent chaining.
	 */
	public PersistenceRootResolverProvider registerRootSupplier(String identifier, Supplier<?> instanceSupplier);

	/**
	 * Bulk variant of {@link #registerRootSupplier(String, Supplier)}: registers every entry from the
	 * passed table.
	 *
	 * @param roots the {@code identifier → supplier} table to register.
	 *
	 * @return this provider, for fluent chaining.
	 */
	public default PersistenceRootResolverProvider registerRootSuppliers(
		final XGettingTable<String, Supplier<?>> roots
	)
	{
		synchronized(this)
		{
			roots.iterate(kv ->
				this.registerRootSupplier(kv.key(), kv.value())
			);
		}

		return this;
	}

	/**
	 * Configures the {@link PersistenceTypeDescriptionResolverProvider} used to wrap the produced resolver
	 * in a {@link PersistenceRootResolver.MappingWrapper}. Takes precedence over a refactoring mapping
	 * supplied via {@link #setRefactoring(PersistenceRefactoringMappingProvider)}.
	 *
	 * @param typeDescriptionResolverProvider the type description resolver provider.
	 *
	 * @return this provider, for fluent chaining.
	 */
	public PersistenceRootResolverProvider setTypeDescriptionResolverProvider(
		PersistenceTypeDescriptionResolverProvider typeDescriptionResolverProvider
	);

	/**
	 * Configures a refactoring mapping that will be turned into a
	 * {@link PersistenceTypeDescriptionResolverProvider} (when no explicit one is set via
	 * {@link #setTypeDescriptionResolverProvider}) and used to wrap the produced resolver.
	 *
	 * @param refactoringMapping the refactoring mapping provider.
	 *
	 * @return this provider, for fluent chaining.
	 */
	public PersistenceRootResolverProvider setRefactoring(PersistenceRefactoringMappingProvider refactoringMapping);


	/**
	 * The forward reference to the {@link PersistenceTypeHandlerManager} that the produced resolver will
	 * consult for enum-constants resolution.
	 *
	 * @return the type handler manager reference, or {@code null} if none has been set.
	 */
	public Reference<? extends PersistenceTypeHandlerManager<?>> typeHandlerManager();

	/**
	 * Sets the forward reference to the {@link PersistenceTypeHandlerManager}. The reference is consulted
	 * lazily by the produced resolver, so it may still be uninitialized when this setter is called.
	 *
	 * @param typeHandlerManager the type handler manager reference.
	 *
	 * @return this provider, for fluent chaining.
	 */
	public PersistenceRootResolverProvider setTypeHandlerManager(
		Reference<? extends PersistenceTypeHandlerManager<?>> typeHandlerManager
	);


	/**
	 * Builds and caches the {@link PersistenceRootResolver}. Subsequent calls return the cached instance.
	 *
	 * @return the produced resolver.
	 */
	public PersistenceRootResolver provideRootResolver();
	
	
	// (20.02.2020 TM)NOTE: too dangerous with the newly required ClassLoaderProvider pattern.
//	public static PersistenceRootResolverProvider New(final PersistenceRootReference rootReference)
//	{
//		return New(rootReference, PersistenceTypeResolver.Default());
//	}
	
	/**
	 * Creates a new {@link Default} provider using {@link PersistenceRootEntry#New(String, Supplier)} as
	 * the entry factory.
	 *
	 * @param <D>           the persistence data type (unused, kept for API symmetry).
	 * @param rootReference the user-defined root reference; must not be {@code null}.
	 * @param typeResolver  the type resolver used by the refactoring layer; must not be {@code null}.
	 *
	 * @return the newly created provider.
	 */
	public static <D> PersistenceRootResolverProvider New(
		final PersistenceRootReference rootReference,
		final PersistenceTypeResolver  typeResolver
	)
	{
		return New(rootReference, typeResolver, PersistenceRootEntry::New);
	}

	/**
	 * Creates a new {@link Default} provider with a custom {@link PersistenceRootEntry.Provider}.
	 *
	 * @param rootReference the user-defined root reference; must not be {@code null}.
	 * @param typeResolver  the type resolver used by the refactoring layer; must not be {@code null}.
	 * @param entryProvider the entry factory; must not be {@code null}.
	 *
	 * @return the newly created provider.
	 */
	public static PersistenceRootResolverProvider New(
		final PersistenceRootReference      rootReference,
		final PersistenceTypeResolver       typeResolver ,
		final PersistenceRootEntry.Provider entryProvider
	)
	{
		final PersistenceRootResolverProvider.Default builder = new PersistenceRootResolverProvider.Default(
			notNull(rootReference),
			notNull(typeResolver) ,
			notNull(entryProvider)
		);

		return builder;
	}


	/**
	 * Default {@link PersistenceRootResolverProvider}: collects root entries in an ordered hash table,
	 * caches the produced resolver between {@link #provideRootResolver()} calls, and synchronizes mutating
	 * operations on the instance.
	 */
	public class Default implements PersistenceRootResolverProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceRootEntry.Provider                         entryProvider                  ;
		private final PersistenceTypeResolver                               typeResolver                   ;
		private final EqHashTable<String, PersistenceRootEntry>             rootEntries                    ;
		private final PersistenceRootReference                              rootReference                  ;
		private       PersistenceTypeDescriptionResolverProvider            typeDescriptionResolverProvider;
		private       PersistenceRefactoringMappingProvider                 refactoringMapping             ;
		private       Reference<? extends PersistenceTypeHandlerManager<?>> refTypeHandlerManager          ;
				
		private transient PersistenceRootResolver cachedRootResolver;
		
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final PersistenceRootReference      rootReference,
			final PersistenceTypeResolver       typeResolver ,
			final PersistenceRootEntry.Provider entryProvider
		)
		{
			super();
			this.rootReference = rootReference; // must be non-null from the start for #initializeRootEntries to work!
			this.typeResolver  = typeResolver ;
			this.entryProvider = entryProvider;
			
			this.rootEntries = this.initializeRootEntries();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final synchronized PersistenceRootReference rootReference()
		{
			return this.rootReference;
		}
		
		private Supplier<?> createRootReferenceSupplier()
		{
			final PersistenceRootReference rootReference = this.rootReference;
			
			return () ->
				rootReference
			;
		}
		
		/**
		 * System constants that must be present and may not be replaced by user logic are initially registered.
		 */
		private EqHashTable<String, PersistenceRootEntry> initializeRootEntries()
		{
			final EqHashTable<String, PersistenceRootEntry> entries = EqHashTable.New();
			
			for(final KeyValue<String, Supplier<?>> entry : PersistenceMetaIdentifiers.defineConstantSuppliers())
			{
				this.initializeEntry(entries, entry.key(), entry.value());
			}

			// gets registered once initially and never modified afterwards
			this.initializeEntry(entries, this.rootIdentifier(), this.createRootReferenceSupplier());
							
			return entries;
		}
		
		private void initializeEntry(
			final EqHashTable<String, PersistenceRootEntry> entries   ,
			final String                                    identifier,
			final Supplier<?>                               supplier
		)
		{
			entries.add(identifier, this.entryProvider.provideRootEntry(identifier, supplier));
		}
		
		@Override
		public final synchronized PersistenceRootResolverProvider setRoot(final Object root)
		{
			// no need to reregister, see #initializeRootEntries
			this.rootReference().setRoot(root);
			
			return this;
		}
		
		@Override
		public final synchronized PersistenceRootResolverProvider registerRootSupplier(
			final String      identifier      ,
			final Supplier<?> instanceSupplier
		)
		{
			this.addEntry(identifier, instanceSupplier);
			
			return this;
		}
		
		private void addEntry(
			final String      identifier      ,
			final Supplier<?> instanceSupplier
		)
		{
			final PersistenceRootEntry entry = this.entryProvider.provideRootEntry(identifier, instanceSupplier);
			this.addEntry(identifier, entry);
		}
		
		private void addEntry(final String identifier, final PersistenceRootEntry entry)
		{
			if(this.rootEntries.add(identifier, entry))
			{
				return;
			}
			
			throw new PersistenceException("Root entry already registered for identifier \"" + identifier + '"');
		}
		
		private PersistenceRootResolver createRootResolver()
		{
			final PersistenceRootResolver resolver = new PersistenceRootResolver.Default(
				this.rootIdentifier()     ,
				this.rootReference()      ,
				this.rootEntries.immure() ,
				this.refTypeHandlerManager
			);
			
			final PersistenceTypeDescriptionResolverProvider refactoring = this.getEffectiveTypeDescriptionResolver();
						
			return refactoring == null
				? resolver
				: PersistenceRootResolver.Wrap(resolver, refactoring)
			;
		}
					
		@Override
		public final synchronized PersistenceRootResolver provideRootResolver()
		{
			if(this.cachedRootResolver == null)
			{
				this.cachedRootResolver = this.createRootResolver();
			}
			
			return this.cachedRootResolver;
		}
		
		protected PersistenceTypeDescriptionResolverProvider getEffectiveTypeDescriptionResolver()
		{
			if(this.typeDescriptionResolverProvider != null)
			{
				return this.typeDescriptionResolverProvider;
			}
			
			if(this.refactoringMapping != null)
			{
				return PersistenceTypeDescriptionResolverProvider.Caching(
					this.typeResolver,
					this.refactoringMapping
				);
			}
			
			return null;
		}
		
		@Override
		public final synchronized PersistenceRootResolverProvider setTypeDescriptionResolverProvider(
			final PersistenceTypeDescriptionResolverProvider typeDescriptionResolverProvider
		)
		{
			this.typeDescriptionResolverProvider = typeDescriptionResolverProvider;
			return this;
		}
		
		@Override
		public final synchronized PersistenceRootResolverProvider setRefactoring(
			final PersistenceRefactoringMappingProvider refactoringMapping
		)
		{
			this.refactoringMapping = refactoringMapping;
			return this;
		}
		
		@Override
		public synchronized Reference<? extends PersistenceTypeHandlerManager<?>> typeHandlerManager()
		{
			return this.refTypeHandlerManager;
		}
		
		@Override
		public synchronized PersistenceRootResolverProvider setTypeHandlerManager(
			final Reference<? extends PersistenceTypeHandlerManager<?>> typeHandlerManager
		)
		{
			this.refTypeHandlerManager = typeHandlerManager;
			return this;
		}
		
	}
}
