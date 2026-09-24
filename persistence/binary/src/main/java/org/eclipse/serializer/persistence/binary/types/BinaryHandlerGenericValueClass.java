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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.persistence.binary.exceptions.BinaryPersistenceException;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import org.eclipse.serializer.persistence.types.PersistenceEagerStoringFieldEvaluator;
import org.eclipse.serializer.persistence.types.PersistenceFieldLengthResolver;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldReflective;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldValueStruct;
import org.eclipse.serializer.persistence.types.PersistenceValueInliningResolver;
import org.eclipse.serializer.reflect.XReflect;

/**
 * Reflective type handler for value classes (JEP 401).
 * <p>
 * Value instances have no identity and no mutable state: they cannot be allocated blank and filled
 * afterwards like identity instances are. This handler therefore reads all field values from the
 * persisted data first and passes them to a constructor, so the instance is complete the moment it
 * exists. Storing is inherited unchanged, so the persisted form is identical to the one an identity
 * class of the same structure would produce.
 * <p>
 * The constructor is required to accept the persistable instance fields in declaration order, which
 * is exactly what a record's canonical constructor does. If no such constructor exists, the type
 * cannot be handled generically and a custom type handler must be registered for it. This is
 * established once, at handler creation.
 * <p>
 * It is required to do more than exist: loading hands it the stored field values, so it is the inverse
 * of storing only if it assigns each argument to the corresponding field unmodified. That cannot be
 * read off the type, so it is verified by constructing from chosen values at handler creation and, where
 * that cannot answer, against the first instance stored - see {@link ValueClassConstructorContract},
 * which also states why the identity case needs no such check.
 * <p>
 * Note that {@link #create(Binary, PersistenceLoadHandler)} resolves the instance's references,
 * unlike the identity case where they are resolved in {@code initializeState}. The loader
 * accommodates this by deferring the creation of value instances until their references can be
 * resolved.
 *
 * @param <T> the handled value class.
 */
