package org.eclipse.serializer.persistence.binary.org.eclipse.serializer.reference;

import org.eclipse.serializer.persistence.binary.types.AbstractBinaryHandlerCustom;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.reference.ControlledLazyReference;
import org.eclipse.serializer.reference.Lazy;
import org.eclipse.serializer.reference.ObjectSwizzling;
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
		}

		// link to object supplier (internal logic can either update, discard or throw exception on mismatch)
		instance.$link(referenceOid, handler.getObjectRetriever());

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
