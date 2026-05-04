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

import org.eclipse.serializer.chars.XChars;
import org.eclipse.serializer.collections.EqHashTable;
import org.eclipse.serializer.collections.types.XGettingTable;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;


/**
 * Sequence of {@link PersistenceTypeDefinition}s belonging to one logical type as it evolved over time. Every
 * type encountered in the persistent dictionary has exactly one lineage; each entry in the lineage is one
 * historical structural definition keyed by its {@code typeId}, with at most one of them flagged as the
 * <em>runtime definition</em> (i.e. the structure that matches the currently loaded {@link Class}).
 * <p>
 * Lineages are what makes legacy mapping possible: the {@link PersistenceLegacyTypeMapper} compares the
 * runtime definition against the older entries and produces a mapping from the obsolete fields to the current
 * ones. A lineage may also have no runtime definition (when the type has been removed from the codebase) or
 * no runtime class (when the type was explicitly registered without a Java counterpart).
 *
 * @see PersistenceTypeDefinition
 * @see PersistenceTypeLineageView
 * @see PersistenceTypeLineageCreator
 */
public interface PersistenceTypeLineage
{
	/**
	 * The textual type name shared by every entry in this lineage. May be {@code null} only for types
	 * explicitly mapped as having no runtime counterpart.
	 *
	 * @return the type name.
	 */
	public String typeName();

	/**
	 * The runtime {@link Class} matching this lineage's {@link #typeName()}, or {@code null} if the name
	 * could not be resolved (e.g. the class has been removed from the codebase).
	 *
	 * @return the runtime class, or {@code null}.
	 */
	public Class<?> type();

	/**
	 * The historical entries by {@code typeId}, kept ordered ascending so that the latest definition is
	 * always at the tail.
	 *
	 * @return the entries by type id.
	 */
	public XGettingTable<Long, PersistenceTypeDefinition> entries();

	/**
	 * The most recently registered {@link PersistenceTypeDefinition}, i.e. the entry with the highest
	 * {@code typeId} &mdash; not necessarily the runtime one.
	 *
	 * @return the latest entry, or {@code null} if the lineage is empty.
	 */
	public PersistenceTypeDefinition latest();

	/**
	 * The entry flagged as the runtime definition (the structure that matches the currently loaded
	 * {@link Class}), or {@code null} if none has been set.
	 *
	 * @return the runtime definition, or {@code null}.
	 */
	public PersistenceTypeDefinition runtimeDefinition();

	/**
	 * Returns an immutable {@link PersistenceTypeLineageView snapshot} of this lineage's current state.
	 *
	 * @return a snapshot view.
	 */
	public PersistenceTypeLineageView view();



	// mutating logic //

	/**
	 * Registers an additional {@link PersistenceTypeDefinition} for this lineage. The passed definition must
	 * agree with the existing entries on {@link #typeName()} and, if an entry already exists for its
	 * {@code typeId}, on its member structure.
	 *
	 * @param typeDefinition the definition to register.
	 *
	 * @return {@code true} if the definition is a new entry, {@code false} if an equivalent entry was
	 *         already present (only the instance was replaced).
	 *
	 * @throws org.eclipse.serializer.persistence.exceptions.PersistenceException if {@code typeDefinition}
	 *         is incompatible with the lineage.
	 */
	public boolean registerTypeDefinition(PersistenceTypeDefinition typeDefinition);

	/**
	 * Flags the passed definition as this lineage's runtime definition. May only be called once unless the
	 * exact same instance is passed again, in which case the call is a no-op.
	 *
	 * @param runtimeDefinition the definition to mark as runtime definition.
	 *
	 * @return {@code true} if the runtime definition was set, {@code false} if the same instance was
	 *         already registered as runtime definition.
	 *
	 * @throws org.eclipse.serializer.persistence.exceptions.PersistenceException if a different runtime
	 *         definition is already registered, or {@code runtimeDefinition} is incompatible with the
	 *         lineage.
	 */
	public boolean setRuntimeTypeDefinition(PersistenceTypeDefinition runtimeDefinition);



	/**
	 * Creates a new empty {@link Default} lineage for the passed type name and class. Both arguments may be
	 * {@code null}: a {@code null} type name is allowed for types explicitly mapped as having no runtime
	 * counterpart, a {@code null} class for type names that cannot be resolved to a runtime class.
	 *
	 * @param runtimeTypeName the textual type name; may be {@code null}.
	 * @param runtimeType     the runtime class; may be {@code null}.
	 *
	 * @return the newly created lineage.
	 */
	public static PersistenceTypeLineage.Default New(
		final String   runtimeTypeName,
		final Class<?> runtimeType
	)
	{
		return new PersistenceTypeLineage.Default(
			mayNull(runtimeTypeName), // can be null for types explicitly mapped as having no runtime type.
			mayNull(runtimeType)      // can be null if the type name cannot be resolved to a runtime class.
		);
	}

