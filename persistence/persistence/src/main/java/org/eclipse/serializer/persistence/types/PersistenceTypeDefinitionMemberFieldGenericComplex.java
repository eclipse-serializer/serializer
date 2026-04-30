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
import static org.eclipse.serializer.util.X.notNull;
import static org.eclipse.serializer.math.XMath.positive;

import org.eclipse.serializer.collections.types.XGettingSequence;

/**
 * Runtime-bound counterpart to {@link PersistenceTypeDescriptionMemberFieldGenericComplex}. Like its
 * description counterpart, it carries a nested {@link #members()} sequence describing one element of
 * the complex field; {@link #type()} is always {@code null} since complex generic fields don't have a
 * direct {@link Class} counterpart.
 */
public interface PersistenceTypeDefinitionMemberFieldGenericComplex
extends PersistenceTypeDefinitionMemberFieldGenericVariableLength, PersistenceTypeDescriptionMemberFieldGenericComplex
{
	@Override
	public default PersistenceTypeDefinitionMemberFieldGenericComplex copyForName(final String name)
	{
		return this.copyForName(this.qualifier(), name);
	}

	@Override
	public PersistenceTypeDefinitionMemberFieldGenericComplex copyForName(String qualifier, String name);



	/**
	 * Lifts an existing description into a definition by re-using its attributes verbatim.
	 *
	 * @param description the description to lift.
	 *
	 * @return a new definition member.
	 */
	public static PersistenceTypeDefinitionMemberFieldGenericComplex New(
		final PersistenceTypeDescriptionMemberFieldGenericComplex description
	)
	{
		return New(
			description.qualifier()              ,
			description.name()                   ,
			description.members()                ,
			description.persistentMinimumLength(),
			description.persistentMaximumLength()
		);
	}
	
	/**
	 * Convenience overload of {@link #New(String, String, XGettingSequence, long, long)} without
	 * qualifier.
	 *
	 * @param name                    the simple name.
	 * @param members                 the nested members; must not be {@code null}.
	 * @param persistentMinimumLength the persistent length lower bound; must be positive.
	 * @param persistentMaximumLength the persistent length upper bound; must be positive.
	 *
	 * @return a new definition member.
	 */
	public static PersistenceTypeDefinitionMemberFieldGenericComplex New(
		final String                                                        name                   ,
		final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members                ,
		final long                                                          persistentMinimumLength,
		final long                                                          persistentMaximumLength
	)
	{
		return New(null, name, members, persistentMinimumLength, persistentMaximumLength);
	}

	/**
	 * Creates a runtime-bound complex generic field definition.
	 *
	 * @param qualifier               the optional qualifier; may be {@code null}.
	 * @param name                    the simple name.
	 * @param members                 the nested members; must not be {@code null}.
	 * @param persistentMinimumLength the persistent length lower bound; must be positive.
	 * @param persistentMaximumLength the persistent length upper bound; must be positive.
	 *
	 * @return a new definition member.
	 */
	public static PersistenceTypeDefinitionMemberFieldGenericComplex New(
		final String                                                        qualifier              ,
		final String                                                        name                   ,
		final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members                ,
		final long                                                          persistentMinimumLength,
		final long                                                          persistentMaximumLength
	)
	{
		return new PersistenceTypeDefinitionMemberFieldGenericComplex.Default(
			 mayNull(qualifier)              ,
			 notNull(name)                   ,
			 notNull(members)                ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}
	
	public class Default
	extends PersistenceTypeDescriptionMemberFieldGenericComplex.Default
	implements PersistenceTypeDefinitionMemberFieldGenericComplex
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final String                                                        qualifier              ,
			final String                                                        name                   ,
			final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members                ,
			final long                                                          persistentMinimumLength,
			final long                                                          persistentMaximumLength
		)
		{
			super(
				qualifier              ,
				name                   ,
				members                ,
				persistentMinimumLength,
				persistentMaximumLength
			);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		@Override
		public final Class<?> type()
		{
			return null;
		}

		@Override
		public PersistenceTypeDefinitionMemberFieldGenericComplex copyForName(
			final String qualifier,
			final String name
		)
		{
			return new PersistenceTypeDefinitionMemberFieldGenericComplex.Default(
				qualifier,
				name,
				this.members(),
				this.persistentMinimumLength(),
				this.persistentMaximumLength()
			);
		}

	}

}
