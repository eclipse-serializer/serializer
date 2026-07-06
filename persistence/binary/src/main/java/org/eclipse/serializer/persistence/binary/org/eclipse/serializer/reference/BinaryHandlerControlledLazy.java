package org.eclipse.serializer.persistence.binary.org.eclipse.serializer.reference;

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

import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustom;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.reference.ControlledLazyReference;
import org.eclipse.serializer.reference.Lazy;
import org.eclipse.serializer.reference.ObjectSwizzling;
import org.eclipse.serializer.reference.Swizzling;
import org.eclipse.serializer.reflect.XReflect;

import java.lang.reflect.Constructor;

/**
 * Nearly identical to {@link BinaryHandlerLazyDefault} except
 * the handled type. That is {@link ControlledLazyReference.Default}.
 * 
 */
public final class BinaryHandlerControlledLazy extends AbstractBinaryHandlerCustom<ControlledLazyReference.Default<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerControlledLazy New()
	{
		return new BinaryHandlerControlledLazy();
	}
	
	@SuppressWarnings("rawtypes")
	static final Constructor<ControlledLazyReference.Default> CONSTRUCTOR = XReflect.setAccessible(
		XReflect.getDeclaredConstructor(ControlledLazyReference.Default.class, Object.class, long.class, ObjectSwizzling.class)
	);
		
	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<ControlledLazyReference.Default<?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)ControlledLazyReference.Default.class;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerControlledLazy()
	{
		super(
			handledType(),
			CustomFields(
				CustomField(Object.class, "subject")
			)
		);
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                             data    ,
		final ControlledLazyReference.Default<?> instance,
		final long                               objectId,
		final PersistenceStoreHandler<Binary>    handler
	)
	{
		final Object referent = instance.peek();
		final long referenceOid;

		if(referent == null)
		{
			referenceOid = instance.objectId();
		}
		else
		{
			// OID validation or updating is done by linking logic
			referenceOid = handler.apply(referent);

			// fail BEFORE anything is written (see BinaryHandlerLazyDefault#store)
			if(Swizzling.isFoundId(instance.objectId()) && instance.objectId() != referenceOid)
			{
				throw new PersistenceException(
						"Cannot persist a lazy reference that is already linked to a different objectId. " +
								"Linked: " + instance.objectId() + ", resolved in this context: " + referenceOid + ". " +
								"The lazy reference most likely belongs to another storage."
				);
			}
		}

		/*
		 * Link to object supplier (internal logic can either update, discard or throw exception on
		 * mismatch) - but DEFERRED to the successful commit, so a failed commit cannot leave a
		 * stale, never-persisted objectId on the instance (see BinaryHandlerLazyDefault#store).
		 */
		final ObjectSwizzling objectRetriever = handler.getObjectRetriever();
		handler.registerCommitListener(() ->
			instance.$link(referenceOid, objectRetriever)
		);

		// lazy reference instance must be stored in any case
		data.storeEntityHeader(Binary.referenceBinaryLength(1), this.typeId(), objectId);
		data.store_long(referenceOid);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final ControlledLazyReference.Default<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		final long objectId = data.read_long(0);
		
		return Lazy.register(
			XReflect.invoke(CONSTRUCTOR, null, objectId, null)
		);
	}

	@Override
	public final void updateState(
		final Binary                 data    ,
		final ControlledLazyReference.Default<?>        instance,
		final PersistenceLoadHandler handler
	)
	{
		/*
		 * Intentionally no subject lookup here as premature strong referencing
		 * might defeat the purpose of memory freeing lazy referencing if no
		 * other strong reference to the subject is present at the moment.
		 */
		instance.$setLoader(handler.getObjectRetriever());
	}

	@Override
	public final void complete(
		final Binary                 data    ,
		final ControlledLazyReference.Default<?>        instance,
		final PersistenceLoadHandler handler
	)
	{
		// no-op for normal implementation (see non-reference-hashing collections for other examples)
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return true;
	}

	@Override
	public final boolean hasPersistedVariableLength()
	{
		return false;
	}
	
	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}

	@Override
	public final void iterateLoadableReferences(
		final Binary                     offset  ,
		final PersistenceReferenceLoader iterator
	)
	{
		// the lazy reference is not naturally loadable, but special-handled by this handler
	}

}
