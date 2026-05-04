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
 * Factory for {@link PersistenceUnreachableTypeHandler}s. Pluggable so callers needing custom handler
 * subtypes can supply their own implementation; the bundled {@link Default} delegates to
 * {@link PersistenceUnreachableTypeHandler#New(PersistenceTypeDefinition)}.
 *
 * @param <D> the persistence data type passed through to the {@link PersistenceUnreachableTypeHandler}.
 *
 * @see PersistenceUnreachableTypeHandler
 */
public interface PersistenceUnreachableTypeHandlerCreator<D>
{
	/**
	 * Creates a new {@link PersistenceUnreachableTypeHandler} bound to the passed type definition.
	 *
	 * @param <T>            the type the handler is nominally bound to.
	 * @param typeDefinition the type definition to wrap.
	 *
	 * @return the newly created handler.
	 */
	public <T> PersistenceUnreachableTypeHandler<D, T> createUnreachableTypeHandler(
		PersistenceTypeDefinition typeDefinition
	);



	/**
	 * Creates a new {@link Default} creator.
	 *
	 * @param <D> the persistence data type.
	 *
	 * @return the newly created creator.
	 */
	public static <D> PersistenceUnreachableTypeHandlerCreator<D> New()
	{
		return new PersistenceUnreachableTypeHandlerCreator.Default<>();
	}

	/**
	 * Default {@link PersistenceUnreachableTypeHandlerCreator}: produces stock
	 * {@link PersistenceUnreachableTypeHandler} instances via
	 * {@link PersistenceUnreachableTypeHandler#New(PersistenceTypeDefinition)}.
	 *
	 * @param <D> the persistence data type.
	 */
	public final class Default<D> implements PersistenceUnreachableTypeHandlerCreator<D>
	{
		@Override
		public <T> PersistenceUnreachableTypeHandler<D, T> createUnreachableTypeHandler(
			final PersistenceTypeDefinition typeDefinition
		)
		{
//			XDebug.println("Creating unreachable type handler for " + typeDefinition.toTypeIdentifier());
			return PersistenceUnreachableTypeHandler.New(typeDefinition);
		}
		
	}
	
}
