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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.persistence.binary.exceptions.BinaryPersistenceException;
import org.eclipse.serializer.persistence.binary.types.BinaryValueHandleFunctions.FieldReader;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

/**
 * Verifies that a value class's constructor reproduces the field values it is handed, which is what the
 * generic value class handling assumes and nothing else can establish.
 * <p>
 * An identity instance is created blank and populated at its fields' memory offsets, so storing and
 * loading address the same fields the same way and the two are symmetric by construction. A value
 * instance cannot be populated - the JVM refuses to write its fields - so it is constructed instead, and
 * the persisted form is read back by handing the stored field values to the constructor. That is the
 * inverse of storing only if the constructor assigns each argument to the corresponding field unmodified.
 * <p>
 * Nothing in the language states that it does. A constructor that swaps two same-typed parameters swaps
 * the two values on every load, and one that computes a field from its argument drifts the value further
 * on every store-and-load cycle. Both are silent, and neither can be recovered from afterwards, so the
 * assumption is verified rather than trusted.
 * <p>
 * It is settled in two stages, because neither alone covers everything:
 * <ul>
 * <li>{@link #probe()}, at handler creation, constructs throwaway instances from values of this class's
 *     own choosing. Handlers are built for every type the storage contains, so a violation surfaces when
 *     the storage is opened - before the application can store anything, and even if it only ever loads.
 *     Chosen values also discriminate where an application's own may not: all-default field values are a
 *     fixed point of many a wrong constructor.</li>
 * <li>{@link #validate(Object)}, on the first instance actually stored, for what the probe could not
 *     answer - a constructor that rejects the probing values, or a layout holding a reference, whose type
 *     cannot be fabricated. It reports before any byte of that instance is written.</li>
 * </ul>
 * The two are complementary: the probe chooses good inputs but can be refused, the validation is always
 * accepted but takes what it is given. Once either has answered, the check is a single field read.
 * <p>
 * One contract per handler and per inlined slot, so a type reachable through several owners is verified
 * once for each of them, which is cheaper than sharing a verdict through a static registry keyed by class
 * would be.
 *
 * @see BinaryHandlerGenericValueClass
 */
