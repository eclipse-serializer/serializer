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

/**
 * Decides what should happen when type handlers cannot be matched against the persistent
 * {@link PersistenceTypeDictionary} &mdash; i.e. when the runtime structure of a class differs from every
 * historical entry in the dictionary, and no legacy mapping bridges the gap.
 * <p>
 * Two ready-made strategies are bundled:
 * <ul>
 * <li>{@link Failing} &mdash; throws a {@link PersistenceException} listing the unmatched types. The default
 * for production setups: silently loading data with mismatched structure would corrupt the graph.</li>
 * <li>{@link NoOp} &mdash; ignores mismatches. Useful for tooling or read-only inspection that explicitly
 * tolerates them.</li>
 * </ul>
 *
 * @param <D> the persistence data type passed through to the {@link PersistenceTypeHandler}.
 *
 * @see PersistenceTypeDictionary
 * @see PersistenceTypeHandler
 */
public interface PersistenceTypeMismatchValidator<D>
{
	/**
	 * Inspects the passed set of type handlers that could not be matched against {@code typeDictionary}
	 * and reacts according to the strategy. May throw to refuse the operation or return normally to allow
	 * it.
	 *
	 * @param typeDictionary          the dictionary against which matching was attempted.
	 * @param unmatchableTypeHandlers the type handlers that could not be matched; may be empty.
	 */
	public void validateTypeMismatches(
		PersistenceTypeDictionary                  typeDictionary         ,
		XGettingEnum<PersistenceTypeHandler<D, ?>> unmatchableTypeHandlers
	);



	/**
	 * Creates a new {@link Failing} validator that throws on any unmatched type.
	 *
	 * @param <D> the persistence data type.
	 *
	 * @return the new failing validator.
	 */
	public static <D> PersistenceTypeMismatchValidator.Failing<D> Failing()
	{
		return new PersistenceTypeMismatchValidator.Failing<>();
	}

	/**
	 * Creates a new {@link NoOp} validator that silently allows any unmatched type.
	 *
	 * @param <D> the persistence data type.
	 *
	 * @return the new no-op validator.
	 */
	public static <D> PersistenceTypeMismatchValidator.NoOp<D> NoOp()
	{
		return new PersistenceTypeMismatchValidator.NoOp<>();
	}

	/**
	 * {@link PersistenceTypeMismatchValidator} that throws a {@link PersistenceException} listing the
	 * unmatched types. Empty input is treated as "no mismatch" and returns normally.
	 *
	 * @param <D> the persistence data type.
	 */
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

	/**
	 * {@link PersistenceTypeMismatchValidator} that silently accepts every unmatched type. Intended for
	 * tooling and read-only inspection.
	 *
	 * @param <D> the persistence data type.
	 */
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
