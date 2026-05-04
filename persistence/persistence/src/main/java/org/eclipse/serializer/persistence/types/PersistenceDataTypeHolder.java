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

/**
 * Carries the {@link Class} literal for the persistence data type {@code D} used by a foundation or
 * subsystem. Foundations need this to bridge the gap between Java's erased generics and runtime decisions
 * that depend on the concrete data type (e.g. byte[] vs. some structured payload type).
 *
 * @param <D> the persistence data type.
 */
public interface PersistenceDataTypeHolder<D>
{
	/**
	 * The {@link Class} literal of the persistence data type {@code D}.
	 *
	 * @return the data type class.
	 */
	public Class<D> dataType();


	/**
	 * Default {@link PersistenceDataTypeHolder} that simply stores the supplied class literal and returns
	 * it from {@link #dataType()}.
	 *
	 * @param <D> the persistence data type.
	 */
	public class Default<D> implements PersistenceDataTypeHolder<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Class<D> dataType;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default(final Class<D> dataType)
		{
			super();
			this.dataType = dataType;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final Class<D> dataType()
		{
			return this.dataType;
		}
		
	}
	
}
