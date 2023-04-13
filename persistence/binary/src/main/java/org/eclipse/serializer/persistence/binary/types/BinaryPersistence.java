package org.eclipse.serializer.persistence.binary.types;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;

import org.eclipse.serializer.afs.types.AFile;
import org.eclipse.serializer.collections.ConstList;
import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.functional.IndexedAcceptor;
import org.eclipse.serializer.functional.InstanceDispatcherLogic;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.exceptions.BinaryPersistenceException;
import org.eclipse.serializer.persistence.binary.internal.BinaryHandlerPrimitive;
import org.eclipse.serializer.persistence.binary.internal.BinaryHandlerSingletonStatelessEnum;
import org.eclipse.serializer.persistence.binary.internal.BinaryHandlerStatelessConstant;
import org.eclipse.serializer.persistence.binary.java.io.BinaryHandlerFile;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerBoolean;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerByte;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerCharacter;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerClass;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerDouble;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerFloat;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerInteger;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerLong;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerNativeArray_boolean;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerNativeArray_byte;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerNativeArray_char;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerNativeArray_double;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerNativeArray_float;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerNativeArray_int;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerNativeArray_long;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerNativeArray_short;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerObject;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerShort;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerString;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerStringBuffer;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerStringBuilder;
import org.eclipse.serializer.persistence.binary.java.lang.BinaryHandlerVoid;
import org.eclipse.serializer.persistence.binary.java.math.BinaryHandlerBigDecimal;
import org.eclipse.serializer.persistence.binary.java.math.BinaryHandlerBigInteger;
import org.eclipse.serializer.persistence.binary.java.net.BinaryHandlerInet4Address;
import org.eclipse.serializer.persistence.binary.java.net.BinaryHandlerInet6Address;
import org.eclipse.serializer.persistence.binary.java.net.BinaryHandlerInetAddress;
import org.eclipse.serializer.persistence.binary.java.net.BinaryHandlerInetSocketAddress;
import org.eclipse.serializer.persistence.binary.java.net.BinaryHandlerURI;
import org.eclipse.serializer.persistence.binary.java.net.BinaryHandlerURL;
import org.eclipse.serializer.persistence.binary.java.nio.file.BinaryHandlerPath;
import org.eclipse.serializer.persistence.binary.java.sql.BinaryHandlerSqlDate;
import org.eclipse.serializer.persistence.binary.java.sql.BinaryHandlerSqlTime;
import org.eclipse.serializer.persistence.binary.java.sql.BinaryHandlerSqlTimestamp;
import org.eclipse.serializer.persistence.binary.java.time.BinaryHandlerZoneOffset;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerCopyOnWriteArraySet;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerCurrency;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerDate;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerIdentityHashMap;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerLinkedHashMap;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerLinkedHashSet;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerOptionalLong;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerStack;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerTreeMap;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerTreeSet;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections.BinaryHandlerBulkList;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections.BinaryHandlerConstHashEnum;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections.BinaryHandlerConstHashTable;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections.BinaryHandlerConstList;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections.BinaryHandlerEqBulkList;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections.BinaryHandlerEqConstHashEnum;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections.BinaryHandlerEqConstHashTable;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections.BinaryHandlerEqHashEnum;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections.BinaryHandlerEqHashTable;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections.BinaryHandlerFixedList;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections.BinaryHandlerHashEnum;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections.BinaryHandlerHashTable;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections.BinaryHandlerLimitList;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections.BinaryHandlerSingleton;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.reference.BinaryHandlerLazyDefault;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.util.BinaryHandlerSubstituterDefault;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerArrayDeque;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerArrayList;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerCopyOnWriteArrayList;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerHashMap;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerHashSet;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerHashtable;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerLinkedList;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerLocale;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerOptionalDouble;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerOptionalInt;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerPriorityQueue;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerProperties;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerVector;
import org.eclipse.serializer.persistence.binary.java.util.BinaryHandlerWeakHashMap;
import org.eclipse.serializer.persistence.binary.java.util.concurrent.BinaryHandlerConcurrentHashMap;
import org.eclipse.serializer.persistence.binary.java.util.concurrent.BinaryHandlerConcurrentLinkedDeque;
import org.eclipse.serializer.persistence.binary.java.util.concurrent.BinaryHandlerConcurrentLinkedQueue;
import org.eclipse.serializer.persistence.binary.java.util.concurrent.BinaryHandlerConcurrentSkipListMap;
import org.eclipse.serializer.persistence.binary.java.util.concurrent.BinaryHandlerConcurrentSkipListSet;
import org.eclipse.serializer.persistence.binary.java.util.regex.BinaryHandlerPattern;
import org.eclipse.serializer.persistence.internal.PersistenceTypeDictionaryFileHandler;
import org.eclipse.serializer.persistence.types.Persistence;
import org.eclipse.serializer.persistence.types.PersistenceCustomTypeHandlerRegistry;
import org.eclipse.serializer.persistence.types.PersistenceFunction;
import org.eclipse.serializer.persistence.types.PersistenceSizedArrayLengthController;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionary;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandler;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandlerCreator;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandlerManager;
import org.eclipse.serializer.persistence.types.PersistenceTypeIdLookup;
import org.eclipse.serializer.reference.Referencing;
import org.eclipse.serializer.reference.Swizzling;
import org.eclipse.serializer.typing.XTypes;

