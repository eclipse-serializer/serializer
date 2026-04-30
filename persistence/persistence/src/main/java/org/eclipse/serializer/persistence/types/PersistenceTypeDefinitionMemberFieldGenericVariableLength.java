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

/**
 * Runtime-bound counterpart to {@link PersistenceTypeDescriptionMemberFieldGenericVariableLength}.
 * Variable-length generic fields have no Java {@link Class} counterpart, so {@link #type()} always
 * returns {@code null}. Two convenience factory shortcuts &mdash; {@link #Bytes} and {@link #Chars}
 * &mdash; produce the typical {@code [byte]} and {@code [char]} entries used by binary handlers.
 */
public interface PersistenceTypeDefinitionMemberFieldGenericVariableLength
extends PersistenceTypeDefinitionMemberFieldGeneric, PersistenceTypeDescriptionMemberFieldGenericVariableLength
{
	@Override
	public default PersistenceTypeDefinitionMemberFieldGenericVariableLength copyForName(final String name)
	{
		return this.copyForName(this.qualifier(), name);
	}

	@Override
	public PersistenceTypeDefinitionMemberFieldGenericVariableLength copyForName(String qualifier, String name);



	/**
	 * Lifts an existing description into a definition by re-using its attributes verbatim.
	 *
	 * @param description the description to lift.
	 *
	 * @return a new definition member.
	 */
	public static PersistenceTypeDefinitionMemberFieldGenericVariableLength.Default New(
		final PersistenceTypeDescriptionMemberFieldGenericVariableLength description
	)
	{
		return PersistenceTypeDefinitionMemberFieldGenericVariableLength.New(
			description.typeName()               ,
			description.name()                   ,
			description.persistentMinimumLength(),
			description.persistentMaximumLength()
		);
	}
	
	/**
	 * Convenience overload of {@link #New(String, String, String, long, long)} without qualifier.
	 *
	 * @param typeName                the textual type name.
	 * @param name                    the simple name.
	 * @param persistentMinimumLength the persistent length lower bound; must be positive.
	 * @param persistentMaximumLength the persistent length upper bound; must be positive.
	 *
	 * @return a new definition member.
	 */
	public static PersistenceTypeDefinitionMemberFieldGenericVariableLength.Default New(
		final String typeName               ,
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return New(typeName, null, name, persistentMinimumLength, persistentMaximumLength);
	}

	/**
	 * Creates a runtime-bound variable-length generic field definition (non-reference, non-primitive).
	 *
	 * @param typeName                the textual type name.
	 * @param qualifier               the optional qualifier; may be {@code null}.
	 * @param name                    the simple name.
	 * @param persistentMinimumLength the persistent length lower bound; must be positive.
	 * @param persistentMaximumLength the persistent length upper bound; must be positive.
	 *
	 * @return a new definition member.
	 */
	public static PersistenceTypeDefinitionMemberFieldGenericVariableLength.Default New(
		final String typeName               ,
		final String qualifier              ,
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return new PersistenceTypeDefinitionMemberFieldGenericVariableLength.Default(
			 notNull(typeName)               ,
			 mayNull(qualifier)              ,
			 notNull(name)                   ,
			         false                   ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}

	/**
	 * Shortcut for a {@code [byte]} variable-length entry without qualifier. See {@link #Bytes(String,
	 * String, long, long)}.
	 *
	 * @param name                    the simple name.
	 * @param persistentMinimumLength the persistent length lower bound; must be positive.
	 * @param persistentMaximumLength the persistent length upper bound; must be positive.
	 *
	 * @return a new {@code [byte]} definition member.
	 */
	public static PersistenceTypeDefinitionMemberFieldGenericVariableLength.Default Bytes(
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return Bytes(
			null                   ,
			name                   ,
			persistentMinimumLength,
			persistentMaximumLength
		);
	}

	/**
	 * Shortcut for a {@code [byte]} variable-length entry &mdash; sets {@code typeName} to
	 * {@link PersistenceTypeDictionary.Symbols#TYPE_BYTES}.
	 *
	 * @param qualifier               the optional qualifier; may be {@code null}.
	 * @param name                    the simple name.
	 * @param persistentMinimumLength the persistent length lower bound; must be positive.
	 * @param persistentMaximumLength the persistent length upper bound; must be positive.
	 *
	 * @return a new {@code [byte]} definition member.
	 */
	public static PersistenceTypeDefinitionMemberFieldGenericVariableLength.Default Bytes(
		final String qualifier              ,
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return New(
			PersistenceTypeDictionary.Symbols.TYPE_BYTES,
			qualifier,
			name,
			persistentMinimumLength,
			persistentMaximumLength
		);
	}

	/**
	 * Shortcut for a {@code [char]} variable-length entry without qualifier. See {@link #Chars(String,
	 * String, long, long)}.
	 *
	 * @param name                    the simple name.
	 * @param persistentMinimumLength the persistent length lower bound; must be positive.
	 * @param persistentMaximumLength the persistent length upper bound; must be positive.
	 *
	 * @return a new {@code [char]} definition member.
	 */
	public static PersistenceTypeDefinitionMemberFieldGenericVariableLength.Default Chars(
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return Chars(null, name, persistentMinimumLength, persistentMaximumLength);
	}

	/**
	 * Shortcut for a {@code [char]} variable-length entry &mdash; sets {@code typeName} to
	 * {@link PersistenceTypeDictionary.Symbols#TYPE_CHARS}.
	 *
	 * @param qualifier               the optional qualifier; may be {@code null}.
	 * @param name                    the simple name.
	 * @param persistentMinimumLength the persistent length lower bound; must be positive.
	 * @param persistentMaximumLength the persistent length upper bound; must be positive.
	 *
	 * @return a new {@code [char]} definition member.
	 */
	public static PersistenceTypeDefinitionMemberFieldGenericVariableLength.Default Chars(
		final String qualifier              ,
		final String name                   ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return New(
			PersistenceTypeDictionary.Symbols.TYPE_CHARS,
			qualifier              ,
			name                   ,
			persistentMinimumLength,
			persistentMaximumLength
		);
	}



	public class Default
	extends PersistenceTypeDescriptionMemberFieldGenericVariableLength.Default
	implements PersistenceTypeDefinitionMemberFieldGenericVariableLength
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final String  typeName               ,
			final String  qualifier              ,
			final String  name                   ,
			final boolean hasReferences          ,
			final long    persistentMinimumLength,
			final long    persistentMaximumLength
		)
		{
			super(typeName, qualifier, name, hasReferences, persistentMinimumLength, persistentMaximumLength);
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
		public PersistenceTypeDefinitionMemberFieldGenericVariableLength copyForName(
			final String qualifier,
			final String name
		)
		{
			return new PersistenceTypeDefinitionMemberFieldGenericVariableLength.Default(
				this.typeName(),
				qualifier,
				name,
				this.isReference(),
				this.persistentMinimumLength(),
				this.persistentMaximumLength()
			);
		}

	}

}
