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

import java.util.function.Supplier;

import org.eclipse.serializer.chars.XChars;

/**
 * Single named entry in the persistent root set: a textual {@code identifier} bound to an instance via a
 * {@link Supplier}. The supplier indirection lets the resolver register entries before the static class
 * fields they reference are initialized, side-stepping class-loading order issues.
 * <p>
 * An entry whose supplier is {@code null} represents a <em>removed</em> root &mdash; e.g. one that an
 * outdated dictionary still references but that the user has explicitly mapped to deletion via the
 * refactoring layer. {@link #isRemoved()} reports this state.
 *
 * @see PersistenceRootResolver
 * @see PersistenceRootResolverProvider
 */
public interface PersistenceRootEntry
{
	/**
	 * The textual identifier under which this root is registered.
	 *
	 * @return the identifier.
	 */
	public String identifier();

	/**
	 * Resolves and returns the current root instance by invoking the entry's supplier. Returns {@code null}
	 * for removed entries.
	 *
	 * @return the root instance, or {@code null} if the entry has been removed.
	 */
	public Object instance();

	/**
	 * Whether this entry has been marked as removed (i.e. its supplier is {@code null}).
	 *
	 * @return {@code true} if the entry is removed.
	 */
	public boolean isRemoved();



	/**
	 * Creates a new {@link Default} root entry. A {@code null} supplier marks the entry as
	 * {@linkplain #isRemoved() removed}; the identifier itself must not be {@code null}.
	 *
	 * @param identifier       the identifier; must not be {@code null}.
	 * @param instanceSupplier the supplier; may be {@code null} to mark the entry as removed.
	 *
	 * @return the newly created entry.
	 */
	public static PersistenceRootEntry New(final String identifier, final Supplier<?> instanceSupplier)
	{
		return new PersistenceRootEntry.Default(
			notNull(identifier)      ,
			mayNull(instanceSupplier) // null means deleted
		);
	}

	/**
	 * Default {@link PersistenceRootEntry}: stores the identifier and the supplier as-is and resolves the
	 * instance lazily on every {@link #instance()} call.
	 */
	public final class Default implements PersistenceRootEntry
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String      identifier      ;
		private final Supplier<?> instanceSupplier;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final String identifier, final Supplier<?> instanceSupplier)
		{
			super();
			this.identifier       = identifier      ;
			this.instanceSupplier = instanceSupplier;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final String identifier()
		{
			return this.identifier;
		}
		
		@Override
		public boolean isRemoved()
		{
			return this.instanceSupplier == null;
		}

		@Override
		public final Object instance()
		{
//			XDebug.println("Calling supplier.get() from " + XChars.systemString(this));
			
			return this.instanceSupplier != null
				? this.instanceSupplier.get()
				: null
			;
		}
		
		@Override
		public String toString()
		{
			return this.identifier + ": " + XChars.systemString(this.instance());
		}
		
	}
	
	
	/**
	 * Factory for {@link PersistenceRootEntry} instances. Pluggable so callers needing custom entry types
	 * can swap in their own; defaults to {@link PersistenceRootEntry#New(String, Supplier)} via the
	 * method reference.
	 */
	@FunctionalInterface
	public interface Provider
	{
		/**
		 * Creates a new {@link PersistenceRootEntry} for the passed identifier and supplier.
		 *
		 * @param identifier       the identifier.
		 * @param instanceSupplier the supplier; may be {@code null} to mark the entry as removed.
		 *
		 * @return the newly created entry.
		 */
		public PersistenceRootEntry provideRootEntry(String identifier, Supplier<?> instanceSupplier);
	}
	
}
