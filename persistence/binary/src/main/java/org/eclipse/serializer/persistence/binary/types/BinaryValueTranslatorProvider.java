package org.eclipse.serializer.persistence.binary.types;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
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

import org.eclipse.serializer.collections.XUtilsCollection;
import org.eclipse.serializer.collections.types.XGettingMap;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.persistence.binary.exceptions.BinaryPersistenceException;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMember;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldReflective;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldValueStruct;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionMemberFieldValueStruct;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionMember;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionResolver;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionResolverProvider;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandler;
import org.eclipse.serializer.reflect.XReflect;
import org.eclipse.serializer.typing.TypeMappingLookup;


/**
 * Resolves the {@link BinaryValueSetter} that translates a persisted source member's value to either the
 * matching current target member (instance-update path) or to the corresponding slot in a rewritten
 * intermediate binary form (rerouting path). Custom translators may be registered under arbitrary keys
 * built by {@link BinaryValueTranslatorKeyBuilder}s; if no custom translator matches, the provider falls
 * back to the generic primitive-to-primitive lookup table or to a generic reference resolver.
 *
 * @see BinaryValueSetter
 * @see BinaryValueTranslatorKeyBuilder
 * @see BinaryValueTranslatorLookupProvider
 */
public interface BinaryValueTranslatorProvider
{
	/**
	 * Normal translator to translate a value from binary form to a target instance.
	 * 
	 * @param sourceLegacyType the source legacy type
	 * @param sourceMember the source member
	 * @param targetCurrentType the target current type
	 * @param targetMember the target member
	 * @return the provided value setter
	 */
	public BinaryValueSetter provideTargetValueTranslator(
		PersistenceTypeDefinition         sourceLegacyType ,
		PersistenceTypeDefinitionMember   sourceMember     ,
		PersistenceTypeHandler<Binary, ?> targetCurrentType,
		PersistenceTypeDefinitionMember   targetMember
	);
	
	/**
	 * Special translator to translate a value from binary form to an intermediate binary form.
	 * 
	 * @param sourceLegacyType the source legacy type
	 * @param sourceMember the source member
	 * @param targetCurrentType the target current type
	 * @param targetMember the target member
	 * @return the provided value setter
	 */
	public BinaryValueSetter provideBinaryValueTranslator(
		PersistenceTypeDefinition         sourceLegacyType ,
		PersistenceTypeDefinitionMember   sourceMember     ,
		PersistenceTypeHandler<Binary, ?> targetCurrentType,
		PersistenceTypeDefinitionMember   targetMember
	);

	/**
	 * The resolver the refactoring rules come from, for a handler that reads a persisted layout itself
	 * instead of having a translator do it - a value class, whose instances are constructed rather than
	 * populated.
	 *
	 * @return the refactoring resolver, or {@code null} where none is configured.
	 */
	public default PersistenceTypeDescriptionResolver provideRefactoringResolver()
	{
		return null;
	}



	/**
	 * Creates a new default {@link BinaryValueTranslatorProvider}.
	 *
	 * @param customTranslatorLookup   optional map of registered custom translators keyed by lookup key, may be {@code null}.
	 * @param translatorKeyBuilders    optional sequence of key builders to consult, may be {@code null} or empty.
	 * @param translatorLookupProvider the generic primitive-to-primitive translator table provider.
	 * @param switchByteOrder          whether the persisted form uses a non-native byte order.
	 *
	 * @return the newly created provider.
	 */
	public static BinaryValueTranslatorProvider New(
		final XGettingMap<String, BinaryValueSetter>                      customTranslatorLookup  ,
		final XGettingSequence<? extends BinaryValueTranslatorKeyBuilder> translatorKeyBuilders   ,
		final BinaryValueTranslatorLookupProvider                         translatorLookupProvider,
		final boolean                                                     switchByteOrder
	)
	{
		return New(
			customTranslatorLookup  ,
			translatorKeyBuilders   ,
			translatorLookupProvider,
			null                    ,
			switchByteOrder
		);
	}

