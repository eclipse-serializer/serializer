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
import org.eclipse.serializer.collections.types.XImmutableSequence;

public interface PersistenceTypeDescriptionMemberFieldGenericComplex
extends PersistenceTypeDescriptionMemberFieldGenericVariableLength
{
	public XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members();

	
	
	@Override
	public default boolean equalsDescription(final PersistenceTypeDescriptionMember other)
	{
		// does NOT call #equalsStructure to avoid redundant member iteration
		return PersistenceTypeDescriptionMember.equalTypeAndNameAndQualifier(this, other)
			&& other instanceof PersistenceTypeDescriptionMemberFieldGenericComplex
			&& PersistenceTypeDescriptionMember.equalDescriptions(
				this.members(),
				((PersistenceTypeDescriptionMemberFieldGenericComplex)other).members()
			)
		;
	}
	
	@Override
	public default boolean equalsStructure(final PersistenceTypeDescriptionMember other)
	{
		return PersistenceTypeDescriptionMemberFieldGenericVariableLength.super.equalsStructure(other)
			&& other instanceof PersistenceTypeDescriptionMemberFieldGenericComplex
			&& PersistenceTypeDescriptionMember.equalStructures(
				this.members(),
				((PersistenceTypeDescriptionMemberFieldGenericComplex)other).members()
			)
		;
	}
	
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMemberFieldGenericComplex m1,
		final PersistenceTypeDescriptionMemberFieldGenericComplex m2
	)
	{
		return PersistenceTypeDescriptionMember.equalDescription(m1, m2);
	}
	
	public static boolean equalStructure(
		final PersistenceTypeDescriptionMemberFieldGenericComplex m1,
		final PersistenceTypeDescriptionMemberFieldGenericComplex m2
	)
	{
		return PersistenceTypeDescriptionMember.equalStructure(m1, m2);
	}
	
	@Override
	public default PersistenceTypeDefinitionMemberFieldGenericComplex createDefinitionMember(
		final PersistenceTypeDefinitionMemberCreator creator
	)
	{
		return creator.createDefinitionMember(this);
	}
	
	public static PersistenceTypeDescriptionMemberFieldGenericComplex New(
		final String                                                         name                   ,
		final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members                ,
		final long                                                           persistentMinimumLength,
		final long                                                           persistentMaximumLength
	)
	{
		return New(null, name, members, persistentMinimumLength, persistentMaximumLength);
	}
	
	public static PersistenceTypeDescriptionMemberFieldGenericComplex New(
		final String                                                         qualifier              ,
		final String                                                         name                   ,
		final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members                ,
		final long                                                           persistentMinimumLength,
		final long                                                           persistentMaximumLength
	)
	{
		return new PersistenceTypeDescriptionMemberFieldGenericComplex.Default(
			 mayNull(qualifier)              ,
			 notNull(name)                   ,
			 notNull(members)                ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}

	public class Default
	extends PersistenceTypeDescriptionMemberFieldGenericVariableLength.Default
	implements PersistenceTypeDescriptionMemberFieldGenericComplex
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final XImmutableSequence<PersistenceTypeDescriptionMemberFieldGeneric> members;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final String                                                         qualifier              ,
			final String                                                         name                   ,
			final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members                ,
			final long                                                           persistentMinimumLength,
			final long                                                           persistentMaximumLength
		)
		{
			super(
				PersistenceTypeDictionary.Symbols.TYPE_COMPLEX,
				qualifier,
				name,
				PersistenceTypeDescriptionMember.determineHasReferences(members),
				persistentMinimumLength,
				persistentMaximumLength
			);
			this.members = members.immure();
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members()
		{
			return this.members;
		}

		@Override
		public void assembleTypeDescription(final PersistenceTypeDescriptionMemberAppender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}

	}

}
