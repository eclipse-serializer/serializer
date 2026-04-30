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

import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.util.X;

/**
 * Mutating-side facade over a {@link PersistenceTypeDictionaryProvider}: extends the read-only provider with
 * validation and registration of new type definitions discovered at runtime.
 * <p>
 * Three concrete flavors are bundled with this interface:
 * <ul>
 * <li>{@link Transient} &mdash; in-memory only, never writes back; discards changes when discarded.</li>
 * <li>{@link Exporting} &mdash; mirrors every successful registration to a
 *     {@link PersistenceTypeDictionaryExporter}, keeping the persistent textual form in sync.</li>
 * <li>{@link Immutable} &mdash; rejects every registration that would change the underlying
 *     {@link PersistenceTypeDictionaryView}, while still allowing structurally identical re-registrations
 *     (e.g. a custom type handler replacing a parsed definition).</li>
 * </ul>
 *
 * @see PersistenceTypeDictionary
 * @see PersistenceTypeDictionaryProvider
 */
public interface PersistenceTypeDictionaryManager extends PersistenceTypeDictionaryProvider
{
	/**
	 * Verifies that {@code typeDefinition} is consistent with what is currently registered in this manager's
	 * dictionary, throwing if not.
	 *
	 * @param typeDefinition the definition to validate.
	 *
	 * @return this manager, for fluent chaining.
	 *
	 * @throws PersistenceException if the definition's typeId is uninitialized or its structure conflicts
	 *                              with an already-registered definition for the same typeId.
	 */
	public PersistenceTypeDictionaryManager validateTypeDefinition(PersistenceTypeDefinition typeDefinition);

	/**
	 * Bulk variant of {@link #validateTypeDefinition(PersistenceTypeDefinition)}.
	 *
	 * @param typeDefinitions the definitions to validate.
	 *
	 * @return this manager, for fluent chaining.
	 *
	 * @throws PersistenceException if any definition fails validation.
	 */
	public PersistenceTypeDictionaryManager validateTypeDefinitions(
		Iterable<? extends PersistenceTypeDefinition> typeDefinitions
	);

	/**
	 * Validates and registers {@code typeDefinition}; mirrors
	 * {@link PersistenceTypeDictionary#registerTypeDefinition(PersistenceTypeDefinition)} on the underlying
	 * dictionary.
	 *
	 * @param typeDefinition the definition to register.
	 *
	 * @return {@code true} if registration changed dictionary state.
	 */
	public boolean registerTypeDefinition(PersistenceTypeDefinition typeDefinition);

	/**
	 * Bulk variant of {@link #registerTypeDefinition(PersistenceTypeDefinition)}.
	 *
	 * @param typeDefinitions the definitions to register.
	 *
	 * @return {@code true} if registration changed dictionary state.
	 */
	public boolean registerTypeDefinitions(Iterable<? extends PersistenceTypeDefinition> typeDefinitions);

	/**
	 * Validates and registers {@code typeDefinition} as the runtime definition of its lineage; mirrors
	 * {@link PersistenceTypeDictionary#registerRuntimeTypeDefinition(PersistenceTypeDefinition)} on the
	 * underlying dictionary.
	 *
	 * @param typeDefinition the definition to register.
	 *
	 * @return {@code true} if registration changed dictionary state.
	 */
	public boolean registerRuntimeTypeDefinition(PersistenceTypeDefinition typeDefinition);

	/**
	 * Bulk variant of {@link #registerRuntimeTypeDefinition(PersistenceTypeDefinition)}.
	 *
	 * @param typeDefinitions the definitions to register.
	 *
	 * @return {@code true} if registration changed dictionary state.
	 */
	public boolean registerRuntimeTypeDefinitions(Iterable<? extends PersistenceTypeDefinition> typeDefinitions);