	/**
	 * Creates a new default {@link BinaryValueTranslatorProvider} consulting the passed resolver for the
	 * members of an inlined layout, which the legacy type mapping does not reach: it pairs the inlined field
	 * as a whole, while the value type's own fields are members inside that pair.
	 *
	 * @param customTranslatorLookup          optional map of registered custom translators keyed by lookup key, may be {@code null}.
	 * @param translatorKeyBuilders           optional sequence of key builders to consult, may be {@code null} or empty.
	 * @param translatorLookupProvider        the generic primitive-to-primitive translator table provider.
	 * @param typeDescriptionResolverProvider the refactoring resolver provider, may be {@code null}.
	 * @param switchByteOrder                 whether the persisted form uses a non-native byte order.
	 *
	 * @return the newly created provider.
	 */
	public static BinaryValueTranslatorProvider New(
		final XGettingMap<String, BinaryValueSetter>                      customTranslatorLookup         ,
		final XGettingSequence<? extends BinaryValueTranslatorKeyBuilder> translatorKeyBuilders          ,
		final BinaryValueTranslatorLookupProvider                         translatorLookupProvider       ,
		final PersistenceTypeDescriptionResolverProvider                  typeDescriptionResolverProvider,
		final boolean                                                     switchByteOrder
	)
	{
		return new BinaryValueTranslatorProvider.Default(
			mayNull(customTranslatorLookup),
			unwrapKeyBuilders(translatorKeyBuilders),
			notNull(translatorLookupProvider),
			mayNull(typeDescriptionResolverProvider),
			switchByteOrder
		);
	}
	
	static BinaryValueTranslatorKeyBuilder[] unwrapKeyBuilders(
		final XGettingSequence<? extends BinaryValueTranslatorKeyBuilder> translatorKeyBuilders
	)
	{
		return translatorKeyBuilders == null || translatorKeyBuilders.isEmpty()
			? null
			: XUtilsCollection.toArray(translatorKeyBuilders, BinaryValueTranslatorKeyBuilder.class)
		;
	}
	
	/**
	 * Default {@link BinaryValueTranslatorProvider} implementation. Caches the resolved translator lookup
	 * table on first use and consults the configured key builders before falling back to the generic
	 * primitive-to-primitive lookup or a generic reference resolver.
	 */
	public final class Default implements BinaryValueTranslatorProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final XGettingMap<String, BinaryValueSetter>     customTranslatorLookup         ;
		private final BinaryValueTranslatorKeyBuilder[]          translatorKeyBuilders          ;
		private final BinaryValueTranslatorLookupProvider        translatorLookupProvider       ;
		private final PersistenceTypeDescriptionResolverProvider typeDescriptionResolverProvider;
		private final boolean                                    switchByteOrder                ;

