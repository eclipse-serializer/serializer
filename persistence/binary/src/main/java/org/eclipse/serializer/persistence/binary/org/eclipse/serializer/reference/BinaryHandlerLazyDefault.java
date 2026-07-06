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

import java.lang.reflect.Constructor;

import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustom;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.reference.Lazy;
import org.eclipse.serializer.reference.ObjectSwizzling;
import org.eclipse.serializer.reference.Swizzling;
import org.eclipse.serializer.reflect.XReflect;


public final class BinaryHandlerLazyDefault extends AbstractBinaryHandlerCustom<Lazy.Default<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerLazyDefault New()
	{
		return new BinaryHandlerLazyDefault();
	}
	
	@SuppressWarnings("rawtypes")
	static final Constructor<Lazy.Default> CONSTRUCTOR = XReflect.setAccessible(
		XReflect.getDeclaredConstructor(Lazy.Default.class, Object.class, long.class, ObjectSwizzling.class)
	);
		
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerLazyDefault()
	{
		super(
			Lazy.Default.genericType(),
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
		final Binary                          data    ,
		final Lazy.Default<?>                 instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		/* (29.09.2015 TM)NOTE: There are several cases that have to be handled here correctly:
		 *
		 * 1) objectId == 0, referent == null
		 * "Empty" lazy reference that must be stored as such
		 *
		 * 2.) objectId == 0, referent != null
		 * Newly created lazy reference with a referent. The referent has to be handled and the lazy reference has
		 * to be stored with the referent's OID
		 *
		 * 3.) objectId != null, referent == null
		 * The lazy reference represents a non-null referent that is currently simply not loaded. The lazy reference
		 * must be stored nonetheless, pointing to its known referent objectId
		 *
		 * 4.) objectId != null, referent != null
		 * The lazy reference represents a non-null referent that is currently loaded. The refernt must be handled,
		 * the lazy reference must be stored, pointing to its known referent objectId.
		 */

		final Object referent = instance.peek();
		final long referenceOid;

		if(referent == null)
		{
			referenceOid = instance.objectId();
			
			if(referenceOid != 0 && instance.$getLoader() != handler.getObjectRetriever())
			{
				throw new PersistenceException(
						"Cannot persist an unloaded lazy reference to another storage. " +
								"The referenced object is not loaded and its state is unknown. " +
								"Persisting it would result in data loss."
				);
			}
		}
		else
		{
			// OID validation or updating is done by linking logic
			referenceOid = handler.apply(referent);

			/*
			 * Fail BEFORE anything is written: the deferred link below performs the identical
			 * validation, but it runs only after the commit's write succeeded. A mismatch - the
			 * lazy reference is already linked to a different objectId, e.g. because it was
			 * loaded from one storage and is being persisted into another, where the referent
			 * resolves to a different objectId - must abort the store cleanly instead of
			 * surfacing only after the data is already durable.
			 */
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
		 * mismatch) - but DEFERRED to the successful commit: linking here would flip isStored() to
		 * true although nothing has been persisted yet. If the commit write fails, the objectId is
		 * never merged into the object registry and the referent's data never reaches the storage,
		 * but the instance would keep reporting a stale, never-persisted objectId. An isStored()-
		 * gated clear (e.g. by the LazyReferenceManager under memory pressure) would then drop the
		 * referent, and a later successful store would persist the stale objectId as a dangling
		 * reference (missing entity on load). Commit listeners run only after the write succeeded
		 * and the object registry merged, so on a failed commit the instance simply remains in its
		 * previous state (unstored and thus not clearable, or linked to its prior valid objectId).
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
	public final Lazy.Default<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		/* (27.04.2016 TM)NOTE: registering a Lazy instance with a reference manager
		 * without having the object supplier set yet might cause an inconsistency if the
		 * LRM iterates lazy references before the update added the supplier reference.
		 * ON the other hand: the lazy reference instance is not yet completed and whatever
		 * logic iterates over the LRM's entries shouldn't rely on anything.
		 */
		final long objectId = data.read_long(0);
		
		return Lazy.register(
			XReflect.invoke(CONSTRUCTOR, null, objectId, null)
		);
	}

	@Override
	public final void updateState(
		final Binary                 data    ,
		final Lazy.Default<?>        instance,
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
		final Lazy.Default<?>        instance,
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
