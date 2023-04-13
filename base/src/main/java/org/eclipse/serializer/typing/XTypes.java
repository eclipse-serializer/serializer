package org.eclipse.serializer.typing;

/*-
 * #%L
 * Eclipse Serializer Base
 * %%
 * Copyright (C) 2023 Eclipse Foundation
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static org.eclipse.serializer.util.X.notNull;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Date;

import org.eclipse.serializer.exceptions.NumberRangeException;

/**
 * Collection of generic util logic missing or too complicated in JDK API.
 *
 */
public final class XTypes
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	/*
	 * Note:
	 * This DirectByteBuffer checking logic is not superfluous, even if it might seem so at first glance.
	 * There are several reasons for it:
	 * 
	 * 1.)
	 * It is precisely about checking a ByteBuffer instance about being a DirectByteBuffer, the type returned by
	 * ByteBuffer#allocateDirect.
	 *
	 * 2.)
	 * Using this detour/trick produces the correct program behavior even across JDK versions with changing classes
	 * (e.g. from JDK 8 to JDK 9) and even on other platforms (e.g. android). Whatever class in whatever package
	 * is returned by ByteBuffer#allocateDirect, THAT class is the one to be tested for.
	 * 
	 * It's a simple and perfectly valid solution to indirectly resolve an internal and name-changing type.
	 * No matter the JDK version or platform.
	 */
	private static final Class<?> CLASS_DirectByteBuffer = ByteBuffer.allocateDirect(Long.BYTES).getClass();
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final Class<?> directByteBufferClass()
	{
		return CLASS_DirectByteBuffer;
	}
	
	public static final boolean isDirectByteBuffer(final ByteBuffer byteBuffer)
	{
		notNull(byteBuffer);
		
		return CLASS_DirectByteBuffer.isInstance(byteBuffer);
	}
	
	public static final ByteBuffer guaranteeDirectByteBuffer(final ByteBuffer directBuffer)
	{
		// null-check inside
		if(isDirectByteBuffer(directBuffer))
		{
			return directBuffer;
		}
		
		throw new ClassCastException(
			directBuffer.getClass().getName() + " cannot be cast to " + CLASS_DirectByteBuffer.getName()
		);
	}

	public static boolean isValueType(final Class<?> c)
	{
		// all value types, ordered in common use probability
		return c == String.class
			|| Number.class.isAssignableFrom(c)
			|| ValueType.class.isAssignableFrom(c)
			|| c == Field.class // Field instances are no cached singletons but get copied over and over as value types
		;
	}
	

	/**
	 * Checks if the type of the passed instance is an immutable special value type of the java language.
	 * This includes all primitive wrappers and {@link String}.
	 *
	 * @param o the instance to be checked
	 * @return {@code true} if the type of the passed instance is an immutable special value type.
	 */
	public static boolean isValueType(final Object o)
	{
		// everything extending Number is a value type by definition.
		return o instanceof String
			|| o instanceof Number
			|| o instanceof ValueType
		;
	}

	public static final byte to_byte(final boolean value)
	{
		return value
			? (byte)1
			: (byte)0
		;
	}
	
	public static final boolean to_boolean(final byte value)
	{
		return value != 0;
	}

	public static final int to_int(final long value) throws NumberRangeException
	{
		if(value > Integer.MAX_VALUE)
		{
			throw new IllegalArgumentException(value + " > " + Integer.MAX_VALUE);
		}
		else if(value < Integer.MIN_VALUE)
		{
			throw new IllegalArgumentException(value + " < " + Integer.MIN_VALUE);
		}
		return (int)value;
	}

	public static final TypeMapping<Float> createDefaultTypeSimilarity()
	{
		final Class<?>[] primitives =
		{
			byte.class, boolean.class, short.class, char.class, int.class, float.class, long.class, double.class
		};
		
		final Class<?>[] wrappers =
		{
			Byte.class, Boolean.class, Short.class, Character.class, Integer.class, Float.class, Long.class, Double.class
		};
		
		final int[][] primSims =
		{
			{100, 50, 80, 70, 60, 30, 40, 30},
			{ 50,100, 40, 10, 30, 20, 20, 10},
			{ 80, 40,100, 50, 80, 50, 60, 50},
			{ 70, 10, 50,100, 50, 40, 30, 20},
			{ 60, 30, 80, 50,100, 70, 80, 60},
			{ 30, 20, 50, 40, 70,100, 60, 80},
			{ 40, 20, 60, 30, 80, 60,100, 70},
			{ 30, 10, 50, 20, 60, 80, 70,100}
		};
		
		final Function percent   = value -> value * 0.010f;
		final Function prim2Wrap = value -> value * 0.008f; // primitive-to-wrapper similarity is flat 80%.
						
		final TypeMapping<Float> typeSimilarities = TypeMapping.New();
		
		// 256 mapping for the 8 primitives and their 8 wrappers amongst each other
		for(int x = 0; x < primitives.length; x++)
		{
			registerDual(typeSimilarities, primitives[x], percent  , primitives, primSims[x]);
			registerDual(typeSimilarities, wrappers  [x], percent  , wrappers  , primSims[x]);
			registerDual(typeSimilarities, wrappers  [x], prim2Wrap, primitives, primSims[x]);
			registerDual(typeSimilarities, primitives[x], prim2Wrap, wrappers  , primSims[x]);
		}
		
		// additional common types' similarities to the primitives
		registerDual(typeSimilarities, BigInteger.class, percent, primitives, 30, 10, 50, 20, 70, 50, 90, 60);
		registerDual(typeSimilarities, BigDecimal.class, percent, primitives, 20,  5, 40, 10, 50, 70, 60, 90);
		registerDual(typeSimilarities, String    .class, percent, primitives, 20, 10, 20, 60, 20, 30, 20, 30);
		registerDual(typeSimilarities, Date      .class, percent, primitives, 10,  0, 20,  0, 30,  0, 60,  0);

		// additional common types' similarities to the primitives wrappers (values identical to primitives above)
		registerDual(typeSimilarities, BigInteger.class, percent, wrappers, 30, 10, 50, 20, 70, 50, 90, 60);
		registerDual(typeSimilarities, BigDecimal.class, percent, wrappers, 20,  5, 40, 10, 50, 70, 60, 90);
		registerDual(typeSimilarities, String    .class, percent, wrappers, 20, 10, 20, 60, 20, 30, 20, 30);
		registerDual(typeSimilarities, Date      .class, percent, wrappers, 10,  0, 20,  0, 30,  0, 60,  0);

		// additional common types among each other, without self-mappings
		registerDual(typeSimilarities, BigInteger.class, percent, BigDecimal.class, 80);
		registerDual(typeSimilarities, BigInteger.class, percent, String.class    , 20);
		registerDual(typeSimilarities, BigInteger.class, percent, Date.class      , 60);
		registerDual(typeSimilarities, BigDecimal.class, percent, String.class    , 30);
		registerDual(typeSimilarities, BigDecimal.class, percent, Date.class      , 20);
		registerDual(typeSimilarities, String.class    , percent, Date.class      , 40);
		
		return typeSimilarities;
	}
		
	interface Function
	{
		public float apply(int value);
	}
		
	static void registerDual(
		final TypeMapping<Float> typeSimilarities,
		final Class<?>           type           ,
		final Function           function        ,
		final Class<?>[]         types           ,
		final int...             values
	)
	{
		for(int i = 0; i < types.length; i++)
		{
			registerDual(typeSimilarities, type, function, types[i], values[i]);
		}
	}
	
	static void registerDual(
		final TypeMapping<Float> typeSimilarities,
		final Class<?>           type1           ,
		final Function           function        ,
		final Class<?>           type2           ,
		final int                value
	)
	{
		final float similarity = function.apply(value);
		typeSimilarities.register(type1, type2, similarity);
		typeSimilarities.register(type2, type1, similarity);
	}
	

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private XTypes()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
