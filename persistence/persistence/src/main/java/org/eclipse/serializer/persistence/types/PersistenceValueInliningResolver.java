package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
 * %%
 * Copyright (C) 2023 - 2026 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import static org.eclipse.serializer.util.X.notNull;

import java.lang.reflect.Field;
import java.util.function.BiPredicate;

import org.eclipse.serializer.collections.HashEnum;
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.reflect.XReflect;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

/**
 * Decides whether an owner's field is written into the owner's own binary form instead of being referenced
 * by an object id, and supplies the inlined type's fields when it is.
 * <p>
 * An object id is a persistent identity, and a value has none, so referencing one says something about it
 * that is not true. The cost follows from that: with no object registry entry the field is assigned a new
 * object id and stored again on every store of its owner, leaving a superseded copy behind each time.
 * Writing it into the owner instead is the representation that matches what it is, which is why
 * {@link #New(PersistenceTypeAnalyzer)} is the default.
 * <p>
 * It cannot be the only representation. A field declared as {@code Object} or as an interface, and every
 * collection element, has no static type to inline against, and an inlined slot carries no type of its own.
 * Those keep the referenced form, so the two coexist within one owner.
 * <p>
 * What it costs is evolution: the owner's layout embeds the inlined type's layout, so changing that type
 * changes every owner that inlines it.
 *
 * @see PersistenceTypeDescriptionMemberFieldValueStruct
 */
