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

import org.eclipse.serializer.reference.ObjectSwizzling;
import org.eclipse.serializer.reference.Swizzling;

/**
 * Per-instance callback handed to {@link PersistenceTypeHandler}s during storing. Combines the storer-side
 * "apply this referenced instance" pattern with the {@link Storer} commit hooks, and lets handlers narrow
 * the dispatch to a specific type handler when polymorphism would otherwise require an extra lookup.
 * <p>
 * The four {@code apply}/{@code applyEager} variants form a 2&times;2 matrix: lazy vs. eager (whether
 * already-stored instances are revisited) and natural vs. local (whether the type handler is auto-resolved
 * from the runtime type or supplied explicitly). Lazy is the default; eager is needed e.g. for composition-
 * pattern aggregates whose lifecycle is tied to their owner.
 *
 * @param <D> the persistence data type produced by the type handlers.
 *
 * @see PersistenceTypeHandler
 * @see Storer
 * @see Persister
 */
public interface PersistenceStoreHandler<D> extends PersistenceFunction, Storer
{
	/**
	 * The "natural" way of handling an instance as defined by the implementation.
	 *
	 * @param <T> the type of the instance
	 * @param instance the instance to store
	 * @return the assigned object id
	 */
	@Override
	public <T> long apply(T instance);

	/**
	 * A way to signal to the implementation that the passed instance is supposed to be handled eagerly,
	 * meaning it shall be handled even if the handling implementation does not deem it necessary.<br>
	 * This is needed, for example, to store composition pattern instances without breaking OOP encapsulation concepts.
	 *
	 * @param <T> the type of the instance
	 * @param instance the instance to store
	 * @return the assigned object id
	 */
	public <T> long applyEager(T instance);

	/**
	 * Variant of {@link #apply(Object)} that uses the supplied {@code localTypeHandler} instead of looking
	 * one up by runtime type. Useful when the calling handler already has the right handler at hand.
	 *
	 * @param <T>              the instance type.
	 * @param instance         the instance to store.
	 * @param localTypeHandler the type handler to use.
	 *
	 * @return the assigned object id.
	 */
	public <T> long apply(T instance, PersistenceTypeHandler<D, T> localTypeHandler);

	/**
	 * Eager variant of {@link #apply(Object, PersistenceTypeHandler)}: handles the instance even if the
	 * implementation would otherwise consider it already up-to-date.
	 *
	 * @param <T>              the instance type.
	 * @param instance         the instance to store.
	 * @param localTypeHandler the type handler to use.
	 *
	 * @return the assigned object id.
	 */
	public <T> long applyEager(T instance, PersistenceTypeHandler<D, T> localTypeHandler);

	/**
	 * Variant of {@link #apply(Object)} for an instance the caller already knows an object id for: the
	 * instance is unchanged since it was stored under {@code knownObjectId}, and the entity of that id
	 * still exists in the target. Referencing it is then equivalent to storing it again, and avoids the
	 * superseded copy an identity-less instance would leave behind - it has no object registry entry,
	 * so {@link #apply(Object)} assigns it a fresh object id every single time.
	 * <p>
	 * Passing an id is a statement about the caller's own state, so the implementation treats it like
	 * any other reference it writes without storing the referent: it reports the id for the target to
	 * validate, together with the instance, so a target that repairs a missing entity can re-store it.
	 * <p>
	 * The id is ignored - and the instance applied - when it is {@link Swizzling#notFoundId()}, when the
	 * instance is {@literal null}, and whenever {@link #isEagerStoring()} holds, an eager store having
	 * to reach every instance regardless of what the caller knows about it.
	 *
	 * @param <T>           the instance type.
	 * @param instance      the instance to reference, may be {@literal null}.
	 * @param knownObjectId the object id the instance was last stored under,
	 *                      {@link Swizzling#notFoundId()} if none is known.
	 *
	 * @return the object id to reference the instance by.
	 */
	public default <T> long applyKnown(final T instance, final long knownObjectId)
	{
		return this.apply(instance);
	}

	/**
	 * Whether this handler stores every encountered instance instead of skipping already known ones.
	 * <p>
	 * Relevant to handlers that may skip storing a referent: skipping is only valid while the storing
	 * logic is lazy, since an eager store's purpose is to reach everything the referent references,
	 * whether the referent itself needs storing or not.
	 * <p>
	 * The default implementation returns {@literal false}.
	 *
	 * @return whether every encountered instance is stored.
	 */
	public default boolean isEagerStoring()
	{
		return false;
	}

	@Override
	public void registerCommitListener(PersistenceCommitListener listener);

	/**
	 * Reports an object id that this handler writes into the data as a reference without the referenced
	 * instance being available to be stored in the same commit (e.g. an unloaded {@link org.eclipse.serializer.reference.Lazy}
	 * reference's cached id). Implementations may record the id to have its existence validated by the
	 * persistence target before the data referencing it is committed.
	 *
	 * @param objectId the referenced object id that is trusted to already exist in the target.
	 */
	public default void noteTrustedReference(final long objectId)
	{
		// no-op by default
	}

	/**
	 * The retriever used to resolve referenced ids when handlers need to read pre-existing data while
	 * storing.
	 *
	 * @return the object retriever.
	 */
	public ObjectSwizzling getObjectRetriever();

	/**
	 * The {@link Persister} backing this store operation, exposed for handlers that need access to the
	 * full read/write facade.
	 *
	 * @return the persister.
	 */
	public Persister getPersister();

}
