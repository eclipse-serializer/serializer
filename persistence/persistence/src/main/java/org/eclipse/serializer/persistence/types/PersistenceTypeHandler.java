package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
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

import static org.eclipse.serializer.util.X.notNull;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import org.eclipse.serializer.util.X;
import org.eclipse.serializer.collections.EqHashEnum;
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.collections.types.XImmutableEnum;
import org.eclipse.serializer.collections.types.XImmutableSequence;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTypeConsistency;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import org.eclipse.serializer.reference.Swizzling;
import org.eclipse.serializer.reflect.XReflect;

/**
 * The unit of serialization logic for a single type {@code T}: a {@link PersistenceTypeDefinition} plus
 * the actual code that translates instances of {@code T} to and from a persistent representation of the
 * data type {@code D} (typically a binary medium).
 * <p>
 * <b>Lifecycle.</b> A type handler is constructed without a typeId, then bound to one via
 * {@link #initialize(long)} when first registered with a dictionary. After initialization the handler is
 * effectively immutable. Subclasses can override {@link Abstract#internalInitialize()} to run one-shot
 * post-constructor setup (e.g. computing per-class binary offsets) once the typeId is known.
 * <p>
 * <b>Storing path.</b> {@link #store(Object, Object, long, PersistenceStoreHandler)} writes an instance's
 * persistent form into the data target {@code D}. {@link #iterateInstanceReferences(Object,
 * PersistenceFunction)} walks the instance's outgoing references so the persister can recursively visit
 * them.
 * <p>
 * <b>Loading path.</b> Loading proceeds in three phases per instance to allow cyclic graphs to be
 * reconstructed safely:
 * <ol>
 * <li>{@link #create(Object, PersistenceLoadHandler)} allocates an empty {@code T} from {@code data}.</li>
 * <li>{@link #initializeState(Object, Object, PersistenceLoadHandler)} (default: same as
 *     {@code updateState}) fills the freshly created instance.</li>
 * <li>{@link #updateState(Object, Object, PersistenceLoadHandler)} re-applies the persisted state to an
 *     already-created instance (used both during initialization and when refreshing an existing
 *     instance from updated data).</li>
 * <li>{@link #complete(Object, Object, PersistenceLoadHandler)} runs once all referenced instances have
 *     been built &mdash; e.g. for hash-based collections that can only hash their elements after the
 *     element instances themselves have been fully built.</li>
 * </ol>
 * <p>
 * <b>Per-instance no-ops.</b> Several methods are documented as "implementing as a no-op makes the
 * instance effectively skipped/shallow for X". A handler that wants to behave like a transient field
 * for storing, for example, can override {@link #store} to do nothing.
 * <p>
 * <b>Two flavors.</b> {@link PersistenceTypeHandlerCustom} signals "I use arbitrary custom logic to
 * map instances to and from their persistent form"; {@link PersistenceTypeHandlerGeneric} signals "I
 * solely operate on declared {@link Field}s of the handled class". The {@link Abstract} base class
 * captures the common bookkeeping (type, typeName, typeId, validation helpers).
 *
 * @param <D> the data target type (e.g. binary buffer).
 * @param <T> the handled Java type.
 *
 * @see PersistenceTypeDefinition
 * @see PersistenceTypeHandlerCustom
 * @see PersistenceTypeHandlerGeneric
 * @see PersistenceTypeHandlerReflective
 */
public interface PersistenceTypeHandler<D, T> extends PersistenceTypeDefinition, PersistenceDataTypeHolder<D>
{
	@Override
	public Class<D> dataType();

	@Override
	public Class<T> type();

	/**
	 * Tests whether the passed entity type is valid input for this handler. Default implementation
	 * accepts {@code type} when it is assignable to {@link #type()} &mdash; i.e. a sub-type of the
	 * handled type. Sub-type acceptance (rather than identity) is required because some classes must
	 * be handled as their super-type, e.g. local platform-specific implementations of
	 * {@link java.nio.file.Path}.
	 *
	 * @param type the entity type to validate.
	 *
	 * @return {@code true} if {@code type} is acceptable.
	 */
	public default boolean isValidEntityType(final Class<? extends T> type)
	{
		/*
		 * Note that type() is validated to never be null prior to type handler instance creation.
		 * Must be super type check instead of simple identity check as some classes must be handleable
		 * as their super types (e.g. local implementation of java.nio.file.Path)
		 */
		return this.type().isAssignableFrom(type);
	}

