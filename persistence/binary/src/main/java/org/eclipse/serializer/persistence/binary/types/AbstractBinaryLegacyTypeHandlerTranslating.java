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

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.serializer.util.X;
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import org.eclipse.serializer.persistence.types.PersistenceFunction;
import org.eclipse.serializer.persistence.types.PersistenceLegacyTypeHandlingListener;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMember;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionMember;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandler;
import org.eclipse.serializer.typing.KeyValue;
import org.eclipse.serializer.util.cql.CQL;

/**
 * Skeletal base for {@link BinaryLegacyTypeHandler} implementations that reconstruct instances by
 * <em>translating</em> persisted legacy values into the layout expected by a current
 * {@link PersistenceTypeHandler}. Holds the wrapped current type handler together with the per-member
 * {@link BinaryValueSetter} translators and their target offsets, and forwards all wrapper-style queries
 * (members, viability checks, type, reference iteration) to the current handler so the wrapping is
 * transparent to surrounding persistence machinery.
 * <p>
 * Subclasses (rerouting and reflective branches) supply the actual instance-creation strategy via
 * {@link #internalCreate(Binary, PersistenceLoadHandler)} and decide whether reference traversers must
 * follow the legacy or the current binary layout.
 *
 * @param <T> the runtime type produced by this handler.
 *
 * @see BinaryLegacyTypeHandlerRerouting
 * @see AbstractBinaryLegacyTypeHandlerReflective
 */