public final class BinaryPersistence extends Persistence
{
	public static BinaryPersistenceFoundation<?> Foundation()
	{
		return Foundation(null);
	}

	public static BinaryPersistenceFoundation<?> Foundation(final InstanceDispatcherLogic dispatcher)
	{
		final BinaryPersistenceFoundation<?> foundation = BinaryPersistenceFoundation.New()
			.setInstanceDispatcher(dispatcher)
		;
		return foundation;
	}

	public static final PersistenceCustomTypeHandlerRegistry<Binary> createDefaultCustomTypeHandlerRegistry(
		final Referencing<PersistenceTypeHandlerManager<Binary>>              typeHandlerManager,
		final PersistenceSizedArrayLengthController                           controller        ,
		final PersistenceTypeHandlerCreator<Binary>                           typeHandlerCreator,
		final XGettingCollection<? extends PersistenceTypeHandler<Binary, ?>> customHandlers
	)
	{
		/* (16.10.2019 TM)NOTE:
		 * Native handlers are split into value and referencing types since plugins that handle references
		 * differently (e.g. load all only on demand, like a data viewer REST service) can reuse the value
		 * type handlers but need to replace the referencing type handlers.
		 */
		final XGettingSequence<? extends PersistenceTypeHandler<Binary, ?>> nativeHandlersValueTypes =
			createNativeHandlersValueTypes(typeHandlerManager, controller, typeHandlerCreator)
		;
		final XGettingSequence<? extends PersistenceTypeHandler<Binary, ?>> nativeHandlersReferencingTypes =
			createNativeHandlersReferencingTypes(typeHandlerManager, controller, typeHandlerCreator)
		;

		final PersistenceCustomTypeHandlerRegistry.Default<Binary> defaultCustomTypeHandlerRegistry =
			PersistenceCustomTypeHandlerRegistry.<Binary>New()
			.registerTypeHandlers(nativeHandlersValueTypes)
			.registerTypeHandlers(nativeHandlersReferencingTypes)
			.registerTypeHandlers(defaultCustomHandlers(controller))
			.registerTypeHandlers(customHandlers)
		;

		return defaultCustomTypeHandlerRegistry;
	}

	static final void initializeNativeTypeId(
		final PersistenceTypeHandler<Binary, ?> typeHandler       ,
		final PersistenceTypeIdLookup           nativeTypeIdLookup
	)
	{
		final long nativeTypeId = nativeTypeIdLookup.lookupTypeId(typeHandler.type());
		if(Swizzling.isNotFoundId(nativeTypeId))
		{
			throw new BinaryPersistenceException("No native TypeId found for type " + typeHandler.type());
		}

		typeHandler.initialize(nativeTypeId);
	}