	/**
	 * Default mutable {@link PersistenceTypeLineage}. Stores entries in an ordered hash table; mutating
	 * operations synchronize on the instance.
	 */
	public final class Default implements PersistenceTypeLineage
	{
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final String                                       runtimeTypeName  ;
		final Class<?>                                     runtimeType      ;
		final EqHashTable<Long, PersistenceTypeDefinition> entries          ;
		      PersistenceTypeDefinition                    runtimeDefinition; // initialized effectively final



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final String runtimeTypeName, final Class<?> runtimeType)
		{
			super();
			this.runtimeTypeName = runtimeTypeName  ;
			this.runtimeType     = runtimeType      ;
			this.entries         = EqHashTable.New();
		}



		////////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final String typeName()
		{
			return this.runtimeTypeName;
		}

		@Override
		public final Class<?> type()
		{
			return this.runtimeType;
		}

		@Override
		public final XGettingTable<Long, PersistenceTypeDefinition> entries()
		{
			return this.entries;
		}

		@Override
		public final synchronized PersistenceTypeDefinition runtimeDefinition()
		{
			return this.runtimeDefinition;
		}
		
		@Override
		public final synchronized PersistenceTypeDefinition latest()
		{
			return this.entries.values().peek();
		}
		
		private void validate(final PersistenceTypeDefinition typeDefinition)
		{
			if(this.isValid(typeDefinition))
			{
				return;
			}
			
			throw new PersistenceException("Invalid type definition for type lineage " + this.typeName());
		}
		
		private boolean isValid(final PersistenceTypeDefinition typeDefinition)
		{
			// checking runtimeTypeName is more precise than checking the type, as the prior might not be resolvable.
			if(!XChars.isEqual(this.runtimeTypeName, typeDefinition.runtimeTypeName()))
			{
				return false;
			}
			
			final PersistenceTypeDefinition alreadyRegistered = this.entries.get(typeDefinition.typeId());
			if(alreadyRegistered == null)
			{
				return true;
			}
			
			return PersistenceTypeDescriptionMember.equalStructures(
				typeDefinition.allMembers(),
				alreadyRegistered.allMembers()
			);
		}
		
		@Override
		public synchronized boolean registerTypeDefinition(final PersistenceTypeDefinition typeDefinition)
		{
			this.validate(typeDefinition);
			return this.synchRegisterTypeDefinition(typeDefinition);
		}
		
		private boolean synchRegisterTypeDefinition(final PersistenceTypeDefinition typeDefinition)
		{
			// the passed (and already validated) instance is always registered, ...
			if(this.entries.put(typeDefinition.typeId(), typeDefinition))
			{
				// ... but the return value is only true to indicate an actual additional entry.
				this.entries.keys().sort(Long::compare);
				return true;
			}
			
			// the definition was already there (and in order), only the instance has been replaced.
			return false;
		}
				
		@Override
		public final synchronized boolean setRuntimeTypeDefinition(final PersistenceTypeDefinition runtimeDefinition)
		{
			// false indicates no-op, actual non-viability causes exceptions
			if(!this.synchCheckViability(runtimeDefinition))
			{
				return false;
			}
			
			// normal case: effective final initialization
			this.runtimeDefinition = runtimeDefinition;
			
			// correct behavior of the put has been checked above
			this.entries.put(runtimeDefinition.typeId(), runtimeDefinition);
			
			return true;
		}
		
		private boolean synchCheckViability(final PersistenceTypeDefinition runtimeDefinition)
		{
			if(this.runtimeDefinition != null)
			{
				if(this.runtimeDefinition == runtimeDefinition)
				{
					// no-op call, abort
					return false;
				}
				
				// conflicting call/usage (runtime types and thus definitions are assumed to be immutable for now)
				throw new PersistenceException("Runtime definition already initialized");
			}
			
			if(this.isValid(runtimeDefinition))
			{
				return true;
			}
			
			throw new PersistenceException(
				"Invalid runtime definition for " + this.typeName() + " with type id: " + runtimeDefinition.typeId()
			);
		}
		
		@Override
		public synchronized PersistenceTypeLineageView view()
		{
			return PersistenceTypeLineageView.New(this);
		}
		
		@Override
		public String toString()
		{
			return PersistenceTypeLineage.class.getSimpleName() + " " + this.runtimeTypeName + " " + this.entries.keys();
		}
		
	}

}
