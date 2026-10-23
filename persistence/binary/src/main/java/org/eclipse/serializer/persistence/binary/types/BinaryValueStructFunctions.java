package org.eclipse.serializer.persistence.binary.types;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
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

import static org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionMemberFieldValueStruct.NULL_MARKER_ABSENT;
import static org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionMemberFieldValueStruct.NULL_MARKER_LENGTH;
import static org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionMemberFieldValueStruct.NULL_MARKER_PRESENT;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.collections.HashEnum;
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.exceptions.BinaryPersistenceException;
import org.eclipse.serializer.persistence.binary.types.BinaryValueHandleFunctions.FieldReader;
import org.eclipse.serializer.persistence.binary.types.BinaryValueHandleFunctions.FieldWriter;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberField;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldValueStruct;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionMemberFieldValueStruct;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionResolver;
import org.eclipse.serializer.reflect.XReflect;
import org.eclipse.serializer.typing.KeyValue;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

/**
 * Storer and setter for a field that is written into its owner's own binary form rather than referenced by
 * an object id.
 * <p>
 * The slot is a null marker byte followed by the inlined type's own persistent layout, so it is fixed-length
 * and its content is byte-identical to what that type's entity form would contain. A {@code null} field
 * writes the marker and zeroes the rest, which keeps the slot's length independent of its content.
 * <p>
 * Storing walks the inlined type's fields with the same per-field storers an entity of that type would use.
 * Loading cannot mirror that, because the fields of an identity-less instance cannot be written after
 * construction: the values are read into an argument array first and the instance is then constructed from
 * them, the same way its own entity form is reconstructed.
 */
public final class BinaryValueStructFunctions
{
	///////////////////////////////////////////////////////////////////////////
	// static fields //
	//////////////////

	private final static Logger logger = Logging.getLogger(BinaryValueStructFunctions.class);



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Creates the storer writing an inlined field into its owner's binary form.
	 *
	 * @param ownerField      the owner field holding the inlined value; must not be {@code null}.
	 * @param members         the inlined layout's members, in persistent order.
	 * @param structLength    the slot's fixed length, including the null marker.
	 * @param switchByteOrder whether the persistent form uses the reversed byte order.
	 *
	 * @return the storer for the inlined field.
	 */
	public static BinaryValueStorer provideStorer(
		final Field                                                            ownerField     ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMemberField> members        ,
		final long                                                             structLength   ,
		final boolean                                                          switchByteOrder
	)
	{
		final int                 count   = members.intSize();
		final BinaryValueStorer[] storers = new BinaryValueStorer[count];
		final long[]              offsets = new long[count];

		int i = 0;
		for(final PersistenceTypeDefinitionMemberField member : members)
		{
			final Field field = validateField(member);
			if(member instanceof PersistenceTypeDefinitionMemberFieldValueStruct)
			{
				/* A nested slot is written by a storer of its own, which reads its value through a handle on
				 * the field and so needs no offset - a value laid out inside its owner has none to take.
				 */
				final PersistenceTypeDefinitionMemberFieldValueStruct nested =
					(PersistenceTypeDefinitionMemberFieldValueStruct)member
				;
				storers[i] = provideStorer(
					field, nested.members(), nested.persistentMinimumLength(), switchByteOrder
				);
				offsets[i] = 0;
			}
			else
			{
				storers[i] = BinaryValueFunctions.getObjectValueStorer(field.getType(), false, switchByteOrder);
				offsets[i] = XMemory.objectFieldOffset(field);
			}
			i++;
		}

		/* The slot is read back by constructing the inlined type from it, so the same constructor contract
		 * the type's own entity handler verifies applies here. Resolved rather than passed in: the setter
		 * resolves it too, and a storer that never wrote a value would otherwise never have it checked.
		 */
		final Class<?>        valueType        = ownerField.getType();
		final HashEnum<Field> declarationOrder = declarationOrder(members, valueType);

		return new StructStorer(
			BinaryValueHandleFunctions.provideFieldReader(ownerField),
			storers,
			offsets,
			structLength,
			ValueClassConstructorContract.New(
				valueType,
				declarationOrder,
				BinaryHandlerGenericValueClass.resolveConstructor(
					valueType,
					BinaryHandlerGenericValueClass.toParameterTypes(declarationOrder),
					declarationOrder
				)
			)
		);
	}

	/**
	 * Creates the setter reading an inlined field out of its owner's binary form.
	 *
	 * @param ownerField        the owner field holding the inlined value; must not be {@code null}.
	 * @param valueType         the inlined type; must not be {@code null}.
	 * @param members           the inlined layout's members, in persistent order.
	 * @param declarationOrder  the inlined type's persistable fields in declaration order, which is the order
	 *                          its constructor accepts them in.
	 * @param structLength      the slot's fixed length, including the null marker.
	 * @param switchByteOrder   whether the persistent form uses the reversed byte order.
	 *
	 * @return the setter for the inlined field.
	 */
	public static BinaryValueSetter provideSetter(
		final Field                                                            ownerField      ,
		final Class<?>                                                         valueType       ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMemberField> members         ,
		final XGettingEnum<Field>                                              declarationOrder,
		final long                                                             structLength    ,
		final boolean                                                          switchByteOrder
	)
	{
		final int              count   = members.intSize();
		final StructReader[]   readers = new StructReader[count];
		final int[]            targets = new int[count];

		int i = 0;
		for(final PersistenceTypeDefinitionMemberField member : members)
		{
			final Field field = validateField(member);
			readers[i] = provideMemberReader(member, switchByteOrder);

			/* The persistent order need not be the declaration order the constructor accepts, so every slot
			 * carries the argument index it belongs to rather than relying on the two coinciding.
			 */
			targets[i] = indexOf(declarationOrder, field);
			if(targets[i] < 0)
			{
				throw new BinaryPersistenceException(
					"Inlined field " + field + " is not among the persistable fields of " + valueType.getName()
				);
			}
			i++;
		}

		final MethodHandle constructor = BinaryHandlerGenericValueClass.resolveConstructor(
			valueType,
			BinaryHandlerGenericValueClass.toParameterTypes(declarationOrder),
			declarationOrder
		);

		return new StructSetter(
			BinaryValueHandleFunctions.provideFieldWriter(ownerField),
			valueType,
			readers,
			targets,
			constructor,
			declarationOrder.intSize(),
			structLength
		);
	}

