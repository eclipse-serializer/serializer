package org.eclipse.serializer.persistence.types;

import org.eclipse.serializer.collections.Set_long;

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

import org.eclipse.serializer.collections.types.XGettingTable;
import org.eclipse.serializer.hashing.HashStatistics;
import org.eclipse.serializer.util.Cloneable;

/**
 * A registry type for biunique associations of arbitrary objects with object ids. Implements
 * {@link PersistenceSwizzlingLookup}, so it serves as the runtime structure that translates between persisted
 * object ids and live instances during loading and storing. Implementations typically hold instances by
 * {@link java.lang.ref.WeakReference} so that registered objects do not prevent garbage collection.
 * <p>
 * In addition to plain {@code (objectId, object)} entries, the registry has a separate notion of
 * <em>constants</em>: instances that must survive {@link #clear()} and {@link #truncate()} because they are
 * structurally part of the persistent graph (e.g. JDK constants). Use {@link #registerConstant(long, Object)}
 * to add them; {@link #clearAll()} and {@link #truncateAll()} remove them too.
 * <p>
 * The default implementation is {@link DefaultObjectRegistry}; the {@link #New()} factory returns it.
 *
 * @see PersistenceSwizzlingLookup
 * @see PersistenceObjectManager
 * @see DefaultObjectRegistry
 */
public interface PersistenceObjectRegistry extends PersistenceSwizzlingLookup, Cloneable<PersistenceObjectRegistry>
{
	/* funny find:
	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4990451
	 * welcome to this user code class
	 */
	
	/**
	 * Useful for {@link PersistenceContextDispatcher}.
	 * @return A Clone of this instance as described in {@link Cloneable}.
	 */
	@Override
	public PersistenceObjectRegistry Clone();
	
	// entry querying //

	@Override
	public long lookupObjectId(Object object);

	@Override
	public Object lookupObject(long objectId);

	/**
	 * Checks whether the passed {@code (objectId, object)} pair is consistent with what is registered.
	 * Returns {@code true} if the mapping matches an existing entry, {@code false} if neither side is
	 * registered.
	 *
	 * @param objectId the candidate object id.
	 * @param object   the candidate instance.
	 *
	 * @return {@code true} if the mapping is registered, {@code false} if it is unknown.
	 */
	public boolean isValid(long objectId, Object object);

	/**
	 * Throws if the passed {@code (objectId, object)} pair conflicts with a registered entry. Returns
	 * normally if the pair is either registered exactly as passed or completely absent.
	 *
	 * @param objectId the candidate object id.
	 * @param object   the candidate instance.
	 */
	public void validate(long objectId, Object object);

	/**
	 * Whether any entry &mdash; live or cleared &mdash; exists for the passed object id.
	 *
	 * @param objectId the object id to check.
	 *
	 * @return {@code true} if some entry is registered for {@code objectId}.
	 */
	public boolean containsObjectId(long objectId);

	/**
	 * Whether the passed object id has a live (non-{@code null}) registered instance. Returns {@code false}
	 * if the entry exists but its weak reference has been cleared.
	 *
	 * @param objectId the object id to check.
	 *
	 * @return {@code true} if a live instance is registered for {@code objectId}.
	 */
    public boolean containsLiveObject(long objectId);

	/**
	 * Whether the passed object id has an entry whose weak reference has already been cleared by the GC.
	 *
	 * @param objectId the object id to check.
	 *
	 * @return {@code true} if the entry exists but its instance has been collected.
	 */
    public boolean containsClearedObject(long objectId);

	/**
	 * Iterates every live registered entry, invoking {@code acceptor} once per {@code (objectId, instance)}
	 * pair.
	 *
	 * @param <A>      the acceptor type, returned for fluent chaining.
	 * @param acceptor the acceptor to invoke for each entry.
	 *
	 * @return the same acceptor that was passed in.
	 */
	public <A extends PersistenceAcceptor> A iterateEntries(A acceptor);

	// general querying //

	/**
	 * The current number of registered entries, including those whose weak reference has been cleared but
	 * not yet purged.
	 *
	 * @return the registry size.
	 */
	public long size();