	public static final XGettingSequence<? extends PersistenceTypeHandler<Binary, ?>> createNativeHandlersValueTypes(
		final Referencing<PersistenceTypeHandlerManager<Binary>> typeHandlerManager,
		final PersistenceSizedArrayLengthController              controller        ,
		final PersistenceTypeHandlerCreator<Binary>              typeHandlerCreator
	)
	{
		final ConstList<? extends PersistenceTypeHandler<Binary, ?>> nativeHandlersValueTypes = ConstList.New(
				BinaryHandlerPrimitive.New(byte   .class),
				BinaryHandlerPrimitive.New(boolean.class),
				BinaryHandlerPrimitive.New(short  .class),
				BinaryHandlerPrimitive.New(char   .class),
				BinaryHandlerPrimitive.New(int    .class),
				BinaryHandlerPrimitive.New(float  .class),
				BinaryHandlerPrimitive.New(long   .class),
				BinaryHandlerPrimitive.New(double .class),

				BinaryHandlerClass.New(typeHandlerManager),

				BinaryHandlerByte.New()     ,
				BinaryHandlerBoolean.New()  ,
				BinaryHandlerShort.New()    ,
				BinaryHandlerCharacter.New(),
				BinaryHandlerInteger.New()  ,
				BinaryHandlerFloat.New()    ,
				BinaryHandlerLong.New()     ,
				BinaryHandlerDouble.New()   ,
				BinaryHandlerVoid.New()     ,
				BinaryHandlerObject.New()   ,

				BinaryHandlerString.New()       ,
				BinaryHandlerStringBuffer.New() ,
				BinaryHandlerStringBuilder.New(),

				BinaryHandlerNativeArray_byte.New()   ,
				BinaryHandlerNativeArray_boolean.New(),
				BinaryHandlerNativeArray_short.New()  ,
				BinaryHandlerNativeArray_char.New()   ,
				BinaryHandlerNativeArray_int.New()    ,
				BinaryHandlerNativeArray_float.New()  ,
				BinaryHandlerNativeArray_long.New()   ,
				BinaryHandlerNativeArray_double.New() ,

				BinaryHandlerBigInteger.New(),
				BinaryHandlerBigDecimal.New(),

				BinaryHandlerFile.New()    ,
				BinaryHandlerDate.New()    ,
				BinaryHandlerLocale.New()  ,
				BinaryHandlerCurrency.New(),
				BinaryHandlerPattern.New() ,

				BinaryHandlerInetAddress.New() ,
				BinaryHandlerInet4Address.New(),
				BinaryHandlerInet6Address.New(),

				BinaryHandlerPath.New(), // "abstract type" TypeHandler

				BinaryHandlerInetSocketAddress.New(),

				BinaryHandlerURI.New(),
				BinaryHandlerURL.New(),

				BinaryHandlerZoneOffset.New(),

				// non-nonsensical handlers required for confused developers
				BinaryHandlerSqlDate.New()     ,
				BinaryHandlerSqlTime.New()     ,
				BinaryHandlerSqlTimestamp.New(),

				BinaryHandlerOptionalInt.New(),
				BinaryHandlerOptionalLong.New(),
				BinaryHandlerOptionalDouble.New(),

			/* (12.11.2019 TM)NOTE:
			 * One might think that "empty" implementations of a collection interface would have no fields, anyway.
			 * But no, those classes extends 5 other classes, some of which bring along several times
			 * redundant delegate fields.
			 * Those fields cause access warnings (and access exceptions in the future) when trying to set them
			 * accessible in the generic handler implementation.
			 *
			 * Also, to avoid an erroneous instance creation that BinaryHandlerStateless might perform
			 * (e.g. when using a dummy object registry as tools might do), the constant instance itself
			 * has to be returned in case the 'create' should ever be invoked.
			 */
			BinaryHandlerStatelessConstant.New(Collections.emptyNavigableSet()),
				BinaryHandlerStatelessConstant.New(Collections.emptyNavigableMap()),

				// not an enum, as opposed to NaturalOrderComparator.
				BinaryHandlerStatelessConstant.New(Collections.reverseOrder())
		);

		/* (24.10.2013 TM)TODO: priv#117 more native handlers (Path, Instant and whatnot)
		 * Also see class Persistence for default TypeIds
		 */

		return nativeHandlersValueTypes;
	}

	public static final XGettingSequence<? extends PersistenceTypeHandler<Binary, ?>> createNativeHandlersReferencingTypes(
		final Referencing<PersistenceTypeHandlerManager<Binary>> typeHandlerManager,
		final PersistenceSizedArrayLengthController              controller        ,
		final PersistenceTypeHandlerCreator<Binary>              typeHandlerCreator
	)
	{
		final ConstList<? extends PersistenceTypeHandler<Binary, ?>> nativeHandlers = ConstList.New(

				// JDK 1.0 collections
				BinaryHandlerVector.New()               ,
				BinaryHandlerStack.New()                ,
				BinaryHandlerHashtable.New()            ,
				BinaryHandlerProperties.New()           ,

				// JDK 1.2 collections
				BinaryHandlerArrayList.New(),
				BinaryHandlerHashSet.New()              ,
				BinaryHandlerHashMap.New()              ,
				BinaryHandlerWeakHashMap.New()          ,
				BinaryHandlerLinkedList.New()           ,
				BinaryHandlerTreeMap.New()              ,
				BinaryHandlerTreeSet.New()              ,

				// JDK 1.4 collections
				BinaryHandlerIdentityHashMap.New()      ,
				BinaryHandlerLinkedHashMap.New()        ,
				BinaryHandlerLinkedHashSet.New()        ,

				// JDK 1.5 collections
				BinaryHandlerPriorityQueue.New()        ,
				BinaryHandlerConcurrentHashMap.New()    ,
				BinaryHandlerConcurrentLinkedQueue.New(),
				BinaryHandlerCopyOnWriteArrayList.New() ,
				BinaryHandlerCopyOnWriteArraySet.New()  ,

				// remaining JDK collections (wrappers and the like) are handled dynamically

				// changed with support of enums. And must change to keep TypeDictionary etc. consistent
				BinaryHandlerSingletonStatelessEnum.New(Comparator.naturalOrder().getClass()),

				// JDK 1.6 collections
				BinaryHandlerArrayDeque.New()           ,
				BinaryHandlerConcurrentSkipListMap.New(),
				BinaryHandlerConcurrentSkipListSet.New(),

				// JDK 1.7 collections
				BinaryHandlerConcurrentLinkedDeque.New(),

				BinaryHandlerLazyDefault.New(),

				// the way Optional is implemented, only a generically (low-level) working handler can handle it correctly
				typeHandlerCreator.createTypeHandlerGeneric(Optional.class)
		);

		return nativeHandlers;
	}