	/**
	 * Reusable static validation that is called by every {@link #validateTypeDefinition(PersistenceTypeDefinition)}
	 * implementation: ensures a valid typeId and rejects any structural mismatch with a previously registered
	 * definition for the same typeId.
	 *
	 * @param dictionary     the dictionary to check against.
	 * @param typeDefinition the candidate definition.
	 *
	 * @throws PersistenceException if the typeId is uninitialized or the structure conflicts with an
	 *                              existing entry.
	 */
	public static void validateTypeDefinition(
		final PersistenceTypeDictionary dictionary    ,
		final PersistenceTypeDefinition typeDefinition
	)
	{
		PersistenceTypeDictionary.validateTypeId(typeDefinition);
		
		// Only the TypeId is the unique identifier. The type name only identifies the TypeLineage.
		final PersistenceTypeDefinition registered = dictionary.lookupTypeById(typeDefinition.typeId());

		// Any type definition (e.g. a custom TypeHandler) must match the structural description in the dictionary.
		if(registered != null && !PersistenceTypeDescription.equalStructure(registered, typeDefinition))
		{
			throw new PersistenceException("Type Definition mismatch: " + typeDefinition);
		}
	}


	
	
	/**
	 * Creates an {@link Exporting} manager that mirrors every successful registration through
	 * {@code typeDictionaryExporter}.
	 *
	 * @param typeDictionaryProvider source of the underlying dictionary; must not be {@code null}.
	 * @param typeDictionaryExporter sink that receives the textual form after every change; must not be
	 *                               {@code null}.
	 *
	 * @return the new manager.
	 */
	public static PersistenceTypeDictionaryManager Exporting(
		final PersistenceTypeDictionaryProvider typeDictionaryProvider,
		final PersistenceTypeDictionaryExporter typeDictionaryExporter
	)
	{
		return new PersistenceTypeDictionaryManager.Exporting(
			notNull(typeDictionaryProvider),
			notNull(typeDictionaryExporter)
		);
	}

	/**
	 * Skeleton {@link PersistenceTypeDictionaryManager} that lazily provides its underlying dictionary via
	 * {@link #internalProvideTypeDictionary()}, caches it, and routes every register/validate call through
	 * the cached instance. Mutation methods are {@code synchronized} on the manager to serialize concurrent
	 * registrations.
	 *
	 * @param <D> the concrete dictionary subtype provided by this manager.
	 */
	public abstract class Abstract<D extends PersistenceTypeDictionary>
	implements PersistenceTypeDictionaryManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private transient D cachedTypeDictionary;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract()
		{
			super();
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		protected final D ensureTypeDictionary()
		{
			if(this.cachedTypeDictionary == null)
			{
				synchronized(this)
				{
					// recheck after synch
					if(this.cachedTypeDictionary == null)
					{
						this.cachedTypeDictionary = this.internalProvideTypeDictionary();
					}
				}
			}
			
			return this.cachedTypeDictionary;
		}
		
		protected abstract D internalProvideTypeDictionary();
		

		@Override
		public synchronized PersistenceTypeDictionary provideTypeDictionary()
		{
			return this.ensureTypeDictionary();
		}
		
		@Override
		public synchronized boolean registerTypeDefinition(final PersistenceTypeDefinition typeDefinition)
		{
			this.validateTypeDefinition(typeDefinition);

			return this.ensureTypeDictionary().registerTypeDefinition(typeDefinition);
		}

		@Override
		public synchronized boolean registerTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			this.validateTypeDefinitions(typeDefinitions);
			
			return this.ensureTypeDictionary().registerTypeDefinitions(typeDefinitions);
		}

		@Override
		public synchronized boolean registerRuntimeTypeDefinition(
			final PersistenceTypeDefinition typeDefinition
		)
		{
			this.validateTypeDefinition(typeDefinition);

			return this.ensureTypeDictionary().registerRuntimeTypeDefinition(typeDefinition);
		}
		

		@Override
		public synchronized boolean registerRuntimeTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			this.validateTypeDefinitions(typeDefinitions);
			
