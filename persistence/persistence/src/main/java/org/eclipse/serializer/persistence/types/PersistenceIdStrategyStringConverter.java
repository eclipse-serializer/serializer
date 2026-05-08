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

import static  org.eclipse.serializer.util.X.notNull;

import org.eclipse.serializer.chars.ObjectStringConverter;
import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.chars.XParsing;
import org.eclipse.serializer.chars._charArrayRange;
import org.eclipse.serializer.collections.EqHashTable;
import org.eclipse.serializer.collections.HashTable;
import org.eclipse.serializer.collections.types.XImmutableMap;
import org.eclipse.serializer.collections.types.XReference;
import org.eclipse.serializer.exceptions.ParsingException;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.typing.KeyValue;
import org.eclipse.serializer.util.X;

/**
 * Round-trip {@link ObjectStringConverter} for {@link PersistenceIdStrategy} &mdash; the converter that
 * turns the combined object-id/type-id strategy into the textual form persisted next to the type
 * dictionary, and reads it back. The textual form looks like
 * {@code Type:'<typeIdStrategy>', Object:'<objectIdStrategy>'} and dispatches per sub-strategy through the
 * registered {@link PersistenceObjectIdStrategy.Assembler}/{@link PersistenceObjectIdStrategy.Parser}
 * (resp. {@link PersistenceTypeIdStrategy.Assembler}/{@link PersistenceTypeIdStrategy.Parser}) plug-ins.
 * <p>
 * {@link #New()} returns a converter pre-loaded with the bundled {@link PersistenceObjectIdStrategy.Transient}
 * / {@link PersistenceObjectIdStrategy.None} and {@link PersistenceTypeIdStrategy.Transient} /
 * {@link PersistenceTypeIdStrategy.None} strategies. Callers needing additional strategies build a custom
 * converter via the {@link Creator} fluent API.
 *
 * @see PersistenceIdStrategy
 * @see PersistenceObjectIdStrategy
 * @see PersistenceTypeIdStrategy
 */
public interface PersistenceIdStrategyStringConverter extends ObjectStringConverter<PersistenceIdStrategy>
{
	@Override
	public VarString assemble(VarString vs, PersistenceIdStrategy subject);

	@Override
	public default VarString provideAssemblyBuffer()
	{
		return ObjectStringConverter.super.provideAssemblyBuffer();
	}

	@Override
	public default String assemble(final PersistenceIdStrategy subject)
	{
		return ObjectStringConverter.super.assemble(subject);
	}

	@Override
	public PersistenceIdStrategy parse(_charArrayRange input);

	@Override
	public default PersistenceIdStrategy parse(final String input)
	{
		return ObjectStringConverter.super.parse(input);
	}


	/**
	 * Validates that {@code idStrategyContent} starts with the discriminator name {@code idStrategyName}
	 * for the strategy class {@code idStrategyType}. Throws if the prefix doesn't match &mdash; the
	 * dispatch in {@link Default#parse(_charArrayRange)} already routes by that prefix, so failing here
	 * indicates a corrupt persisted strategy.
	 *
	 * @param idStrategyType    the strategy class being parsed (used in the error message).
	 * @param idStrategyName    the expected discriminator name.
	 * @param idStrategyContent the textual content to validate.
	 *
	 * @throws ParsingException if the content does not begin with the expected name.
	 */
	public static void validateIdStrategyName(
		final Class<?> idStrategyType   ,
		final String   idStrategyName   ,
		final String   idStrategyContent
	)
		throws ParsingException
	{
		if(idStrategyContent.startsWith(idStrategyName))
		{
			return;
		}
		
		throw new ParsingException(
			"Invalid id strategy content for type name \"" + idStrategyName + "\""
			+ " of type " + idStrategyType.getName()
			+ ": " + idStrategyContent
		);
	}
	
	
	
