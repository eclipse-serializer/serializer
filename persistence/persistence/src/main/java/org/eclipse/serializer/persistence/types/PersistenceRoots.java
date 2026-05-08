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

import static org.eclipse.serializer.util.X.mayNull;
import static org.eclipse.serializer.util.X.notNull;

import java.util.function.BiConsumer;

import org.eclipse.serializer.collections.EqConstHashTable;
import org.eclipse.serializer.collections.EqHashTable;
import org.eclipse.serializer.collections.types.XGettingTable;
import org.eclipse.serializer.util.cql.CQL;


/**
 * Mutable counterpart to {@link PersistenceRootsView}: the runtime representation of the technical root set
 * the persistence layer reads from and writes to. Backed by a {@link PersistenceRootResolver} that produces
 * the resolved entries on demand and refreshed by the loading code via {@link #updateEntries(XGettingTable)}.
 * <p>
 * The {@link #hasChanged()} flag marks whether the in-memory entries have diverged from the last persisted
 * snapshot &mdash; e.g. when an entry was removed during loading because its supplier resolved to
 * {@code null}.
 *
 * @see PersistenceRootsView
 * @see PersistenceRootResolver
 */
public interface PersistenceRoots extends PersistenceRootsView
{
	@Override
	public PersistenceRootReference rootReference();

	/**
	 * The currently resolved root entries by identifier. Triggers resolution via the underlying
	 * {@link PersistenceRootResolver} on first access.
	 *
	 * @return the entries.
	 */
	public XGettingTable<String, Object> entries();

	/**
	 * Whether the in-memory entries diverge from the last persisted snapshot.
	 *
	 * @return {@code true} if a change has been recorded.
	 */
	public boolean hasChanged();

	/**
	 * Replaces the in-memory entries with {@code newEntries} <em>without</em> setting the change flag. Used
	 * by the type handler when reinitializing roots from persisted data.
	 *
	 * @param newEntries the entries to install.
	 */
	public void reinitializeEntries(XGettingTable<String, Object> newEntries);

	/**
	 * Replaces the in-memory entries with {@code newEntries} and marks the roots as changed, so the next
	 * commit will re-persist them. Used during runtime synchronization (e.g. when initializing an embedded
	 * storage instance).
	 *
	 * @param newEntries the entries to install.
	 */
	public void updateEntries(XGettingTable<String, Object> newEntries);

	@Override
	public default <C extends BiConsumer<String, Object>> C iterateEntries(final C iterator)
	{
		this.entries().iterate(e ->
			iterator.accept(e.key(), e.value())
		);

		return iterator;
	}



	/**
	 * Creates a new {@link Default} {@link PersistenceRoots} backed by {@code rootResolver}, with no
	 * pre-existing id mapping.
	 *
	 * @param rootResolver the resolver that produces the entries; must not be {@code null}.
	 *
	 * @return the newly created roots.
	 */
	public static PersistenceRoots New(final PersistenceRootResolver rootResolver)
	{
		return PersistenceRoots.Default.New(rootResolver, null);
	}

