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

import java.nio.ByteBuffer;

import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.exceptions.BinaryPersistenceException;
import org.eclipse.serializer.persistence.types.PersistenceLegacyTypeHandlingListener;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandler;
import org.eclipse.serializer.reflect.XReflect;
import org.eclipse.serializer.typing.KeyValue;

/**
 * Legacy type handler that, for each persisted entity, allocates a fresh native buffer, copies translated
 * values from the legacy layout into the offsets of the current layout, and reroutes the load item to
 * point at the new buffer before delegating instance creation, state update, and completion to the wrapped
 * current {@link PersistenceTypeHandler}. This is the strategy used for custom (non-reflective) handlers
 * that cannot be updated field-by-field.
 * <p>
 * Because rerouting rewrites the persisted bytes into the current layout before any of the wrapped
 * handler's methods see them, reference traversal must follow the <em>new</em> binary layout described by
 * the current type handler.
 *
 * @param <T> the runtime type produced by this handler.
 *
 * @see AbstractBinaryLegacyTypeHandlerTranslating
 * @see AbstractBinaryLegacyTypeHandlerReflective
 */
public final class BinaryLegacyTypeHandlerRerouting<T>
extends AbstractBinaryLegacyTypeHandlerTranslating<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Creates a new {@link BinaryLegacyTypeHandlerRerouting} for the given legacy/current type pairing.
	 *
	 * @param typeDefinition               the legacy type definition describing the persisted layout.
	 * @param typeHandler                  the current type handler whose layout is the rerouting target.
	 * @param translatorsWithTargetOffsets ordered offset/translator pairs derived from the legacy mapping.
	 * @param listener                     optional listener invoked on each legacy creation, may be {@code null}.
	 * @param switchByteOrder              whether persisted values use a non-native byte order.
	 *
	 * @param <T> the runtime type produced by the handler.
	 *
	 * @return the newly created legacy handler.
	 */
	public static <T> BinaryLegacyTypeHandlerRerouting<T> New(
		final PersistenceTypeDefinition                       typeDefinition              ,
		final PersistenceTypeHandler<Binary, T>               typeHandler                 ,
		final XGettingEnum<KeyValue<Long, BinaryValueSetter>> translatorsWithTargetOffsets,
		final PersistenceLegacyTypeHandlingListener<Binary>   listener                    ,
		final boolean                                         switchByteOrder
	)
	{
		return New(
			typeDefinition              ,
			typeHandler                 ,
			translatorsWithTargetOffsets,
			listener                    ,
			""                          ,
			switchByteOrder
		);
	}

	/**
	 * Like {@link #New(PersistenceTypeDefinition, PersistenceTypeHandler, XGettingEnum, PersistenceLegacyTypeHandlingListener, boolean)},
	 * additionally naming the current members the persisted layout does not carry.
	 * <p>
	 * A handler that builds its instances from the persisted values instead of populating them - a value
	 * class handler - can legitimately reject what a defaulted member holds, and then that member is what
	 * the failure is about. It is named where it is known, which is here and not in the handler itself.
	 *
	 * @param defaultedMembers the current members that take their type's default, comma-separated; empty if none.
	 *
	 * @return the newly created legacy handler.
	 */
	public static <T> BinaryLegacyTypeHandlerRerouting<T> New(
		final PersistenceTypeDefinition                       typeDefinition              ,
		final PersistenceTypeHandler<Binary, T>               typeHandler                 ,
		final XGettingEnum<KeyValue<Long, BinaryValueSetter>> translatorsWithTargetOffsets,
		final PersistenceLegacyTypeHandlingListener<Binary>   listener                    ,
		final String                                          defaultedMembers            ,
		final boolean                                         switchByteOrder
	)
	{
		return new BinaryLegacyTypeHandlerRerouting<>(
			notNull(typeDefinition)                      ,
			notNull(typeHandler)                         ,
			toTranslators(translatorsWithTargetOffsets)  ,
			toTargetOffsets(translatorsWithTargetOffsets),
			mayNull(listener)                            ,
			notNull(defaultedMembers)                    ,
			switchByteOrder
		);
	}


	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final BinaryReferenceTraverser[] newBinaryLayoutReferenceTraversers;
	private final String                     defaultedMembers                  ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryLegacyTypeHandlerRerouting(
		final PersistenceTypeDefinition                     typeDefinition  ,
		final PersistenceTypeHandler<Binary, T>             typeHandler     ,
		final BinaryValueSetter[]                           valueTranslators,
		final long[]                                        targetOffsets   ,
		final PersistenceLegacyTypeHandlingListener<Binary> listener        ,
		final String                                        defaultedMembers,
		final boolean                                       switchByteOrder
	)
	{
		super(typeDefinition, typeHandler, valueTranslators, targetOffsets, listener, switchByteOrder);

		this.defaultedMembers = defaultedMembers;

		/* (01.01.2020 TM)NOTE: Bugfix:
		 * Moved from AbstractBinaryLegacyTypeHandlerTranslating here as this is only correct for ~Rerouting
		 * but incorrect for ~Reflective LegacyHandler
		 */
		// (12.11.2019 TM)NOTE: must be derived from the NEW type definition since #create relayouts the load data.
		this.newBinaryLayoutReferenceTraversers = deriveReferenceTraversers(typeHandler, switchByteOrder);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void iterateLoadableReferences(final Binary rawData, final PersistenceReferenceLoader iterator)
	{
		rawData.iterateReferences(this.newBinaryLayoutReferenceTraversers, iterator);
	}

	@Override
	public void prepareLoadItem(final Binary rawData)
	{
		/* The rewritten data is what #iterateLoadableReferences describes, so the rewrite has to
		 * happen before any reference is read out of the load item - which rules out doing it in
		 * #create, whose execution the loader may defer.
		 */
		final long entityContentLength = this.typeHandler().membersPersistedLengthMaximum();

		// kept and new header values
		final long entityTotalLength = Binary.entityTotalLength(entityContentLength);
		final long entityTypeId      = this.typeHandler().typeId();
		final long entityObjectId    = rawData.getBuildItemObjectId();

		// so funny how they crippled their memory handling API to int just because there is a toArray somewhere.
		final ByteBuffer directByteBuffer = XMemory.allocateDirectNative(entityTotalLength);

		// hardly more than a consistently used and documentable label for the value 0.
		final long entityOffset = 0;

		// replacement binary content is filled and afterwards set as the productive content
		final long targetContentOffset = Binary.toEntityContentOffset(entityOffset);

		// note: DirectByteBuffer instantiation resets all bytes to 0, so no target value "Zeroer" is needed.
		rawData.copyMemory(directByteBuffer, targetContentOffset, this.valueTranslators(), this.targetOffsets());

		// replace the original rawData's content address with the new address, effectively rerouting to the new data
		rawData.modifyLoadItem(directByteBuffer, entityOffset, entityTotalLength, entityTypeId, entityObjectId);

		// registered here to ensure deallocating raw memory at the end of the building process. Neither sooner nor later.
		rawData.registerHelper(directByteBuffer, directByteBuffer);
	}

	@Override
	protected T internalCreate(final Binary rawData, final PersistenceLoadHandler handler)
	{
		// the data has been rearranged by #prepareLoadItem, so the current type handler can read it directly.
		if(this.defaultedMembers.isEmpty() || !XReflect.isValueClass(this.typeHandler().type()))
		{
			/* Every custom-wrapped handler reaches this class, enums included, and only one of them builds
			 * its instance from the persisted values. For all the others a failure of #create says nothing
			 * about a defaulted member, so naming one would assert a cause rather than report it - and the
			 * wrapping would hide the exception type a caller matches on.
			 */
			return this.typeHandler().create(rawData, handler);
		}

		try
		{
			return this.typeHandler().create(rawData, handler);
		}
		catch(final RuntimeException e)
		{
			/* Naming them is the point: a handler constructing its instance from the persisted values can
			 * reject what a defaulted member holds, and the persisted layout not carrying it is the reason.
			 */
			throw new BinaryPersistenceException(
				"Reading " + this.legacyTypeDefinition().toTypeIdentifier() + " as "
				+ this.typeHandler().toRuntimeTypeIdentifier() + " failed, whose persisted layout did not"
				+ " carry " + this.defaultedMembers + " so it was read with the default.",
				e
			);
		}
	}

	@Override
	public final void updateState(final Binary rawData, final T instance, final PersistenceLoadHandler handler)
	{
		// rawData is rerouted to the newly allocated memory (handled by a DirectByteBuffer) with rearranged values.
		this.typeHandler().updateState(rawData, instance, handler);
	}

	@Override
	public final void complete(final Binary rawData, final T instance, final PersistenceLoadHandler handler)
	{
		// rawData is rerouted to the newly allocated memory (handled by a DirectByteBuffer) with rearranged values.
		this.typeHandler().complete(rawData, instance, handler);
	}

}