	/**
	 * Creates the setter for an inlined field described by a legacy type definition, deriving the inlined
	 * type's declaration order from the type itself.
	 * <p>
	 * Only applicable while the described layout still matches the type's current one, which the caller has
	 * to establish: the constructor takes every field, so a described layout missing one could not be
	 * invoked, and one carrying an extra field would leave bytes unread.
	 *
	 * @param member          the inlined member as described by the legacy definition.
	 * @param switchByteOrder whether the persistent form uses the reversed byte order.
	 *
	 * @return the setter for the inlined field.
	 */
	public static BinaryValueSetter provideSetter(
		final PersistenceTypeDefinitionMemberFieldValueStruct member         ,
		final boolean                                        switchByteOrder
	)
	{
		final Class<?> valueType = member.type();
		if(valueType == null)
		{
			throw new BinaryPersistenceException(
				"Inlined field " + member.identifier() + " has no runtime type."
			);
		}

		final HashEnum<Field> declarationOrder = HashEnum.New();
		for(final Field field : valueType.getDeclaredFields())
		{
			if(!XReflect.isStatic(field) && isDescribed(member, field))
			{
				declarationOrder.add(field);
			}
		}

		if(declarationOrder.intSize() != member.members().intSize())
		{
			throw new BinaryPersistenceException(
				"Inlined layout of " + member.identifier() + " describes " + member.members().intSize()
				+ " fields, but " + valueType.getName() + " has " + declarationOrder.intSize() + " of them."
			);
		}

		return provideSetter(
			member.field()                  ,
			valueType                       ,
			member.members()                ,
			declarationOrder                ,
			member.persistentMinimumLength(),
			switchByteOrder
		);
	}

	/**
	 * Creates the setter for an inlined field whose described layout has since gained or lost a member,
	 * which is what an inlined type evolving under existing data produces.
	 * <p>
	 * The described members are matched to the current ones by name. A member the type no longer has is
	 * stepped over by its persisted length, and a member it has gained takes its type's default - the same
	 * answer legacy mapping gives for a referenced field. What is not translated is a member whose type
	 * changed: its bytes would have to be converted rather than placed, and placing them would misread
	 * silently, so that is refused.
	 * <p>
	 * The two members describe the same field of the same owner, paired by the legacy mapping, so a renamed
	 * inlined type is already accounted for by the time this is reached. A renamed member of that type is
	 * matched by the refactoring mapping, keyed by the member's own identifier ({@code <valueType>#<field>})
	 * exactly as it would be while the field is still referenced. A rule mapping it to {@code null} states
	 * that it was removed deliberately, which discards its persisted value without further complaint.
	 * <p>
	 * A member lost and another of the same type gained, neither of them mapped, is refused: on the wire a
	 * rename is those same two facts, so carrying the value would guess and dropping it would lose it
	 * silently. A mapping entry - to the gained member, or to {@code null} - states which it is.
	 *
	 * @param sourceMember        the inlined member as the legacy definition describes it.
	 * @param targetMember        the inlined member as the current type describes it.
	 * @param refactoringResolver the resolver consulted for member mappings; may be {@code null}.
	 * @param switchByteOrder     whether the persistent form uses the reversed byte order.
	 *
	 * @return the setter for the evolved inlined field.
	 */
	public static BinaryValueSetter provideEvolvingSetter(
		final PersistenceTypeDefinitionMemberFieldValueStruct sourceMember       ,
		final PersistenceTypeDefinitionMemberFieldValueStruct targetMember       ,
		final PersistenceTypeDescriptionResolver              refactoringResolver,
		final boolean                                         switchByteOrder
	)
	{
		final EvolvingLayout layout = evolvingLayout(
			sourceMember, targetMember, refactoringResolver, switchByteOrder
		);

		return new EvolvingStructSetter(
			BinaryValueHandleFunctions.provideFieldWriter(targetMember.field()),
			layout.valueType                                                  ,
			layout.readers                                                    ,
			layout.targets                                                    ,
			layout.constructor                                                ,
			layout.defaults                                                   ,
			layout.defaultedNames                                             ,
			layout.structLength
		);
	}

	/**
	 * The reader for a nested inlined member whose described layout differs from the current one, which is
	 * what an inlined type nested in another evolving under existing data produces. It answers one level
	 * down exactly as {@link #provideEvolvingSetter} does for the owner's field.
	 */
	private static StructReader provideEvolvingNestedReader(
		final PersistenceTypeDefinitionMemberFieldValueStruct sourceMember       ,
		final PersistenceTypeDefinitionMemberFieldValueStruct targetMember       ,
		final PersistenceTypeDescriptionResolver              refactoringResolver,
		final boolean                                         switchByteOrder
	)
	{
		final EvolvingLayout layout = evolvingLayout(
			sourceMember, targetMember, refactoringResolver, switchByteOrder
		);

		return new EvolvingNestedStructReader(
			layout.valueType     ,
			layout.readers       ,
			layout.targets       ,
			layout.constructor   ,
			layout.defaults      ,
			layout.defaultedNames,
			layout.structLength
		);
	}

	/**
	 * Matches a persisted inlined layout to the current one and derives everything needed to read the one
	 * into the other, whether the result is written to a field or handed to an enclosing constructor.
	 */
	private static EvolvingLayout evolvingLayout(
		final PersistenceTypeDefinitionMemberFieldValueStruct sourceMember       ,
		final PersistenceTypeDefinitionMemberFieldValueStruct targetMember       ,
		final PersistenceTypeDescriptionResolver              refactoringResolver,
		final boolean                                         switchByteOrder
	)
	{
		final Class<?> valueType = targetMember.type();
		if(valueType == null)
		{
			throw new BinaryPersistenceException(
				"Inlined field " + targetMember.identifier() + " has no runtime type."
			);
		}

		/* The order the constructor accepts, derived from the type itself the way the unchanged path does,
		 * and restricted to what the current layout describes.
		 */
		final HashEnum<Field> declarationOrder = declarationOrder(targetMember, valueType);

		final BulkList<MemberMatch> matches = matchEvolvingMembers(
			sourceMember, targetMember, refactoringResolver, declarationOrder
		);

		final int            count   = matches.intSize();
		final StructReader[] readers = new StructReader[count];
		final int[]          targets = new int[count];

		int i = 0;
		for(final MemberMatch match : matches)
		{
			if(match.isDropped())
			{
				// the type no longer has this member: step over what was written for it
				final long skipped = match.source.persistentMinimumLength();
				readers[i] = (address, args, index) -> skipped;
				targets[i] = 0;
			}
			else
			{
				readers[i] = match.isNested()
					? provideEvolvingNestedReader(
						match.sourceStruct(), match.targetStruct(), refactoringResolver, switchByteOrder
					)
					: provideReader(match.field.getType(), switchByteOrder)
				;
				targets[i] = indexOf(declarationOrder, match.field);
			}
			i++;
		}

		final HashEnum<Field> defaulted = defaultedFields(declarationOrder, matches);

		final MethodHandle constructor = BinaryHandlerGenericValueClass.resolveConstructor(
			valueType,
			BinaryHandlerGenericValueClass.toParameterTypes(declarationOrder),
			declarationOrder
		);

		return new EvolvingLayout(
			valueType                         ,
			readers                           ,
			targets                           ,
			constructor                       ,
			defaultArguments(declarationOrder),
			fieldNames(defaulted)             ,
			sourceMember.persistentMinimumLength()
		);
	}