			return this.ensureTypeDictionary().registerRuntimeTypeDefinitions(typeDefinitions);
		}

		@Override
		public synchronized PersistenceTypeDictionaryManager validateTypeDefinition(
			final PersistenceTypeDefinition typeDefinition
		)
		{
			PersistenceTypeDictionaryManager.validateTypeDefinition(
				this.ensureTypeDictionary(),
				typeDefinition
			);
			
			return this;
		}

		@Override
		public synchronized PersistenceTypeDictionaryManager validateTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			final PersistenceTypeDictionary typeDictionary = this.ensureTypeDictionary();
			for(final PersistenceTypeDefinition td : typeDefinitions)
			{
				PersistenceTypeDictionaryManager.validateTypeDefinition(typeDictionary, td);
			}
			
			return this;
		}
	}

	
	
	/**
	 * {@link PersistenceTypeDictionaryManager} that, on top of the underlying dictionary, mirrors every
	 * successful registration through a {@link PersistenceTypeDictionaryExporter} so that the persistent
	 * textual form stays in sync. A {@code changed} flag is used to coalesce export work to one call per
	 * batch.
	 */
	public final class Exporting extends PersistenceTypeDictionaryManager.Abstract<PersistenceTypeDictionary>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceTypeDictionaryProvider typeDictionaryProvider;
		private final PersistenceTypeDictionaryExporter typeDictionaryExporter;

		private transient boolean changed;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Exporting(
			final PersistenceTypeDictionaryProvider typeDictionaryProvider,
			final PersistenceTypeDictionaryExporter typeDictionaryExporter
		)
		{
			super();
			this.typeDictionaryProvider = typeDictionaryProvider;
			this.typeDictionaryExporter = typeDictionaryExporter;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		private boolean hasChanged()
		{
			return this.changed;
		}

		private void markChanged()
		{
			this.changed = true;
		}

		private void resetChangeMark()
		{
			this.changed = false;
		}
		
		@Override
		protected final PersistenceTypeDictionary internalProvideTypeDictionary()
		{
			final PersistenceTypeDictionary typeDictionary = this.typeDictionaryProvider.provideTypeDictionary();
			this.markChanged();
			
			return typeDictionary;
		}

		/**
		 * Exports the dictionary if and only if a change has been recorded since the last export, then clears
		 * the change flag.
		 *
		 * @return this manager, for fluent chaining.
		 */
		public final PersistenceTypeDictionaryManager.Exporting synchUpdateExport()
		{
			if(this.hasChanged())
			{
				this.exportTypeDictionary();
				this.resetChangeMark();
			}

			return this;
		}

		/**
		 * Forces an immediate export of the underlying dictionary regardless of the change flag.
		 *
		 * @return this manager, for fluent chaining.
		 */
		public final synchronized PersistenceTypeDictionaryManager.Exporting exportTypeDictionary()
		{
			this.typeDictionaryExporter.exportTypeDictionary(this.ensureTypeDictionary());
			return this;
		}
		
		@Override
		public final synchronized boolean registerTypeDefinition(final PersistenceTypeDefinition typeDefinition)
		{
			final boolean hasChanged = super.registerTypeDefinition(typeDefinition);
			if(hasChanged)
			{
				this.markChanged();
				this.synchUpdateExport();
			}
			
			return hasChanged;
		}

		@Override
		public final synchronized boolean registerTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			final boolean hasChanged = super.registerTypeDefinitions(typeDefinitions);
			if(hasChanged)
			{
				this.markChanged();
				this.synchUpdateExport();
			}
			
			return hasChanged;
		}

		@Override
		public final synchronized boolean registerRuntimeTypeDefinition(
			final PersistenceTypeDefinition typeDefinition
		)
		{
			final boolean hasChanged = super.registerRuntimeTypeDefinition(typeDefinition);
			if(hasChanged)
			{
				this.markChanged();
				this.synchUpdateExport();
			}
			
			return hasChanged;
		}
		

		@Override
		public final synchronized boolean registerRuntimeTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			final boolean hasChanged = super.registerRuntimeTypeDefinitions(typeDefinitions);
			if(hasChanged)
			{
				this.markChanged();
				this.synchUpdateExport();
			}
			
			return hasChanged;
		}

	}
	
	
	
	/**
	 * Creates a {@link Transient} manager that holds new registrations only in memory and never writes them
	 * back to a persistent dictionary location.
	 *
	 * @param typeDictionaryProvider source of the underlying dictionary; must not be {@code null}.
	 *
	 * @return the new manager.
	 */
	public static PersistenceTypeDictionaryManager Transient(
		final PersistenceTypeDictionaryProvider typeDictionaryProvider
	)
	{
		return new PersistenceTypeDictionaryManager.Transient(
			notNull(typeDictionaryProvider)
		);
	}

	/**
	 * In-memory-only {@link PersistenceTypeDictionaryManager}: validates and registers definitions but never
	 * exports them. Suited to one-off serialization scenarios where the dictionary is rebuilt on demand.
	 */
	public final class Transient extends PersistenceTypeDictionaryManager.Abstract<PersistenceTypeDictionary>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeDictionaryProvider typeDictionaryProvider;
				
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Transient(final PersistenceTypeDictionaryProvider typeDictionaryProvider)
		{
			super();
			this.typeDictionaryProvider = typeDictionaryProvider;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected PersistenceTypeDictionary internalProvideTypeDictionary()
		{
			return this.typeDictionaryProvider.provideTypeDictionary();
		}
		
	}

	
	
	/**
	 * Creates an {@link Immutable} manager that wraps a read-only
	 * {@link PersistenceTypeDictionaryViewProvider} and rejects any registration that would change the view.
	 *
	 * @param typeDictionaryProvider source of the underlying view; must not be {@code null}.
	 *
	 * @return the new manager.
	 */
	public static PersistenceTypeDictionaryManager Immutable(
		final PersistenceTypeDictionaryViewProvider typeDictionaryProvider
	)
	{
		return new PersistenceTypeDictionaryManager.Immutable(
			notNull(typeDictionaryProvider)
		);
	}

	/**
	 * Read-only {@link PersistenceTypeDictionaryManager}: re-registrations are accepted only when the
	 * passed definition's description is {@linkplain PersistenceTypeDescription#equalDescription(
	 * PersistenceTypeDescription, PersistenceTypeDescription) structurally identical} to what is already
	 * stored. Any other change throws {@link UnsupportedOperationException}.
	 */
	public final class Immutable extends PersistenceTypeDictionaryManager.Abstract<PersistenceTypeDictionaryView>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeDictionaryViewProvider typeDictionaryProvider;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Immutable(final PersistenceTypeDictionaryViewProvider typeDictionaryProvider)
		{
			super();
			this.typeDictionaryProvider = typeDictionaryProvider;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		protected PersistenceTypeDictionaryView internalProvideTypeDictionary()
		{
			return this.typeDictionaryProvider.provideTypeDictionary();
		}

		@Override
		public final synchronized boolean registerTypeDefinition(
			final PersistenceTypeDefinition typeDefinition
		)
		{
			return this.registerTypeDefinitions(X.Constant(typeDefinition));
		}

		@Override
		public final synchronized boolean registerTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			final PersistenceTypeDictionaryView typeDictionary = this.ensureTypeDictionary();
			for(final PersistenceTypeDefinition td : typeDefinitions)
			{
				PersistenceTypeDictionary.validateTypeId(td);
				
				// Only the TypeId is the unique identifier. The type name only identifies the TypeLineage.
				final PersistenceTypeDefinition registered = typeDictionary.lookupTypeById(td.typeId());

				// Any type definition (e.g. a custom TypeHandler) must match the (exact) description in the dictionary.
				if(registered == null || !PersistenceTypeDescription.equalDescription(registered, td))
				{
					throw new UnsupportedOperationException("Read-only TypeDictionary cannot change.");
				}
			}
			
			// no change required (no exception)
			return false;
		}

		@Override
		public final synchronized boolean registerRuntimeTypeDefinition(
			final PersistenceTypeDefinition typeDefinition
		)
		{
			return this.registerTypeDefinition(typeDefinition);
		}

		@Override
		public final synchronized boolean registerRuntimeTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			return this.registerTypeDefinitions(typeDefinitions);
		}
		
	}
	
}