final class ValueClassConstructorContract
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private final static Logger logger = Logging.getLogger(ValueClassConstructorContract.class);



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * @param type        the value class whose constructor is to be verified; must not be {@code null}.
	 * @param fields      the persistable fields, in the order the constructor accepts them;
	 *                    must not be {@code null}.
	 * @param constructor the constructor as a {@code (Object[])Object} handle; must not be {@code null}.
	 *
	 * @return the contract for that type.
	 */
	static ValueClassConstructorContract New(
		final Class<?>            type       ,
		final XGettingEnum<Field> fields     ,
		final MethodHandle        constructor
	)
	{
		final ValueClassConstructorContract contract = new ValueClassConstructorContract(
			notNull(type)                       ,
			notNull(fields).toArray(Field.class),
			notNull(constructor)
		);

		contract.probe();

		return contract;
	}

	/**
	 * The value a parameter is given in one probing round. Two per type is all the scheme needs, since a
	 * round encodes one bit of the parameter's index - which is what lets {@code boolean}, with only two
	 * values to its name, be probed at all.
	 */
	private static Object sentinel(final Class<?> type, final boolean high)
	{
		if(type == boolean.class)
		{
			return Boolean.valueOf(high);
		}

		/* Deliberately 1 and 2 rather than the extremes: a constructor that range-checks its arguments
		 * accepts small positive values far more often, and a probe that is rejected answers nothing.
		 */
		if(type == byte.class)
		{
			return Byte.valueOf((byte)(high ? 2 : 1));
		}
		if(type == short.class)
		{
			return Short.valueOf((short)(high ? 2 : 1));
		}
		if(type == char.class)
		{
			return Character.valueOf((char)(high ? 2 : 1));
		}
		if(type == int.class)
		{
			return Integer.valueOf(high ? 2 : 1);
		}
		if(type == long.class)
		{
			return Long.valueOf(high ? 2L : 1L);
		}
		if(type == float.class)
		{
			return Float.valueOf(high ? 2f : 1f);
		}
		if(type == double.class)
		{
			return Double.valueOf(high ? 2d : 1d);
		}

		throw new BinaryPersistenceException("No sentinel for " + type.getName());
	}

	/**
	 * Whether parameter {@code index} takes the high sentinel in round {@code round}.
	 * <p>
	 * The first {@code bits} rounds encode the parameter's index, counted from one so that no parameter
	 * keeps the same value throughout: two parameters differ in at least one bit, so a constructor that
	 * swaps them hands one of them the other's value in that round. The two rounds after those are all-low
	 * and all-high, which gives every parameter both values and is what catches a constructor that computes
	 * its field rather than storing it.
	 */
	private static boolean isHigh(final int index, final int round, final int bits)
	{
		if(round < bits)
		{
			return ((index + 1) >>> round & 1) == 1;
		}

		return round != bits;
	}

	private static boolean areAllPrimitive(final Field[] fields)
	{
		for(final Field field : fields)
		{
			if(!field.getType().isPrimitive())
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Whether the constructor placed the passed argument where it belongs.
	 * <p>
	 * A primitive is compared by value: the constructor either stored what it was given or it did not,
	 * and a mismatch is a computed field, which drifts further on every cycle.
	 * <p>
	 * A reference is compared by {@code ==}, which for a value-typed field is substitutability and for an
	 * identity-typed one is identity. Where that does not hold, the two cases are told apart rather than
	 * lumped together: the argument turning up in <i>another</i> field is a permutation and always wrong,
	 * as is a value that was dropped or conjured. Anything else means the constructor derived the field
	 * from its argument - a defensive copy being the common case - which cannot be distinguished from a
	 * faithful one by inspection and is therefore left to the author.
	 */
	private static boolean isFaithful(
		final Class<?> fieldType,
		final Object[] arguments,
		final Object   rebuilt  ,
		final int      index
	)
	{
		final Object argument = arguments[index];

		if(fieldType.isPrimitive())
		{
			// a primitive argument is never null, the reader having boxed it
			return argument.equals(rebuilt);
		}

		if(argument == rebuilt)
		{
			return true;
		}

		if(argument == null || rebuilt == null)
		{
			// dropped or conjured, which no derivation can excuse
			return false;
		}

		for(int i = 0; i < arguments.length; i++)
		{
			if(i != index && arguments[i] == rebuilt)
			{
				// this field received another field's argument: a permutation
				return false;
			}
		}

		// derived from its argument, which cannot be judged from here
		return true;
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Class<?>     type       ;
	private final Field[]      fields     ;
	private final MethodHandle constructor;

	// resolved on first use and kept, the probe and the store-time fallback needing the same ones.
	private FieldReader[] readers;

	/* Set once the contract is established, which it cannot stop being - and also once it has been
	 * established that it cannot be checked at all, since retrying would answer the same. See #validate.
	 */
	private boolean verified;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private ValueClassConstructorContract(
		final Class<?>     type       ,
		final Field[]      fields     ,
		final MethodHandle constructor
	)
	{
		super();
		this.type        = type       ;
		this.fields      = fields     ;
		this.constructor = constructor;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	/**
	 * Settles the contract at handler creation where it can be, by constructing throwaway instances from
	 * chosen values instead of waiting for one the application produces.
	 * <p>
	 * Preferred over {@link #validate(Object)} for two reasons. It fails at startup - handlers are built
	 * for every type the storage contains, so a violation surfaces when the storage is opened rather than
	 * on the first store of that type, and it is caught even in an application that only ever loads. And
	 * its values are chosen to be discriminating, where an application's first instance may happen not to
	 * be: all-default field values are a fixed point of many a wrong constructor, so a real specimen can
	 * pass while proving nothing.
	 * <p>
	 * Silent where it cannot answer - a constructor that rejects the values, or a field whose type has no
	 * sentinel - and {@link #validate(Object)} then remains as the net. The two are complementary: this one
	 * chooses good inputs but may be refused, that one is always accepted but takes what it is given.
	 *
	 * @throws PersistenceExceptionTypeNotPersistable if the constructor does not reproduce the values.
	 */
	private void probe()
	{
		/* Restricted to all-primitive layouts: a reference field would need two distinguishable instances
		 * of its own type, which cannot be fabricated for an arbitrary one. Every inlined layout qualifies
		 * by construction, its members being primitives or inlined themselves.
		 */
		if(!areAllPrimitive(this.fields))
		{
			return;
		}

		if(this.fields.length == 0)
		{
			// a layout with no members has nothing that could be misplaced
			this.verified = true;
			return;
		}

		final FieldReader[] readers = this.resolveReaders();
		if(readers == null)
		{
			// reported once by #resolveReaders; nothing here can be checked
			return;
		}

		final int count = this.fields.length;
		final int bits  = Math.max(1, Integer.SIZE - Integer.numberOfLeadingZeros(count));

		// the index-encoding rounds, then the all-low and all-high ones
		for(int round = 0; round < bits + 2; round++)
		{
			final Object[] arguments = new Object[count];
			for(int i = 0; i < count; i++)
			{
				arguments[i] = sentinel(this.fields[i].getType(), isHigh(i, round, bits));
			}

			final Object rebuilt;
			try
			{
				rebuilt = (Object)this.constructor.invokeExact(arguments);
			}
			catch(final Error e)
			{
				throw e;
			}
			catch(final Throwable t)
			{
				// the constructor refused the probing values, which says nothing about the contract
				return;
			}

			this.compare(readers, arguments, rebuilt, true);
		}

		this.verified = true;
	}

	/**
	 * Verifies the contract against the passed instance, unless it already has been - the fallback for
	 * what {@link #probe()} could not settle at handler creation.
	 * <p>
	 * Deliberately unsynchronized: concurrent first stores verify the same immutable state and reach the
	 * same verdict, so the worst a race costs is a second verification.
	 *
	 * @param specimen an instance of the handled type, may be {@literal null}.
	 *
	 * @throws PersistenceExceptionTypeNotPersistable if the constructor does not reproduce the instance's
	 *         field values.
	 */
	void validate(final Object specimen)
	{
		if(this.verified || specimen == null)
		{
			return;
		}

		final FieldReader[] readers = this.resolveReaders();
		if(readers == null)
		{
			// reported once by #resolveReaders; nothing here can be checked
			return;
		}

		final Object[] arguments = new Object[readers.length];
		for(int i = 0; i < arguments.length; i++)
		{
			arguments[i] = readers[i].readValue(specimen);
		}

		this.compare(readers, arguments, this.construct(arguments), false);

		this.verified = true;
	}

	/**
	 * @param probing whether the values are this class's own rather than an instance the application
	 *                produced, which the message has to say or it describes an instance nobody wrote.
	 */
	private void compare(
		final FieldReader[] readers  ,
		final Object[]      arguments,
		final Object        rebuilt  ,
		final boolean       probing
	)
	{
		for(int i = 0; i < arguments.length; i++)
		{
			final Object rebuiltValue = readers[i].readValue(rebuilt);
			if(!isFaithful(this.fields[i].getType(), arguments, rebuiltValue, i))
			{
				throw this.violation(
					(probing ? "constructed with test values, " : "")
					+ "field " + this.fields[i].getName() + " was given " + arguments[i]
					+ " but came back as " + rebuiltValue
					+ ". Its constructor must assign each argument to the corresponding field unmodified,"
					+ " since loading hands it the stored field values"
					, null
				);
			}
		}
	}

	/**
	 * The readers for this type's fields, or {@literal null} where the type's module does not grant the
	 * access to read them.
	 * <p>
	 * Resolved here rather than at handler creation, and answered with {@literal null} rather than a
	 * throw, because a type can be perfectly persistable while its fields cannot be read reflectively: a
	 * JDK value type opted into inlining is stored through memory offsets and needs no such access. Making
	 * the check a new reason for such a configuration to stop working would trade a silent defect for a
	 * loud regression. It is reported once, since an unverifiable contract is still worth knowing about.
	 */
	private FieldReader[] resolveReaders()
	{
		if(this.readers != null)
		{
			return this.readers;
		}

		final FieldReader[] readers = new FieldReader[this.fields.length];

		for(int i = 0; i < readers.length; i++)
		{
			try
			{
				readers[i] = BinaryValueHandleFunctions.provideFieldReader(this.fields[i]);
			}
			catch(final RuntimeException e)
			{
				logger.warn(
					"Cannot verify that the constructor of value class {} reproduces the field values it is"
					+ " given, field {} not being readable: {}. A constructor that assigns an argument to"
					+ " another field, or computes a field from it, would change the stored values on every"
					+ " load without any indication.",
					this.type.getName(),
					this.fields[i].getName(),
					e.getMessage()
				);

				// asking again would answer the same, so the contract stops trying
				this.verified = true;

				return null;
			}
		}

		return this.readers = readers;
	}

	private Object construct(final Object[] arguments)
	{
		try
		{
			return (Object)this.constructor.invokeExact(arguments);
		}
		catch(final Error e)
		{
			throw e;
		}
		catch(final Throwable t)
		{
			/* Rejecting the very values one of its own instances holds means no instance of this type
			 * could be loaded back, so it is the same contract violation reported one step earlier.
			 */
			throw this.violation("its constructor rejected the field values of one of its own instances", t);
		}
	}

	private PersistenceExceptionTypeNotPersistable violation(final String reason, final Throwable cause)
	{
		return new PersistenceExceptionTypeNotPersistable(this.type,
			new BinaryPersistenceException(
				"Value class " + this.type.getName() + " cannot be handled generically: " + reason
				+ ". Register a custom type handler for it.",
				cause
			)
		);
	}

}