	/**
	 * Pairs every persisted member of an inlined layout with the current member it is read into, which is
	 * the half of evolving a layout that does not depend on where it is read from. Both readers build on
	 * it: the one reading a slot inside an owner's entity and the one reading it inside a value class's.
	 * <p>
	 * Everything that can refuse the evolution happens here - a member changing between inlined and plain,
	 * a changed type, two rules pointing at one member, and the ambiguous loss-plus-gain - as does the
	 * report of what was carried, defaulted and dropped.
	 *
	 * @return one match per persisted member, in persisted order.
	 */
	private static BulkList<MemberMatch> matchEvolvingMembers(
		final PersistenceTypeDefinitionMemberFieldValueStruct sourceMember       ,
		final PersistenceTypeDefinitionMemberFieldValueStruct targetMember       ,
		final PersistenceTypeDescriptionResolver              refactoringResolver,
		final HashEnum<Field>                                 declarationOrder
	)
	{
		final BulkList<MemberMatch> matches = BulkList.New();
		final HashEnum<String>      carried = HashEnum.New();

		// persisted members whose value is discarded without a rule saying so, which is worth reporting
		final HashEnum<PersistenceTypeDefinitionMemberField> dropped = HashEnum.New();

		for(final PersistenceTypeDefinitionMemberField source : sourceMember.members())
		{
			// null means a mapping rule states the member was removed deliberately
			final String targetName = resolveTargetName(refactoringResolver, targetMember, source);
			final Field  field      = targetName == null
				? null
				: findField(declarationOrder, targetName)
			;
			if(field == null)
			{
				matches.add(new MemberMatch(source, null, null));
				if(targetName != null)
				{
					dropped.add(source);
				}

				continue;
			}

			final PersistenceTypeDefinitionMemberField target = findMember(targetMember, targetName);

			final boolean sourceIsStruct = source instanceof PersistenceTypeDefinitionMemberFieldValueStruct;
			final boolean targetIsStruct = target instanceof PersistenceTypeDefinitionMemberFieldValueStruct;
			if(sourceIsStruct != targetIsStruct)
			{
				/* The same member, inlined on one side and referenced on the other. The persisted bytes are
				 * a whole layout where the current form expects one value, or the other way round, and
				 * neither can be read as the other.
				 */
				throw new BinaryPersistenceException(
					"Inlined layout member " + source.identifier() + " was persisted "
					+ (sourceIsStruct ? "as an inlined layout and is now a single value"
					                  : "as a single value and is now an inlined layout")
					+ ". Changing whether a member of an inlined layout is itself inlined is not supported."
				);
			}

			if(!isSameType(refactoringResolver, source.typeName(), field.getType()))
			{
				throw new BinaryPersistenceException(
					"Inlined layout member " + source.identifier() + " was persisted as "
					+ source.typeName() + " and is now " + field.getType().getName()
					+ ". Converting the type of an inlined member is not supported."
				);
			}

			matches.add(new MemberMatch(source, target, field));

			if(!carried.add(field.getName()))
			{
				/* Two persisted members reading into one, which only a mapping rule can produce: the
				 * construction would take whichever is read last and discard the other.
				 */
				throw new BinaryPersistenceException(
					"Inlined layout of " + targetMember.identifier() + " reads more than one persisted"
					+ " member into " + field.getName() + ", the last of them " + source.identifier()
					+ ". Every persisted member needs a target of its own, or none."
				);
			}
		}

		final HashEnum<Field> defaulted = HashEnum.New();
		for(final Field field : declarationOrder)
		{
			if(!carried.contains(field.getName()))
			{
				defaulted.add(field);
			}
		}

		validateUnambiguousEvolution(targetMember, dropped, defaulted);
		reportEvolution(targetMember, carried, dropped, defaulted);

		return matches;
	}

	/** The current members no persisted member is read into, which take their type's default. */
	private static HashEnum<Field> defaultedFields(
		final HashEnum<Field>       declarationOrder,
		final BulkList<MemberMatch> matches
	)
	{
		final HashEnum<Field> defaulted = HashEnum.New();

		for(final Field field : declarationOrder)
		{
			if(!matches.containsSearched(match -> field.equals(match.field)))
			{
				defaulted.add(field);
			}
		}

		return defaulted;
	}

	/** One persisted member of an inlined layout and the current member it is read into, if any. */
	private static final class MemberMatch
	{
		final PersistenceTypeDefinitionMemberField source;
		final PersistenceTypeDefinitionMemberField target;
		final Field                                field ;

		MemberMatch(
			final PersistenceTypeDefinitionMemberField source,
			final PersistenceTypeDefinitionMemberField target,
			final Field                                field
		)
		{
			super();
			this.source = source;
			this.target = target;
			this.field  = field ;
		}

		boolean isDropped()
		{
			return this.field == null;
		}

		boolean isNested()
		{
			return this.target instanceof PersistenceTypeDefinitionMemberFieldValueStruct;
		}

		PersistenceTypeDefinitionMemberFieldValueStruct sourceStruct()
		{
			return (PersistenceTypeDefinitionMemberFieldValueStruct)this.source;
		}

		PersistenceTypeDefinitionMemberFieldValueStruct targetStruct()
		{
			return (PersistenceTypeDefinitionMemberFieldValueStruct)this.target;
		}
	}

	/**
	 * Whether the persisted member's type is the current field's, either by name or because a refactoring
	 * rule renamed it - the same rule that carries such a rename while the type is referenced rather than
	 * nested, so the two forms answer alike.
	 */
	private static boolean isSameType(
		final PersistenceTypeDescriptionResolver resolver     ,
		final String                             persistedName,
		final Class<?>                           currentType
	)
	{
		if(currentType.getName().equals(persistedName))
		{
			return true;
		}

		return resolver != null && currentType.getName().equals(resolver.resolveRuntimeTypeName(persistedName));
	}

	/** The current member of the passed name, or {@code null} where the current layout has none. */
	private static PersistenceTypeDefinitionMemberField findMember(
		final PersistenceTypeDefinitionMemberFieldValueStruct targetMember,
		final String                                         name
	)
	{
		for(final PersistenceTypeDefinitionMemberField member : targetMember.members())
		{
			if(member.name().equals(name))
			{
				return member;
			}
		}

		return null;
	}

	/** Everything needed to read a persisted inlined layout into the current one. */
	private static final class EvolvingLayout
	{
		final Class<?>       valueType     ;
		final StructReader[] readers       ;
		final int[]          targets       ;
		final MethodHandle   constructor   ;
		final Object[]       defaults      ;
		final String         defaultedNames;
		final long           structLength  ;

		EvolvingLayout(
			final Class<?>       valueType     ,
			final StructReader[] readers       ,
			final int[]          targets       ,
			final MethodHandle   constructor   ,
			final Object[]       defaults      ,
			final String         defaultedNames,
			final long           structLength
		)
		{
			super();
			this.valueType      = valueType     ;
			this.readers        = readers       ;
			this.targets        = targets       ;
			this.constructor    = constructor   ;
			this.defaults       = defaults      ;
			this.defaultedNames = defaultedNames;
			this.structLength   = structLength  ;
		}

	}

