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

import org.eclipse.serializer.afs.types.AFile;
import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.collections.ConstList;
import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.functional.IndexedAcceptor;
import org.eclipse.serializer.functional.InstanceDispatcherLogic;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.exceptions.BinaryPersistenceException;
import org.eclipse.serializer.persistence.binary.java.io.BinaryHandlerFile;
import org.eclipse.serializer.persistence.binary.java.lang.*;
import org.eclipse.serializer.persistence.binary.java.math.BinaryHandlerBigDecimal;
import org.eclipse.serializer.persistence.binary.java.math.BinaryHandlerBigInteger;
import org.eclipse.serializer.persistence.binary.java.net.*;
import org.eclipse.serializer.persistence.binary.java.nio.file.BinaryHandlerPath;
import org.eclipse.serializer.persistence.binary.java.sql.BinaryHandlerSqlDate;
import org.eclipse.serializer.persistence.binary.java.sql.BinaryHandlerSqlTime;
import org.eclipse.serializer.persistence.binary.java.sql.BinaryHandlerSqlTimestamp;
import org.eclipse.serializer.persistence.binary.java.time.BinaryHandlerPeriod;
import org.eclipse.serializer.persistence.binary.java.time.BinaryHandlerZoneOffset;
import org.eclipse.serializer.persistence.binary.java.util.*;
import org.eclipse.serializer.persistence.binary.java.util.concurrent.*;
import org.eclipse.serializer.persistence.binary.java.util.regex.BinaryHandlerPattern;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections.*;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections.lazy.BinaryHandlerLazyArrayList;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections.lazy.BinaryHandlerLazyHashMap;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections.lazy.BinaryHandlerLazyHashMapSegmentEntryList;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.collections.lazy.BinaryHandlerLazyHashSet;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.reference.BinaryHandlerControlledLazy;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.reference.BinaryHandlerLazyDefault;
import org.eclipse.serializer.persistence.binary.org.eclipse.serializer.util.BinaryHandlerSubstituterDefault;
import org.eclipse.serializer.persistence.types.*;
import org.eclipse.serializer.reference.Referencing;
import org.eclipse.serializer.reference.Swizzling;
import org.eclipse.serializer.typing.XTypes;
import org.eclipse.serializer.util.VMInfo;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;

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
			.registerTypeHandlers(lazyCollectionsHandlers())
			.registerTypeHandlers(platformDependentHandlers())
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
				BinaryHandlerPeriod.New()  ,
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
				
				BinaryHandlerBitSet.New(),

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
				
				// JDK 17
				BinaryHandlerImmutableCollectionsList12.New(),
				BinaryHandlerImmutableCollectionsSet12.New(),
				
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
			/* (29.10.2013 TM)TODO: more default custom handlers
			 * - VarString
			 * - VarByte etc.
			 */
		);

		// default custom handlers have no fixed typeId like native handlers.
		return defaultHandlers;
	}
	
	public static final XGettingSequence<? extends PersistenceTypeHandler<Binary, ?>> lazyCollectionsHandlers()
	{
		final ConstList<? extends PersistenceTypeHandler<Binary, ?>> lazyCollectionsHandlers = ConstList.New(
			BinaryHandlerLazyArrayList.New(),
			BinaryHandlerLazyHashMap.New(),
			BinaryHandlerLazyHashMapSegmentEntryList.New(),
			BinaryHandlerLazyHashSet.New(),
			BinaryHandlerControlledLazy.New()
		);

		return lazyCollectionsHandlers;
	}
	
	@SuppressWarnings("unchecked")
	public static final XGettingSequence<? extends PersistenceTypeHandler<Binary, ?>> platformDependentHandlers()
	{
		VMInfo vmInfo = new VMInfo.Default();
		
		final BulkList<PersistenceTypeHandler<Binary, ?>> platformDependentHandlers = BulkList.New();
		
		if(!vmInfo.isAnyAndroid())
		{
			platformDependentHandlers.add(BinaryHandlerSetFromMap.New());
		}
		
		return platformDependentHandlers;
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
