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

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.collections.EqConstHashEnum;
import org.eclipse.serializer.collections.EqHashEnum;
import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.exceptions.TypeCastException;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTypeConsistency;
import org.eclipse.serializer.persistence.types.PersistenceEagerStoringFieldEvaluator;
import org.eclipse.serializer.persistence.types.PersistenceFieldLengthResolver;
import org.eclipse.serializer.persistence.types.PersistenceFunction;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceStoreHandler;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMember;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberField;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldReflective;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMemberFieldValueStruct;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionMember;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandlerReflective;
import org.eclipse.serializer.persistence.types.Persister;
import org.eclipse.serializer.persistence.types.PersistenceValueInliningResolver;
import org.eclipse.serializer.reflect.XReflect;
import org.eclipse.serializer.util.UtilStackTrace;

/**
 * Skeletal base for binary type handlers that derive their persistent layout reflectively from the fields
 * of the handled class. Builds per-member {@link BinaryValueStorer}s and {@link BinaryValueSetter}s
 * (consulting the {@link BinaryFieldHandlerProvider} for custom-field overrides), tracks storing vs.
 * setting members, evaluates eager-storing rules via the supplied
 * {@link PersistenceEagerStoringFieldEvaluator}, and provides default {@link #store}, {@link #updateState},
 * and reference-iteration logic on top of these arrays.
 * <p>
 * Concrete subclasses are {@link BinaryHandlerGenericType} (regular classes) and
 * {@link BinaryHandlerGenericEnum} (enums). The base class is also reused by reflective legacy handlers
 * via {@link AbstractBinaryLegacyTypeHandlerReflective}.
 *
 * @param <T> the runtime type handled.
 *
 * @see PersistenceTypeHandlerReflective
 * @see BinaryHandlerGenericType
 * @see BinaryHandlerGenericEnum
 */