	/**
	 * The name of the current member the persisted one is to be read into: the one a refactoring rule names,
	 * or the persisted member's own name where no rule applies.
	 * <p>
	 * The rule is keyed by the member's own identifier, which is the inlined type's name and the field's,
	 * the same key a rename of that field carries while it is still referenced rather than inlined.
	 *
	 * @return the current member's name, or {@code null} where a rule states the member was removed.
	 */
	private static String resolveTargetName(
		final PersistenceTypeDescriptionResolver              resolver    ,
		final PersistenceTypeDefinitionMemberFieldValueStruct targetMember,
		final PersistenceTypeDefinitionMemberField            source
	)
	{
		if(resolver == null)
		{
			return source.name();
		}

		final KeyValue<String, String> entry = resolver.lookup(source.identifier());
		if(entry == null)
		{
			return source.name();
		}
		if(entry.value() == null)
		{
			// can be null for members explicitly marked as deleted
			return null;
		}

		for(final PersistenceTypeDefinitionMemberField member : targetMember.members())
		{
			if(entry.value().equals(member.identifier())
			|| entry.value().equals(XReflect.fieldIdentifierDelimiter() + member.name())
			)
			{
				return member.name();
			}
		}

		throw new BinaryPersistenceException(
			"Unresolvable inlined member refactoring mapping: " + source.identifier() + " -> \""
			+ entry.value() + "\", which is no member of the inlined layout of " + targetMember.identifier()
			+ "."
		);
	}

	/**
	 * Refuses a layout that lost one member and gained another of the same type without either of them being
	 * mapped.
	 * <p>
	 * A rename and a removal plus an addition are the same two facts on the wire, so there is nothing to tell
	 * them apart by: carrying the value over would guess, and the alternative loses it without saying so. A
	 * mapping entry states which of the two it is, and the refusal names the entry to write.
	 */
	private static void validateUnambiguousEvolution(
		final PersistenceTypeDefinitionMemberFieldValueStruct              targetMember,
		final XGettingEnum<? extends PersistenceTypeDefinitionMemberField> dropped     ,
		final XGettingEnum<Field>                                          defaulted
	)
	{
		for(final PersistenceTypeDefinitionMemberField drop : dropped)
		{
			for(final Field gain : defaulted)
			{
				if(!gain.getType().getName().equals(drop.typeName()))
				{
					continue;
				}

				throw new BinaryPersistenceException(
					"Inlined layout of " + targetMember.identifier() + " lost member " + drop.identifier()
					+ " and gained member " + gain.getName() + ", both of type " + drop.typeName()
					+ ". A renamed member and a removed one plus an added one cannot be told apart here,"
					+ " so the persisted value would either be placed by guess or discarded silently."
					+ " State which it is by mapping \"" + drop.identifier() + "\" to \""
					+ gain.getDeclaringClass().getName() + XReflect.fieldIdentifierDelimiter()
					+ gain.getName() + "\" to carry the value over, or to null to discard it."
				);
			}
		}
	}

	/**
	 * States what became of the persisted layout's members, which the mapping report for the owner cannot
	 * show: there the inlined field is one member mapped to one member, and this is what happened inside it.
	 */
	private static void reportEvolution(
		final PersistenceTypeDefinitionMemberFieldValueStruct              targetMember,
		final XGettingEnum<String>                                         carried     ,
		final XGettingEnum<? extends PersistenceTypeDefinitionMemberField> dropped     ,
		final XGettingEnum<Field>                                          defaulted
	)
	{
		final VarString vs = VarString.New()
			.add("Inlined layout of ").add(targetMember.identifier())
			.add(" differs from the persisted one. Carried: ").list(", ", carried)
			.add(". Defaulted: ").add(fieldNames(defaulted))
			.add(". Dropped: ").add(memberNames(dropped))
			.add('.')
		;

		if(dropped.isEmpty())
		{
			logger.info(vs.toString());
		}
		else
		{
			logger.warn(vs.add(" A dropped member's persisted value is discarded.").toString());
		}
	}

	private static String fieldNames(final XGettingEnum<Field> fields)
	{
		final VarString vs = VarString.New();
		for(final Field field : fields)
		{
			vs.add(vs.isEmpty() ? "" : ", ").add(field.getName());
		}

		return vs.toString();
	}

	private static String memberNames(
		final XGettingEnum<? extends PersistenceTypeDefinitionMemberField> members
	)
	{
		final VarString vs = VarString.New();
		for(final PersistenceTypeDefinitionMemberField member : members)
		{
			vs.add(vs.isEmpty() ? "" : ", ").add(member.name());
		}

		return vs.toString();
	}

	/**
	 * The argument array a construction starts from, so a member the persisted layout does not carry takes
	 * its type's default.
	 */
	private static Object[] defaultArguments(final XGettingEnum<Field> declarationOrder)
	{
		final Object[] defaults = new Object[declarationOrder.intSize()];

		/* Deliberately not a conditional-operator chain: mixing the box types in one would have binary
		 * numeric promotion widen every branch to the same type, so every default would come back as the
		 * widest of them and the constructor would reject it.
		 */
		int i = 0;
		for(final Field field : declarationOrder)
		{
			defaults[i++] = defaultValue(field.getType());
		}

		return defaults;
	}

	/**
	 * The value a member takes where the persisted layout does not carry one: the type's own default.
	 * Shared with {@link BinaryLegacyTypeHandlerValueClass}, which starts its argument array from these.
	 *
	 * @param type the member's type.
	 *
	 * @return the boxed default, or {@code null} for a non-primitive.
	 */
	static Object defaultValue(final Class<?> type)
	{
		if(type == byte.class)
		{
			return Byte.valueOf((byte)0);
		}
		if(type == boolean.class)
		{
			return Boolean.FALSE;
		}
		if(type == short.class)
		{
			return Short.valueOf((short)0);
		}
		if(type == char.class)
		{
			return Character.valueOf('\0');
		}
		if(type == int.class)
		{
			return Integer.valueOf(0);
		}
		if(type == float.class)
		{
			return Float.valueOf(0f);
		}
		if(type == long.class)
		{
			return Long.valueOf(0L);
		}
		if(type == double.class)
		{
			return Double.valueOf(0d);
		}

		/* A member of an inlined layout is either a primitive or inlined itself, and the default of the
		 * latter is the absence of a value - which is what its own slot's marker states for it.
		 */
		return null;
	}

	private static Field findField(final XGettingEnum<Field> fields, final String name)
	{
		for(final Field field : fields)
		{
			if(field.getName().equals(name))
			{
				return field;
			}
		}

		return null;
	}

	private static boolean isDescribed(
		final PersistenceTypeDefinitionMemberFieldValueStruct member,
		final Field                                          field
	)
	{
		return isDescribed(member.members(), field);
	}

