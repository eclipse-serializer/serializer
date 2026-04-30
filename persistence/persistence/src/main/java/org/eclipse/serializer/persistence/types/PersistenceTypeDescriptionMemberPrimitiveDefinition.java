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
import static org.eclipse.serializer.math.XMath.positive;

import java.util.Objects;

import org.eclipse.serializer.persistence.exceptions.PersistenceException;

/**
 * A non-instance member entry that records the persistent bit-layout of a Java primitive type.
 * <p>
 * In the type dictionary every Java primitive ({@code byte}, {@code short}, {@code int}, {@code long},
 * {@code float}, {@code double}, {@code char}, {@code boolean}) is represented as a type whose <i>only</i>
 * member is a primitive-definition entry like {@code primitive 32}, {@code primitive 64}, etc. The fixed
 * persistent length captured here is what tells legacy reading code how many units to consume for that
 * primitive on this platform / persister (typically bytes for the binary persister).
 * <p>
 * Because primitive definitions do not have a name or qualifier, {@link #typeName()}, {@link #qualifier()}
 * and {@link #name()} return {@code null}, and {@link #identifier()} falls back to
 * {@link #primitiveDefinition()}.
 *
 * @see PersistenceTypeDescriptionMember
 */
public interface PersistenceTypeDescriptionMemberPrimitiveDefinition extends PersistenceTypeDescriptionMember
{
	/**
	 * The textual primitive-definition string (e.g. {@code "primitive 32"}) recorded for this entry.
	 *
	 * @return the primitive-definition string.
	 */
	public String primitiveDefinition();
	
	@Override
	public default boolean isInstanceMember()
	{
		return false;
	}
	
	@Override
	public default String identifier()
	{
		return this.primitiveDefinition();
	}

	
	@Override
	public default boolean equalsDescription(final PersistenceTypeDescriptionMember member)
	{
		return member instanceof PersistenceTypeDescriptionMemberPrimitiveDefinition
			&& equalDescription(this, (PersistenceTypeDescriptionMemberPrimitiveDefinition)member)
		;
	}

	@Override
	public default boolean equalsLayout(final PersistenceTypeDescriptionMember other)
	{
		// primitive-definition members carry a null typeName; identity lives in primitiveDefinition().
		return other instanceof PersistenceTypeDescriptionMemberPrimitiveDefinition
			&& Objects.equals(
				this.primitiveDefinition(),
				((PersistenceTypeDescriptionMemberPrimitiveDefinition)other).primitiveDefinition()
			)
		;
	}
	
	/**
	 * Tests whether two primitive-definition entries record the same {@link #primitiveDefinition()}
	 * string.
	 *
	 * @param m1 the first primitive-definition entry.
	 * @param m2 the second primitive-definition entry.
	 *
	 * @return {@code true} if both record the same primitive definition.
	 */
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMemberPrimitiveDefinition m1,
		final PersistenceTypeDescriptionMemberPrimitiveDefinition m2
	)
	{
		return m1.primitiveDefinition().equals(m2.primitiveDefinition());
	}
	
	@Override
	public default PersistenceTypeDefinitionMemberPrimitiveDefinition createDefinitionMember(
		final PersistenceTypeDefinitionMemberCreator creator
	)
	{
		return creator.createDefinitionMember(this);
	}

	
	/**
	 * Creates a primitive-definition entry.
	 *
	 * @param primitiveDefinition the textual definition (e.g. {@code "primitive 32"}); must not be {@code null}.
	 * @param persistentLength    the fixed persistent length in this persister's unit; must be positive.
	 *
	 * @return a new primitive-definition entry.
	 */
	public static PersistenceTypeDescriptionMemberPrimitiveDefinition New(
		final String primitiveDefinition,
		final long   persistentLength
	)
	{
		return new PersistenceTypeDefinitionMemberPrimitiveDefinition.Default(
			 notNull(primitiveDefinition),
			positive(persistentLength)
		);
	}

	public class Default
	implements PersistenceTypeDescriptionMemberPrimitiveDefinition
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String primitiveDefinition;
		private final long   persistentLength   ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final String primitiveDefinition,
			final long   persistentLength
		)
		{
			super();
			this.primitiveDefinition =  notNull(primitiveDefinition);
			this.persistentLength    = positive(persistentLength)   ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public String primitiveDefinition()
		{
			return this.primitiveDefinition;
		}

		@Override
		public void assembleTypeDescription(final PersistenceTypeDescriptionMemberAppender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}
		
		@Override
		public long persistentMinimumLength()
		{
			return this.persistentLength;
		}
		
		@Override
		public long persistentMaximumLength()
		{
			return this.persistentLength;
		}

		@Override
		public boolean isValidPersistentLength(final long persistentLength)
		{
			return persistentLength == this.persistentLength;
		}
		
		@Override
		public boolean equalsStructure(final PersistenceTypeDescriptionMember other)
		{
			// the check for equal (namely null) typename and name is still valid here.
			return PersistenceTypeDescriptionMemberPrimitiveDefinition.super.equalsStructure(other)
				&& other instanceof PersistenceTypeDescriptionMemberPrimitiveDefinition
				&& Objects.equals(
					this.primitiveDefinition(),
					((PersistenceTypeDescriptionMemberPrimitiveDefinition)other).primitiveDefinition()
				)
			;
		}

		@Override
		public String typeName()
		{
			return null;
		}
		
		@Override
		public String qualifier()
		{
			return null;
		}
		
		@Override
		public String name()
		{
			return null;
		}

		@Override
		public final boolean isReference()
		{
			return false;
		}

		@Override
		public final boolean isPrimitive()
		{
			return false;
		}

		@Override
		public final boolean isPrimitiveDefinition()
		{
			return true;
		}
		
		@Override
		public final boolean isEnumConstant()
		{
			return false;
		}

		@Override
		public final boolean hasReferences()
		{
			return false;
		}

		@Override
		public void validatePersistentLength(final long persistentLength)
		{
			if(this.isValidPersistentLength(persistentLength))
			{
				return;
			}
			throw new PersistenceException(
				"Invalid persistent length: " + persistentLength
				+ " != " + this.persistentLength + "."
			);
		}
		
		@Override
		public final boolean isInstanceMember()
		{
			return false;
		}

	}

}