public abstract class AbstractBinaryHandlerReflective<T>
extends BinaryTypeHandler.Abstract<T>
implements PersistenceTypeHandlerReflective<Binary, T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
		
	protected static <D extends PersistenceTypeDefinitionMember> EqHashEnum<D> MemberEnum()
	{
		return EqHashEnum.New(
			PersistenceTypeDescriptionMember.identityHashEqualator()
		);
	}
	
	protected static <D extends PersistenceTypeDefinitionMember> EqHashEnum<D> MemberEnum(
		final XGettingCollection<D> initialMembers
	)
	{
		return AbstractBinaryHandlerReflective.<D>MemberEnum().addAll(initialMembers);
	}
	
	protected static EqHashEnum<PersistenceTypeDefinitionMemberFieldReflective> deriveMembers(
		final Class<?>                         entityType      ,
		final XGettingEnum<Field>              fields          ,
		final PersistenceFieldLengthResolver   lengthResolver  ,
		final PersistenceValueInliningResolver inliningResolver
	)
	{
		final EqHashEnum<PersistenceTypeDefinitionMemberFieldReflective> members = MemberEnum();

		for(final Field field : fields)
		{
			// just a precaution
			if(XReflect.isStatic(field))
			{
				throw new PersistenceExceptionTypeConsistency("static fields are not persistable.");
			}

			final PersistenceTypeDefinitionMemberFieldReflective member = deriveMember(
				entityType, field, lengthResolver, inliningResolver
			);

			if(!members.add(member))
			{
				throw new PersistenceExceptionTypeConsistency("Duplicate member descriptions.");
			}
		}

		return members;
	}

	private static PersistenceTypeDefinitionMemberFieldReflective deriveMember(
		final Class<?>                         entityType      ,
		final Field                            field           ,
		final PersistenceFieldLengthResolver   lengthResolver  ,
		final PersistenceValueInliningResolver inliningResolver
	)
	{
		final XGettingEnum<Field> inlinedFields = inliningResolver.resolveInlinedFields(entityType, field);
		if(inlinedFields == null)
		{
			return declaredField(field, lengthResolver);
		}

		final BulkList<PersistenceTypeDefinitionMemberField> inlinedMembers = BulkList.New();
		for(final Field inlinedField : inlinedFields)
		{
			/* A member of the inlined layout may be inlined itself, and then carries no object id either, so
			 * the slot stays free of them at every depth. The resolver has already established that the whole
			 * tree is eligible and acyclic, so asking it again per level only derives what it decided.
			 */
			final PersistenceTypeDefinitionMemberField inlinedMember = deriveMember(
				field.getType(), inlinedField, lengthResolver, inliningResolver
			);
			if(inlinedMember.hasReferences())
			{
				/* An inlined slot is a fixed-length non-reference member, which is what lets the storage engine
				 * skip it as it skips a primitive. An object id inside one would be skipped along with it, so
				 * the garbage collector would never see the entity it points to and would collect it while it is
				 * still referenced. The member creation below refuses that as well; this here adds the field
				 * context to point at the resolver that wrongly deemed the field inlinable.
				 */
				throw new PersistenceExceptionTypeConsistency(
					"Field " + field.getDeclaringClass().getName() + "#" + field.getName()
					+ " cannot be inlined: " + field.getType().getName()
					+ " holds references, and an inlined slot must not."
				);
			}
			inlinedMembers.add(inlinedMember);
		}

		return PersistenceTypeDefinitionMemberFieldValueStruct.New(field, inlinedMembers);
	}
	
	protected static final EqConstHashEnum<PersistenceTypeDefinitionMemberFieldReflective> filter(
		final XGettingCollection<? extends PersistenceTypeDefinitionMemberFieldReflective> fields    ,
		final Predicate<? super PersistenceTypeDefinitionMemberFieldReflective>            predicate
	)
	{
		return fields.filterTo(EqHashEnum.<PersistenceTypeDefinitionMemberFieldReflective>New(), predicate).immure();
	}
	
	protected static final <C extends Consumer<? super Field>> C unbox(
		final XGettingCollection<? extends PersistenceTypeDefinitionMemberFieldReflective> members,
		final C collector
	)
	{
		return PersistenceTypeDefinitionMemberFieldReflective.unbox(members, collector);
	}
	
	protected static final long equal(final long value1, final long value2) throws IllegalArgumentException
	{
		if(value1 != value2)
		{
			throw UtilStackTrace.cutStacktraceByOne(new IllegalArgumentException());
		}
		
		return value1;
	}
			
	protected static void createStorers(
		final Class<?>                                                 entityType          ,
		final Iterable<PersistenceTypeDefinitionMemberFieldReflective> storingMembers      ,
		final BinaryValueStorer[]                                      storers             ,
		final PersistenceEagerStoringFieldEvaluator                    eagerEvaluator      ,
		final BinaryFieldHandlerProvider				               fieldHandlerProvider,
		final boolean                                                  switchByteOrder
	)
	{
		int i = 0;
		for(final PersistenceTypeDefinitionMemberFieldReflective member : storingMembers)
		{
			final boolean isEager = eagerEvaluator.isEagerStoring(entityType, member.field());

			BinaryValueStorer customFieldStorer = fieldHandlerProvider.lookupFieldStorer(member.field(), isEager, switchByteOrder);
			if(member instanceof PersistenceTypeDefinitionMemberFieldValueStruct)
			{
				final PersistenceTypeDefinitionMemberFieldValueStruct struct =
					(PersistenceTypeDefinitionMemberFieldValueStruct)member
				;

				if(customFieldStorer != null)
				{
					/* The two describe the same field differently: the custom storer writes an object id where
					 * the type description states an inlined layout. Reading that back would misinterpret the
					 * owner's whole binary form, so the conflict must be resolved by configuration.
					 */
					throw new PersistenceExceptionTypeConsistency(
						"Field " + member.identifier() + " is both inlined and handled by a custom field storer."
					);
				}

				storers[i++] = BinaryValueStructFunctions.provideStorer(
					struct.field(), struct.members(), struct.persistentMinimumLength(), switchByteOrder
				);
			}
			else if (customFieldStorer != null)
			{
				storers[i++] = customFieldStorer;
			}
			else if(XReflect.isValueClass(member.type()))
			{
				// a value may be laid out inside its owner, where its offset holds no object reference
				storers[i++] = BinaryValueHandleFunctions.provideReferenceStorer(
					member.field(), isEager, switchByteOrder
				);
			}
			else
			{
				storers[i++] = BinaryValueFunctions.getObjectValueStorer(member.type(), isEager, switchByteOrder);
			}
		}
	}
	
	protected static long calculcateBinaryContentLength(
		final Iterable<PersistenceTypeDefinitionMemberFieldReflective> storingMembers
	)
	{
		long binaryContentLength = 0;
		
		for(final PersistenceTypeDefinitionMemberFieldReflective member : storingMembers)
		{
			final long fixedBinaryLength = equal(member.persistentMinimumLength(), member.persistentMaximumLength());
			binaryContentLength += fixedBinaryLength;
		}
		
		return binaryContentLength;
	}
	
	protected static final XGettingSequence<PersistenceTypeDefinitionMemberFieldReflective> createTypeDescriptionMembers(
		final Field[]                        persistentOrderFields,
		final PersistenceFieldLengthResolver lengthResolver
	)
	{
		final BulkList<PersistenceTypeDefinitionMemberFieldReflective> members = BulkList.New();
		
		for(final Field field : persistentOrderFields)
		{
			members.add(declaredField(field, lengthResolver));
		}
		
		return members;
	}
	
	protected static final long[] objectFieldOffsets(
		final Class<?>                                                                   entityClass,
		final XGettingSequence<? extends PersistenceTypeDefinitionMemberFieldReflective> members
	)
	{
		// (11.11.2019 TM)NOTE: important for usage of MemoryAccessorGeneric to provide the fields' class context
		final Field[] fields             = unbox(members, BulkList.New()).toArray(Field.class);
		final long[]  objectFieldOffsets = XMemory.objectFieldOffsets(entityClass, fields);
		
		return objectFieldOffsets;
		
	}

	

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	/*
	 * The persisted order of the fields differs from the declared order. For efficiency reasons, all
	 * reference fields come first, the primitive fields come second. The benefit from this is that the
	 * storage's garbage collector does not need to load primitive field data for traversing the entity graph.
	 * It might seem irrelevant if a few bytes more or less are loaded, but considering millions of entities
	 * have to be loaded, the savings can amass to a substantial amount.
	 * The relative order of reference fields and primitive fields respectively is maintained.
	 */
	private final EqConstHashEnum<PersistenceTypeDefinitionMember> membersInDeclaredOrder;
	
	private final EqConstHashEnum<PersistenceTypeDefinitionMemberFieldReflective>
		referenceMembers,
		primitiveMembers,
		structMembers   ,
		storingMembers  ,
		settingMembers
	;
	private final long[]
		storingMemoryOffsets,
		settingMemoryOffsets,
		refrnceMemoryOffsets
	;
	private final long
		refBinaryOffsetStart,
		refBinaryOffsetBound,
		binaryContentLength
	;
	private final BinaryValueStorer[]
		storers
	;
	private final BinaryValueSetter[]
		setters
	;
	private final EqConstHashEnum<Field>
		declOrderFields,
		referenceFields,
		primitiveFields
	;
	
	private final Field[] persisterFields;
	
	private final boolean switchByteOrder;

	private final BinaryFieldHandlerProvider fieldHandlerProvider;

	private final PersistenceValueInliningResolver inliningResolver;
	
	/* (28.10.2019 TM)TODO: encapsulate / abstract BinaryValue~ handling types.
	 * While the per-field handling via the BinaryValue~ handling types is perfectly fine for JDK
	 * and all fully Unsafe-compatible JVMs, it poses a considerable inefficiency for the generic
	 * memory handling implementation. There, every call with an object-based "offset" has to be
	 * translated to the corresponding field and then executed via that.
	 * A more efficient solution would be to encapsulate / abstract all BinaryValue~ handling type instances
	 * in a single BinaryObjectValues~ handling type and let its implementation use cached Fields directly.
	 */


	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractBinaryHandlerReflective(
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
		super(type, typeName);

		this.switchByteOrder = switchByteOrder;
		this.fieldHandlerProvider = fieldHandlerProvider;
		this.inliningResolver = inliningResolver;
		
		/*
		 * Unsafe JavaDoc says ensureClassInitialized is "often needed" for getting the field base, so better do it.
		 * MemoryAccessor implementations that do not use the field base don't need to do anything here.
		 * They probably also can't do anything to ensure a class is initialized.
		 */
		XMemory.ensureClassInitialized(type, persistableFields);
		
		final EqHashEnum<PersistenceTypeDefinitionMemberFieldReflective> instMembersInDeclOrdr =
			deriveMembers(type, persistableFields, lengthResolver, inliningResolver)
		;

		this.membersInDeclaredOrder = this.deriveAllMembers(instMembersInDeclOrdr);

		// member instances are created from the persistable fields and split into references and primitives
		this.referenceMembers = this.filterReferenceMembers(instMembersInDeclOrdr, MemberEnum()).immure();
		this.primitiveMembers = this.filterPrimitiveMembers(instMembersInDeclOrdr, MemberEnum()).immure();
		this.structMembers    = this.filterStructMembers(instMembersInDeclOrdr, MemberEnum()).immure();

		/* Persistent order is all reference fields in declared order, then all inlined fields, then all
		 * primitive fields. Inlined fields sit behind the references so that the leading run of object ids
		 * stays exactly the references, which is what the reference iteration relies on.
		 */
		this.storingMembers = MemberEnum(this.referenceMembers)
			.addAll(this.structMembers)
			.addAll(this.primitiveMembers)
			.immure()
		;

		/* Every member must land in exactly one group. Were one to fall through, it would silently vanish
		 * from the persistent form while still being part of the instance.
		 */
		if(this.storingMembers.size() != instMembersInDeclOrdr.size())
		{
			throw new PersistenceExceptionTypeConsistency(
				"Persistent member count " + this.storingMembers.size() + " of " + type.getName()
				+ " does not match its " + instMembersInDeclOrdr.size() + " persistable fields."
			);
		}
		this.settingMembers = this.filterSettingMembers(this.storingMembers);
		
		// storing/setting memory offsets initialization must be overridable for enum special casing
		this.storingMemoryOffsets = this.initializeStoringMemoryOffsets();
		this.settingMemoryOffsets = this.initializeSettingMemoryOffsets();
		this.refrnceMemoryOffsets = this.initializeStoringRefMemOffsets();
		
		// references are always stored at the beginning of the content (0 bytes after header)
		this.refBinaryOffsetStart = 0;
		this.refBinaryOffsetBound = Binary.referenceBinaryLength(this.referenceMembers.size());

		// storers set a field's value from the instance in memory to a buffered persistent form.
		this.storers = new BinaryValueStorer[this.storingMembers.intSize()];
		createStorers(type, this.storingMembers, this.storers, eagerEvaluator, fieldHandlerProvider, switchByteOrder);
		
		// setters set a field's value from a buffered persistent form to the instance in memory.
		this.setters = this.deriveSetters();

		// binary content length (without the entity header) is calculated based on the storing ("all") members.
		this.binaryContentLength = calculcateBinaryContentLength(this.storingMembers);

		// lots of data copying detour, but only once per handler and nicely readable
		this.declOrderFields = unbox(instMembersInDeclOrdr, EqHashEnum.New()).immure();
		this.referenceFields = unbox(this.referenceMembers, EqHashEnum.New()).immure();
		this.primitiveFields = unbox(this.primitiveMembers, EqHashEnum.New()).immure();
		
		this.persisterFields = persisterFields == null || persisterFields.isEmpty()
			? null
			: persisterFields.toArray(Field.class)
		;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// initializer logic //
	//////////////////////
		
	protected long[] initializeStoringMemoryOffsets()
	{
		return objectFieldOffsets(this.type(), this.storingMembers);
	}
	
	protected long[] initializeSettingMemoryOffsets()
	{
		// no difference by default (enums get skipping setters, but the offsets stay the same)
		return this.storingMemoryOffsets;
	}
	
	protected BinaryValueSetter[] deriveSetters()
	{
		final EqConstHashEnum<PersistenceTypeDefinitionMemberFieldReflective> members = this.settingMembers;
		
		final BinaryValueSetter[] setters = new BinaryValueSetter[members.intSize()];
		
		int i = 0;
		for(final PersistenceTypeDefinitionMemberFieldReflective member : members)
		{
			setters[i++] = this.deriveSetter(member);
		}
		
		return setters;
	}
	
	protected BinaryValueSetter deriveSetter(
		final PersistenceTypeDefinitionMemberFieldReflective member
	)
	{
		if(member instanceof PersistenceTypeDefinitionMemberFieldValueStruct)
		{
			return this.deriveStructSetter((PersistenceTypeDefinitionMemberFieldValueStruct)member);
		}

		BinaryValueSetter customFieldSetter = this.fieldHandlerProvider.lookupFieldSetter(member.field(), this.isSwitchedByteOrder());
		if (customFieldSetter != null)
		{
			return customFieldSetter;
		}
		else if(XReflect.isValueClass(member.type()))
		{
			// a value may be laid out inside its owner, where its offset holds no object reference
			return BinaryValueHandleFunctions.provideReferenceSetter(member.field(), this.isSwitchedByteOrder());
		}
		else
		{
			return BinaryValueFunctions.getObjectValueSetter(member.type(), this.isSwitchedByteOrder());
		}
	}

	private BinaryValueSetter deriveStructSetter(final PersistenceTypeDefinitionMemberFieldValueStruct member)
	{
		/* The inlined instance is constructed rather than populated, so the constructor's parameter order is
		 * needed alongside the persistent order. Re-resolving yields it, since the resolver reports the
		 * inlined type's fields in declaration order.
		 */
		final XGettingEnum<Field> declarationOrder = this.inliningResolver.resolveInlinedFields(
			this.type(), member.field()
		);
		if(declarationOrder == null)
		{
			throw new PersistenceExceptionTypeConsistency(
				"Field " + member.identifier() + " is described as inlined but is no longer eligible for inlining."
			);
		}

		return BinaryValueStructFunctions.provideSetter(
			member.field()                  ,
			member.type()                   ,
			member.members()                ,
			declarationOrder                ,
			member.persistentMinimumLength(),
			this.isSwitchedByteOrder()
		);
	}
	
	protected long[] initializeStoringRefMemOffsets()
	{
		return objectFieldOffsets(this.type(), this.referenceMembers);
	}
	
	protected EqConstHashEnum<PersistenceTypeDefinitionMemberFieldReflective> filterSettingMembers(
		final EqConstHashEnum<PersistenceTypeDefinitionMemberFieldReflective> members
	)
	{
		// by default, all members are settable (enums get skipping setters, but the fields stay the same)
		return members;
	}
	
	protected EqHashEnum<PersistenceTypeDefinitionMemberFieldReflective> filterReferenceMembers(
		final XGettingCollection<PersistenceTypeDefinitionMemberFieldReflective> members,
		final EqHashEnum<PersistenceTypeDefinitionMemberFieldReflective>         target
	)
	{
		return members.filterTo(target, m ->
			m.isReference()
		);
	}
	
	protected EqHashEnum<PersistenceTypeDefinitionMemberFieldReflective> filterStructMembers(
		final XGettingCollection<PersistenceTypeDefinitionMemberFieldReflective> members,
		final EqHashEnum<PersistenceTypeDefinitionMemberFieldReflective>         target
	)
	{
		return members.filterTo(target, m ->
			m instanceof PersistenceTypeDefinitionMemberFieldValueStruct
		);
	}

	protected EqHashEnum<PersistenceTypeDefinitionMemberFieldReflective> filterPrimitiveMembers(
		final XGettingCollection<PersistenceTypeDefinitionMemberFieldReflective> members,
		final EqHashEnum<PersistenceTypeDefinitionMemberFieldReflective>         target
	)
	{
		return members.filterTo(target, m ->
			m.isPrimitive()
		);
	}
	
	protected EqConstHashEnum<PersistenceTypeDefinitionMember> deriveAllMembers(
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> declaredOrderInstanceMembers
	)
	{
		return EqConstHashEnum.New(declaredOrderInstanceMembers);
	}
	
		
	
	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////
	
	public final boolean isSwitchedByteOrder()
	{
		return this.switchByteOrder;
	}
			
	@Override
	public XGettingEnum<Field> instanceFields()
	{
		return this.declOrderFields;
	}

	@Override
	public XGettingEnum<Field> instancePrimitiveFields()
	{
		return this.primitiveFields;
	}

	@Override
	public XGettingEnum<Field> instanceReferenceFields()
	{
		return this.referenceFields;
	}
	
	@Override
	public final boolean isPrimitiveType()
	{
		return false;
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
	{
		// with the exception of some special types (primitive definition and enums), there are only instance members.
		return this.instanceMembers();
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMemberFieldReflective> instanceMembers()
	{
		return this.storingMembers();
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMemberFieldReflective> storingMembers()
	{
		return this.storingMembers;
	}

	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMemberFieldReflective> settingMembers()
	{
		return this.settingMembers;
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> membersInDeclaredOrder()
	{
		return this.membersInDeclaredOrder;
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return !this.referenceMembers.isEmpty();
	}
	
	@Override
	public final long membersPersistedLengthMinimum()
	{
		return this.binaryContentLength;
	}
	
	@Override
	public final long membersPersistedLengthMaximum()
	{
		return this.binaryContentLength;
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
	
	
	///////////////////////////////////////////////////////////////////////////
	// actual operating logic //
	///////////////////////////

	@Override
	public void store(
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeFixedSize(
			handler                  ,
			this.binaryContentLength ,
			this.typeId()            ,
			objectId                 ,
			instance                 ,
			this.storingMemoryOffsets,
			this.storers
		);
	}

	@Override
	public abstract T create(final Binary data, PersistenceLoadHandler handler);

	@Override
	public void updateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		/*
		 * Explicit type check to avoid memory getting overwritten with bytes not fitting to the actual type.
		 * This can be especially critical if a custom root resolver returns an instance that does not match
		 * the type defined by the typeId.
		 */
		if(!this.type().isInstance(instance))
		{
			throw new TypeCastException(this.type(), instance);
		}

		data.updateFixedSize(instance, this.setters, this.settingMemoryOffsets, handler);
		
		this.setPersister(instance, handler);
	}
	
	private void setPersister(final T instance, final PersistenceLoadHandler handler)
	{
		if(this.persisterFields == null)
		{
			return;
		}
		
		final Persister persister = handler.getPersister();
		
		for(final Field field : this.persisterFields)
		{
			// field type must be compatible with the specific persister's class.
			if(!field.getType().isAssignableFrom(persister.getClass()))
			{
				continue;
			}
			
			// (11.02.2020 TM)NOTE: isn't this check a consistency danger? What if it needs to be updated?
			final Object existingPersister = XReflect.getFieldValue(field, instance);
			if(existingPersister == null)
			{
				XReflect.setFieldValue(field, instance, persister);
			}
		}
	}

	@Override
	public final void complete(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		// no-op for normal implementation (see non-reference-hashing collections for other examples)
	}

	@Override
	public void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		BinaryPersistence.iterateInstanceReferences(iterator, instance, this.refrnceMemoryOffsets);
	}

	@Override
	public void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		// "bytes" points to the entity content address, the offsets are relative to the content address.
		data.iterateReferenceRange(
			this.refBinaryOffsetStart,
			this.refBinaryOffsetBound,
			iterator
		);
	}
	
	@Override
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		for(final Field field : this.instanceFields())
		{
			logic.accept(field.getType());
		}
		
		return logic;
	}

}
