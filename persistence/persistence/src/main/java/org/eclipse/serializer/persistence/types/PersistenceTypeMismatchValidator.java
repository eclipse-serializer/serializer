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

import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;

public interface PersistenceTypeMismatchValidator<D>
{
	public void validateTypeMismatches(
		PersistenceTypeDictionary                  typeDictionary         ,
		XGettingEnum<PersistenceTypeHandler<D, ?>> unmatchableTypeHandlers
	);
	
	
	
	public static <D> PersistenceTypeMismatchValidator.Failing<D> Failing()
	{
		return new PersistenceTypeMismatchValidator.Failing<>();
	}
	
	public static <D> PersistenceTypeMismatchValidator.NoOp<D> NoOp()
	{
		return new PersistenceTypeMismatchValidator.NoOp<>();
	}
	
	public final class Failing<D> implements PersistenceTypeMismatchValidator<D>
	{

		@Override
		public void validateTypeMismatches(
			final PersistenceTypeDictionary                  typeDictionary         ,
			final XGettingEnum<PersistenceTypeHandler<D, ?>> unmatchableTypeHandlers
		)
		{
			if(unmatchableTypeHandlers.isEmpty())
			{
				return;
			}
			
			final VarString vs = VarString.New("[");
			unmatchableTypeHandlers.iterate(th -> vs.add(',').add(th.type().getName()));
			vs.deleteLast().setLast(']');
			
			throw new PersistenceException("Persistence type definition mismatch for the following types: " + vs);
			
		}
	}
	
	public final class NoOp<D> implements PersistenceTypeMismatchValidator<D>
	{

		@Override
		public void validateTypeMismatches(
			final PersistenceTypeDictionary                  typeDictionary         ,
			final XGettingEnum<PersistenceTypeHandler<D, ?>> unmatchableTypeHandlers
		)
		{
			// no-op
		}
	}
	
}
