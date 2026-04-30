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
import org.eclipse.serializer.chars.XChars;
import org.eclipse.serializer.math.XMath;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;


/**
 * Runtime-bound counterpart to {@link PersistenceTypeDescriptionMemberPrimitiveDefinition}.
 * <p>
 * Primitive definitions describe the bit-layout of a primitive type at the type level &mdash; the
 * Java primitive class itself is recorded on the owning {@link PersistenceTypeDefinition}, not on
 * this member &mdash; so {@link #type()} always returns {@code null}. The {@link Default} inner class
 * additionally provides the canonical textual encodings used in the dictionary
 * ({@link Default#assemblePrimitiveDefinition(Class)}) and the inverse parser
 * ({@link Default#resolvePrimitiveDefinition(String)}).
 */
public interface PersistenceTypeDefinitionMemberPrimitiveDefinition
extends PersistenceTypeDescriptionMemberPrimitiveDefinition, PersistenceTypeDefinitionMember
{
	@Override
	public default String identifier()
	{
		return this.primitiveDefinition();
	}


	/**
	 * Lifts an existing description into a definition. Verifies that the description's minimum and
	 * maximum persistent lengths agree (primitive definitions are fixed-length).
	 *
	 * @param description the description to lift.
	 *
	 * @return a new definition member.
	 */
	public static PersistenceTypeDefinitionMemberPrimitiveDefinition New(
		final PersistenceTypeDescriptionMemberPrimitiveDefinition description
	)
	{
		final long persistentLength = XMath.equal(
			description.persistentMinimumLength(),
			description.persistentMaximumLength()
		);
		
		return new PersistenceTypeDefinitionMemberPrimitiveDefinition.Default(
			description.primitiveDefinition(),
			persistentLength
		);
	}
	
	/**
	 * Creates a definition member for the passed Java primitive type. The textual primitive definition
	 * is produced by {@link Default#assemblePrimitiveDefinition(Class)}.
	 *
	 * @param primitiveType    the primitive {@link Class} (e.g. {@code int.class}).
	 * @param persistentLength the fixed persistent length.
	 *
	 * @return a new definition member.
	 */
	public static PersistenceTypeDefinitionMemberPrimitiveDefinition New(
		final Class<?> primitiveType   ,
		final long     persistentLength
	)
	{
		return new PersistenceTypeDefinitionMemberPrimitiveDefinition.Default(
			Default.assemblePrimitiveDefinition(primitiveType),
			persistentLength
		);
	}

	public class Default
	extends PersistenceTypeDescriptionMemberPrimitiveDefinition.Default
	implements PersistenceTypeDefinitionMemberPrimitiveDefinition
	{
		// CHECKSTYLE.OFF: ConstantName: literals and type names are intentionally unchanged
		
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		private static final String
			_bit             = " bit"            ,
			_integer_signed  = " integer signed" ,
			_integer_unicode = " integer unicode",
			_decimal_IEEE754 = " decimal IEEE754",
			_boolean         = " boolean"
		;

		private static final char[]
			DEFINITION_byte    = (Byte     .SIZE + _bit + _integer_signed ).toCharArray(),
			DEFINITION_boolean = (Byte     .SIZE + _bit + _boolean        ).toCharArray(),
			DEFINITION_short   = (Short    .SIZE + _bit + _integer_signed ).toCharArray(),
			DEFINITION_char    = (Character.SIZE + _bit + _integer_unicode).toCharArray(),
			DEFINITION_int     = (Integer  .SIZE + _bit + _integer_signed ).toCharArray(),
			DEFINITION_float   = (Float    .SIZE + _bit + _decimal_IEEE754).toCharArray(),
			DEFINITION_long    = (Long     .SIZE + _bit + _integer_signed ).toCharArray(),
			DEFINITION_double  = (Double   .SIZE + _bit + _decimal_IEEE754).toCharArray(),
			DEFINITION_void    = void.class.getSimpleName().toCharArray()
		;

		// CHECKSTYLE.ON: ConstantName
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		/**
		 * Appends the canonical textual primitive definition for the passed primitive class to
		 * {@code vc}, e.g. {@code "32 bit integer signed"} for {@code int.class}. Used during type
		 * dictionary assembly. Throws {@link IllegalArgumentException} for non-primitive classes.
		 *
		 * @param vc            the {@link VarString} to append to.
		 * @param primitiveType the primitive class.
		 *
		 * @return the same {@link VarString}, for fluent chaining.
		 */
		public static final VarString assemblePrimitiveDefinition(final VarString vc, final Class<?> primitiveType)
		{
			if(primitiveType == byte.class)
			{
				return vc.add(DEFINITION_byte);
			}
			if(primitiveType == boolean.class)
			{
				return vc.add(DEFINITION_boolean);
			}
			if(primitiveType == short.class)
			{
				return vc.add(DEFINITION_short);
			}
			if(primitiveType == char.class)
			{
				return vc.add(DEFINITION_char);
			}
			if(primitiveType == int.class)
			{
				return vc.add(DEFINITION_int);
			}
			if(primitiveType == float.class)
			{
				return vc.add(DEFINITION_float);
			}
			if(primitiveType == long.class)
			{
				return vc.add(DEFINITION_long);
			}
			if(primitiveType == double.class)
			{
				return vc.add(DEFINITION_double);
			}
			if(primitiveType == void.class)
			{
				return vc.add(DEFINITION_void);
			}
			throw new IllegalArgumentException();
		}
		
		/**
		 * Returns the canonical textual primitive definition for the passed primitive class as a
		 * {@link String}. See {@link #assemblePrimitiveDefinition(VarString, Class)}.
		 *
		 * @param primitiveType the primitive class.
		 *
		 * @return the canonical textual definition.
		 */
		public static final String assemblePrimitiveDefinition(final Class<?> primitiveType)
		{
			return assemblePrimitiveDefinition(VarString.New(), primitiveType).toString();
		}

		/**
		 * Inverse of {@link #assemblePrimitiveDefinition(Class)}: parses a canonical textual primitive
		 * definition back to its primitive {@link Class}. Whitespace around the input is trimmed.
		 *
		 * @param primitiveDefinition the textual definition.
		 *
		 * @return the matching primitive class.
		 *
		 * @throws PersistenceException if the input is not a known canonical primitive definition.
		 */
		public static final Class<?> resolvePrimitiveDefinition(final String primitiveDefinition)
		{
			// trim string just in case, will be very fast / won't create a new instance if unnecessary
			final String trimmed = primitiveDefinition.trim();

			if(XChars.equals(trimmed, DEFINITION_byte, 0))
			{
				return byte.class;
			}
			if(XChars.equals(trimmed, DEFINITION_boolean, 0))
			{
				return boolean.class;
			}
			if(XChars.equals(trimmed, DEFINITION_short, 0))
			{
				return short.class;
			}
			if(XChars.equals(trimmed, DEFINITION_char, 0))
			{
				return char.class;
			}
			if(XChars.equals(trimmed, DEFINITION_int, 0))
			{
				return int.class;
			}
			if(XChars.equals(trimmed, DEFINITION_float, 0))
			{
				return float.class;
			}
			if(XChars.equals(trimmed, DEFINITION_long, 0))
			{
				return long.class;
			}
			if(XChars.equals(trimmed, DEFINITION_double, 0))
			{
				return double.class;
			}
			if(XChars.equals(trimmed, DEFINITION_void, 0))
			{
				return void.class;
			}
			throw new PersistenceException("Unknown primitive definition: " + trimmed);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final String primitiveDefinition,
			final long   persistentLength
		)
		{
			super(primitiveDefinition, persistentLength);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		@Override
		public final Class<?> type()
		{
			// a definition does not have a type of a member field. The defined primitive type is in the owner type.
			return null;
		}
		
	}

}