	/**
	 * Whether the registry currently has no entries.
	 *
	 * @return {@code true} if {@link #size()} is zero.
	 */
	public boolean isEmpty();

	/**
	 * The hash range used for the internal hash tables (a bit mask, i.e. {@code hashLength - 1}).
	 *
	 * @return the hash range.
	 */
	public int hashRange();

	/**
	 * The configured hash density &mdash; the average number of entries per hash table slot at which a
	 * rebuild is triggered.
	 *
	 * @return the hash density.
	 */
	public float hashDensity();

	/**
	 * The configured minimum capacity below which the registry will not shrink.
	 *
	 * @return the minimum capacity.
	 */
	public long minimumCapacity();

	/**
	 * @return the current size potential before a (maybe costly) rebuild becomes necessary.
	 */
	public long capacity();
	
	/**
	 * Sets the hash density, possibly triggering a rebuild of the internal hash tables.
	 *
	 * @param hashDensity the new hash density (reasonable values are within {@code [0.75; 2.00]}).
	 *
	 * @return {@code true} if a rebuild of internal storage structures was performed.
	 */
	public boolean setHashDensity(float hashDensity);

	/**
	 * Sets the minimum capacity, possibly triggering a rebuild of the internal hash tables.
	 *
	 * @param minimumCapacity the new minimum capacity.
	 *
	 * @return {@code true} if a rebuild of internal storage structures was performed.
	 */
	public boolean setMinimumCapacity(long minimumCapacity);

	/**
	 * Sets hash density and minimum capacity in one operation, performing at most one rebuild.
	 *
	 * @param hashDensity     the new hash density.
	 * @param minimumCapacity the new minimum capacity.
	 *
	 * @return {@code true} if a rebuild of internal storage structures was performed.
	 */
	public boolean setConfiguration(float hashDensity, long  minimumCapacity);
	
	/**
	 * Makes sure the internal storage structure is prepared to provide a {@link #capacity()} of at least
	 * the passed capacity value.
	 * 
	 * @param capacity the new minimum capacity
	 * @return whether a rebuild of internal storage structures was necessary.
	 */
	public boolean ensureCapacity(long capacity);
		
	// registering //

	/**
	 * Registers the passed mapping. Returns {@code true} if it was newly added, {@code false} if the same
	 * mapping was already present.
	 *
	 * @param objectId the object id.
	 * @param object   the instance to bind to {@code objectId}.
	 *
	 * @return {@code true} if the mapping was newly registered, {@code false} if it was already present.
	 *
	 * @throws org.eclipse.serializer.persistence.exceptions.PersistenceExceptionConsistency if either side
	 *         is already bound to a different counterpart.
	 */
	public boolean registerObject(long objectId, Object object);

	/**
	 * Optional variant of {@link #registerObject(long, Object)}: if some other live instance is already
	 * registered for {@code objectId}, that instance is returned and the passed {@code object} is not
	 * registered. Otherwise {@code object} is registered and returned.
	 *
	 * @param objectId the object id.
	 * @param object   the candidate instance.
	 *
	 * @return the instance now associated with {@code objectId} (either the previously registered one or
	 *         the passed candidate).
	 */
	public Object optionalRegisterObject(long objectId, Object object);

	/**
	 * Registers the passed mapping as a <em>constant</em> &mdash; an entry that survives {@link #clear()}
	 * and {@link #truncate()} (but not {@link #clearAll()} / {@link #truncateAll()}).
	 *
	 * @param objectId the object id.
	 * @param constant the constant instance to bind to {@code objectId}.
	 *
	 * @return {@code true} if the mapping was newly registered, {@code false} if it was already present.
	 */
	public boolean registerConstant(long objectId, Object constant);

	/**
	 * Consolidate internal data structures, e.g. by removing orphan entries and empty hash chains.
	 * Depending on the implementation and the size of the registry, this can take a considerable amount of time.
	 * 
	 * @return whether a rebuild was required.
	 */
	public boolean consolidate();