	/**
	 * Returns a fresh empty {@link Creator} for assembling a custom converter via fluent registration of
	 * assemblers and parsers.
	 *
	 * @return the newly created builder.
	 */
	public static PersistenceIdStrategyStringConverter.Creator Creator()
	{
		return new PersistenceIdStrategyStringConverter.Creator.Default();
	}

	/**
	 * Fluent builder for a {@link PersistenceIdStrategyStringConverter}. Lets callers register custom
	 * assemblers (one per Java type, used to write the textual form) and parsers (one per discriminator
	 * name, used to read it back) for both object-id and type-id strategy subtypes, then materialize the
	 * configured converter via {@link #create()}.
	 */
	public static interface Creator
	{
		/**
		 * Registers an assembler for the passed {@link PersistenceObjectIdStrategy} subtype. Subsequent
		 * calls for the same type override.
		 *
		 * @param <S>                  the strategy subtype.
		 * @param objectIdStrategyType the strategy class.
		 * @param assembler            the assembler.
		 *
		 * @return this builder, for fluent chaining.
		 */
		public <S extends PersistenceObjectIdStrategy> Creator register(
			Class<S>                             objectIdStrategyType,
			PersistenceObjectIdStrategy.Assembler<S> assembler
		);

		/**
		 * Registers an assembler for the passed {@link PersistenceTypeIdStrategy} subtype. Subsequent
		 * calls for the same type override.
		 *
		 * @param <S>                the strategy subtype.
		 * @param typeIdStrategyType the strategy class.
		 * @param assembler          the assembler.
		 *
		 * @return this builder, for fluent chaining.
		 */
		public <S extends PersistenceTypeIdStrategy> Creator register(
			Class<S>                           typeIdStrategyType,
			PersistenceTypeIdStrategy.Assembler<S> assembler
		);

		/**
		 * Registers a parser for the passed {@link PersistenceObjectIdStrategy} discriminator name.
		 * Parsers are consulted by prefix-match against the textual content.
		 *
		 * @param <S>              the strategy subtype.
		 * @param strategyTypeName the discriminator name.
		 * @param parser           the parser.
		 *
		 * @return this builder, for fluent chaining.
		 */
		public <S extends PersistenceObjectIdStrategy> Creator register(
			String                            strategyTypeName,
			PersistenceObjectIdStrategy.Parser<S> parser
		);

		/**
		 * Registers a parser for the passed {@link PersistenceTypeIdStrategy} discriminator name. Parsers
		 * are consulted by prefix-match against the textual content.
		 *
		 * @param <S>              the strategy subtype.
		 * @param strategyTypeName the discriminator name.
		 * @param parser           the parser.
		 *
		 * @return this builder, for fluent chaining.
		 */
		public <S extends PersistenceTypeIdStrategy> Creator register(
			String                          strategyTypeName,
			PersistenceTypeIdStrategy.Parser<S> parser
		);

		/**
		 * Materializes a {@link PersistenceIdStrategyStringConverter} from the registrations made on this
		 * builder. The returned converter is backed by immutable copies of the registration tables.
		 *
		 * @return the newly created converter.
		 */
		public PersistenceIdStrategyStringConverter create();



		/**
		 * Default {@link Creator}: collects registrations in mutable hash tables and snapshots them into
		 * immutable copies on {@link #create()}. All registration methods synchronize on the builder
		 * instance.
		 */
		public final class Default implements PersistenceIdStrategyStringConverter.Creator
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final HashTable<Class<?>, PersistenceObjectIdStrategy.Assembler<?>> oidAssemblers = HashTable.New();
			private final HashTable<Class<?>, PersistenceTypeIdStrategy.Assembler<?>>   tidAssemblers = HashTable.New();
			
