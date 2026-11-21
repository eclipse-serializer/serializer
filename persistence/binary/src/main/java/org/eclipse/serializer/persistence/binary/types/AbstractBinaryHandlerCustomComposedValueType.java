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

import static org.eclipse.serializer.util.X.notNull;

import java.util.function.Function;

import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.exceptions.BinaryPersistenceException;
import org.eclipse.serializer.persistence.types.PersistenceFunction;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldGeneric;
import org.eclipse.serializer.reflect.XReflect;

/**
 * Base for handlers of a type that holds its state in references to parts, e.g. a
 * {@code java.time.LocalDateTime} holding a date and a time, and that is a value class on a suitably
 * recent JVM.
 * <p>
 * Where the handled type is a value class, its instances cannot be created empty and populated
 * afterwards, and its constructor cannot be reached because its module does not open the package. The
 * instance is therefore built from its resolved parts by {@link #createFromParts(Object[])}. That
 * resolves references in {@link #create}, which only a handler reporting
 * {@link #isCreationDeferred()} may do; the loader defers such a creation until they can be resolved.
 * Note that a resolved part is guaranteed to exist but not to be populated, so
 * {@link #createFromParts(Object[])} may only use a part's state if that part's own handler creates
 * complete instances.
 * <p>
 * Where it is an ordinary class, it keeps being created empty and populated, so that its instances
 * stay registered by identity as before. A subclass whose instances must be built from their
 * content on every JVM &mdash; because populating them would leave them incomplete &mdash; opts out
 * of that by overriding {@link #isCreationDeferred()} to a constant {@literal true}, which this base
 * reads in its constructor.
 * <p>
 * The persisted form is one reference per part, under the field's name qualified with the declaring
 * type, so it is byte-identical to the one the reflective handling produced and existing data is
 * unaffected.
 *
 * @param <T> the handled type.
 */