	/**
	 * Clears all entries except those that are essential for a correctly executed program (e.g. constants). <br>
	 * Clearing means to leave the current capacity as it is and just to actually clear its entries.
	 * <p>
	 * NOTE:<br>
	 * This method is currently only intended to be used for testing since calling it can cause inconsistencies
	 * if there still exist uncleared lazy references.
	 */
	public void clear();

	/**
	 * Clears all entries, including those that are essential for a correctly executed program (e.g. constants),
	 * effectively leaving a completely empty registry.<br>
	 * Clearing means to leave the current capacity as it is and just to actually clear its entries.
	 * <p>
	 * NOTE:<br>
	 * This method is currently only intended to be used for testing since calling it can cause inconsistencies
	 * if there still exist uncleared lazy references.
	 */
	public void clearAll();
	
	/**
	 * Truncates all entries except those that are essential for a correctly executed program (e.g. constants).
	 * Truncating means to quickly empty the registry by reinitializing the internal storage structures with a
	 * new and minimal capacity.
	 * <p>
	 * NOTE:<br>
	 * This method is currently only intended to be used for testing since calling it can cause inconsistencies
	 * if there still exist uncleared lazy references.
	 */
	public void truncate();

	/**
	 * Truncates all entries, including those that are essential for a correctly executed program (e.g. constants),
	 * effectively leaving a completely empty registry.<br>
	 * Truncating means to quickly empty the registry by reinitializing the internal storage structures with a
	 * new and minimal capacity.
	 * <p>
	 * NOTE:<br>
	 * This method is currently only intended to be used for testing since calling it can cause inconsistencies
	 * if there still exist uncleared lazy references.
	 */
	public void truncateAll();
	
	// removing logic is not viable except for testing purposes, which can be done implementation-specific.

	/**
	 * Returns hash statistics for the internal storage structures, keyed by an implementation-specific name
	 * (e.g. one entry per internal hash table). Useful for diagnostics.
	 *
	 * @return the hash statistics by table name.
	 */
	public XGettingTable<String, ? extends HashStatistics> createHashStatistics();
	
	/**
	 * 
	 * @param processor the object id processor
	 * @return <code>true</code> on success, <code>false</code> if lock rejected.
	 */
	public boolean processLiveObjectIds(ObjectIdsProcessor processor);

	// for bulk processing of objectIds. Most efficient way for server mode, inefficient for embedded mode.
	/**
	 * 
	 * @param objectIdsBaseSet the ids to select
	 * @return null if lock rejected
	 */
	public Set_long selectLiveObjectIds(Set_long objectIdsBaseSet);
	
	/**
	 * Cleanup object registry.
	 */
	public default void cleanUp()
	{
		//by default do nothing
		return;
	}

	/**
	 * A monotonically increasing counter of new {@code (objectId, instance)} association insertions.
	 * The value changes ONLY when a new association is inserted (registering an object or constant
	 * that creates a new entry, including re-binding an objectId whose previous weak entry was
	 * cleared). Lookups, updates of existing entries and entry removals do NOT change it. Readers
	 * compare snapshots for equality; the absolute value carries no meaning.
	 * <p>
	 * The storage garbage collector uses this to detect registrations that happened after its
	 * live-OID mark seed ran but before the sweep &mdash; such registrations must re-arm the seed, or
	 * entities referenced only from a freshly registered instance's persisted binary (e.g. an
	 * unloaded lazy reference's target) would be swept while the registered instance survives.
	 * <p>
	 * The default implementation returns a constant {@code 0}, meaning "registrations are never
	 * observable". Implementations that can gain entries at runtime MUST override this, otherwise
	 * the storage GC's mid-cycle registration re-seed is disabled for them.
	 *
	 * @return the current registration version.
	 */
	public default long registrationVersion()
	{
		return 0L;
	}

	/**
	 * Creates a new empty {@link DefaultObjectRegistry} with default hash density and capacity.
	 *
	 * @return the newly created registry.
	 */
	public static DefaultObjectRegistry New()
	{
		return DefaultObjectRegistry.New();
	}
	
}
