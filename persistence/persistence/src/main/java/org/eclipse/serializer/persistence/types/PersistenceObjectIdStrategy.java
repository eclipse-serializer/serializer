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
import org.eclipse.serializer.chars.XParsing;
import org.eclipse.serializer.collections.types.XReference;
import org.eclipse.serializer.exceptions.ParsingException;
import org.eclipse.serializer.util.X;

/**
 * Persistable description of how new object ids are generated. A strategy is the configuration-side
 * counterpart of a {@link PersistenceObjectIdProvider}: it carries only the parameters needed to set up a
 * provider (e.g. the starting id), is round-tripped to and from a textual form by
 * {@link PersistenceIdStrategyStringConverter}, and is turned into a live, mutable provider via
 * {@link #createObjectIdProvider()}.
 * <p>
 * The textual form is what allows two processes opening the same persistence layer to agree on id generation
 * without sharing runtime state. {@link #strategyTypeNameObjectId()} returns the discriminator string used in
 * that form (see {@link Transient#typeName()}, {@link None#typeName()}); it is intentionally decoupled from
 * the Java class name so strategy classes can be renamed without breaking persisted configurations.
 * <p>
 * Two ready-made strategies are bundled:
 * <ul>
 * <li>{@link Transient} &mdash; pairs with {@link PersistenceObjectIdProvider.Transient} for in-memory id
 * generation starting at a configurable value.</li>
 * <li>{@link None} &mdash; pairs with {@link PersistenceObjectIdProvider.Failing} for read-only setups that
 * must not mint new object ids.</li>
 * </ul>
 *
 * @see PersistenceObjectIdProvider
 * @see PersistenceTypeIdStrategy
 * @see PersistenceIdStrategy
 * @see PersistenceIdStrategyStringConverter
 */
public interface PersistenceObjectIdStrategy
{
	/**
	 * Creates a fresh, mutable {@link PersistenceObjectIdProvider} configured according to this strategy. Every
	 * call yields an independent provider instance, so the strategy itself can be reused as immutable
	 * configuration.
	 *
	 * @return the newly created provider.
	 */
	public PersistenceObjectIdProvider createObjectIdProvider();

	/**
	 * The discriminator name written into the persisted id-strategy string. Used by
	 * {@link PersistenceIdStrategyStringConverter} to dispatch parsing back to the correct strategy
	 * implementation and intentionally decoupled from the Java class name so strategy classes can be renamed
	 * without breaking persisted configurations.
	 *
	 * @return this strategy's persisted discriminator name.
	 */
	public String strategyTypeNameObjectId();



	/**
	 * Creates a {@link Transient} strategy starting at {@link Persistence#defaultStartObjectId()}, which
	 * places the object-id range above the type-id range so the two never overlap.
	 *
	 * @return the new transient strategy.
	 */
	public static PersistenceObjectIdStrategy.Transient Transient()
	{
		return new PersistenceObjectIdStrategy.Transient(Persistence.defaultStartObjectId());
	}

	/**
	 * Creates a {@link Transient} strategy whose providers start handing out ids one above the passed value.
	 * The starting id is validated via {@link Persistence#validateObjectId(long)}.
	 *
	 * @param startingObjectId the starting value; the first generated object id is one above this.
	 *
	 * @return the new transient strategy.
	 */
	public static PersistenceObjectIdStrategy.Transient Transient(final long startingObjectId)
	{
		return new PersistenceObjectIdStrategy.Transient(Persistence.validateObjectId(startingObjectId));
	}

	/**
	 * Strategy that produces {@link PersistenceObjectIdProvider.Transient} providers from a configured
	 * starting id. Round-tripped through the textual form as {@code Transient(<startingObjectId>)}.
	 */
	public final class Transient implements PersistenceObjectIdStrategy
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		/**
		 * The discriminator written into the persisted id-strategy string for {@link Transient}. Intentionally
		 * a fixed literal rather than {@link Class#getSimpleName()} so the class can be renamed without
		 * breaking existing persisted configurations.
		 *
		 * @return the literal {@code "Transient"}.
		 */
		public static String typeName()
		{
			// intentionally not the class name since it must stay the same, even if the class should get renamed.
			return "Transient";
		}

		/**
		 * The character opening the textual parameter list (an opening parenthesis).
		 *
		 * @return the opening character.
		 */
		public static char openingCharacter()
		{
			return '(';
		}

		/**
		 * The character closing the textual parameter list (a closing parenthesis).
		 *
		 * @return the closing character.
		 */
		public static char closingCharacter()
		{
			return ')';
		}

		/**
		 * Appends the textual form of the passed {@link Transient} strategy to {@code vs}, in the shape
		 * {@code Transient(<startingObjectId>)}.
		 *
		 * @param vs         the buffer to append to.
		 * @param idStrategy the strategy to assemble.
		 */
		public static void assemble(final VarString vs, final PersistenceObjectIdStrategy.Transient idStrategy)
		{
			vs
			.add(PersistenceObjectIdStrategy.Transient.typeName())
			.add(openingCharacter()).add(idStrategy.startingObjectId()).add(closingCharacter())
			;
		}