public final class BinaryHandlerGenericValueClass<T> extends AbstractBinaryHandlerReflective<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Creates a new {@link BinaryHandlerGenericValueClass} for the passed value class.
	 *
	 * @param <T>                  the handled value class.
	 * @param type                 the value class to be handled.
	 * @param typeName             the type name to be used in the type dictionary.
	 * @param persistableFields    the fields to be persisted.
	 * @param persisterFields      the fields to be set to the persister.
	 * @param lengthResolver       the field length resolver.
	 * @param eagerEvaluator       the eager storing evaluator.
	 * @param fieldHandlerProvider the custom field handler provider.
	 * @param inliningResolver     the resolver deciding which of the type's own fields are inlined.
	 * @param switchByteOrder      whether persisted values use a non-native byte order.
	 *
	 * @return the newly created handler.
	 *
	 * @throws PersistenceExceptionTypeNotPersistable if the type has no suitable constructor.
	 */
	public static <T> BinaryHandlerGenericValueClass<T> New(
		final Class<T>                              type                ,
		final String                                typeName            ,
		final XGettingEnum<Field>                   persistableFields   ,
		final XGettingEnum<Field>                   persisterFields     ,
		final PersistenceFieldLengthResolver        lengthResolver      ,
		final PersistenceEagerStoringFieldEvaluator eagerEvaluator      ,
		final BinaryFieldHandlerProvider            fieldHandlerProvider,
		final PersistenceValueInliningResolver      inliningResolver    ,
		final boolean                               switchByteOrder
	)
	{
		return new BinaryHandlerGenericValueClass<>(
			type                ,
			typeName            ,
			persistableFields   ,
			persisterFields     ,
			lengthResolver      ,
			eagerEvaluator      ,
			fieldHandlerProvider,
			inliningResolver    ,
			switchByteOrder
		);
	}

	/**
	 * Whether the type has a matching constructor that cannot be made accessible because its module
	 * does not open the package. Such types (e.g. JDK value types) must keep being handled
	 * reflectively, since there is no legal way to invoke their constructor.
	 * <p>
	 * A type without any matching constructor is not reported here: that case is a genuine
	 * persistability problem and is reported by {@link #New} with a fitting explanation.
	 *
	 * @param type              the value class to be tested.
	 * @param persistableFields the fields the constructor would have to accept.
	 *
	 * @return whether the type's constructor is inaccessible.
	 */
	public static boolean isConstructorModuleProtected(
		final Class<?>            type             ,
		final XGettingEnum<Field> persistableFields
	)
	{
		try
		{
			return !type.getDeclaredConstructor(toParameterTypes(persistableFields)).trySetAccessible();
		}
		catch(final NoSuchMethodException e)
		{
			return false;
		}
	}

	static MethodHandle resolveConstructor(
		final Class<?>            type             ,
		final Class<?>[]          parameterTypes   ,
		final XGettingEnum<Field> persistableFields
	)
	{
		final Constructor<?> constructor;
		try
		{
			constructor = type.getDeclaredConstructor(parameterTypes);
		}
		catch(final NoSuchMethodException e)
		{
			throw new PersistenceExceptionTypeNotPersistable(type,
				new BinaryPersistenceException(
					"Value class " + type.getName() + " cannot be handled generically: it has no constructor"
					+ " accepting its persistable fields in declaration order"
					+ toParameterListString(parameterTypes)
					+ ". Register a custom type handler for it.",
					e
				)
			);
		}

		try
		{
			/* Pre-adapted to a fixed (Object[])Object shape so creation can use invokeExact instead of
			 * invokeWithArguments, which would redo the argument conversion on every instance.
			 */
			return MethodHandles.lookup().unreflectConstructor(XReflect.setAccessible(constructor))
				.asSpreader(Object[].class, parameterTypes.length)
				.asType(MethodType.methodType(Object.class, Object[].class))
			;
		}
		catch(final IllegalAccessException e)
		{
			throw new PersistenceExceptionTypeNotPersistable(type,
				new BinaryPersistenceException(
					"Constructor of value class " + type.getName() + " is not accessible.", e
				)
			);
		}
	}

	private static String toParameterListString(final Class<?>[] parameterTypes)
	{
		final StringBuilder sb = new StringBuilder(" (");
		for(int i = 0; i < parameterTypes.length; i++)
		{
			if(i != 0)
			{
				sb.append(", ");
			}
			sb.append(parameterTypes[i].getName());
		}

		return sb.append(')').toString();
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final MethodHandle       constructor    ;

	// settled at handler creation where it can be, otherwise on the first instance stored. See #store.
	private final ValueClassConstructorContract constructorContract;

	/* Kept so a legacy handler can build its own readers over the persisted layout and still place the
	 * values where this type's constructor expects them. See BinaryLegacyTypeHandlerValueClass.
	 */
	private final XGettingEnum<Field> persistableFields;

	// all three arrays are parallel and indexed in persisted (= storing) member order.
	private final BinaryValueReader[] readers       ;
	private final long[]              readerOffsets ;
	private final int[]               argumentIndices;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerGenericValueClass(
		final Class<T>                              type                ,
		final String                                typeName            ,
		final XGettingEnum<Field>                   persistableFields   ,
		final XGettingEnum<Field>                   persisterFields     ,
		final PersistenceFieldLengthResolver        lengthResolver      ,
		final PersistenceEagerStoringFieldEvaluator eagerEvaluator      ,
		final BinaryFieldHandlerProvider            fieldHandlerProvider,
		final PersistenceValueInliningResolver      inliningResolver    ,
		final boolean                               switchByteOrder
	)
	{
		super(
			type                ,
			typeName            ,
			persistableFields   ,
			persisterFields     ,
			lengthResolver      ,
			eagerEvaluator      ,
			fieldHandlerProvider,
			inliningResolver    ,
			switchByteOrder
		);

		final XGettingEnum<? extends PersistenceTypeDefinitionMemberFieldReflective> storingMembers =
			this.storingMembers()
		;
		final int memberCount = storingMembers.intSize();

		this.readers         = new BinaryValueReader[memberCount];
		this.readerOffsets   = new long[memberCount]             ;
		this.argumentIndices = new int[memberCount]              ;

		// members are persisted in storing order (references first), but constructed in declaration order.
		long offset = 0;
		int  i      = 0;
		for(final PersistenceTypeDefinitionMemberFieldReflective member : storingMembers)
		{
			/* An inlined member is read as the slot it is and constructed from it, which is the reader the
			 * construction-based path needs and the reason the fields of a value class can be inlined at all.
			 */
			this.readers[i]         = member instanceof PersistenceTypeDefinitionMemberFieldValueStruct
				? BinaryValueStructFunctions.provideValueReader(
					(PersistenceTypeDefinitionMemberFieldValueStruct)member
				)
				: BinaryValueReader.provideReader(member.type())
			;
			this.readerOffsets[i]   = offset;
			this.argumentIndices[i] = indexOfField(persistableFields, member.field());
			offset += member.persistentMinimumLength();
			i++;
		}

		validateNoCustomFieldHandlers(type, persistableFields, fieldHandlerProvider, switchByteOrder);

		this.persistableFields = persistableFields;
		this.constructor       = resolveConstructor(type, toParameterTypes(persistableFields), persistableFields);

		this.constructorContract = ValueClassConstructorContract.New(type, persistableFields, this.constructor);
	}

	/**
	 * The number of arguments the constructor accepts, which is the length every argument array must have.
	 */
	final int argumentCount()
	{
		return this.persistableFields.intSize();
	}

	/**
	 * The constructor argument the passed field is, so a handler reading another layout can place a value
	 * where this type expects it.
	 *
	 * @return the argument index, or {@literal -1} if the field is none of the persistable ones.
	 */
	final int argumentIndex(final Field field)
	{
		int i = 0;
		for(final Field persistableField : this.persistableFields)
		{
			if(persistableField.equals(field))
			{
				return i;
			}
			i++;
		}

		return -1;
	}

	/**
	 * Constructs an instance from a complete argument array, which is the only way an identity-less
	 * instance comes into existence. Shared with the legacy handler, which fills the array from the
	 * persisted layout instead of the current one.
	 *
	 * @param arguments the constructor arguments, in declaration order.
	 * @param objectId  the entity's object id, for the failure message.
	 *
	 * @return the newly created instance.
	 */
	final T createInstance(final Object[] arguments, final long objectId)
	{
		return this.createInstance(arguments, objectId, "");
	}

	/**
	 * @param defaultedMembers the current members the persisted layout did not carry, comma-separated;
	 *                         empty where all of them were carried. Naming them is the actionable half of
	 *                         a rejection: the constructor's own reason says what it refused, not that a
	 *                         defaulted value is why it was there to refuse.
	 */
	final T createInstance(final Object[] arguments, final long objectId, final String defaultedMembers)
	{
		try
		{
			// cast safety guaranteed by the constructor being the handled type's own.
			@SuppressWarnings("unchecked")
			final T instance = (T)this.constructor.invokeExact(arguments);

			return instance;
		}
		catch(final Throwable t)
		{
			// a failure of the JVM itself is none of this handler's business.
			if(t instanceof Error)
			{
				throw (Error)t;
			}

			/* A value class constructor validating its arguments can legitimately reject persisted
			 * state, e.g. when a field was added and the missing value is defaulted by a mapping.
			 */
			throw new BinaryPersistenceException(
				"Failed to construct instance of value class " + this.type().getName()
				+ " for objectId " + objectId
				+ (defaultedMembers.isEmpty()
					? ""
					: ", whose persisted layout did not carry " + defaultedMembers
					+ " so it was constructed with the default")
				+ ".",
				t
			);
		}
	}

	/**
	 * Rejects a value class with a custom field handler registered for one of its fields.
	 * <p>
	 * A custom field handler is a pair: a storer writing the field's own representation and a setter
	 * writing the value back <em>into an instance</em>. The second half is not applicable here, since
	 * a value instance is constructed from its field values rather than populated, so honoring the
	 * custom representation on the storing side while reading it back generically would silently
	 * produce wrong values.
	 */
	private static void validateNoCustomFieldHandlers(
		final Class<?>                   type                ,
		final XGettingEnum<Field>        persistableFields   ,
		final BinaryFieldHandlerProvider fieldHandlerProvider,
		final boolean                    switchByteOrder
	)
	{
		for(final Field field : persistableFields)
		{
			if(fieldHandlerProvider.lookupFieldStorer(field, false, switchByteOrder) == null
				&& fieldHandlerProvider.lookupFieldStorer(field, true, switchByteOrder) == null
				&& fieldHandlerProvider.lookupFieldSetter(field, switchByteOrder) == null
			)
			{
				continue;
			}

			throw new PersistenceExceptionTypeNotPersistable(type,
				new BinaryPersistenceException(
					"Field " + field.getName() + " of value class " + type.getName() + " has a custom field"
					+ " handler registered, which cannot be applied to a type whose instances are created"
					+ " by their constructor. Register a custom type handler for " + type.getName()
					+ " instead."
				)
			);
		}
	}

	static Class<?>[] toParameterTypes(final XGettingEnum<Field> persistableFields)
	{
		final Class<?>[] parameterTypes = new Class<?>[persistableFields.intSize()];

		int i = 0;
		for(final Field field : persistableFields)
		{
			parameterTypes[i++] = field.getType();
		}

		return parameterTypes;
	}

	private static int indexOfField(final XGettingEnum<Field> persistableFields, final Field field)
	{
		int i = 0;
		for(final Field persistableField : persistableFields)
		{
			if(persistableField.equals(field))
			{
				return i;
			}
			i++;
		}

		// cannot happen: members are derived from exactly these fields.
		throw new BinaryPersistenceException("Unknown field " + field + " for value class handler.");
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	/**
	 * Verifies the constructor contract before storing the first instance, then stores as the reflective
	 * base does.
	 * <p>
	 * A no-op where the contract was already settled at handler creation, which is the usual case. It
	 * matters for what a probe cannot answer - a constructor that rejects the probing values, or a layout
	 * holding a reference - and then this is the first point an instance exists, still before any byte of
	 * it is written. See {@link ValueClassConstructorContract}.
	 */
	@Override
	public void store(
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		this.constructorContract.validate(instance);

		super.store(data, instance, objectId, handler);
	}

	@Override
	public T create(final Binary data, final PersistenceLoadHandler handler)
	{
		final Object[] arguments = new Object[this.readers.length];
		for(int i = 0; i < this.readers.length; i++)
		{
			arguments[this.argumentIndices[i]] = this.readers[i].readValue(data, this.readerOffsets[i], handler);
		}

		return this.createInstance(arguments, data.getBuildItemObjectId());
	}

	/**
	 * None, since {@link #updateState} has nothing to do: a value instance is constructed from the
	 * persisted data rather than populated afterwards, so no setter is ever invoked.
	 * <p>
	 * Deriving them is not merely waste. A member whose type is itself a value class is set through a
	 * handle on the field rather than at its memory offset, because a value may be laid out inside its
	 * owner - and resolving that handle on a field <i>declared in</i> a value class is refused by the
	 * JVM outright, since such a field can never be written. The handler could then not even be
	 * created, so a value class holding another value class would be unpersistable.
	 */
	@Override
	protected BinaryValueSetter[] deriveSetters()
	{
		return new BinaryValueSetter[0];
	}

	@Override
	public void initializeState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		// value instances are complete when created, there is no state to initialize afterwards.
	}

	@Override
	public void updateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		/* Updating means replacing the state of an existing instance, which is impossible for an
		 * immutable one. Since a value instance is always created from the very data that would be
		 * applied here, there is also nothing to do.
		 */
	}

}