	/**
	 * Like {@link #isValidEntityType(Class)} but throws {@link PersistenceExceptionTypeConsistency} for
	 * unacceptable types.
	 *
	 * @param type the entity type to validate.
	 *
	 * @throws PersistenceExceptionTypeConsistency if the type is not handleable by this handler.
	 */
	public default void validateEntityType(final Class<? extends T> type)
	{
		if(this.isValidEntityType(type))
		{
			return;
		}

		throw new PersistenceExceptionTypeConsistency(
			"Invalid entity type "+ type  +" for type handler " + this.toTypeIdentifier()
		);
	}

	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers();

	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers();

	/**
	 * Iterates every outgoing reference held by {@code instance} and forwards each to {@code iterator}.
	 * Used by the persister to traverse the object graph during storing. Implementing this as a no-op
	 * makes the handler effectively <i>shallow</i>: the instance is persisted but its references are
	 * not recursed into.
	 *
	 * @param instance the source instance.
	 * @param iterator receives each outgoing reference.
	 */
	public void iterateInstanceReferences(T instance, PersistenceFunction iterator);

	/**
	 * Iterates every reference embedded in the passed persisted {@code data} and forwards it to
	 * {@code iterator}. Used during loading to discover the objectIds of all instances that need to be
	 * loaded transitively before the owning instance can be completed.
	 *
	 * @param data     the persisted form of an instance of the handled type.
	 * @param iterator receives each outgoing reference's objectId.
	 */
	public void iterateLoadableReferences(D data, PersistenceReferenceLoader iterator);

	/**
	 * Writes the persistent form of {@code instance} (under the given {@code objectId}) into the data
	 * target via {@code handler}. Implementing this as a no-op makes the handler effectively skip the
	 * instance for storing.
	 *
	 * @param instance the instance to persist.
	 * @param objectId the objectId already associated with {@code instance}.
	 * @param data     the data target.
	 * @param handler  the persister-provided callback for writing nested references and primitive values.
	 */
	public void store(D data, T instance, long objectId, PersistenceStoreHandler<D> handler);

	/**
	 * Allocates and returns a fresh, uninitialized {@code T} from the passed persisted form. The
	 * returned instance must already be of the right runtime type but does not yet need its fields
	 * populated &mdash; field population happens in {@link #initializeState} or {@link #updateState}.
	 *
	 * @param data    the persisted form to read identity-information from.
	 * @param handler receives nested-reference resolution requests.
	 *
	 * @return a freshly allocated instance.
	 */
	public T create(D data, PersistenceLoadHandler handler);

	/**
	 * Populates a freshly {@linkplain #create created} instance from its persisted form. The default
	 * implementation simply delegates to {@link #updateState(Object, Object, PersistenceLoadHandler)};
	 * value-type-style handlers may override to distinguish initial population from later refreshes.
	 * Implementing this as a no-op effectively makes the handler skip initial loading of an instance.
	 *
	 * @param data     the persisted form.
	 * @param instance the freshly created instance.
	 * @param handler  the load-side callback.
	 */
	public default void initializeState(final D data, final T instance, final PersistenceLoadHandler handler)
	{
		// for non-value-types, initialize is the same as update. Value-types
		this.updateState(data, instance, handler);
	}

	/**
	 * Re-applies the persisted state to an existing instance &mdash; called both as the workhorse of
	 * {@link #initializeState} and when an already-loaded instance must be refreshed from updated
	 * persisted data. Implementing this as a no-op makes the handler effectively skip loading.
	 *
	 * @param data     the persisted form.
	 * @param instance the target instance.
	 * @param handler  the load-side callback.
	 */
	public void updateState(D data, T instance, PersistenceLoadHandler handler);

	/**
	 * Completes an initially built instance after all loaded instances have been built.
	 * E.g. can be used to cause a hash collection to hash all its initially collected entries after their
	 * instances have been built.
	 *
	 * @param data the data target
	 * @param instance the source instance
	 * @param handler the appropriate handler
	 */
	public void complete(D data, T instance, PersistenceLoadHandler handler);