public abstract class AbstractBinaryLegacyTypeHandlerTranslating<T>
extends BinaryLegacyTypeHandler.Abstract<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Extracts the {@link BinaryValueSetter} values from the offset/translator pairs into a flat array,
	 * preserving iteration order. Validates that no entry has a {@code null} translator.
	 *
	 * @param translatorsWithTargetOffsets ordered offset/translator pairs.
	 *
	 * @return the translator array.
	 *
	 * @throws org.eclipse.serializer.persistence.exceptions.PersistenceException if any entry has a {@code null} translator.
	 */
	public static BinaryValueSetter[] toTranslators(
		final XGettingEnum<KeyValue<Long, BinaryValueSetter>> translatorsWithTargetOffsets
	)
	{
		validate(translatorsWithTargetOffsets);
		return CQL.from(translatorsWithTargetOffsets)
			.project(KeyValue<Long, BinaryValueSetter>::value)
			.executeInto(new BinaryValueSetter[translatorsWithTargetOffsets.intSize()])
		;
	}

	/**
	 * Extracts the target offsets from the offset/translator pairs into a flat {@code long[]}, preserving
	 * iteration order. A {@code null} key (member to be discarded) is encoded as {@code -1L}.
	 *
	 * @param translatorsWithTargetOffsets ordered offset/translator pairs.
	 *
	 * @return the target offset array.
	 *
	 * @throws org.eclipse.serializer.persistence.exceptions.PersistenceException if any entry has a {@code null} translator.
	 */
	public static long[] toTargetOffsets(
		final XGettingEnum<KeyValue<Long, BinaryValueSetter>> translatorsWithTargetOffsets
	)
	{
		validate(translatorsWithTargetOffsets);
		return X.unbox(
			CQL.from(translatorsWithTargetOffsets)
				.project(kv -> {
					final Long offset= kv.key();
					return offset == null ? -1L : offset;
				})
				.executeInto(new Long[translatorsWithTargetOffsets.intSize()])
		);
	}

	private static void validate(final XGettingEnum<KeyValue<Long, BinaryValueSetter>> translatorsWithTargetOffsets)
	{
		final Predicate<KeyValue<Long, BinaryValueSetter>> isNullEntry = e ->
			e.value() == null
		;

		if(translatorsWithTargetOffsets.containsSearched(isNullEntry))
		{
			throw new PersistenceException("Value translator mapping contains an invalid null-entry.");
		}
	}

	/**
	 * Derives the {@link BinaryReferenceTraverser}s needed to walk reference values in entities that follow
	 * the binary layout described by {@code typeDefinition}. Only instance members are considered, not enum
	 * constant definitions, and the result is cropped to traversers that actually see references.
	 *
	 * @param typeDefinition  the type definition whose layout dictates the traversal pattern.
	 * @param switchByteOrder whether persisted values use a non-native byte order.
	 *
	 * @return the reference-only traverser array for the described layout.
	 */
	public static final BinaryReferenceTraverser[] deriveReferenceTraversers(
		final PersistenceTypeDefinition typeDefinition ,
		final boolean                   switchByteOrder
	)
	{
		// only instance members, here. Not enum constants definitions!
		final BinaryReferenceTraverser[] referenceTraversers =
			BinaryReferenceTraverser.Static.deriveReferenceTraversers(
				typeDefinition.instanceMembers(),
				switchByteOrder
			)
		;

		return BinaryReferenceTraverser.Static.cropToReferences(referenceTraversers);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final PersistenceTypeHandler<Binary, T>             typeHandler     ;
	private final BinaryValueSetter[]                           valueTranslators;
	private final long[]                                        targetOffsets   ;
	private final PersistenceLegacyTypeHandlingListener<Binary> listener        ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * @param typeDefinition   the legacy type definition describing the persisted binary layout.
	 * @param typeHandler      the current type handler whose layout is the translation target.
	 * @param valueTranslators per-member value translators in legacy iteration order.
	 * @param targetOffsets    target offsets corresponding to {@code valueTranslators}.
	 * @param listener         optional listener invoked on each legacy creation, may be {@code null}.
	 * @param switchByteOrder  whether persisted values use a non-native byte order.
	 */
	protected AbstractBinaryLegacyTypeHandlerTranslating(
		final PersistenceTypeDefinition                     typeDefinition     ,
		final PersistenceTypeHandler<Binary, T>             typeHandler        ,
		final BinaryValueSetter[]                           valueTranslators   ,
		final long[]                                        targetOffsets      ,
		final PersistenceLegacyTypeHandlingListener<Binary> listener           ,
		final boolean                                       switchByteOrder
	)
	{
		super(typeDefinition);
		this.typeHandler         = typeHandler        ;
		this.valueTranslators    = valueTranslators   ;
		this.targetOffsets       = targetOffsets      ;
		this.listener            = listener           ;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	/**
	 * @return the per-member value translators applied during instance creation.
	 */
	protected BinaryValueSetter[] valueTranslators()
	{
		return this.valueTranslators;
	}

	/**
	 * @return the target offsets that {@link #valueTranslators()} write into.
	 */
	protected long[] targetOffsets()
	{
		return this.targetOffsets;
	}

	/**
	 * @return the wrapped current type handler whose layout is the translation target.
	 */
	public PersistenceTypeHandler<Binary, T> typeHandler()
	{
		return this.typeHandler;
	}



	///////////////////////////////////////////////////////////////////////////
	// default method implementations //
	///////////////////////////////////

	/*
	 * Tricky:
	 * Must pass through all default methods to be a correct wrapper.
	 * Otherwise, the wrapper changes the behavior in an unwanted fashion.
	 */

	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> membersInDeclaredOrder()
	{
		// Must pass through all default methods to be a correct wrapper.
		return this.typeHandler.membersInDeclaredOrder();
	}

	@Override
	public XGettingEnum<? extends PersistenceTypeDescriptionMember> storingMembers()
	{
		return this.typeHandler.storingMembers();
	}

	@Override
	public XGettingEnum<? extends PersistenceTypeDescriptionMember> settingMembers()
	{
		return this.typeHandler.settingMembers();
	}

	@Override
	public void guaranteeSpecificInstanceViablity() throws PersistenceExceptionTypeNotPersistable
	{
		// Must pass through all default methods to be a correct wrapper.
		this.typeHandler.guaranteeSpecificInstanceViablity();
	}

	@Override
	public boolean isSpecificInstanceViable()
	{
		// Must pass through all default methods to be a correct wrapper.
		return this.typeHandler.isSpecificInstanceViable();
	}

	@Override
	public void guaranteeSubTypeInstanceViablity() throws PersistenceExceptionTypeNotPersistable
	{
		// Must pass through all default methods to be a correct wrapper.
		this.typeHandler.guaranteeSubTypeInstanceViablity();
	}

	@Override
	public boolean isSubTypeInstanceViable()
	{
		// Must pass through all default methods to be a correct wrapper.
		return this.typeHandler.isSubTypeInstanceViable();
	}

	@Override
	public Object[] collectEnumConstants()
	{
		// indicate discarding of constants root entry during root resolving
		return null;
	}

	@Override
	public int getPersistedEnumOrdinal(final Binary data)
	{
		// Must pass through all default methods to be a correct wrapper.
		return this.typeHandler.getPersistedEnumOrdinal(data);
	}


	// runtime instance-related methods, so the current type handler must be used //

	@Override
	public Class<T> type()
	{
		return this.typeHandler.type();
	}

	@Override
	public void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		this.typeHandler.iterateInstanceReferences(instance, iterator);
	}

	@Override
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		return this.typeHandler.iterateMemberTypes(logic);
	}

	// end of runtime instance-related methods //



	@Override
	public final T create(final Binary rawData, final PersistenceLoadHandler handler)
	{
		// the method splitting might help jitting out the not occurring case.
		return this.listener == null
			? this.internalCreate(rawData, handler)
			: this.internalCreateListening(rawData, handler)
		;
	}

	private final T internalCreateListening(final Binary rawData, final PersistenceLoadHandler handler)
	{
		final T instance = this.internalCreate(rawData, handler);
		this.listener.registerLegacyTypeHandlingCreation(
			rawData.getBuildItemObjectId(),
			instance,
			this.legacyTypeDefinition(),
			this.typeHandler()
		);

		return instance;
	}

	/**
	 * Subclass extension point: produces the instance from the persisted legacy data. Called by
	 * {@link #create(Binary, PersistenceLoadHandler)}, optionally wrapped with listener notification.
	 *
	 * @param rawData the persisted entity data in legacy layout.
	 * @param handler the load handler driving the current load operation.
	 *
	 * @return the newly created instance.
	 */
	protected abstract T internalCreate(Binary rawData, PersistenceLoadHandler handler);

}
