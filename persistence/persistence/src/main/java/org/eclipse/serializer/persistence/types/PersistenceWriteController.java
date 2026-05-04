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

import org.eclipse.serializer.afs.types.WriteController;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionStoringDisabled;


/**
 * Gates whether the persistence layer is allowed to perform write/store operations. Specializes the AFS
 * {@link WriteController} (which gates raw file-system writes) with the higher-level "storing" predicate
 * &mdash; storing is by default tied to writability, but can be disabled independently for setups that
 * keep the underlying files writable while temporarily refusing to persist new graph state.
 * <p>
 * Three ready-made implementations are bundled:
 * <ul>
 * <li>{@link Wrapper} &mdash; lifts an existing {@link WriteController} to a {@link PersistenceWriteController},
 * tying storing to writability.</li>
 * <li>{@link Enabled} &mdash; permits everything.</li>
 * <li>{@link Disabled} &mdash; refuses everything.</li>
 * </ul>
 *
 * @see WriteController
 * @see PersistenceExceptionStoringDisabled
 */
@FunctionalInterface
public interface PersistenceWriteController extends WriteController
{
	/**
	 * Throws {@link PersistenceExceptionStoringDisabled} if {@link #isStoringEnabled()} returns
	 * {@code false}; returns normally otherwise. The persistence layer calls this before every store
	 * operation.
	 *
	 * @throws PersistenceExceptionStoringDisabled if storing is disabled.
	 */
	public default void validateIsStoringEnabled()
	{
		if(this.isStoringEnabled())
		{
			return;
		}

		throw new PersistenceExceptionStoringDisabled();
	}

	/**
	 * Whether storing new graph state is currently permitted. Defaults to delegating to {@link #isWritable()}.
	 *
	 * @return {@code true} if storing is permitted.
	 */
	public default boolean isStoringEnabled()
	{
		return this.isWritable();
	}


	/**
	 * Wraps a generic {@link WriteController} as a {@link PersistenceWriteController}, tying storing to
	 * writability.
	 *
	 * @param writeController the wrapped write controller; must not be {@code null}.
	 *
	 * @return the wrapping persistence controller.
	 */
	public static PersistenceWriteController Wrap(final WriteController writeController)
	{
		return new PersistenceWriteController.Wrapper(
			notNull(writeController)
		);
	}

	/**
	 * Wrapping {@link PersistenceWriteController}: delegates writability to a wrapped {@link WriteController}
	 * and ties {@link #isStoringEnabled()} to it.
	 */
	public final class Wrapper implements PersistenceWriteController
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final WriteController writeController;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Wrapper(final WriteController writeController)
		{
			super();
			this.writeController = writeController;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final void validateIsWritable()
		{
			this.writeController.validateIsWritable();
		}
		
		@Override
		public final boolean isWritable()
		{
			return this.writeController.isWritable();
		}
		
		@Override
		public final boolean isStoringEnabled()
		{
			return this.isWritable();
		}
				
	}
	
	/**
	 * Creates a new {@link Enabled} controller that permits every write and store operation.
	 *
	 * @return the new enabled controller.
	 */
	public static PersistenceWriteController Enabled()
	{
		// Singleton is (usually) an anti pattern.
		return new PersistenceWriteController.Enabled();
	}

	/**
	 * {@link PersistenceWriteController} that permits every operation: {@link #validateIsWritable()} and
	 * {@link #validateIsStoringEnabled()} are no-ops, {@link #isWritable()} and
	 * {@link #isStoringEnabled()} always return {@code true}.
	 */
	public final class Enabled implements PersistenceWriteController
	{
		Enabled()
		{
			super();
		}
		
		@Override
		public final void validateIsWritable()
		{
			// no-op
		}
		
		@Override
		public final void validateIsStoringEnabled()
		{
			// no-op
		}

		@Override
		public final boolean isWritable()
		{
			return true;
		}
		
		@Override
		public final boolean isStoringEnabled()
		{
			return true;
		}
		
	}
	
	/**
	 * Creates a new {@link Disabled} controller that refuses every write and store operation.
	 *
	 * @return the new disabled controller.
	 */
	public static PersistenceWriteController Disabled()
	{
		// Singleton is (usually) an anti pattern.
		return new PersistenceWriteController.Disabled();
	}

	/**
	 * {@link PersistenceWriteController} that refuses every operation: {@link #isWritable()} and
	 * {@link #isStoringEnabled()} always return {@code false}, so the inherited
	 * {@code validate*} methods throw on every call.
	 */
	public final class Disabled implements PersistenceWriteController
	{
		Disabled()
		{
			super();
		}

		@Override
		public final boolean isWritable()
		{
			return false;
		}
		
		@Override
		public final boolean isStoringEnabled()
		{
			return false;
		}
		
	}
	
}