	private static boolean isDescribed(
		final XGettingSequence<? extends PersistenceTypeDefinitionMemberField> members,
		final Field                                                            field
	)
	{
		for(final PersistenceTypeDefinitionMemberField describedMember : members)
		{
			if(field.getName().equals(describedMember.name()))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Creates the translator copying an inlined slot from a persisted layout into the current one, for the
	 * rerouting path that rewrites the binary form rather than writing into an instance.
	 * <p>
	 * Only applicable while the two layouts are equal, which the caller has to establish: the bytes are
	 * copied as they are, so a layout that changed would be reinterpreted rather than converted. Byte order
	 * needs no handling, since source and target are the same persisted form.
	 *
	 * @param structLength the slot's fixed length, including the null marker.
	 *
	 * @return the translator copying the slot.
	 */
	public static BinaryValueSetter provideRewriter(final long structLength)
	{
		return (srcAddress, target, trgOffset, handler) ->
		{
			XMemory.copyRange(srcAddress, trgOffset, structLength);

			return srcAddress + structLength;
		};
	}

	/**
	 * Creates the setter skipping an inlined field whose owner no longer has it.
	 *
	 * @param member the inlined member as described by the legacy definition.
	 *
	 * @return a setter advancing past the slot without reading it.
	 */
	public static BinaryValueSetter provideSkipper(
		final PersistenceTypeDescriptionMemberFieldValueStruct member
	)
	{
		final long structLength = member.persistentMinimumLength();

		return (srcAddress, target, trgOffset, handler) -> srcAddress + structLength;
	}

	private static Field validateField(final PersistenceTypeDefinitionMemberField member)
	{
		final Field field = member.field();
		if(field == null)
		{
			throw new BinaryPersistenceException(
				"Inlined layout member " + member.identifier() + " has no runtime field."
			);
		}

		return field;
	}

	private static int indexOf(final XGettingEnum<Field> fields, final Field field)
	{
		int i = 0;
		for(final Field f : fields)
		{
			if(f.equals(field))
			{
				return i;
			}
			i++;
		}

		return -1;
	}

	/**
	 * Creates the reader for an inlined field of a value class, which is read out of the entity's own data
	 * rather than out of an enclosing slot.
	 * <p>
	 * A value instance is constructed rather than populated, so its members are read through
	 * {@link BinaryValueReader}s into an argument array instead of being written to fields. An inlined
	 * member is read the same way, one level down: its slot's marker decides whether a value is
	 * constructed at all.
	 * <p>
	 * Byte order needs no handling here, for the reason {@link BinaryValueReader#provideReader} states: a
	 * load item for data in a non-native byte order reverses every value it reads.
	 *
	 * @param member the inlined member as the current type describes it.
	 *
	 * @return the reader for the inlined member.
	 */
	public static BinaryValueReader provideValueReader(
		final PersistenceTypeDefinitionMemberFieldValueStruct member
	)
	{
		final Class<?> valueType = member.type();
		if(valueType == null)
		{
			throw new BinaryPersistenceException(
				"Inlined layout member " + member.identifier() + " has no runtime type."
			);
		}

		final HashEnum<Field> declarationOrder = declarationOrder(member, valueType);

		final int                 count   = member.members().intSize();
		final BinaryValueReader[] readers = new BinaryValueReader[count];
		final long[]              offsets = new long[count];
		final int[]               targets = new int[count];

		// the marker precedes the content, so the first member sits behind it
		long offset = NULL_MARKER_LENGTH;
		int  i      = 0;
		for(final PersistenceTypeDefinitionMemberField nested : member.members())
		{
			final Field field = validateField(nested);
			readers[i] = nested instanceof PersistenceTypeDefinitionMemberFieldValueStruct
				? provideValueReader((PersistenceTypeDefinitionMemberFieldValueStruct)nested)
				: BinaryValueReader.provideReader(field.getType())
			;
			offsets[i] = offset;
			targets[i] = indexOf(declarationOrder, field);
			if(targets[i] < 0)
			{
				throw new BinaryPersistenceException(
					"Inlined field " + field + " is not among the persistable fields of " + valueType.getName()
				);
			}
			offset += nested.persistentMinimumLength();
			i++;
		}

		final MethodHandle constructor = BinaryHandlerGenericValueClass.resolveConstructor(
			valueType,
			BinaryHandlerGenericValueClass.toParameterTypes(declarationOrder),
			declarationOrder
		);

		return new StructValueReader(valueType, readers, offsets, targets, constructor, declarationOrder.intSize());
	}

	/**
	 * Creates the reader for an inlined field of a value class whose described layout differs from the
	 * current one - the counterpart of {@link #provideEvolvingSetter} for a type that is constructed rather
	 * than populated, and the reason a value class stored as an entity can evolve an inlined member of its
	 * own.
	 * <p>
	 * The members are paired exactly as they are for an owner's field, by
	 * {@link #matchEvolvingMembers}, so the same renames are carried, the same mapping rules apply and the
	 * same shapes are refused. Only the reading differs: each carried member is read at its own offset in
	 * the persisted slot, so a dropped one is simply never read rather than stepped over.
	 *
	 * @param sourceMember        the inlined member as the legacy definition describes it.
	 * @param targetMember        the inlined member as the current type describes it.
	 * @param refactoringResolver the resolver consulted for member mappings; may be {@code null}.
	 *
	 * @return the reader for the evolved inlined member.
	 */
	public static BinaryValueReader provideEvolvingValueReader(
		final PersistenceTypeDefinitionMemberFieldValueStruct sourceMember       ,
		final PersistenceTypeDefinitionMemberFieldValueStruct targetMember       ,
		final PersistenceTypeDescriptionResolver              refactoringResolver
	)
	{
		final Class<?> valueType = targetMember.type();
		if(valueType == null)
		{
			throw new BinaryPersistenceException(
				"Inlined field " + targetMember.identifier() + " has no runtime type."
			);
		}

		final HashEnum<Field>       declarationOrder = declarationOrder(targetMember, valueType);
		final BulkList<MemberMatch> matches          = matchEvolvingMembers(
			sourceMember, targetMember, refactoringResolver, declarationOrder
		);

		final BulkList<BinaryValueReader> readers = BulkList.New();
		final BulkList<Long>              offsets = BulkList.New();
		final BulkList<Integer>           targets = BulkList.New();

		// the marker precedes the content, so the first member sits behind it
		long offset = NULL_MARKER_LENGTH;
		for(final MemberMatch match : matches)
		{
			if(!match.isDropped())
			{
				readers.add(match.isNested()
					? provideEvolvingValueReader(match.sourceStruct(), match.targetStruct(), refactoringResolver)
					: BinaryValueReader.provideReader(match.field.getType())
				);
				offsets.add(offset);
				targets.add(indexOf(declarationOrder, match.field));
			}

			offset += match.source.persistentMinimumLength();
		}

		final HashEnum<Field> defaulted = defaultedFields(declarationOrder, matches);

		final MethodHandle constructor = BinaryHandlerGenericValueClass.resolveConstructor(
			valueType,
			BinaryHandlerGenericValueClass.toParameterTypes(declarationOrder),
			declarationOrder
		);

		return new EvolvingStructValueReader(
			valueType                                                      ,
			readers.toArray(BinaryValueReader.class)                       ,
			toLongArray(offsets)                                           ,
			toIntArray(targets)                                            ,
			constructor                                                    ,
			defaultArguments(declarationOrder)                             ,
			fieldNames(defaulted)
		);
	}

	private static long[] toLongArray(final BulkList<Long> values)
	{
		final long[] array = new long[values.intSize()];

		int i = 0;
		for(final Long value : values)
		{
			array[i++] = value.longValue();
		}

		return array;
	}

	private static int[] toIntArray(final BulkList<Integer> values)
	{
		final int[] array = new int[values.intSize()];

		int i = 0;
		for(final Integer value : values)
		{
			array[i++] = value.intValue();
		}

		return array;
	}

	/**
	 * The reader for one member of an inlined layout: a nested inlined member is read as a slot of its own
	 * and constructed, anything else is read as the primitive it is.
	 */
	private static StructReader provideMemberReader(
		final PersistenceTypeDefinitionMemberField member         ,
		final boolean                             switchByteOrder
	)
	{
		if(member instanceof PersistenceTypeDefinitionMemberFieldValueStruct)
		{
			return provideNestedReader(
				(PersistenceTypeDefinitionMemberFieldValueStruct)member, switchByteOrder
			);
		}

		return provideReader(validateField(member).getType(), switchByteOrder);
	}

	/**
	 * The reader for an inlined member that is itself inlined: it reads the nested slot's own marker and
	 * members, constructs the instance and places it in the enclosing layout's argument array, the same way
	 * {@link StructSetter} does for the owner - only returning the value instead of writing it to a field.
	 */
	private static StructReader provideNestedReader(
		final PersistenceTypeDefinitionMemberFieldValueStruct member         ,
		final boolean                                        switchByteOrder
	)
	{
		final Class<?> valueType = member.type();
		if(valueType == null)
		{
			throw new BinaryPersistenceException(
				"Inlined layout member " + member.identifier() + " has no runtime type."
			);
		}

		final HashEnum<Field> declarationOrder = declarationOrder(member, valueType);

		final int            count   = member.members().intSize();
		final StructReader[] readers = new StructReader[count];
		final int[]          targets = new int[count];

		int i = 0;
		for(final PersistenceTypeDefinitionMemberField nested : member.members())
		{
			final Field field = validateField(nested);
			readers[i] = provideMemberReader(nested, switchByteOrder);
			targets[i] = indexOf(declarationOrder, field);
			if(targets[i] < 0)
			{
				throw new BinaryPersistenceException(
					"Inlined field " + field + " is not among the persistable fields of " + valueType.getName()
				);
			}
			i++;
		}

		final MethodHandle constructor = BinaryHandlerGenericValueClass.resolveConstructor(
			valueType,
			BinaryHandlerGenericValueClass.toParameterTypes(declarationOrder),
			declarationOrder
		);

		return new NestedStructReader(
			valueType                       ,
			readers                         ,
			targets                         ,
			constructor                     ,
			declarationOrder.intSize()      ,
			member.persistentMinimumLength()
		);
	}

	/**
	 * The inlined type's described fields in declaration order, which is the order its constructor accepts
	 * them in - derived from the type itself, as the persistent order need not match it.
	 */
	private static HashEnum<Field> declarationOrder(
		final PersistenceTypeDefinitionMemberFieldValueStruct member   ,
		final Class<?>                                       valueType
	)
	{
		return declarationOrder(member.members(), valueType);
	}

	private static HashEnum<Field> declarationOrder(
		final XGettingSequence<? extends PersistenceTypeDefinitionMemberField> members  ,
		final Class<?>                                                         valueType
	)
	{
		final HashEnum<Field> declarationOrder = HashEnum.New();
		for(final Field field : valueType.getDeclaredFields())
		{
			if(!XReflect.isStatic(field) && isDescribed(members, field))
			{
				declarationOrder.add(field);
			}
		}

		return declarationOrder;
	}

	private static StructReader provideReader(final Class<?> type, final boolean switchByteOrder)
	{
		if(switchByteOrder)
		{
			return provideReaderReversed(type);
		}

		if(type == byte.class)
		{
			return (address, args, index) -> { args[index] = XMemory.get_byte(address); return Byte.BYTES; };
		}
		if(type == boolean.class)
		{
			return (address, args, index) -> { args[index] = XMemory.get_boolean(address); return Byte.BYTES; };
		}
		if(type == short.class)
		{
			return (address, args, index) -> { args[index] = XMemory.get_short(address); return Short.BYTES; };
		}
		if(type == char.class)
		{
			return (address, args, index) -> { args[index] = XMemory.get_char(address); return Character.BYTES; };
		}
		if(type == int.class)
		{
			return (address, args, index) -> { args[index] = XMemory.get_int(address); return Integer.BYTES; };
		}
		if(type == float.class)
		{
			return (address, args, index) -> { args[index] = XMemory.get_float(address); return Float.BYTES; };
		}
		if(type == long.class)
		{
			return (address, args, index) -> { args[index] = XMemory.get_long(address); return Long.BYTES; };
		}
		if(type == double.class)
		{
			return (address, args, index) -> { args[index] = XMemory.get_double(address); return Double.BYTES; };
		}

		throw new BinaryPersistenceException("Type cannot be inlined: " + type.getName());
	}

	private static StructReader provideReaderReversed(final Class<?> type)
	{
		if(type == byte.class)
		{
			return (address, args, index) -> { args[index] = XMemory.get_byte(address); return Byte.BYTES; };
		}
		if(type == boolean.class)
		{
			return (address, args, index) -> { args[index] = XMemory.get_boolean(address); return Byte.BYTES; };
		}
		if(type == short.class)
		{
			return (address, args, index) ->
			{
				args[index] = Short.reverseBytes(XMemory.get_short(address));
				return Short.BYTES;
			};
		}
		if(type == char.class)
		{
			return (address, args, index) ->
			{
				args[index] = Character.reverseBytes(XMemory.get_char(address));
				return Character.BYTES;
			};
		}
		if(type == int.class)
		{
			return (address, args, index) ->
			{
				args[index] = Integer.reverseBytes(XMemory.get_int(address));
				return Integer.BYTES;
			};
		}
		if(type == float.class)
		{
			return (address, args, index) ->
			{
				args[index] = Float.intBitsToFloat(Integer.reverseBytes(XMemory.get_int(address)));
				return Float.BYTES;
			};
		}
		if(type == long.class)
		{
			return (address, args, index) ->
			{
				args[index] = Long.reverseBytes(XMemory.get_long(address));
				return Long.BYTES;
			};
		}
		if(type == double.class)
		{
			return (address, args, index) ->
			{
				args[index] = Double.longBitsToDouble(Long.reverseBytes(XMemory.get_long(address)));
				return Double.BYTES;
			};
		}

		throw new BinaryPersistenceException("Type cannot be inlined: " + type.getName());
	}



	///////////////////////////////////////////////////////////////////////////
	// member types //
	/////////////////

	@FunctionalInterface
	private interface StructReader
	{
		/**
		 * Reads one inlined value into the constructor argument it belongs to.
		 *
		 * @return the number of bytes read.
		 */
		long readValue(long address, Object[] args, int index);
	}

	/**
	 * Reads an inlined slot out of a value class's own entity data and returns the instance it describes,
	 * so the enclosing value class can pass it to its constructor. An absent marker yields {@code null}.
	 */
	private static final class StructValueReader implements BinaryValueReader
	{
		private final Class<?>            valueType    ;
		private final BinaryValueReader[] readers      ;
		private final long[]              offsets      ;
		private final int[]               targets      ;
		private final MethodHandle        constructor  ;
		private final int                 argumentCount;

		StructValueReader(
			final Class<?>            valueType    ,
			final BinaryValueReader[] readers      ,
			final long[]              offsets      ,
			final int[]               targets      ,
			final MethodHandle        constructor  ,
			final int                 argumentCount
		)
		{
			super();
			this.valueType     = valueType    ;
			this.readers       = readers      ;
			this.offsets       = offsets      ;
			this.targets       = targets      ;
			this.constructor   = constructor  ;
			this.argumentCount = argumentCount;
		}

		@Override
		public Object readValue(final Binary data, final long offset, final PersistenceLoadHandler handler)
		{
			if(data.read_byte(offset) == NULL_MARKER_ABSENT)
			{
				return null;
			}

			final Object[] args = new Object[this.argumentCount];
			for(int i = 0; i < this.readers.length; i++)
			{
				args[this.targets[i]] = this.readers[i].readValue(data, offset + this.offsets[i], handler);
			}

			try
			{
				return (Object)this.constructor.invokeExact(args);
			}
			catch(final Error e)
			{
				throw e;
			}
			catch(final Throwable t)
			{
				throw new BinaryPersistenceException(
					"Could not construct inlined instance of " + this.valueType.getName(), t
				);
			}
		}

	}

	/**
	 * Reads an inlined slot whose described layout differs from the current one out of a value class's own
	 * entity data. The argument array starts from the current type's defaults, so a member the persisted
	 * layout does not carry keeps its default, and one the type no longer has is simply never read.
	 */
	private static final class EvolvingStructValueReader implements BinaryValueReader
	{
		private final Class<?>            valueType     ;
		private final BinaryValueReader[] readers       ;
		private final long[]              offsets       ;
		private final int[]               targets       ;
		private final MethodHandle        constructor   ;
		private final Object[]            defaults      ;
		private final String              defaultedNames;

		EvolvingStructValueReader(
			final Class<?>            valueType     ,
			final BinaryValueReader[] readers       ,
			final long[]              offsets       ,
			final int[]               targets       ,
			final MethodHandle        constructor   ,
			final Object[]            defaults      ,
			final String              defaultedNames
		)
		{
			super();
			this.valueType      = valueType     ;
			this.readers        = readers       ;
			this.offsets        = offsets       ;
			this.targets        = targets       ;
			this.constructor    = constructor   ;
			this.defaults       = defaults      ;
			this.defaultedNames = defaultedNames;
		}

		@Override
		public Object readValue(final Binary data, final long offset, final PersistenceLoadHandler handler)
		{
			if(data.read_byte(offset) == NULL_MARKER_ABSENT)
			{
				return null;
			}

			final Object[] args = this.defaults.clone();
			for(int i = 0; i < this.readers.length; i++)
			{
				args[this.targets[i]] = this.readers[i].readValue(data, offset + this.offsets[i], handler);
			}

			try
			{
				return (Object)this.constructor.invokeExact(args);
			}
			catch(final Error e)
			{
				throw e;
			}
			catch(final Throwable t)
			{
				throw new BinaryPersistenceException(
					"Could not construct inlined instance of " + this.valueType.getName()
					+ (this.defaultedNames.isEmpty()
						? ""
						: ", whose persisted layout did not carry " + this.defaultedNames
						+ " so it was constructed with the default")
					, t
				);
			}
		}

	}

	/**
	 * Reads a nested inlined slot whose described layout differs from the current one: the argument array
	 * starts from the current type's defaults, so a member the persisted layout does not carry keeps its
	 * default, and a reader for a member the type no longer has advances past it without writing.
	 */
	private static final class EvolvingNestedStructReader implements StructReader
	{
		private final Class<?>       valueType     ;
		private final StructReader[] readers       ;
		private final int[]          targets       ;
		private final MethodHandle   constructor   ;
		private final Object[]       defaults      ;
		private final String         defaultedNames;
		private final long           structLength  ;

		EvolvingNestedStructReader(
			final Class<?>       valueType     ,
			final StructReader[] readers       ,
			final int[]          targets       ,
			final MethodHandle   constructor   ,
			final Object[]       defaults      ,
			final String         defaultedNames,
			final long           structLength
		)
		{
			super();
			this.valueType      = valueType     ;
			this.readers        = readers       ;
			this.targets        = targets       ;
			this.constructor    = constructor   ;
			this.defaults       = defaults      ;
			this.defaultedNames = defaultedNames;
			this.structLength   = structLength  ;
		}

		@Override
		public long readValue(final long address, final Object[] args, final int index)
		{
			if(XMemory.get_byte(address) == NULL_MARKER_ABSENT)
			{
				args[index] = null;

				return this.structLength;
			}

			final Object[] nested = this.defaults.clone();

			long a = address + NULL_MARKER_LENGTH;
			for(int i = 0; i < this.readers.length; i++)
			{
				a += this.readers[i].readValue(a, nested, this.targets[i]);
			}

			args[index] = this.createValue(nested);

			return this.structLength;
		}

		private Object createValue(final Object[] args)
		{
			try
			{
				return (Object)this.constructor.invokeExact(args);
			}
			catch(final Error e)
			{
				throw e;
			}
			catch(final Throwable t)
			{
				throw new BinaryPersistenceException(
					"Could not construct nested inlined instance of " + this.valueType.getName()
					+ (this.defaultedNames.isEmpty()
						? ""
						: ", whose persisted layout did not carry " + this.defaultedNames
						+ " so it was constructed with the default")
					, t
				);
			}
		}

	}

	/**
	 * Reads a nested inlined slot and returns the instance it describes, so the layout enclosing it can pass
	 * it to its own constructor. An absent marker yields {@code null}, and the slot's fixed length is
	 * returned either way - that length, not the bytes actually read, is what the enclosing layout advances
	 * by.
	 */
	private static final class NestedStructReader implements StructReader
	{
		private final Class<?>       valueType    ;
		private final StructReader[] readers      ;
		private final int[]          targets      ;
		private final MethodHandle   constructor  ;
		private final int            argumentCount;
		private final long           structLength ;

		NestedStructReader(
			final Class<?>       valueType    ,
			final StructReader[] readers      ,
			final int[]          targets      ,
			final MethodHandle   constructor  ,
			final int            argumentCount,
			final long           structLength
		)
		{
			super();
			this.valueType     = valueType    ;
			this.readers       = readers      ;
			this.targets       = targets      ;
			this.constructor   = constructor  ;
			this.argumentCount = argumentCount;
			this.structLength  = structLength ;
		}

		@Override
		public long readValue(final long address, final Object[] args, final int index)
		{
			if(XMemory.get_byte(address) == NULL_MARKER_ABSENT)
			{
				args[index] = null;

				return this.structLength;
			}

			final Object[] nested = new Object[this.argumentCount];

			long a = address + NULL_MARKER_LENGTH;
			for(int i = 0; i < this.readers.length; i++)
			{
				a += this.readers[i].readValue(a, nested, this.targets[i]);
			}

			args[index] = this.createValue(nested);

			return this.structLength;
		}

		private Object createValue(final Object[] args)
		{
			try
			{
				return (Object)this.constructor.invokeExact(args);
			}
			catch(final Error e)
			{
				throw e;
			}
			catch(final Throwable t)
			{
				throw new BinaryPersistenceException(
					"Could not construct nested inlined instance of " + this.valueType.getName(), t
				);
			}
		}

	}

	private static final class StructStorer implements BinaryValueStorer
	{
		private final FieldReader                   ownerReader ;
		private final BinaryValueStorer[]           storers     ;
		private final long[]                        offsets     ;
		private final long                          structLength;
		private final ValueClassConstructorContract contract    ;

		StructStorer(
			final FieldReader                   ownerReader ,
			final BinaryValueStorer[]           storers     ,
			final long[]                        offsets     ,
			final long                          structLength,
			final ValueClassConstructorContract contract
		)
		{
			super();
			this.ownerReader  = ownerReader ;
			this.storers      = storers     ;
			this.offsets      = offsets     ;
			this.structLength = structLength;
			this.contract     = contract    ;
		}

		@Override
		public long storeValueFromMemory(
			final Object                          source       ,
			final long                            sourceOffset ,
			final long                            targetAddress,
			final PersistenceStoreHandler<Binary> persister
		)
		{
			// reached through a handle: a value may be laid out inside its owner, with no reference at its offset
			final Object value = this.ownerReader.readValue(source);
			if(value == null)
			{
				// zeroed rather than skipped, so the slot's content never depends on what was there before
				XMemory.fillMemory(targetAddress, this.structLength, (byte)0);
				return targetAddress + this.structLength;
			}

			// before the first slot is written, so a violation is reported instead of persisted
			this.contract.validate(value);

			XMemory.set_byte(targetAddress, NULL_MARKER_PRESENT);

			long address = targetAddress + NULL_MARKER_LENGTH;
			for(int i = 0; i < this.storers.length; i++)
			{
				address = this.storers[i].storeValueFromMemory(value, this.offsets[i], address, persister);
			}

			return address;
		}

	}

	/**
	 * Reads an inlined slot whose described layout differs from the current one: the argument array starts
	 * from the current type's defaults, so a member the persisted layout does not carry keeps its default,
	 * and a reader for a member the type no longer has advances past it without writing.
	 */
	private static final class EvolvingStructSetter implements BinaryValueSetter
	{
		private final FieldWriter    ownerWriter    ;
		private final Class<?>       valueType      ;
		private final StructReader[] readers        ;
		private final int[]          targets        ;
		private final MethodHandle   constructor    ;
		private final Object[]       defaults       ;
		private final String         defaultedNames ;
		private final long           structLength   ;

		EvolvingStructSetter(
			final FieldWriter    ownerWriter   ,
			final Class<?>       valueType     ,
			final StructReader[] readers       ,
			final int[]          targets       ,
			final MethodHandle   constructor   ,
			final Object[]       defaults      ,
			final String         defaultedNames,
			final long           structLength
		)
		{
			super();
			this.ownerWriter    = ownerWriter   ;
			this.valueType      = valueType     ;
			this.readers        = readers       ;
			this.targets        = targets       ;
			this.constructor    = constructor   ;
			this.defaults       = defaults      ;
			this.defaultedNames = defaultedNames;
			this.structLength   = structLength  ;
		}

		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			if(XMemory.get_byte(srcAddress) == NULL_MARKER_ABSENT)
			{
				this.ownerWriter.writeValue(target, null);
				return srcAddress + this.structLength;
			}

			final Object[] args = this.defaults.clone();

			long address = srcAddress + NULL_MARKER_LENGTH;
			for(int i = 0; i < this.readers.length; i++)
			{
				address += this.readers[i].readValue(address, args, this.targets[i]);
			}

			this.ownerWriter.writeValue(target, this.createValue(args));

			return address;
		}

		private Object createValue(final Object[] args)
		{
			try
			{
				return (Object)this.constructor.invokeExact(args);
			}
			catch(final Error e)
			{
				throw e;
			}
			catch(final Throwable t)
			{
				/* Naming the defaulted members is the point: a constructor validating its arguments is the
				 * likely reason a construction that used to work now fails, and the defaulted value is what
				 * it rejected.
				 */
				throw new BinaryPersistenceException(
					"Could not construct inlined instance of " + this.valueType.getName()
					+ (this.defaultedNames.isEmpty()
						? ""
						: ", whose persisted layout did not carry " + this.defaultedNames
						+ " so it was constructed with the default")
					, t
				);
			}
		}

	}

	private static final class StructSetter implements BinaryValueSetter
	{
		private final FieldWriter    ownerWriter  ;
		private final Class<?>       valueType    ;
		private final StructReader[] readers      ;
		private final int[]          targets      ;
		private final MethodHandle   constructor  ;
		private final int            argumentCount;
		private final long           structLength ;

		StructSetter(
			final FieldWriter    ownerWriter  ,
			final Class<?>       valueType    ,
			final StructReader[] readers      ,
			final int[]          targets      ,
			final MethodHandle   constructor  ,
			final int            argumentCount,
			final long           structLength
		)
		{
			super();
			this.ownerWriter   = ownerWriter  ;
			this.valueType     = valueType    ;
			this.readers       = readers      ;
			this.targets       = targets      ;
			this.constructor   = constructor  ;
			this.argumentCount = argumentCount;
			this.structLength  = structLength ;
		}

		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			if(XMemory.get_byte(srcAddress) == NULL_MARKER_ABSENT)
			{
				this.ownerWriter.writeValue(target, null);
				return srcAddress + this.structLength;
			}

			final Object[] args = new Object[this.argumentCount];

			long address = srcAddress + NULL_MARKER_LENGTH;
			for(int i = 0; i < this.readers.length; i++)
			{
				address += this.readers[i].readValue(address, args, this.targets[i]);
			}

			this.ownerWriter.writeValue(target, this.createValue(args));

			return address;
		}

		private Object createValue(final Object[] args)
		{
			try
			{
				return (Object)this.constructor.invokeExact(args);
			}
			catch(final Error e)
			{
				throw e;
			}
			catch(final Throwable t)
			{
				throw new BinaryPersistenceException(
					"Could not construct inlined instance of " + this.valueType.getName(), t
				);
			}
		}

	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private BinaryValueStructFunctions()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
