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

import static org.eclipse.serializer.util.X.mayNull;
import static org.eclipse.serializer.util.X.notNull;

import java.lang.reflect.Field;

import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.collections.types.XGettingTable;
import org.eclipse.serializer.persistence.binary.exceptions.BinaryPersistenceException;
import org.eclipse.serializer.persistence.types.PersistenceLegacyTypeHandlingListener;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionResolver;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMember;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldReflective;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldValueStruct;
import org.eclipse.serializer.typing.KeyValue;
import org.eclipse.serializer.util.similarity.Similarity;

/**
 * Legacy type handler for a value class: it reads the <em>persisted</em> layout directly and passes the
 * values to the current type's constructor, instead of rewriting the bytes into the current layout first.
 * <p>
 * An identity-less instance is constructed rather than populated, so it never needed a rewritten buffer -
 * the buffer only existed because {@link BinaryHandlerGenericValueClass#create} reads the current layout.
 * Dropping it removes what the rewriting path structurally cannot do: a member that was referenced and is
 * now inlined has to be <em>resolved</em>, and a rewrite runs in
 * {@link #prepareLoadItem}, before any reference is loaded and with no load handler in reach. Reading the
 * persisted layout at creation time has both.
 * <p>
 * What this changes per member, compared to {@link BinaryLegacyTypeHandlerRerouting}:
 * <ul>
 * <li>a persisted <b>reference</b> whose member is now <b>inlined</b> is resolved and handed over as the
 *     instance it is - no slot is written at all,</li>
 * <li>a persisted <b>inlined slot</b> is read by {@link BinaryValueStructFunctions#provideValueReader},</li>
 * <li>a persisted <b>primitive</b> is read as its own type, so a widening to the current one is done by the
 *     constructor's own argument conversion,</li>
 * <li>a member the current type has <b>gained</b> takes its type's default, one it has <b>lost</b> is not
 *     read.</li>
 * </ul>
 * Reference traversal therefore has to describe the <b>persisted</b> layout, as it does for the reflective
 * legacy path and unlike the rerouting one - which is also what gets a now-inlined referent loaded in time
 * to be resolved.
 *
 * @param <T> the value class produced by this handler.
 *
 * @see BinaryHandlerGenericValueClass
 * @see BinaryLegacyTypeHandlerRerouting
 */
