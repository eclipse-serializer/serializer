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

import static java.lang.System.identityHashCode;
import static org.eclipse.serializer.util.X.notNull;

import org.eclipse.serializer.math.XMath;

/**
 * Walks an object graph and registers every reachable instance with the {@link PersistenceObjectManager}
 * <em>without</em> writing any data. Used to assign object ids ahead of time, e.g. when a graph must be
 * known to the persistence layer before the actual store is triggered.
 * <p>
 * As a {@link PersistenceFunction}, the registerer also serves as the iteration callback used by
 * {@link PersistenceTypeHandler#iterateInstanceReferences} during the walk; a small identity-hash table
 * suppresses repeated visits within a single registration call.
 *
 * @see PersistenceObjectManager
 * @see PersistenceTypeHandlerManager
 */
public interface PersistenceRegisterer extends PersistenceFunction
{
	/**
	 * Registers {@code instance} and recursively every instance reachable from it, assigning object ids
	 * along the way.
	 *
	 * @param instance the root of the sub-graph to register.
	 *
	 * @return always {@code 0L} &mdash; the registerer does not return the assigned id.
	 */
	public long register(Object instance);

	/**
	 * Bulk variant of {@link #register(Object)} for an array of root instances. Returns an array of the
	 * same length filled with zeros (mirroring {@code register}'s return convention).
	 *
	 * @param instances the roots to register.
	 *
	 * @return a zero-filled array of the same length.
	 */
	public long[] registerAll(Object... instances);



	/**
	 * Default {@link PersistenceRegisterer}. Maintains a small identity-hash registry of instances seen
	 * during the current walk so the same instance is not registered twice within one call. The hash
	 * table size is configurable through the constructor for graphs of known shape.
	 */
	public class Default implements PersistenceRegisterer
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceObjectManager<?>      objectManager     ;
		private final PersistenceTypeHandlerManager<?> typeHandlerManager;

		private final Entry[]                          oidsSlots         ;
		private final int                              oidsModulo        ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
			final PersistenceObjectManager<?>      objectManager,
			final PersistenceTypeHandlerManager<?> typeManager
		)
		{
			super();
			this.objectManager      = notNull(objectManager);
			this.typeHandlerManager = notNull(typeManager)  ;
			this.oidsSlots          = new Entry[1]          ;
			this.oidsModulo         = 0                     ;
		}

		public Default(
			final PersistenceObjectManager<?>      objectManager,
			final PersistenceTypeHandlerManager<?> typeManager  ,
			final int                              hashRange
		)
		{
			super();
			this.objectManager      = notNull(objectManager);
			this.typeHandlerManager = notNull(typeManager  );
			this.oidsSlots          = new Entry[XMath.pow2BoundCapped(hashRange)];
			this.oidsModulo         = this.oidsSlots.length - 1;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public <T> long apply(final T instance)
		{
			// abort on null reference or already handled instance
			if(instance == null || this.isRegisteredLocal(instance))
			{
				return 0L;
			}

			// ensure type handler (or fail if type is not persistable) before ensuring oid
			@SuppressWarnings("unchecked") // cast type safety guaranteed by management logic
			final PersistenceTypeHandler<?, Object> handler =
				(PersistenceTypeHandler<?, Object>)this.typeHandlerManager.ensureTypeHandler(instance.getClass())
			;

			// ensure and register oid for that instance
			this.registerLocal(instance);
			this.objectManager.ensureObjectId(instance);

			// iterate references
			handler.iterateInstanceReferences(instance, this);

			return 0L; // registerer does not need to return the oid
		}

//		@Override
//		public void clearRegistered()
//		{
//			this.clearRegistry();
//		}
//
//		@Override
//		public void registerSkip(final Object instance)
//		{
//			this.registerLocal(instance);
//		}



		///////////////////////////////////////////////////////////////////////////
		// OID registry map //
		/////////////////////

		private boolean isRegisteredLocal(final Object instance)
		{
			for(Entry e = this.oidsSlots[identityHashCode(instance) & this.oidsModulo]; e != null; e = e.link)
			{
				if(e.ref == instance)
				{
					return true;
				}
			}
			return false;
		}

		private void registerLocal(final Object instance)
		{
			final int index;
			Entry e;
			if((e = this.oidsSlots[index = identityHashCode(instance) & this.oidsModulo]) == null)
			{
				this.oidsSlots[index] = new Entry(instance);
				return;
			}
			do
			{
				if(e.ref == instance)
				{
					return;
				}
			}
			while((e = e.link) != null);
			this.oidsSlots[index] = new Entry(instance, this.oidsSlots[index]);
		}

//		private void clearRegistry()
//		{
//			final Entry[] slots = this.oidsSlots;
//			for(int i = 0; i < slots.length; i++)
//			{
//				slots[i] = null;
//			}
//		}

		private static final class Entry
		{
			final Object ref;
			Entry link;

			Entry(final Object instance)
			{
				super();
				this.ref  = instance;
				this.link = null;
			}

			Entry(final Object instance, final Entry link)
			{
				super();
				this.ref  = instance;
				this.link = link;
			}

		}

		///////////////////////////////////////////////////////////////////////////
		// End OID registry map //
		/////////////////////////

		public static class Creator implements PersistenceRegisterer.Creator
		{
			@Override
			public PersistenceRegisterer createRegisterer(
				final PersistenceObjectManager<?>      objectManager,
				final PersistenceTypeHandlerManager<?> typeManager
			)
			{
				return new PersistenceRegisterer.Default(objectManager, typeManager);

			}
		}

		@Override
		public long register(final Object instance)
		{
			return this.apply(instance);
		}

		@Override
		public long[] registerAll(final Object... instances)
		{
			final long[] oids = new long[instances.length]; // implicit null check
			for(int i = 0; i < instances.length; i++)
			{
				oids[i] = this.apply(instances[i]);
			}
			return oids;
		}

	}

	/**
	 * Pluggable factory for {@link PersistenceRegisterer} instances. Stored on the foundation so callers
	 * needing a custom registerer subtype can swap in their own implementation.
	 */
	public interface Creator
	{
		/**
		 * Creates a new registerer wired up against the passed {@link PersistenceObjectManager} and
		 * {@link PersistenceTypeHandlerManager}.
		 *
		 * @param objectManager the object manager that will assign object ids.
		 * @param typeManager   the type handler manager that will resolve handlers per type.
		 *
		 * @return the newly created registerer.
		 */
		public PersistenceRegisterer createRegisterer(
			PersistenceObjectManager<?>      objectManager,
			PersistenceTypeHandlerManager<?> typeManager
		);
	}
}