	/* (06.10.2012 TM)XXX: PersistenceDomainTypeHandler<D,T> ?
	 * to bind a generic TypeHandler to a specific registry inside a Domain
	 * specific registry could replace the oidResolver parameter.
	 * But only in an additional overloaded method.
	 * And what about the existing one that still gets called? What if it gets passed another oidresolver?
	 * Maybe solve by a PersistenceDomain-specific Builder? Wouldn't even have to have a new interface, just a class
	 */
	
	/**
	 * Binds this handler to a typeId. Called once when the handler is first registered with a
	 * {@link PersistenceTypeDictionary}. Re-initializing with the same typeId is a tolerated no-op
	 * (relevant for hardcoded typeIds, e.g. for primitive-array handlers); re-initializing with a
	 * conflicting typeId throws.
	 * <p>
	 * Returns the post-initialization handler instance &mdash; usually {@code this}, since most
	 * implementations are immutable, but the interface allows returning a different instance for
	 * implementations that swap themselves out at initialization time.
	 *
	 * @param typeId the typeId to assign.
	 *
	 * @return the initialized handler.
	 *
	 * @throws PersistenceException if the handler is already initialized with a different typeId.
	 */
	public PersistenceTypeHandler<D, T> initialize(long typeId);

	/**
	 * Iterates the types of persistent members (e.g. non-transient {@link Field}s).
	 * The same type may occur more than once.
	 * The order in which the types are provided is undefined, i.e. depending on the implementation.
	 *
	 * @param <C> the logic type
	 * @param logic the iteration logic
	 * @return the given logic
	 */
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(C logic);
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// default methods //
	////////////////////

	/*!*\
	 * All default methods must be implemented in
	 * PersistenceLegacyTypeHandler$Wrapper and AbstractBinaryLegacyTypeHandlerTranslating
	 * to prevent bugs!
	\*!*/
	
	/**
	 * The members in their original declaration order, as opposed to the persisted order returned by
	 * {@link #allMembers()}. By default the two coincide; reflective handlers may reorder fields in
	 * persistent form (e.g. references first, then primitives) and use this method to recover the
	 * original ordering when needed.
	 *
	 * @return the members in declaration order.
	 */
	public default XGettingEnum<? extends PersistenceTypeDefinitionMember> membersInDeclaredOrder()
	{
		// by default, there is no difference between members (in persisted order) and members in declared order.
		return this.allMembers();
	}

	/**
	 * The subset of members that contributes to the persistent form during storing. Default
	 * implementation: alias for {@link #instanceMembers()} &mdash; all instance members are stored.
	 *
	 * @return the members written during storing.
	 */
	public default XGettingEnum<? extends PersistenceTypeDescriptionMember> storingMembers()
	{
		// "storingMembers" is just an alias for instanceMembers since all instance members get stored.
		return this.instanceMembers();
	}

	/**
	 * The subset of members whose values may be overwritten on an existing instance during state
	 * updates. Same as {@link #storingMembers()} except for {@link java.lang.Enum} handlers, where
	 * {@code name} and {@code ordinal} are never overwritten on a constant once initialized.
	 *
	 * @return the members written during state updates.
	 */
	public default XGettingEnum<? extends PersistenceTypeDescriptionMember> settingMembers()
	{
		// same as storingMembers except for java.lang.Enum (where name and ordinal may never be overwritten)
		return this.storingMembers();
	}
	
	/**
	 * Guarantees that the {@link PersistenceTypeHandler} implementation is actually viably usable to handle instances.
	 * That is the natural purpose of type handlers, but there are exceptions, like type handlers created for
	 * abstract types or unpersistable types just to have a metadata representation that links a type and a type id.
	 * <p>
	 * See occurances of {@link PersistenceExceptionTypeNotPersistable}.
	 * 
	 * @throws PersistenceExceptionTypeNotPersistable if the handler's type is not persistable
	 * 
	 * @see PersistenceExceptionTypeNotPersistable
	 */
	public default void guaranteeSpecificInstanceViablity() throws PersistenceExceptionTypeNotPersistable
	{
		/*
		 * no-op by default, meaning the handler is viable to be used with instances
		 * not checking #isSpecificInstanceViable is intentional because this method gets called for every
		 * encountered instance and therefore should not execute any logic if no exception is thrown.
		 */
	}
	
