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

import java.util.function.Supplier;

import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.reference.Reference;
import org.eclipse.serializer.reflect.XReflect;

/**
 * Mutable reference to the application's user-defined root instance. The reference itself is what the
 * persistence dictionary stores under the well-known root identifier ({@link Persistence#rootIdentifier()});
 * its target can be replaced at runtime via {@link #setRoot(Object)} or {@link #setRootSupplier(Supplier)}.
 * <p>
 * The {@link Supplier} indirection is what lets the persistence layer register a stable reference object
 * before the user has supplied (or even constructed) the actual root instance &mdash; useful e.g. when the
 * root is loaded from disk after the foundation has been built.
 *
 * @see PersistenceRootReferencing
 * @see PersistenceRootReferenceProvider
 */
public interface PersistenceRootReference extends PersistenceRootReferencing, Reference<Object>
{
	@Override
	public Object get();

	@Override
	public <F extends PersistenceFunction> F iterate(F iterator);

	@Override
	public default void set(final Object newRoot)
	{
		this.setRoot(newRoot);
	}

	/**
	 * Replaces the currently referenced root with {@code newRoot} by wrapping it in a constant supplier.
	 *
	 * @param newRoot the new root instance; may be {@code null}.
	 *
	 * @return the previously referenced root (resolved via the previous supplier).
	 *
	 * @throws PersistenceException if {@code newRoot} is a value instance.
	 */
	public default Object setRoot(final Object newRoot)
	{
		validateRootInstance(newRoot);

		return this.setRootSupplier(() ->
			newRoot
		);
	}

	/**
	 * Guarantees that {@code rootInstance} can serve as an explicitly set root.
	 * <p>
	 * A value instance cannot: setting a root means the persisted state is applied to the very
	 * instance the application holds, which is impossible without identity, so the persisted state
	 * would be dropped without any indication. Rejecting the root is the only way to not lose it
	 * silently.
	 * <p>
	 * This applies to an <i>explicitly set</i> root only. A root that is merely loaded needs no state
	 * applied to it, so a persisted value root resolves like any other instance.
	 *
	 * @param rootInstance the instance to be validated; may be {@code null}.
	 *
	 * @throws PersistenceException if {@code rootInstance} is a value instance.
	 */
	public static void validateRootInstance(final Object rootInstance)
	{
		if(!XReflect.isValueInstance(rootInstance))
		{
			return;
		}

		throw new PersistenceException(
			"A value instance cannot be used as a root: " + rootInstance.getClass().getName()
			+ " has no identity, so its persisted state could not be applied to it on loading."
			+ " Use an identity type as root and hold the value instance in one of its fields."
		);
	}

	/**
	 * Replaces the supplier that resolves the root instance. Use this overload when the root cannot be
	 * eagerly created (e.g. because doing so would trigger class-loading order issues).
	 *
	 * @param rootSupplier the new supplier; may be {@code null}.
	 *
	 * @return the previously referenced root (resolved via the previous supplier).
	 */
	public Object setRootSupplier(Supplier<?> rootSupplier);



	/**
	 * Creates a new empty {@link Default} root reference. The reference initially resolves to {@code null}
	 * and can be populated later via {@link #setRoot(Object)} or {@link #setRootSupplier(Supplier)}.
	 *
	 * @return the newly created reference.
	 */
	public static PersistenceRootReference New()
	{
		return New(null);
	}

	/**
	 * Creates a new {@link Default} root reference initialized to {@code root}. Equivalent to
	 * {@link #New()} followed by {@link #setRoot(Object)}.
	 *
	 * @param root the initial root instance; may be {@code null}.
	 *
	 * @return the newly created reference.
	 */
	public static PersistenceRootReference New(final Object root)
	{
		final PersistenceRootReference.Default instance = new PersistenceRootReference.Default(null);
		instance.setRoot(root);

		return instance;
	}

	/**
	 * Creates a new {@link Default} root reference initialized with the passed supplier.
	 *
	 * @param rootSupplier the initial root supplier; may be {@code null}.
	 *
	 * @return the newly created reference.
	 */
	public static PersistenceRootReference New(final Supplier<?> rootSupplier)
	{
		return new PersistenceRootReference.Default(
			mayNull(rootSupplier)
		);
	}

	/**
	 * Default {@link PersistenceRootReference}: stores a single mutable {@link Supplier} and resolves the
	 * root lazily on every {@link #get()} call.
	 */
	public final class Default implements PersistenceRootReference
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		// there is no problem that cannot be solved through one more level of indirection
		private Supplier<?> rootSupplier;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		public Default(final Supplier<?> rootSupplier)
		
		{
			super();
			this.rootSupplier = rootSupplier;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final Object get()
		{
			return this.rootSupplier != null
				? this.rootSupplier.get()
				: null
			;
		}
		
		@Override
		public final Object setRootSupplier(final Supplier<?> rootSupplier)
		{
			final Object currentRoot = this.get();
			this.rootSupplier = rootSupplier;
			
			return currentRoot;
		}
		
		@Override
		public final <F extends PersistenceFunction> F iterate(final F iterator)
		{
			final Object currentRoot = this.get();
			if(currentRoot == null)
			{
				return iterator;
			}
			iterator.apply(currentRoot);
			
			return iterator;
		}
		
	}
	
}