			private final EqHashTable<String, PersistenceObjectIdStrategy.Parser<?>> oidParsers = EqHashTable.New();
			private final EqHashTable<String, PersistenceTypeIdStrategy.Parser<?>>   tidParsers = EqHashTable.New();
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Default()
			{
				super();
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public synchronized <S extends PersistenceObjectIdStrategy> Creator.Default register(
				final Class<S>                             objectIdStrategyType,
				final PersistenceObjectIdStrategy.Assembler<S> assembler
			)
			{
				this.oidAssemblers.put(
					notNull(objectIdStrategyType),
					notNull(assembler)
				);

				return this;
			}
			
			@Override
			public synchronized <S extends PersistenceTypeIdStrategy>  Creator.Default register(
				final Class<S>                           typeIdStrategyType,
				final PersistenceTypeIdStrategy.Assembler<S> assembler
			)
			{
				this.tidAssemblers.put(
					notNull(typeIdStrategyType),
					notNull(assembler)
				);

				return this;
			}
			
			@Override
			public synchronized <S extends PersistenceObjectIdStrategy> Creator register(
				final String                            strategyTypeName,
				final PersistenceObjectIdStrategy.Parser<S> parser
			)
			{
				this.oidParsers.put(
					notNull(strategyTypeName),
					notNull(parser)
				);
				
				return this;
			}
			
			@Override
			public synchronized <S extends PersistenceTypeIdStrategy> Creator register(
				final String                          strategyTypeName,
				final PersistenceTypeIdStrategy.Parser<S> parser
			)
			{
				this.tidParsers.put(
					notNull(strategyTypeName),
					notNull(parser)
				);
				
				return this;
			}
			
			@Override
			public final synchronized PersistenceIdStrategyStringConverter create()
			{
				return new PersistenceIdStrategyStringConverter.Default(
					this.oidAssemblers.immure(),
					this.tidAssemblers.immure(),
					this.oidParsers.immure()   ,
					this.tidParsers.immure()
				);
			}
			
		}
		
	}
	
	/**
	 * Creates a new converter pre-loaded with assemblers and parsers for the bundled
	 * {@link PersistenceObjectIdStrategy.Transient}/{@link PersistenceObjectIdStrategy.None} and
	 * {@link PersistenceTypeIdStrategy.Transient}/{@link PersistenceTypeIdStrategy.None} strategies.
	 *
	 * @return the newly created converter.
	 */
	public static PersistenceIdStrategyStringConverter New()
	{
		// generics magic! 8-)
		return Creator()
			.register(PersistenceObjectIdStrategy.Transient.class     , PersistenceObjectIdStrategy.Transient::assemble)
			.register(PersistenceObjectIdStrategy.Transient.typeName(), PersistenceObjectIdStrategy.Transient::parse)

			.register(PersistenceObjectIdStrategy.None.class     , PersistenceObjectIdStrategy.None::assemble)
			.register(PersistenceObjectIdStrategy.None.typeName(), PersistenceObjectIdStrategy.None::parse)

			.register(PersistenceTypeIdStrategy.Transient.class     , PersistenceTypeIdStrategy.Transient::assemble)
			.register(PersistenceTypeIdStrategy.Transient.typeName(), PersistenceTypeIdStrategy.Transient::parse)

			.register(PersistenceTypeIdStrategy.None.class     , PersistenceTypeIdStrategy.None::assemble)
			.register(PersistenceTypeIdStrategy.None.typeName(), PersistenceTypeIdStrategy.None::parse)
			.create()
		;
	}

	/**
	 * Default {@link PersistenceIdStrategyStringConverter}. Backed by immutable assembler/parser tables
	 * keyed by Java class (assembly side) and discriminator name (parse side); the textual form is
	 * {@code Type:'<typeIdStrategy>', Object:'<objectIdStrategy>'} with a literal label, a colon, and the
	 * sub-strategy enclosed in single quotes.
	 */
	public final class Default implements PersistenceIdStrategyStringConverter
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		/**
		 * The label preceding the type-id sub-strategy in the textual form.
		 *
		 * @return the literal {@code "Type"}.
		 */
		public static String labelType()
		{
			return "Type";
		}

		/**
		 * The label preceding the object-id sub-strategy in the textual form.
		 *
		 * @return the literal {@code "Object"}.
		 */
		public static String labelObject()
		{
			return "Object";
		}