	/**
	 * @return {@code true} unless this handler is one of the special-cased "unpersistable" / "abstract"
	 *         handlers, in which case calling {@link #guaranteeSpecificInstanceViablity} would throw.
	 */
	public default boolean isSpecificInstanceViable()
	{
		// true for virtually all handlers except a special-cased "unpersistable" and "abstract" handler.
		return true;
	}

	/**
	 * Guarantees that the handler is viable for sub-type instances (i.e. instances that pass
	 * {@link #isValidEntityType(Class)} but are not of the exact handled type). No-op by default.
	 * Overridden by special-cased handlers that accept their own type but reject sub-types.
	 *
	 * @throws PersistenceExceptionTypeNotPersistable if sub-type instances are not viable.
	 */
	public default void guaranteeSubTypeInstanceViablity() throws PersistenceExceptionTypeNotPersistable
	{
		/*
		 * no-op by default, meaning the handler is viable to be used with instances
		 * not checking #isSubTypeInstanceViable is intentional because this method gets called for every
		 * encountered instance and therefore should not execute any logic if no exception is thrown.
		 */
	}

	/**
	 * @return {@code true} unless this handler rejects sub-type instances.
	 */
	public default boolean isSubTypeInstanceViable()
	{
		// true for virtually all handlers except a special-cased "unpersistable" handler.
		return true;
	}

	// (27.08.2019 TM)TODO: "~Enum~" methods actually belong in a "PersistenceTypeHandlerEnum" subtype. Maybe refactor.

	/**
	 * Returns the enum-constant array for enum handlers. Throws {@link UnsupportedOperationException}
	 * for non-enum handlers.
	 *
	 * @return the constants of the handled enum.
	 *
	 * @throws UnsupportedOperationException if this is not an enum handler.
	 */
	public default Object[] collectEnumConstants()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Reads the persisted enum ordinal from {@code data}. Throws for non-enum handlers.
	 *
	 * @param data the persisted form.
	 *
	 * @return the persisted ordinal.
	 *
	 * @throws UnsupportedOperationException if this is not an enum handler.
	 */
	public default int getPersistedEnumOrdinal(final D data)
	{
		throw new UnsupportedOperationException();
	}

	/*!*\
	 * All default methods must be implemented in
	 * PersistenceLegacyTypeHandler$Wrapper and AbstractBinaryLegacyTypeHandlerTranslating
	 * to prevent bugs!
	\*!*/
	
	
	/**
	 * Resolves the enum constant of {@code type} at the given {@code ordinal}, dealing with the
	 * subclass-typing hassle that anonymous-inner-class-style enum constants cause: the runtime
	 * instance is actually of type {@code T}, but it is stored in a {@code ? super T} array on its
	 * parent enum class, so a raw cast is needed.
	 *
	 * @param <T>     the enum type.
	 * @param <M>     unused; legacy type parameter.
	 * @param type    the enum class.
	 * @param ordinal the constant's ordinal.
	 *
	 * @return the resolved constant.
	 */
	public static <T, M> T resolveEnumConstant(final Class<T> type, final int ordinal)
	{
		/*
		 * Required for AIC-like special subclass enums constants:
		 * The instance is actually of type T, but it is stored in a "? super T" array of its parent enum type.
		 */
		final Object enumConstantInstance = XReflect.resolveEnumConstantInstance(type, ordinal);
		
		// compensate the subclass typing hassle
		@SuppressWarnings("unchecked")
		final T enumConstantinstance = (T)enumConstantInstance;
		
		return enumConstantinstance;
	}
	
	
	/**
	 * Abstract base class for {@link PersistenceTypeHandler} implementations. Stores the bound runtime
	 * {@link Class}, the (possibly synthetic-aware) {@code typeName}, and the post-init {@code typeId},
	 * and provides static helpers for member validation, declared-field lookup, and type-name derivation.
	 *
	 * @param <D> the data target type.
	 * @param <T> the handled Java type.
	 */
	public abstract class Abstract<D, T> implements PersistenceTypeHandler<D, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		/**
		 * Validates that the passed members have unique identifiers and returns an immutable enum view
		 * of them, keyed by {@link PersistenceTypeDescriptionMember#identityHashEqualator()}. Returns
		 * {@code null} for {@code null} input to allow delayed on-demand member initialization.
		 *
		 * @param <D>     the member type.
		 * @param members the members to validate.
		 *
		 * @return an immutable enum view, or {@code null} if {@code members} is {@code null}.
		 *
		 * @throws PersistenceExceptionTypeConsistency if duplicate member identifiers are detected.
		 */
		public static <D extends PersistenceTypeDefinitionMember> XImmutableEnum<D> validateAndImmure(
			final XGettingSequence<D> members
		)
		{
			if(members == null)
			{
				// members may be null to allow delayed on-demand BinaryField initialization.
				return null;
			}
			
			// note that this is descriptionMember-identity, meaning #identifier
			final EqHashEnum<D> validatedMembers = EqHashEnum.New(
				PersistenceTypeDescriptionMember.identityHashEqualator()
			);
			validatedMembers.addAll(members);
			if(validatedMembers.size() != members.size())
			{
				throw new PersistenceExceptionTypeConsistency("Duplicate member descriptions.");
			}
			
			return validatedMembers.immure();
		}
		