public final class BinaryLegacyTypeHandlerValueClass<T>
extends AbstractBinaryLegacyTypeHandlerTranslating<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Creates a legacy handler reading the persisted layout of a value class directly.
	 *
	 * @param typeDefinition   the legacy type definition describing the persisted layout.
	 * @param typeHandler      the current value class handler.
	 * @param legacyToCurrent  the member mapping, persisted member to current member.
	 * @param listener         optional listener invoked on each legacy creation, may be {@code null}.
	 * @param switchByteOrder  whether persisted values use a non-native byte order.
	 *
	 * @param <T> the value class produced by the handler.
	 *
	 * @return the newly created legacy handler.
	 */
	public static <T> BinaryLegacyTypeHandlerValueClass<T> New(
		final PersistenceTypeDefinition                 typeDefinition ,
		final BinaryHandlerGenericValueClass<T>         typeHandler    ,
		final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>>
		                                                legacyToCurrent,
		final PersistenceTypeDescriptionResolver        refactoringResolver,
		final PersistenceLegacyTypeHandlingListener<Binary> listener   ,
		final boolean                                   switchByteOrder
	)
	{
		return new BinaryLegacyTypeHandlerValueClass<>(
			notNull(typeDefinition)   ,
			notNull(typeHandler)      ,
			notNull(legacyToCurrent)  ,
			mayNull(refactoringResolver),
			mayNull(listener)         ,
			switchByteOrder
		);
	}

	/**
	 * Derives one argument source per persisted member: the reader for its own type, the offset it sits at
	 * in the persisted layout, and the constructor argument it belongs to.
	 */
	private static <T> BulkList<ArgumentSource> deriveSources(
		final PersistenceTypeDefinition                 typeDefinition ,
		final BinaryHandlerGenericValueClass<T>         typeHandler    ,
		final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>>
		                                                legacyToCurrent,
		final PersistenceTypeDescriptionResolver        refactoringResolver
	)
	{
		final BulkList<ArgumentSource> sources = BulkList.New();

		long offset = 0;
		for(final PersistenceTypeDefinitionMember source : typeDefinition.instanceMembers())
		{
			if(!source.isFixedLength())
			{
				throw new BinaryPersistenceException(
					"Value class " + typeHandler.type().getName() + " cannot be read from the persisted"
					+ " layout: member " + source.identifier() + " has no fixed length."
				);
			}

			final PersistenceTypeDefinitionMember target = resolveTarget(legacyToCurrent, source);
			if(target != null)
			{
				sources.add(new ArgumentSource(
					deriveReader(source, target, typeHandler, refactoringResolver),
					offset,
					argumentIndex(typeHandler, target)
				));
			}

			// a member the current type no longer has is simply not read
			offset += source.persistentMinimumLength();
		}

		return sources;
	}

	private static PersistenceTypeDefinitionMember resolveTarget(
		final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>>
		                                          legacyToCurrent,
		final PersistenceTypeDefinitionMember     source
	)
	{
		final Similarity<PersistenceTypeDefinitionMember> match = legacyToCurrent.get(source);

		return match == null ? null : match.targetElement();
	}

	private static int argumentIndex(
		final BinaryHandlerGenericValueClass<?> typeHandler,
		final PersistenceTypeDefinitionMember   target
	)
	{
		if(!(target instanceof PersistenceTypeDefinitionMemberFieldReflective))
		{
			throw new BinaryPersistenceException(
				"Value class " + typeHandler.type().getName() + " cannot take " + target.identifier()
				+ ": only a field of the type itself is a constructor argument."
			);
		}

		final Field field = ((PersistenceTypeDefinitionMemberFieldReflective)target).field();
		final int   index = field == null ? -1 : typeHandler.argumentIndex(field);
		if(index < 0)
		{
			throw new BinaryPersistenceException(
				"Member " + target.identifier() + " is none of the persistable fields of value class "
				+ typeHandler.type().getName() + "."
			);
		}

		return index;
	}

	/**
	 * The reader for one persisted member, chosen by what the member <em>was</em> and what it <em>is</em>.
	 */
	private static BinaryValueReader deriveReader(
		final PersistenceTypeDefinitionMember   source     ,
		final PersistenceTypeDefinitionMember   target     ,
		final BinaryHandlerGenericValueClass<?> typeHandler,
		final PersistenceTypeDescriptionResolver refactoringResolver
	)
	{
		final boolean sourceIsStruct = source instanceof PersistenceTypeDefinitionMemberFieldValueStruct;
		final boolean targetIsStruct = target instanceof PersistenceTypeDefinitionMemberFieldValueStruct;

		if(sourceIsStruct)
		{
			if(!targetIsStruct)
			{
				/* The slot holds a whole layout and the current member takes one value. Reading it would
				 * have to decide which of the layout's values that is, which nothing states.
				 */
				throw new BinaryPersistenceException(
					"Inlined member " + source.identifier() + " of value class "
					+ typeHandler.type().getName() + " is no longer inlined. Reading an inlined layout as a"
					+ " single value is not supported."
				);
			}

			final PersistenceTypeDefinitionMemberFieldValueStruct sourceStruct =
				(PersistenceTypeDefinitionMemberFieldValueStruct)source
			;
			final PersistenceTypeDefinitionMemberFieldValueStruct targetStruct =
				(PersistenceTypeDefinitionMemberFieldValueStruct)target
			;

			/* A changed layout is paired member for member the same way the owner's path pairs it, so the
			 * same renames are carried and the same shapes refused. An unchanged one skips that and is read
			 * by its offsets directly.
			 */
			return sourceStruct.equalsLayout(targetStruct)
				? BinaryValueStructFunctions.provideValueReader(targetStruct)
				: BinaryValueStructFunctions.provideEvolvingValueReader(
					sourceStruct, targetStruct, refactoringResolver
				)
			;
		}

		if(targetIsStruct)
		{
			/* The case the rewriting path cannot do: the persisted form is an object id and the current
			 * member is a slot. Constructed from its values, the instance is simply the resolved referent -
			 * no slot has to be written for it at all.
			 */
			if(!source.isReference())
			{
				throw new BinaryPersistenceException(
					"Member " + source.identifier() + " of value class " + typeHandler.type().getName()
					+ " was persisted as a single value and is now an inlined layout, which cannot be read"
					+ " from it."
				);
			}

			return BinaryValueReader.provideReader(Object.class);
		}

		// read as what was persisted, so a widening to the current type is the constructor's own conversion
		return BinaryValueReader.provideReader(persistedType(source));
	}

	/**
	 * The type the persisted member is read as. A reference is read as an object id and resolved, so any
	 * non-primitive stands for it - including one whose class no longer exists.
	 */
	private static Class<?> persistedType(final PersistenceTypeDefinitionMember source)
	{
		if(source.isReference())
		{
			return Object.class;
		}

		final Class<?> type = source.type();
		if(type == null || !type.isPrimitive())
		{
			throw new BinaryPersistenceException(
				"Member " + source.identifier() + " is neither a reference nor a primitive, so it cannot be"
				+ " read out of the persisted layout of a value class."
			);
		}

		return type;
	}

	/**
	 * The argument array a construction starts from, so a member the persisted layout does not carry takes
	 * its type's default.
	 */
	private static Object[] deriveDefaults(final BinaryHandlerGenericValueClass<?> typeHandler)
	{
		final Object[] defaults = new Object[typeHandler.argumentCount()];

		for(final PersistenceTypeDefinitionMember member : typeHandler.instanceMembers())
		{
			if(!(member instanceof PersistenceTypeDefinitionMemberFieldReflective))
			{
				continue;
			}

			final Field field = ((PersistenceTypeDefinitionMemberFieldReflective)member).field();
			if(field == null)
			{
				continue;
			}

			final int index = typeHandler.argumentIndex(field);
			if(index >= 0)
			{
				defaults[index] = BinaryValueStructFunctions.defaultValue(field.getType());
			}
		}

		return defaults;
	}



	///////////////////////////////////////////////////////////////////////////
	// member types //
	/////////////////

	/**
	 * The current members no persisted member is read into, comma-separated. They take their type's
	 * default, which is what a constructor rejecting one of them is actually rejecting.
	 */
	private static String deriveDefaultedMembers(
		final BinaryHandlerGenericValueClass<?> typeHandler,
		final BulkList<ArgumentSource>          sources
	)
	{
		final VarString vs = VarString.New();

		for(final PersistenceTypeDefinitionMember member : typeHandler.instanceMembers())
		{
			if(!(member instanceof PersistenceTypeDefinitionMemberFieldReflective))
			{
				continue;
			}

			final Field field = ((PersistenceTypeDefinitionMemberFieldReflective)member).field();
			final int   index = field == null ? -1 : typeHandler.argumentIndex(field);
			if(index < 0 || sources.containsSearched(source -> source.index == index))
			{
				continue;
			}

			vs.add(vs.isEmpty() ? "" : ", ").add(member.name());
		}

		return vs.toString();
	}

	/** One persisted member as a constructor argument: how to read it, where it sits, where it goes. */
	private static final class ArgumentSource
	{
		final BinaryValueReader reader;
		final long              offset;
		final int               index ;

		ArgumentSource(final BinaryValueReader reader, final long offset, final int index)
		{
			super();
			this.reader = reader;
			this.offset = offset;
			this.index  = index ;
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final BinaryHandlerGenericValueClass<T> valueClassHandler                 ;
	private final BinaryReferenceTraverser[]        oldBinaryLayoutReferenceTraversers;
	private final ArgumentSource[]                  sources                           ;
	private final String                            defaultedMembers                  ;
	private final Object[]                          defaults                          ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryLegacyTypeHandlerValueClass(
		final PersistenceTypeDefinition                 typeDefinition ,
		final BinaryHandlerGenericValueClass<T>         typeHandler    ,
		final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>>
		                                                legacyToCurrent,
		final PersistenceTypeDescriptionResolver        refactoringResolver,
		final PersistenceLegacyTypeHandlingListener<Binary> listener   ,
		final boolean                                   switchByteOrder
	)
	{
		/* No value translators: nothing is rewritten, so there is no target layout to write into. The base
		 * class is used for the wrapper delegation it provides and for #internalCreate, which is the only
		 * place this handler does any work.
		 */
		super(typeDefinition, typeHandler, new BinaryValueSetter[0], new long[0], listener, switchByteOrder);

		final BulkList<ArgumentSource> sources = deriveSources(typeDefinition, typeHandler, legacyToCurrent, refactoringResolver);

		this.valueClassHandler = typeHandler;
		this.defaults          = deriveDefaults(typeHandler);
		this.defaultedMembers  = deriveDefaultedMembers(typeHandler, sources);
		this.sources           = sources.toArray(ArgumentSource.class);

		// the persisted layout, not the current one: nothing rewrote the data this handler reads
		this.oldBinaryLayoutReferenceTraversers = deriveReferenceTraversers(typeDefinition, switchByteOrder);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void iterateLoadableReferences(final Binary rawData, final PersistenceReferenceLoader iterator)
	{
		rawData.iterateReferences(this.oldBinaryLayoutReferenceTraversers, iterator);
	}

	/**
	 * None. The instance is complete the moment it exists, and a value class has no setters to apply -
	 * which is why it is constructed rather than populated in the first place. Delegating here would hand
	 * the current handler data in the persisted layout, which is not the layout it reads.
	 */
	@Override
	public void updateState(final Binary rawData, final T instance, final PersistenceLoadHandler handler)
	{
		// no-op, see above
	}

	@Override
	public void complete(final Binary rawData, final T instance, final PersistenceLoadHandler handler)
	{
		// no-op for a reflective handler, and the same reasoning as #updateState applies to the data
	}

	@Override
	protected T internalCreate(final Binary rawData, final PersistenceLoadHandler handler)
	{
		final Object[] arguments = this.defaults.clone();

		for(final ArgumentSource source : this.sources)
		{
			arguments[source.index] = source.reader.readValue(rawData, source.offset, handler);
		}

		return this.valueClassHandler.createInstance(
			arguments, rawData.getBuildItemObjectId(), this.defaultedMembers
		);
	}

}