@FunctionalInterface
public interface PersistenceValueInliningResolver
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final Logger logger = Logging.getLogger(PersistenceValueInliningResolver.class);



	/**
	 * Decides whether the passed field is inlined into its owner.
	 *
	 * @param ownerType the class declaring the field.
	 * @param field     the field to decide about.
	 *
	 * @return the inlined type's persistable fields, or {@code null} if the field is not inlined.
	 */
	public XGettingEnum<Field> resolveInlinedFields(Class<?> ownerType, Field field);

	/**
	 * @return a resolver that inlines nothing.
	 */
	public static PersistenceValueInliningResolver Disabled()
	{
		return (ownerType, field) -> null;
	}

	/**
	 * Creates the default resolver: it inlines every eligible field whose type the application itself
	 * declares, and leaves the types the JDK declares referenced.
	 * <p>
	 * A JDK value type is excluded because its persistent form is not the application's to decide: several
	 * have custom type handlers, and the cached instances among them are persisted under reserved constant
	 * ids ({@link Persistence}), both of which inlining would bypass. Opting one in is possible through
	 * {@link #New(PersistenceTypeAnalyzer, BiPredicate)}, but it changes the layout of every owner that
	 * has such a field, so it is a decision to take deliberately.
	 *
	 * @param typeAnalyzer the analyzer determining a type's persistable fields; must not be {@code null}.
	 *
	 * @return the new resolver.
	 */
	public static PersistenceValueInliningResolver New(final PersistenceTypeAnalyzer typeAnalyzer)
	{
		return New(typeAnalyzer, (ownerType, field) -> isApplicationDeclared(field.getType()));
	}

	/**
	 * @param type the type to test.
	 *
	 * @return whether the type is declared by the application rather than by the JDK.
	 */
	public static boolean isApplicationDeclared(final Class<?> type)
	{
		final ClassLoader loader = type.getClassLoader();

		return loader != null && loader != ClassLoader.getPlatformClassLoader();
	}

	/**
	 * @return whether the type has a constructor accepting the passed fields that can actually be invoked.
	 */
	static boolean isConstructorInvocable(final Class<?> type, final XGettingEnum<Field> fields)
	{
		final Class<?>[] parameterTypes = new Class<?>[fields.intSize()];

		int i = 0;
		for(final Field field : fields)
		{
			parameterTypes[i++] = field.getType();
		}

		try
		{
			return type.getDeclaredConstructor(parameterTypes).trySetAccessible();
		}
		catch(final NoSuchMethodException e)
		{
			return false;
		}
	}

	static XGettingEnum<Field> decline(final Field field, final String reason)
	{
		logger.debug("Field {} is not inlined: {}", field, reason);

		return null;
	}

	/**
	 * Creates a resolver that inlines every field the passed selector accepts and that is eligible: the field's
	 * declared type must be a concrete value class whose own persistable fields are each either a primitive or
	 * inlinable by the same rules, so the eligibility test is recursive.
	 * <p>
	 * The type must be a value class because only an identity-less instance can be reconstructed from its
	 * content alone without changing what the owner refers to. It must be the field's declared type because
	 * the inlined form carries no type information, so a field typed as a supertype could not be read back.
	 * Requiring every member to be a primitive or itself inlined keeps the inlined slot free of object ids at
	 * every depth, which is what lets the storage engine skip it as it skips any other fixed-length
	 * non-reference member. A type that would contain itself is refused rather than expanded: such a class
	 * compiles, but its layout has no fixed length.
	 *
	 * @param typeAnalyzer the analyzer determining a type's persistable fields; must not be {@code null}.
	 * @param selector     decides which eligible fields to actually inline; must not be {@code null}.
	 *
	 * @return the new resolver.
	 */
	public static PersistenceValueInliningResolver New(
		final PersistenceTypeAnalyzer            typeAnalyzer,
		final BiPredicate<Class<?>, Field>       selector
	)
	{
		return new PersistenceValueInliningResolver.Default(
			notNull(typeAnalyzer),
			notNull(selector)
		);
	}

	public final class Default implements PersistenceValueInliningResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceTypeAnalyzer      typeAnalyzer;
		private final BiPredicate<Class<?>, Field> selector    ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceTypeAnalyzer      typeAnalyzer,
			final BiPredicate<Class<?>, Field> selector
		)
		{
			super();
			this.typeAnalyzer = typeAnalyzer;
			this.selector     = selector    ;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public XGettingEnum<Field> resolveInlinedFields(final Class<?> ownerType, final Field field)
		{
			return this.resolveInlinedFields(ownerType, field, HashEnum.New());
		}

		/**
		 * @param enclosing the value types the current layout is already nested in, so a type that would
		 *                  contain itself is refused instead of expanded forever.
		 */
		private XGettingEnum<Field> resolveInlinedFields(
			final Class<?>           ownerType,
			final Field              field    ,
			final HashEnum<Class<?>> enclosing
		)
		{
			final Class<?> valueType = field.getType();

			if(!XReflect.isValueClass(valueType))
			{
				// not a value class, so there is nothing to decide and nothing worth reporting
				return null;
			}

			if(enclosing.contains(valueType))
			{
				/* A value class may hold itself, directly or through another, and such a type compiles. Its
				 * inlined layout would have no fixed length and the computation of it would not terminate,
				 * so the cycle has to be refused rather than merely not reached.
				 */
				return decline(field, valueType.getName() + " already encloses this field, so the layout would"
					+ " contain itself"
				);
			}

			if(XReflect.isAbstract(valueType))
			{
				/* An abstract type is not exact: the field may hold subtype instances, whose state the
				 * declared type's layout cannot represent. Only a concrete value class, which is final by
				 * definition, guarantees that the layout written is the layout of every instance held.
				 */
				return decline(field, valueType.getName() + " is abstract, so the field may hold subtype instances");
			}

			if(!this.selector.test(ownerType, field))
			{
				return decline(field, "not selected for inlining");
			}

			final HashEnum<Field> persistable = HashEnum.New();
			final HashEnum<Field> persister   = HashEnum.New();
			final HashEnum<Field> problematic = HashEnum.New();
			this.typeAnalyzer.collectPersistableFieldsEntity(valueType, persistable, persister, problematic);

			if(!persister.isEmpty() || !problematic.isEmpty())
			{
				return decline(field, valueType.getName() + " has persister or problematic fields");
			}

			enclosing.add(valueType);
			try
			{
				for(final Field valueField : persistable)
				{
					if(valueField.getType().isPrimitive())
					{
						continue;
					}

					/* A member that is inlined itself carries no object id either, so the slot stays free of
					 * them at every depth. One that is not leaves the slot holding a reference the storage
					 * engine cannot see, which is what makes the whole layout ineligible rather than only
					 * that member.
					 */
					if(this.resolveInlinedFields(valueType, valueField, enclosing) == null)
					{
						return decline(field, XReflect.deriveFieldIdentifier(valueField)
							+ " is neither a primitive nor inlinable itself"
						);
					}
				}
			}
			finally
			{
				enclosing.removeOne(valueType);
			}

			if(!isConstructorInvocable(valueType, persistable))
			{
				/* The inlined instance is constructed rather than populated, so a constructor that cannot be
				 * reached would only fail later, when the handler is built.
				 */
				return decline(field, valueType.getName() + " has no reachable constructor for its fields");
			}

			return persistable;
		}

	}

}