		public static final PersistenceTypeDefinitionMemberFieldReflective declaredField(
			final Field                          field         ,
			final PersistenceFieldLengthResolver lengthResolver
		)
		{
			return PersistenceTypeDefinitionMemberFieldReflective.New(
				field                                              ,
				lengthResolver.resolveMinimumLengthFromField(field),
				lengthResolver.resolveMaximumLengthFromField(field)
			);
		}

		public static final XImmutableSequence<PersistenceTypeDescriptionMemberFieldReflective> declaredFields(
			final PersistenceTypeDescriptionMemberFieldReflective... declaredFields
		)
		{
			return X.ConstList(declaredFields);
		}

		protected static final String deriveTypeName(final Class<?> type)
		{
			// to centralized logic accross child classes
			return Persistence.derivePersistentTypeName(type);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// basic type swizzling //
		private final Class<T> type;
		
		// differs from Class#getName to properly identify synthetic classes instead using of those "$1,2,3..." names.
		private final String typeName;
		
		// effectively final / immutable: gets only initialized once later on and is never mutated again. initially 0.
		private long typeId = Swizzling.notFoundId();


		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Abstract(final Class<T> type)
		{
			this(type, type.getName());
		}

		protected Abstract(final Class<T> type, final String typeName)
		{
			super();
			this.type     = notNull(type)    ;
			this.typeName = notNull(typeName);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		protected final void validateInstance(final T instance)
		{
			if(this.type.isInstance(instance))
			{
				return;
			}
			throw new PersistenceExceptionTypeConsistency();
		}

		@Override
		public final Class<T> type()
		{
			return this.type;
		}

		@Override
		public final long typeId()
		{
			return this.typeId;
		}

		@Override
		public final String typeName()
		{
			return this.typeName;
		}
		
		protected void internalInitialize()
		{
			/*
			 * Empty by itself, this method is required to have a convenient entry point for executing
			 * logic in sub classes only once for initialization, but after the constructor chain has been completed.
			 * For example:
			 * Collecting BinaryField instances accross the class hiararchy and initializing their binary offsets
			 * afterwards. Trying to do that in the constructors directly would cause some fields to be null.
			 */
		}
		
		@Override
		public synchronized PersistenceTypeHandler<D, T> initialize(final long typeId)
		{
			/* note:
			 * Type handlers can have hardcoded typeIds, e.g. for native types like primitive arrays.
			 * As long as the same typeId (originating from the dictionary file) is passed for initialization,
			 * everything is fine.
			 */
			if(Swizzling.isFoundId(this.typeId))
			{
				if(this.typeId == typeId)
				{
					// consistent no-op, abort
					return this;
				}
				
				throw new PersistenceException(
					"Specified type ID " + typeId + " conflicts with already initalized type ID " + this.typeId
				);
			}
			
			this.typeId = typeId;
			
			this.internalInitialize();
			
			// by default, implementations are assumed to be (effectively) immutable and thus can return themselves.
			return this;
		}
		
		@Override
		public final String toString()
		{
			return this.toRuntimeTypeIdentifier();
		}

	}

}
