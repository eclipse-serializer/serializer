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

	@Override
	public void registerCommitListener(PersistenceCommitListener listener);

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