public abstract class AbstractBinaryHandlerCustomComposedValueType<T> extends AbstractBinaryHandlerCustom<T>
implements ValidatingBinaryHandler<T, T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	private static <T> XGettingSequence<? extends PersistenceTypeDefinitionMemberFieldGeneric> buildFields(
		final Class<T>  type ,
		final Part<T>[] parts
	)
	{
		final BulkList<PersistenceTypeDefinitionMemberFieldGeneric> fields = BulkList.New();
		for(final Part<T> part : parts)
		{
			/* Qualified with the declaring type, so this description stays the one the reflective
			 * handling produced and data written before the handler existed still matches.
			 */
			fields.add(CustomField(part.type, type.getName(), part.name));
		}

		return CustomFields(fields.toArray(PersistenceTypeDefinitionMemberFieldGeneric.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Part<T>[] parts       ;
	private final long      binaryLength;

	// only needed where the type is an ordinary class and its instances are populated after creation.
	private final long[] memoryOffsets;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	@SafeVarargs
	protected AbstractBinaryHandlerCustomComposedValueType(final Class<T> type, final Part<T>... parts)
	{
		super(type, buildFields(type, parts));

		this.parts        = parts.clone();
		this.binaryLength = parts.length * Binary.referenceBinaryLength(1);

		final boolean populated = !this.isCreationDeferred();
		this.memoryOffsets = new long[parts.length];
		for(int i = 0; i < parts.length; i++)
		{
			this.memoryOffsets[i] = populated
				? XMemory.objectFieldOffset(XReflect.getAnyField(type, parts[i].name))
				: -1
			;
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	/**
	 * Builds an instance from its resolved parts, in the declared part order. Only called where this
	 * handler's creation is deferred; see the type comment for what a part guarantees at this point.
	 *
	 * @param parts the resolved parts, in the order the handler declares them.
	 *
	 * @return the built instance.
	 */
	protected abstract T createFromParts(Object[] parts);

	private long binaryOffset(final int index)
	{
		return index * Binary.referenceBinaryLength(1);
	}

	private Object[] resolveParts(final Binary data, final PersistenceLoadHandler handler)
	{
		final Object[] parts = new Object[this.parts.length];
		for(int i = 0; i < parts.length; i++)
		{
			parts[i] = handler.lookupObject(data.read_long(this.binaryOffset(i)));
		}

		return parts;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void store(
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(this.binaryLength, this.typeId(), objectId);
		for(int i = 0; i < this.parts.length; i++)
		{
			data.store_long(this.binaryOffset(i), handler.apply(this.parts[i].getter.apply(instance)));
		}
	}

	@Override
	public T create(final Binary data, final PersistenceLoadHandler handler)
	{
		if(!this.isCreationDeferred())
		{
			// created blank; the parts are set in #updateState
			return XMemory.instantiateBlank(this.type());
		}

		return this.createFromParts(this.resolveParts(data, handler));
	}

	@Override
	public void initializeState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		if(this.isCreationDeferred())
		{
			// built complete from its parts, so there is nothing to initialize afterwards
			return;
		}

		// created blank, so initializing it is populating it
		this.updateState(data, instance, handler);
	}

	@Override
	public void updateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		if(this.isCreationDeferred())
		{
			/* Reached for an instance this handler did not build - the ones it builds are complete and
			 * go through #initializeState. Such an instance cannot be updated, so validating is what
			 * keeps a divergence from dropping the persisted state silently, e.g. for an explicitly set
			 * root, whose instance is registered for the persisted objectId before this handler ever
			 * sees the data.
			 */
			this.validateState(data, instance, handler);

			return;
		}

		for(int i = 0; i < this.parts.length; i++)
		{
			XMemory.setObject(
				instance,
				this.memoryOffsets[i],
				handler.lookupObject(data.read_long(this.binaryOffset(i)))
			);
		}
	}

	@Override
	public void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		for(final Part<T> part : this.parts)
		{
			iterator.apply(part.getter.apply(instance));
		}
	}

	@Override
	public void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		for(int i = 0; i < this.parts.length; i++)
		{
			iterator.acceptObjectId(data.read_long(this.binaryOffset(i)));
		}
	}

	@Override
	public boolean hasPersistedReferences()
	{
		return true;
	}

	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}

	@Override
	public void validateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		this.validateStates(
			instance,
			instance,
			this.createFromParts(this.resolveParts(data, handler))
		);
	}

	@Override
	public T getValidationStateFromInstance(final T instance)
	{
		return instance;
	}

	/**
	 * Never called: the state a part carries can only be read through the load handler, so
	 * {@link #validateState} is overridden instead of composed from this method.
	 */
	@Override
	public T getValidationStateFromBinary(final Binary data)
	{
		throw new BinaryPersistenceException(
			"The persisted state of " + this.type().getName() + " can only be read with a load handler."
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// member types //
	/////////////////

	/**
	 * One referenced part of the handled type: the field it is persisted as, and how to read it from
	 * an instance. The field name must be the runtime field's name, since populating on an ordinary
	 * JVM resolves the field by it.
	 *
	 * @param <T> the handled type.
	 */
	public static final class Part<T>
	{
		final Class<?>            type  ;
		final String              name  ;
		final Function<T, Object> getter;

		Part(final Class<?> type, final String name, final Function<T, Object> getter)
		{
			super();
			this.type   = type  ;
			this.name   = name  ;
			this.getter = getter;
		}

		/**
		 * Creates a part description.
		 *
		 * @param type   the part's declared type, as persisted in the type dictionary.
		 * @param name   the field's name.
		 * @param getter reads the part from an instance.
		 *
		 * @param <T> the handled type.
		 *
		 * @return a new part description.
		 */
		public static <T> Part<T> New(final Class<?> type, final String name, final Function<T, Object> getter)
		{
			return new Part<>(notNull(type), notNull(name), notNull(getter));
		}
	}

}