		private transient TypeMappingLookup<BinaryValueSetter> translatorLookup;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final XGettingMap<String, BinaryValueSetter>     customTranslatorLookup         ,
			final BinaryValueTranslatorKeyBuilder[]          translatorKeyBuilders          ,
			final BinaryValueTranslatorLookupProvider        translatorLookupProvider       ,
			final PersistenceTypeDescriptionResolverProvider typeDescriptionResolverProvider,
			final boolean                                    switchByteOrder
		)
		{
			super();
			this.customTranslatorLookup          = customTranslatorLookup         ;
			this.translatorKeyBuilders           = translatorKeyBuilders          ;
			this.translatorLookupProvider        = translatorLookupProvider       ;
			this.typeDescriptionResolverProvider = typeDescriptionResolverProvider;
			this.switchByteOrder                 = switchByteOrder                ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private TypeMappingLookup<BinaryValueSetter> translatorLookup()
		{
			if(this.translatorLookup == null)
			{
				this.translatorLookup = this.translatorLookupProvider.mapping(this.switchByteOrder);
			}
			
			return this.translatorLookup;
		}
		
		private BinaryValueSetter provideValueSkipper(final PersistenceTypeDefinitionMember sourceMember)
		{
			if(sourceMember instanceof PersistenceTypeDescriptionMemberFieldValueStruct)
			{
				// an inlined slot is longer than the object id a reference would occupy here
				return BinaryValueStructFunctions.provideSkipper(
					(PersistenceTypeDescriptionMemberFieldValueStruct)sourceMember
				);
			}

			if(sourceMember.isReference())
			{
				// skip the long-typed OID value
				return BinaryValueTranslators::skip_long;
			}

			/* (27.09.2018 TM)TODO: Legacy Type Mapping: implement skipping a variable length type.
			 * This even already exists in BinaryReferenceTraverser
			 */

			return resolvePrimitiveSkipper(sourceMember);
		}
		
		private static void validateIsPrimitiveType(final PersistenceTypeDefinitionMember member)
		{
			final Class<?> memberType = member.type();
			if(memberType == null || !memberType.isPrimitive())
			{
				throw new BinaryPersistenceException("Unhandled type " + toTypedIdentifier(member) + ".");
			}
		}
		
		private static BinaryValueSetter resolvePrimitiveSkipper(
			final PersistenceTypeDefinitionMember sourceMember
		)
		{
			validateIsPrimitiveType(sourceMember);
			
			final Class<?> sourceType = sourceMember.type();
			return sourceType == byte.class
				? BinaryValueTranslators::skip_byte
				: sourceType == boolean.class
				? BinaryValueTranslators::skip_boolean
				: sourceType == short.class
				? BinaryValueTranslators::skip_short
				: sourceType == char.class
				? BinaryValueTranslators::skip_char
				: sourceType == int.class
				? BinaryValueTranslators::skip_int
				: sourceType == float.class
				? BinaryValueTranslators::skip_float
				: sourceType == long.class
				? BinaryValueTranslators::skip_long
				: sourceType == double.class
				? BinaryValueTranslators::skip_double
				: throwUnhandledPrimitiveException(sourceMember)
			;
		}
		
		private static BinaryValueSetter throwUnhandledPrimitiveException(
			final PersistenceTypeDescriptionMember sourceMember
		)
		{
			throw new BinaryPersistenceException(
				"Unhandled primitive type " + toTypedIdentifier(sourceMember) + "."
			);
		}
		
		private BinaryValueSetter provideValueTranslator(
			final Class<?> sourceType,
			final Class<?> targetType
		)
		{
			final BinaryValueSetter translator = this.translatorLookup().lookup(sourceType, targetType);
			if(translator != null)
			{
				return translator;
			}
			
			validateIsReferenceType(sourceType);
			validateIsReferenceType(targetType);
			
			/*
			 * In case none of the other mapping tools (explicit mapping, member matching and translator registration)
			 * covered the current case, it is essential to check the target type compatibility, since it is
			 * too dangerous to arbitrarily copy references to instances of one type into fields of another type.
			 */
			validateCompatibleTargetType(sourceType, targetType);
			
			return this.provideReferenceResolver();
		}
		
		private static void validateCompatibleTargetType(final Class<?> sourceType, final Class<?> targetType)
		{
			if(targetType.isAssignableFrom(sourceType))
			{
				return;
			}
			
			throw new BinaryPersistenceException(
				"Incompatible types: " + sourceType.getName() + " -> " + targetType.getName()
			);
		}
		
		private BinaryValueSetter provideReferenceResolver(
			final PersistenceTypeDescriptionMember sourceMember,
			final PersistenceTypeDescriptionMember targetMember
		)
		{
			validateIsReferenceType(sourceMember);
			validateIsReferenceType(targetMember);
			
			return this.provideReferenceResolver();
		}
		
		private BinaryValueSetter provideReferenceResolver()
		{
			return BinaryValueFunctions.getObjectValueSetter(Object.class, this.switchByteOrder);
		}

		@Override
		public PersistenceTypeDescriptionResolver provideRefactoringResolver()
		{
			return this.typeDescriptionResolverProvider == null
				? null
				: this.typeDescriptionResolverProvider.provideTypeDescriptionResolver()
			;
		}

		/**
		 * An inlined slot carries the field's content rather than an object id, so reading it means
		 * constructing the instance from that content. That is only possible while the described layout still
		 * matches the type's current one, member for member and name for name: the constructor takes every
		 * field, so a layout that has since gained or lost one cannot be invoked from what was written, and
		 * one whose members merely swapped places would have each of them take the other's value.
		 * <p>
		 * A layout that has since gained or lost a member is translated member by member, matched by name:
		 * a gained one takes its type's default and a lost one is stepped over, which is the answer legacy
		 * mapping already gives for a referenced field. Defaulting the field as a whole is what would drop
		 * what was stored without saying so, and is not what happens here.
		 * <p>
		 * Two changes are still refused, because placing the bytes would misread them rather than convert
		 * them: a member whose type changed, and a current form that is not inlined at all.
		 */
		private BinaryValueSetter provideInlinedValueTranslator(
			final PersistenceTypeDefinitionMember sourceMember,
			final PersistenceTypeDefinitionMember targetMember
		)
		{
			/* Derived from the target, not the source: the two describe the same layout by the same names
			 * here, but only the target is bound to the runtime field it has to be written through. A legacy
			 * member deliberately binds no field once its declaring type was renamed, and the value would
			 * then be written through nothing at all.
			 */
			if(sourceMember.equalsLayout(targetMember)
			&& targetMember instanceof PersistenceTypeDefinitionMemberFieldValueStruct
			)
			{
				return BinaryValueStructFunctions.provideSetter(
					(PersistenceTypeDefinitionMemberFieldValueStruct)targetMember,
					this.switchByteOrder
				);
			}

			/* The layout differs, but both describe the same field of the same owner, paired by the legacy
			 * mapping, so their members can be matched by name, or by the refactoring rule naming one where
			 * the name changed: one the type has gained takes its default, one it has lost is stepped over.
			 * A member whose type changed is refused there, as is an unmapped loss plus gain.
			 */
			if(sourceMember instanceof PersistenceTypeDefinitionMemberFieldValueStruct
			&& targetMember instanceof PersistenceTypeDefinitionMemberFieldValueStruct
			)
			{
				return BinaryValueStructFunctions.provideEvolvingSetter(
					(PersistenceTypeDefinitionMemberFieldValueStruct)sourceMember,
					(PersistenceTypeDefinitionMemberFieldValueStruct)targetMember,
					this.provideRefactoringResolver()                            ,
					this.switchByteOrder
				);
			}

			throw new BinaryPersistenceException(
				"The inlined layout of " + toTypedIdentifier(sourceMember) + " cannot be read into "
				+ toTypedIdentifier(targetMember) + ": the current form is not an inlined one, and an"
				+ " inlined slot carries no object id to reference the value by instead."
			);
		}
		
		/**
		 * A field whose type is a value class may be laid out inside its owner, where the field's memory
		 * offset does not address it: writing there misses the field and corrupts whatever is embedded at
		 * that place instead. The value is written through a handle on the field, the way the current
		 * handler's own setters do it.
		 * <p>
		 * A reference source is resolved into the field. A persisted primitive widening into its own
		 * wrapper is boxed into it, because the offset-based translator for that pair boxes and writes in
		 * one step and so cannot be redirected by swapping the setter. Any other source form is refused
		 * rather than written blindly - writing it is precisely what this guards against.
		 *
		 * Only applicable where the translator writes into an <i>instance</i>, which is the reflective
		 * legacy path. The rewriting path hands its setters a {@literal null} target and an address to
		 * write to, and a handle writes into neither - it would fail on the null. That path cannot reach
		 * here today, its target members being generic rather than reflective, so this is stated rather
		 * than guarded: a guard would suggest the case is expected and leave the reader wondering what
		 * produces it.
		 *
		 * @param sourceMember    the legacy member being read.
		 * @param targetMember    the current member being written, whose type is a value class.
		 * @param switchByteOrder whether the persisted form has the opposite byte order.
		 *
		 * @return the setter writing the value into the target field.
		 */
		private static BinaryValueSetter provideValueTypeFieldSetter(
			final PersistenceTypeDefinitionMember                sourceMember   ,
			final PersistenceTypeDefinitionMemberFieldReflective targetMember   ,
			final boolean                                        switchByteOrder
		)
		{
			if(sourceMember.isReference())
			{
				return BinaryValueHandleFunctions.provideReferenceSetter(targetMember.field(), switchByteOrder);
			}

			final BinaryValueSetter boxingSetter = BinaryValueHandleFunctions.provideBoxingSetter(
				targetMember.field(),
				sourceMember.type() ,
				switchByteOrder
			);
			if(boxingSetter != null)
			{
				return boxingSetter;
			}

			throw new BinaryPersistenceException(
				"Cannot read " + toTypedIdentifier(sourceMember) + " into " + toTypedIdentifier(targetMember)
				+ ": a field whose type is a value class may be laid out inside its owner, so only a"
				+ " reference or a primitive widening into its own wrapper can be written into it."
			);
		}

		private static void validateIsReferenceType(final PersistenceTypeDescriptionMember member)
		{
			if(member.isReference())
			{
				return;
			}
			
			throw new BinaryPersistenceException(
				"Non-reference type " + toTypedIdentifier(member) + " cannot be handled generically."
			);
		}
		
		private static void validateIsReferenceType(final Class<?> type)
		{
			if(!type.isPrimitive())
			{
				return;
			}

			throw new BinaryPersistenceException("Unhandled primitive type: \"" + type.getName() + "\".");
		}
		
		private static String toTypedIdentifier(final PersistenceTypeDescriptionMember member)
		{
			return "\"" + member.typeName() + "\" of "
				+ PersistenceTypeDescriptionMember.class.getSimpleName() + " " + member.identifier()
			;
		}
		
		private BinaryValueSetter lookupCustomValueTranslator(
			final PersistenceTypeDefinition         sourceLegacyType ,
			final PersistenceTypeDescriptionMember  sourceMember     ,
			final PersistenceTypeHandler<Binary, ?> targetCurrentType,
			final PersistenceTypeDescriptionMember  targetMember
		)
		{
			if(this.translatorKeyBuilders == null || this.customTranslatorLookup == null)
			{
				return null;
			}
			
			final XGettingMap<String, BinaryValueSetter> customTranslatorLookup = this.customTranslatorLookup;
			final BinaryValueTranslatorKeyBuilder[]      translatorKeyBuilders  = this.translatorKeyBuilders ;
			
			for(final BinaryValueTranslatorKeyBuilder keyBuilder : translatorKeyBuilders)
			{
				final String key = keyBuilder.buildTranslatorLookupKey(
					sourceLegacyType ,
					sourceMember     ,
					targetCurrentType,
					targetMember
				);
				
				final BinaryValueSetter customValueSetter = customTranslatorLookup.get(key);
				if(customValueSetter != null)
				{
					return customValueSetter;
				}
			}
			
			return null;
		}
		
		@Override
		public BinaryValueSetter provideTargetValueTranslator(
			final PersistenceTypeDefinition         sourceLegacyType ,
			final PersistenceTypeDefinitionMember   sourceMember     ,
			final PersistenceTypeHandler<Binary, ?> targetCurrentType,
			final PersistenceTypeDefinitionMember   targetMember
		)
		{
			if(targetMember == null)
			{
				return this.provideValueSkipper(sourceMember);
			}
			
			// check for potential custom value translator
			final BinaryValueSetter customValueSetter = this.lookupCustomValueTranslator(
				sourceLegacyType ,
				sourceMember     ,
				targetCurrentType,
				targetMember
			);
			if(customValueSetter != null)
			{
				return customValueSetter;
			}
			
			if(sourceMember instanceof PersistenceTypeDescriptionMemberFieldValueStruct)
			{
				return this.provideInlinedValueTranslator(sourceMember, targetMember);
			}

			if(targetMember instanceof PersistenceTypeDefinitionMemberFieldReflective
			&& XReflect.isValueClass(targetMember.type())
			)
			{
				return provideValueTypeFieldSetter(
					sourceMember,
					(PersistenceTypeDefinitionMemberFieldReflective)targetMember,
					this.switchByteOrder
				);
			}

			// note: see #validateCompatibleTargetType for target field type compatability validation.

			// check for generically handleable types on both sides
			final Class<?> sourceType = sourceMember.type();
			final Class<?> targetType = targetMember.type();
			if(sourceType != null && targetType != null)
			{
				return this.provideValueTranslator(sourceType, targetType);
			}
						
			// generic fallback: for two reference fields, simply resolve the OID to a reference/instance.
			return this.provideReferenceResolver(sourceMember, targetMember);
		}
		
		@Override
		public final BinaryValueSetter provideBinaryValueTranslator(
			final PersistenceTypeDefinition         sourceLegacyType ,
			final PersistenceTypeDefinitionMember   sourceMember     ,
			final PersistenceTypeHandler<Binary, ?> targetCurrentType,
			final PersistenceTypeDefinitionMember   targetMember
		)
		{
			/* Before the reference test, not after it: an inlined slot on either side is what this path
			 * cannot rewrite, and a reference source reaching the generic translator below would be
			 * reported as a primitive-versus-reference mismatch, which names neither the slot nor the
			 * reason.
			 */
			if(sourceMember instanceof PersistenceTypeDescriptionMemberFieldValueStruct)
			{
				if(targetMember == null)
				{
					// the current type no longer has the field: step over the slot, writing nothing
					return this.provideValueSkipper(sourceMember);
				}

				/* This path rewrites one binary form into another, so an unchanged slot is copied as it is.
				 * A changed one would have to be laid out anew, which is refused rather than reinterpreted:
				 * the bytes of one layout read as another are wrong values, not a conversion.
				 */
				if(sourceMember.equalsLayout(targetMember))
				{
					return BinaryValueStructFunctions.provideRewriter(
						sourceMember.persistentMinimumLength()
					);
				}

				throw new BinaryPersistenceException(
					"The inlined layout of " + toTypedIdentifier(sourceMember) + " changed and cannot be"
					+ " rewritten into " + toTypedIdentifier(targetMember) + ": a type whose instances are"
					+ " constructed rather than populated is read through its current layout, which the"
					+ " persisted bytes no longer describe."
				);
			}

			if(targetMember instanceof PersistenceTypeDescriptionMemberFieldValueStruct)
			{
				/* Writing a slot means having the value it describes, and this path runs before any
				 * reference is resolved - it has no load handler and the referent does not exist yet.
				 * A type whose instances are constructed reads its members instead of being rewritten
				 * into, which is what BinaryLegacyTypeHandlerValueClass does; anything else has to be
				 * refused here rather than guessed.
				 */
				throw new BinaryPersistenceException(
					"Field " + toTypedIdentifier(sourceMember) + " cannot be rewritten into the inlined slot "
					+ toTypedIdentifier(targetMember) + ": an inlined slot carries the content of a whole"
					+ " type, which a single persisted value does not, and the value it would need cannot"
					+ " be resolved before the referenced entities are loaded."
				);
			}

			if(sourceMember.isReference())
			{
				return BinaryValueTranslators.provideReferenceValueBinaryTranslator(sourceMember, targetMember);
			}

			validateIsPrimitiveType(sourceMember);
			
			// target may be null (meaning the source member/field value shall be skipped)
			if(targetMember != null)
			{
				validateIsPrimitiveType(targetMember);
			}

			// primitives can be handled the normal way: copy/translate the bytes from source to target.
			return this.provideTargetValueTranslator(sourceLegacyType, sourceMember, targetCurrentType, targetMember);
		}
		
	}
	
}
