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

public interface PersistenceUnreachableTypeHandlerCreator<D>
{
	public <T> PersistenceUnreachableTypeHandler<D, T> createUnreachableTypeHandler(
		PersistenceTypeDefinition typeDefinition
	);
	
	
	
	public static <D> PersistenceUnreachableTypeHandlerCreator<D> New()
	{
		return new PersistenceUnreachableTypeHandlerCreator.Default<>();
	}
	
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