		/**
		 * Parses a {@code Transient(<startingObjectId>)} textual form into a {@link Transient} strategy. An
		 * empty parameter list (i.e. {@code Transient()}) yields the default starting id, otherwise the
		 * parameter is parsed as a {@code long} and validated.
		 *
		 * @param typeIdStrategyContent the textual form to parse.
		 *
		 * @return the parsed strategy.
		 *
		 * @throws ParsingException if the input does not match the expected shape.
		 */
		public static PersistenceObjectIdStrategy.Transient parse(final String typeIdStrategyContent)
		{
			PersistenceIdStrategyStringConverter.validateIdStrategyName(
				PersistenceObjectIdStrategy.Transient.class,
				typeName(),
				typeIdStrategyContent
			);
			
			final char[] input  = XChars.readChars(typeIdStrategyContent);
			final int    iBound = input.length;
			
			final XReference<String> valueString = X.Reference(null);
			
			int i = typeName().length();
			i = XParsing.skipWhiteSpaces(input, i, iBound);
			i = XParsing.checkCharacter(input, i, openingCharacter(), typeName());
			i = XParsing.parseToSimpleTerminator(input, i, iBound, closingCharacter(), valueString);
			i = XParsing.skipWhiteSpaces(input, i, iBound);
			
			if(i != iBound)
			{
				throw new ParsingException("Invalid trailing content at index " + i);
			}
			
			return valueString.get().isEmpty()
				? PersistenceObjectIdStrategy.Transient()
				: PersistenceObjectIdStrategy.Transient(Long.parseLong(valueString.get()))
			;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final long startingObjectId;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Transient(final long startingObjectId)
		{
			super();
			this.startingObjectId = startingObjectId;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		/**
		 * The starting object id this strategy was configured with. Providers created from this strategy hand
		 * out ids starting one above this value.
		 *
		 * @return the configured starting object id.
		 */
		public final long startingObjectId()
		{
			return this.startingObjectId;
		}
		
		@Override
		public String strategyTypeNameObjectId()
		{
			return Transient.typeName();
		}

		@Override
		public final PersistenceObjectIdProvider createObjectIdProvider()
		{
			return PersistenceObjectIdProvider.Transient(this.startingObjectId);
		}
		
	}
	
	/**
	 * Creates a {@link None} strategy that produces {@link PersistenceObjectIdProvider.Failing} providers,
	 * refusing to mint new object ids.
	 *
	 * @return the new {@link None} strategy.
	 */
	public static PersistenceObjectIdStrategy.None None()
	{
		return new PersistenceObjectIdStrategy.None();
	}

	/**
	 * Strategy for read-only setups that must not generate new object ids: produces
	 * {@link PersistenceObjectIdProvider.Failing} providers, which throw on
	 * {@link PersistenceObjectIdProvider#provideNextObjectId()}. Round-tripped through the textual form simply
	 * as {@code None}, with no parameters.
	 */
	public final class None implements PersistenceObjectIdStrategy
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		/**
		 * The discriminator written into the persisted id-strategy string for {@link None}. Intentionally a
		 * fixed literal rather than {@link Class#getSimpleName()} so the class can be renamed without breaking
		 * existing persisted configurations.
		 *
		 * @return the literal {@code "None"}.
		 */
		public static String typeName()
		{
			// intentionally not the class name since it must stay the same, even if the class should get renamed.
			return "None";
		}

		/**
		 * Appends the textual form of the passed {@link None} strategy to {@code vs}. The strategy carries no
		 * parameters, so only the discriminator name is written.
		 *
		 * @param vs         the buffer to append to.
		 * @param idStrategy the strategy to assemble.
		 */
		public static void assemble(final VarString vs, final PersistenceObjectIdStrategy.None idStrategy)
		{
			vs
			.add(PersistenceObjectIdStrategy.None.typeName())
			;
		}

		/**
		 * Parses the textual form of a {@link None} strategy. Only the discriminator prefix is validated; any
		 * trailing content is ignored.
		 *
		 * @param typeIdStrategyContent the textual form to parse.
		 *
		 * @return a new {@link None} strategy.
		 *
		 * @throws ParsingException if the input does not begin with the expected discriminator name.
		 */
		public static PersistenceObjectIdStrategy.None parse(final String typeIdStrategyContent) throws ParsingException
		{
			PersistenceIdStrategyStringConverter.validateIdStrategyName(
				PersistenceObjectIdStrategy.None.class,
				typeName()                      ,
				typeIdStrategyContent
			);
			
			// the rest of the string is ignored intentionally.
			return PersistenceObjectIdStrategy.None();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		None()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public String strategyTypeNameObjectId()
		{
			return None.typeName();
		}

		@Override
		public final PersistenceObjectIdProvider createObjectIdProvider()
		{
			return PersistenceObjectIdProvider.Failing();
		}
		
	}
	
	/**
	 * Plug-in for serializing a specific {@link PersistenceObjectIdStrategy} subtype to its textual form.
	 * Registered with {@link PersistenceIdStrategyStringConverter} so user-defined strategies can be added
	 * without subclassing the converter.
	 *
	 * @param <S> the concrete strategy subtype this assembler handles.
	 */
	@FunctionalInterface
	public interface Assembler<S extends PersistenceObjectIdStrategy>
	{
		/**
		 * Appends the textual form of {@code idStrategy} to {@code vs}.
		 *
		 * @param vs         the buffer to append to.
		 * @param idStrategy the strategy to assemble.
		 */
		public void assembleIdStrategy(VarString vs, S idStrategy);
	}

	/**
	 * Plug-in for parsing a specific {@link PersistenceObjectIdStrategy} subtype from its textual form.
	 * Registered with {@link PersistenceIdStrategyStringConverter} so user-defined strategies can be added
	 * without subclassing the converter.
	 *
	 * @param <S> the concrete strategy subtype this parser produces.
	 */
	@FunctionalInterface
	public interface Parser<S extends PersistenceObjectIdStrategy>
	{
		/**
		 * Parses the textual form of a strategy and returns the corresponding instance.
		 *
		 * @param typeIdStrategyContent the textual form to parse.
		 *
		 * @return the parsed strategy instance.
		 */
		public S parse(String typeIdStrategyContent);
	}
	
}