		/**
		 * The character separating a label from its sub-strategy content.
		 *
		 * @return the literal {@code ':'}.
		 */
		public static char typeAssigner()
		{
			return ':';
		}

		/**
		 * The character separating the two sub-strategy entries.
		 *
		 * @return the literal {@code ','}.
		 */
		public static char separator()
		{
			return ',';
		}

		/**
		 * The character enclosing each sub-strategy's textual content.
		 *
		 * @return the literal {@code '\''}.
		 */
		public static char quote()
		{
			return '\'';
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final XImmutableMap<Class<?>, PersistenceObjectIdStrategy.Assembler<?>> oidsAssemblers;
		final XImmutableMap<Class<?>, PersistenceTypeIdStrategy.Assembler<?>  > tidsAssemblers;
		final XImmutableMap<String, PersistenceObjectIdStrategy.Parser<?>>      oidsParsers   ;
		final XImmutableMap<String, PersistenceTypeIdStrategy.Parser<?>>        tidsParsers   ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
			final XImmutableMap<Class<?>, PersistenceObjectIdStrategy.Assembler<?>> oidsAssemblers,
			final XImmutableMap<Class<?>, PersistenceTypeIdStrategy.Assembler<?>  > tidsAssemblers,
			final XImmutableMap<String, PersistenceObjectIdStrategy.Parser<?>>      oidsParsers   ,
			final XImmutableMap<String, PersistenceTypeIdStrategy.Parser<?>>        tidsParsers
		)
		{
			super();
			this.oidsAssemblers = oidsAssemblers;
			this.tidsAssemblers = tidsAssemblers;
			this.oidsParsers    = oidsParsers   ;
			this.tidsParsers    = tidsParsers   ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private <S extends PersistenceObjectIdStrategy> PersistenceObjectIdStrategy.Assembler<S> lookupObjectIdStrategyAssembler(
			final Class<?> type
		)
		{
			@SuppressWarnings("unchecked") // cast safety is guaranteed by the registration logic
			final PersistenceObjectIdStrategy.Assembler<S> assembler =
				(PersistenceObjectIdStrategy.Assembler<S>)this.oidsAssemblers.get(type)
			;
			
			return assembler;
		}
		
		private <S extends PersistenceTypeIdStrategy> PersistenceTypeIdStrategy.Assembler<S> lookupTypeIdStrategyAssembler(
			final Class<?> type
		)
		{
			@SuppressWarnings("unchecked") // cast safety is guaranteed by the registration logic
			final PersistenceTypeIdStrategy.Assembler<S> assembler =
				(PersistenceTypeIdStrategy.Assembler<S>)this.tidsAssemblers.get(type)
			;
			
			return assembler;
		}
		
		private <S extends PersistenceObjectIdStrategy> void assembleObjectIdStrategy(
			final VarString vs        ,
			final S         idStrategy
		)
		{
			final PersistenceObjectIdStrategy.Assembler<S> assembler = this.lookupObjectIdStrategyAssembler(idStrategy.getClass());
			assembler.assembleIdStrategy(vs, idStrategy);
		}
		
		private <S extends PersistenceTypeIdStrategy> void assembleTypeIdStrategy(
			final VarString vs        ,
			final S         idStrategy
		)
		{
			final PersistenceTypeIdStrategy.Assembler<S> assembler = this.lookupTypeIdStrategyAssembler(idStrategy.getClass());
			assembler.assembleIdStrategy(vs, idStrategy);
		}
		
		@Override
		public VarString assemble(final VarString vs, final PersistenceIdStrategy idStrategy)
		{
			vs
			.add(labelType()).add(typeAssigner()).add(quote()).apply(v ->
				this.assembleTypeIdStrategy(v, idStrategy.typeIdStragegy())
			).add(quote()).add(separator()).blank()
			.add(labelObject()).add(typeAssigner()).add(quote()).apply(v ->
				this.assembleObjectIdStrategy(v, idStrategy.objectIdStragegy())
			).add(quote());
			
			return vs;
		}

		@Override
		public PersistenceIdStrategy parse(final _charArrayRange input)
		{
			final XReference<String> tidsContent = X.Reference(null);
			final XReference<String> oidsContent = X.Reference(null);
			
			parseContent(input, tidsContent, oidsContent);
			
			final PersistenceTypeIdStrategy.Parser<?>   tidsParser = this.lookupTypeIdStrategyParser(tidsContent.get());
			final PersistenceObjectIdStrategy.Parser<?> oidsParser = this.lookupObjectIdStrategyParser(oidsContent.get());
			
			final PersistenceTypeIdStrategy   tidStrategy = tidsParser.parse(tidsContent.get());
			final PersistenceObjectIdStrategy oidStrategy = oidsParser.parse(oidsContent.get());
			
			return PersistenceIdStrategy.New(oidStrategy, tidStrategy);
		}
		
		private static void parseContent(
			final _charArrayRange    inputRange ,
			final XReference<String> tidsContent,
			final XReference<String> oidsContent
		)
		{
			final char[] input = inputRange.array();
			
			// the effective bounding index is the position of the last non-whitespace plus 1.
			final int iBound = XParsing.skipWhiteSpacesReversed(input, inputRange.start(), inputRange.bound()) + 1;
			
			final int iTypeEnd = parsePart(input, inputRange.start(), iBound, labelType(), tidsContent);
			
			XParsing.checkIncompleteInput(iTypeEnd, iBound);
			XParsing.checkCharacter(input, iTypeEnd, separator(), "IdStrategy");
			
			parsePart(input, iTypeEnd + 1, iBound, labelObject(), oidsContent);
		}
		
		private static int parsePart(
			final char[]             input        ,
			final int                iStart       ,
			final int                iBound       ,
			final String             label        ,
			final XReference<String> contentHolder
		)
		{
			int i = iStart;
			
			i = XParsing.skipWhiteSpaces(input, i, iBound);
			
			XParsing.checkIncompleteInput(i, iBound);
			i = checkStartsWith          (input, i, iBound, label);
			i = XParsing.skipWhiteSpaces (input, i, iBound);

			XParsing.checkIncompleteInput(i, iBound);
			i = XParsing.checkCharacter  (input, i, typeAssigner(), label);
			i = XParsing.skipWhiteSpaces (input, i, iBound);

			XParsing.checkIncompleteInput(i, iBound);
			XParsing.checkCharacter      (input, i, quote(), label);
			i = XParsing.parseSimpleQuote(input, i, iBound, contentHolder);
			i = XParsing.skipWhiteSpaces (input, i, iBound);
			
			return i;
		}
				
		private static int checkStartsWith(
			final char[] input ,
			final int    i     ,
			final int    iBound,
			final String label
		)
		{
			if(XParsing.startsWith(input, i, iBound, label))
			{
				return i + label.length();
			}
			
			throw new PersistenceException("IdStrategy type label \"" + label + "\" not found at index " + i + ".");
		}
		
		private PersistenceTypeIdStrategy.Parser<?> lookupTypeIdStrategyParser(final String content)
		{
			for(final KeyValue<String, PersistenceTypeIdStrategy.Parser<?>> e : this.tidsParsers)
			{
				if(content.startsWith(e.key()))
				{
					return e.value();
				}
			}
			
			throw new PersistenceException("Unknown TypeIdStrategy: \"" + content + "\".");
		}
		
		private PersistenceObjectIdStrategy.Parser<?> lookupObjectIdStrategyParser(final String content)
		{
			for(final KeyValue<String, PersistenceObjectIdStrategy.Parser<?>> e : this.oidsParsers)
			{
				if(content.startsWith(e.key()))
				{
					return e.value();
				}
			}
			
			throw new PersistenceException("Unknown ObjectIdStrategy: \"" + content + "\".");
		}
				
	}
		
}
