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
 * Persistable description of how new type ids are generated. A strategy is the configuration-side counterpart of a
 * {@link PersistenceTypeIdProvider}: it carries only the parameters needed to set up a provider (e.g. the starting
 * id), is round-tripped to and from a textual form by {@link PersistenceIdStrategyStringConverter}, and is turned
 * into a live, mutable provider via {@link #createTypeIdProvider()}.
 * <p>
 * The textual form is what allows two processes opening the same persistence layer to agree on id generation
 * without sharing runtime state. {@link #strategyTypeNameTypeId()} returns the discriminator string used in that
 * form (see {@link Transient#typeName()}, {@link None#typeName()}); it is intentionally decoupled from the Java
 * class name so strategy classes can be renamed without breaking persisted configurations.
 * <p>
 * Two ready-made strategies are bundled:
 * <ul>
 * <li>{@link Transient} &mdash; pairs with {@link PersistenceTypeIdProvider.Transient} for in-memory id generation
 * starting at a configurable value.</li>
 * <li>{@link None} &mdash; pairs with {@link PersistenceTypeIdProvider.Failing} for read-only setups that must not
 * mint new type ids.</li>
 * </ul>
 *
 * @see PersistenceTypeIdProvider
 * @see PersistenceObjectIdStrategy
 * @see PersistenceIdStrategy
 * @see PersistenceIdStrategyStringConverter
 */
public interface PersistenceTypeIdStrategy
{
	/**
	 * Creates a fresh, mutable {@link PersistenceTypeIdProvider} configured according to this strategy. Every call
	 * yields an independent provider instance, so the strategy itself can be reused as immutable configuration.
	 *
	 * @return the newly created provider.
	 */
	public PersistenceTypeIdProvider createTypeIdProvider();

	/**
	 * The discriminator name written into the persisted id-strategy string. Used by
	 * {@link PersistenceIdStrategyStringConverter} to dispatch parsing back to the correct strategy implementation
	 * and intentionally decoupled from the Java class name so strategy classes can be renamed without breaking
	 * persisted configurations.
	 *
	 * @return this strategy's persisted discriminator name.
	 */
	public String strategyTypeNameTypeId();


	/**
	 * Creates a {@link Transient} strategy starting at {@link Persistence#defaultStartTypeId()}, leaving the range
	 * below that value reserved for the JDK's native types.
	 *
	 * @return the new transient strategy.
	 */
	public static PersistenceTypeIdStrategy.Transient Transient()
	{
		return new PersistenceTypeIdStrategy.Transient(Persistence.defaultStartTypeId());
	}

	/**
	 * Creates a {@link Transient} strategy whose providers start handing out ids one above the passed value. The
	 * starting id is validated via {@link Persistence#validateTypeId(long)}.
	 *
	 * @param startingTypeId the watermark to start from; the first generated type id is one above this.
	 *
	 * @return the new transient strategy.
	 */
	public static PersistenceTypeIdStrategy.Transient Transient(final long startingTypeId)
	{
		return new PersistenceTypeIdStrategy.Transient(Persistence.validateTypeId(startingTypeId));
	}

	/**
	 * Strategy that produces {@link PersistenceTypeIdProvider.Transient} providers from a configured starting
	 * id. Round-tripped through the textual form as {@code Transient(<startingTypeId>)}.
	 */
	public final class Transient implements PersistenceTypeIdStrategy
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		/**
		 * The discriminator written into the persisted id-strategy string for {@link Transient}. Intentionally a
		 * fixed literal rather than {@link Class#getSimpleName()} so the class can be renamed without breaking
		 * existing persisted configurations.
		 *
		 * @return the literal {@code "Transient"}.
		 */
		public static String typeName()
		{
			// intentionally not the class name since it must stay the same, even if the class should get renamed.
			return "Transient";
		}

		/**
		 * Appends the textual form of the passed {@link Transient} strategy to {@code vs}, in the shape
		 * {@code Transient(<startingTypeId>)}.
		 *
		 * @param vs         the buffer to append to.
		 * @param idStrategy the strategy to assemble.
		 */
		public static void assemble(final VarString vs, final PersistenceTypeIdStrategy.Transient idStrategy)
		{
			vs
			.add(PersistenceTypeIdStrategy.Transient.typeName())
			.add(openingCharacter()).add(idStrategy.startingTypeId()).add(closingCharacter())
			;
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
		 * Parses a {@code Transient(<startingTypeId>)} textual form into a {@link Transient} strategy. An empty
		 * parameter list (i.e. {@code Transient()}) yields the default starting id, otherwise the parameter is
		 * parsed as a {@code long} and validated.
		 *
		 * @param typeIdStrategyContent the textual form to parse.
		 *
		 * @return the parsed strategy.
		 *
		 * @throws ParsingException if the input does not match the expected shape.
		 */
		public static PersistenceTypeIdStrategy.Transient parse(final String typeIdStrategyContent)
		{
			PersistenceIdStrategyStringConverter.validateIdStrategyName(
				PersistenceTypeIdStrategy.Transient.class,
				typeName()                      ,
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
				? PersistenceTypeIdStrategy.Transient()
				: PersistenceTypeIdStrategy.Transient(Long.parseLong(valueString.get()))
			;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final long startingTypeId;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Transient(final long startingTypeId)
		{
			super();
			this.startingTypeId = startingTypeId;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		/**
		 * The starting type id this strategy was configured with. Providers created from this strategy hand out
		 * ids starting one above this value.
		 *
		 * @return the configured starting type id.
		 */
		public final long startingTypeId()
		{
			return this.startingTypeId;
		}
		
		@Override
		public String strategyTypeNameTypeId()
		{
			return Transient.typeName();
		}

		@Override
		public final PersistenceTypeIdProvider createTypeIdProvider()
		{
			return PersistenceTypeIdProvider.Transient(this.startingTypeId);
		}
		
	}
	
	
	
	/**
	 * Creates a {@link None} strategy that produces {@link PersistenceTypeIdProvider.Failing} providers, refusing
	 * to mint new type ids.
	 *
	 * @return the new {@link None} strategy.
	 */
	public static PersistenceTypeIdStrategy.None None()
	{
		return new PersistenceTypeIdStrategy.None();
	}

	/**
	 * Strategy for read-only setups that must not generate new type ids: produces
	 * {@link PersistenceTypeIdProvider.Failing} providers, which throw on
	 * {@link PersistenceTypeIdProvider#provideNextTypeId()}. Round-tripped through the textual form simply as
	 * {@code None}, with no parameters.
	 */
	public final class None implements PersistenceTypeIdStrategy
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		/**
		 * The discriminator written into the persisted id-strategy string for {@link None}. Intentionally a fixed
		 * literal rather than {@link Class#getSimpleName()} so the class can be renamed without breaking existing
		 * persisted configurations.
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
		public static void assemble(final VarString vs, final PersistenceTypeIdStrategy.None idStrategy)
		{
			vs
			.add(PersistenceTypeIdStrategy.None.typeName())
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
		public static PersistenceTypeIdStrategy.None parse(final String typeIdStrategyContent) throws ParsingException
		{
			PersistenceIdStrategyStringConverter.validateIdStrategyName(
				PersistenceTypeIdStrategy.None.class,
				typeName()                      ,
				typeIdStrategyContent
			);
			
			// the rest of the string is ignored intentionally.
			return PersistenceTypeIdStrategy.None();
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
		public String strategyTypeNameTypeId()
		{
			return None.typeName();
		}

		@Override
		public final PersistenceTypeIdProvider createTypeIdProvider()
		{
			return PersistenceTypeIdProvider.Failing();
		}
		
	}
	
	/**
	 * Plug-in for serializing a specific {@link PersistenceTypeIdStrategy} subtype to its textual form. Registered
	 * with {@link PersistenceIdStrategyStringConverter} so user-defined strategies can be added without subclassing
	 * the converter.
	 *
	 * @param <S> the concrete strategy subtype this assembler handles.
	 */
	@FunctionalInterface
	public interface Assembler<S extends PersistenceTypeIdStrategy>
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
	 * Plug-in for parsing a specific {@link PersistenceTypeIdStrategy} subtype from its textual form. Registered
	 * with {@link PersistenceIdStrategyStringConverter} so user-defined strategies can be added without subclassing
	 * the converter.
	 *
	 * @param <S> the concrete strategy subtype this parser produces.
	 */
	@FunctionalInterface
	public interface Parser<S extends PersistenceTypeIdStrategy>
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