	/**
	 * Default mutable {@link PersistenceRoots}. Caches the resolver-produced entries lazily, supports an
	 * optional {@code identifier → objectId} mapping consumed by the type handler when binding loaded
	 * entries, and synchronizes mutating operations on the instance.
	 */
	public final class Default implements PersistenceRoots
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		/**
		 * Creates a new {@link Default} backed by {@code rootResolver}, optionally seeded with a previously
		 * persisted {@code identifier → objectId} mapping that the type handler will consume during
		 * loading.
		 *
		 * @param rootResolver  the resolver; must not be {@code null}.
		 * @param rootIdMapping the persisted id mapping; may be {@code null}.
		 *
		 * @return the newly created roots.
		 */
		public static PersistenceRoots.Default New(
			final PersistenceRootResolver   rootResolver ,
			final EqHashTable<String, Long> rootIdMapping
		)
		{
			return new PersistenceRoots.Default(
				notNull(rootResolver) ,
				mayNull(rootIdMapping),
				null                  ,
				false
			);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		/*
		 * The transient keyword actually doesn't matter at all since a custom TypeHandler is used.
		 * Its only purpose here is to indicate that the fields are not directly persisted.
		 */

		private final transient PersistenceRootResolver          rootResolver   ;
		private       transient EqHashTable<String, Long>        rootIdMapping  ;
		private       transient EqConstHashTable<String, Object> resolvedEntries;
		private       transient boolean                          hasChanged     ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final PersistenceRootResolver          rootResolver   ,
			final EqHashTable<String, Long>        rootIdMapping  ,
			final EqConstHashTable<String, Object> resolvedEntries,
			final boolean                          hasChanged
		)
		{
			super();
			this.rootResolver    = rootResolver   ;
			this.rootIdMapping   = rootIdMapping  ;
			this.resolvedEntries = resolvedEntries;
			this.hasChanged      = hasChanged     ;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final synchronized boolean hasChanged()
		{
			return this.hasChanged;
		}
		
		@Override
		public final synchronized PersistenceRootReference rootReference()
		{
			return this.rootResolver.root();
		}

		@Override
		public final synchronized XGettingTable<String, Object> entries()
		{
			if(this.resolvedEntries == null)
			{
				final XGettingTable<String, Object> effectiveRoots = this.rootResolver.resolveDefinedRootInstances();
				this.resolvedEntries = EqConstHashTable.New(effectiveRoots);
				this.hasChanged = false;
			}
			
			/*
			 * Internal collection is Intentionally publicly available
			 * as this implementation is actually just a typed wrapper for it.
			 * The instance is imutable, so there can be no harm done
			 */
			return this.resolvedEntries;
		}
		

		@Override
		public final synchronized void reinitializeEntries(final XGettingTable<String, Object> newEntries)
		{
			// having to replace/update the entries is a change as well.
			this.resolvedEntries = EqConstHashTable.New(newEntries);
		}
		
		/**
		 * Used for example during roots synchronization when initializing an embedded storage instance.
		 * 
		 * @param newEntries the actual entries to be set.
		 */
		@Override
		public final synchronized void updateEntries(final XGettingTable<String, Object> newEntries)
		{
			this.reinitializeEntries(newEntries);
			this.hasChanged = true;
		}

		/**
		 * Variant of {@link #updateEntries(XGettingTable)} used during loading: drops {@code null}-valued
		 * entries (treating them as removed root identifiers) and sets {@link #hasChanged()} only when at
		 * least one such entry was dropped.
		 *
		 * @param resolvedRoots the loaded entries before the {@code null}-filter is applied.
		 */
		public final synchronized void loadingUpdateEntries(final XGettingTable<String, Object> resolvedRoots)
		{
			final XGettingTable<String, Object> effectiveRoots = CQL
				.from(resolvedRoots)
				.select(kv -> kv.value() != null)
				.executeInto(EqHashTable.New())
			;
			
			// if at least one null entry was removed, the roots at runtime changed compared to the persistant state
			this.resolvedEntries = EqConstHashTable.New(effectiveRoots);
			this.hasChanged      = effectiveRoots.size() != resolvedRoots.size();
		}
		
		///////////////////////////////////////////////////////////////////////////
		// Hooks for TypeHandler //
		////////////

		/**
		 * Internal hook used by the roots type handler to access the underlying {@link PersistenceRootResolver}.
		 *
		 * @return the root resolver.
		 */
		public final PersistenceRootResolver $rootResolver()
		{
			return this.rootResolver;
		}

		/**
		 * Internal hook used by the roots type handler to access the persisted
		 * {@code identifier → objectId} mapping seeded at construction time.
		 *
		 * @return the id mapping, or {@code null} if none was supplied or it has been discarded.
		 */
		public final EqHashTable<String, Long> $rootIdMapping()
		{
			return this.rootIdMapping;
		}

		/**
		 * Internal hook used by the roots type handler to drop the persisted id mapping once it has been
		 * fully consumed.
		 */
		public final void $discardRootIdMapping()
		{
			this.rootIdMapping = null;
		}

	}

}