	public static final XGettingSequence<? extends PersistenceTypeHandler<Binary, ?>> defaultCustomHandlers(
		final PersistenceSizedArrayLengthController controller
	)
	{
		final ConstList<? extends PersistenceTypeHandler<Binary, ?>> defaultHandlers = ConstList.New(
				BinaryHandlerBulkList.New(controller)   ,
				BinaryHandlerLimitList.New(controller)  ,
				BinaryHandlerFixedList.New()            ,
				BinaryHandlerConstList.New()            ,
				BinaryHandlerEqBulkList.New(controller) ,
				BinaryHandlerHashEnum.New()             ,
				BinaryHandlerConstHashEnum.New()        ,
				BinaryHandlerEqHashEnum.New()           ,
				BinaryHandlerEqConstHashEnum.New()      ,
				BinaryHandlerHashTable.New()            ,
				BinaryHandlerConstHashTable.New()       ,
				BinaryHandlerEqHashTable.New()          ,
				BinaryHandlerEqConstHashTable.New()     ,
				BinaryHandlerSingleton.New()            ,
				BinaryHandlerSubstituterDefault.New()
			/* (29.10.2013 TM)TODO: more MicroStream default custom handlers
			 * - VarString
			 * - VarByte
			 * - _intList etc.
			 */
		);

		// default custom handlers have no fixed typeId like native handlers.
		return defaultHandlers;
	}

	public static final long resolveFieldBinaryLength(final Class<?> fieldType)
	{
		return fieldType.isPrimitive()
			? resolvePrimitiveFieldBinaryLength(fieldType)
			: Binary.objectIdByteLength()
		;
	}

	public static final long resolvePrimitiveFieldBinaryLength(final Class<?> primitiveType)
	{
		return XMemory.byteSizePrimitive(primitiveType);
	}

	public static final BinaryFieldLengthResolver createFieldLengthResolver()
	{
		return new BinaryFieldLengthResolver.Default();
	}

	public static PersistenceTypeDictionary provideTypeDictionaryFromFile(final AFile dictionaryFile)
	{
		final BinaryPersistenceFoundation<?> f = BinaryPersistenceFoundation.New()
			.setTypeDictionaryLoader(
				PersistenceTypeDictionaryFileHandler.New(dictionaryFile)
			)
		;
		return f.getTypeDictionaryProvider().provideTypeDictionary();
	}

	public static final int binaryValueSize(final Class<?> type)
	{
		return type.isPrimitive()
			? XMemory.byteSizePrimitive(type)
			: Binary.objectIdByteLength()
		;
	}

	public static int[] calculateBinarySizes(final XGettingSequence<Field> fields)
	{
		final int[] fieldOffsets = new int[XTypes.to_int(fields.size())];
		fields.iterateIndexed(new IndexedAcceptor<Field>()
		{
			@Override
			public void accept(final Field e, final long index)
			{
				fieldOffsets[(int)index] = binaryValueSize(e.getType());
			}
		});
		return fieldOffsets;
	}

	public static final void iterateInstanceReferences(
		final PersistenceFunction iterator,
		final Object              instance,
		final long[]      referenceOffsets
	)
	{
		for(int i = 0; i < referenceOffsets.length; i++)
		{
			if(referenceOffsets[i] != 0)
			{
				iterator.apply(XMemory.getObject(instance, referenceOffsets[i]));
			}
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 *
	 * @throws UnsupportedOperationException when called
	 */
	private BinaryPersistence()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
