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

import static org.eclipse.serializer.util.X.mayNull;
import static org.eclipse.serializer.util.X.notNull;

import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.collections.types.XImmutableEnum;
import org.eclipse.serializer.util.X;

/**
 * A {@link PersistenceTypeDescription} additionally bound to a runtime {@link Class}.
 * <p>
 * While a description captures only the textual type name and the structural member sequence, a definition
 * fills in two further pieces of information:
 * <ul>
 * <li>the resolved runtime {@link #type()} (possibly {@code null} for types explicitly mapped as deleted),</li>
 * <li>the {@link #runtimeTypeName()}, which can differ from the dictionary's stable {@link #typeName()}
 *     after a class rename has been recorded in the {@link PersistenceRefactoringMapping}.</li>
 * </ul>
 * <p>
 * <b>Stable vs. runtime name.</b> {@link #typeName()} is fixed for a given {@link #typeId()} for the
 * lifetime of the dictionary &mdash; renaming a class on disk would invalidate references in older
 * persistent data, so the dictionary records the name at type-id assignment time. {@link #runtimeTypeName()}
 * tracks the current Java class name the data should be re-bound to. {@link #toRuntimeTypeIdentifier()}
 * formats {@link PersistenceTypeIdentity} using the runtime name (with a placeholder if the type was
 * deleted), which is what diagnostic output and {@link Default#toString()} use.
 * <p>
 * <b>Length and shape predicates.</b> Beyond {@link PersistenceTypeDescription}, a definition also exposes
 * cached predicates that summarize the member sequence: {@link #hasPersistedReferences()},
 * {@link #isPrimitiveType()}, {@link #hasPersistedVariableLength()},
 * {@link #hasVaryingPersistedLengthInstances()}, plus the cumulative
 * {@link #membersPersistedLengthMinimum()} / {@link #membersPersistedLengthMaximum()} bounds. These are
 * computed once at construction and looked up cheaply afterwards.
 * <p>
 * <b>Member collection contract.</b> {@link #allMembers()} and {@link #instanceMembers()} both return an
 * {@link XGettingEnum} (i.e. ordered set) using
 * {@link PersistenceTypeDescriptionMember#identityHashEqualator()} for equality &mdash; this is what
 * {@link #New} verifies before constructing the {@link Default} instance.
 *
 * @see PersistenceTypeDescription
 * @see PersistenceTypeLink
 * @see PersistenceTypeDefinitionMember
 */
public interface PersistenceTypeDefinition extends PersistenceTypeDescription, PersistenceTypeLink
{
	/**
	 * The biuniquely associated id value identifying a type description.
	 */
	@Override
	public long typeId();

	/**
	 * The name of the type as defined in the type dictionary. This name may never change for a given typeId,
	 * even if the runtime {@link #runtimeTypeName()} did to reflect a design-level type renaming.
	 */
	@Override
	public String typeName();

	/**
	 * The runtime {@link Class} this definition is bound to, or {@code null} if no runtime counterpart
	 * exists &mdash; e.g. for types explicitly mapped as deleted in the
	 * {@link PersistenceRefactoringMapping}, or for legacy dictionary entries whose class can no longer
	 * be loaded.
	 *
	 * @return the runtime class, or {@code null} if unbound.
	 */
	@Override
	public Class<?> type();

	/**
	 * The name of the corresponding runtime type.
	 * If not implemented otherwise (e.g. to cache the name), this method simply calls {@link Class#getName()} of
	 * a non-null {@link #type()} reference.
	 * @return the name of the corresponding runtime type
	 */
	public default String runtimeTypeName()
	{
		return this.type() == null
			? null
			: this.type().getName()
		;
	}

	/* (30.06.2015 TM)TODO: PersistenceTypeDescription <?>Generics
	 * Must consider Generics Type information as well, at least as a simple normalized String for
	 * equality comparison.
	 * Otherwise, changing type parameter won't be recognized by the type validation and
	 * loading/building of entities will result in heap pollution (wrong instance for the type).
	 * Example:
	 * Lazy<Person> changed to Lazy<Employee>.
	 * Currently, this is just recognized as Lazy.
	 * 
	 * (05.04.2017 TM)NOTE: but does it really have to be stored here?
	 * Wouldn't it be enough to store it in the member description?
	 * E.g. Type "Lazy" PLUS type parameter "[full qualified] Person"
	 */
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers();

	/**
	 * Enum (unique elements with order), using {@link PersistenceTypeDescriptionMember#identityHashEqualator()}.
	 * Contains all persistent members (similar, but not identical to fields) in persistent order, which can
	 * differ from the declaration order.
	 *
	 */
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers();

	/**
	 * @return {@code true} if any of this definition's instance members holds a reference (object pointer)
	 *         in its persistent form. Pre-computed at construction.
	 */
	public boolean hasPersistedReferences();

	/**
	 * The minimum total persistent length of an instance of this type, summed across all instance
	 * members' {@link PersistenceTypeDescriptionMember#persistentMinimumLength()}.
	 *
	 * @return the lower bound of an instance's persistent length.
	 */
	public long membersPersistedLengthMinimum();

	/**
	 * The maximum total persistent length of an instance of this type, summed across all instance
	 * members' {@link PersistenceTypeDescriptionMember#persistentMaximumLength()}.
	 *
	 * @return the upper bound of an instance's persistent length.
	 */
	public long membersPersistedLengthMaximum();
	
	/**
	 * Provides information if two instances of the handled type can have different length in persisted form.<p>
	 * Examples for variable length types:
	 * <ul>
	 * <li> arrays</li>
	 * <li>{@code java.lang.String}</li>
	 * <li>{@code java.util.ArrayList}</li>
	 * <li>{@code java.math.BigDecimal}</li>
	 * </ul><p>
	 * Examples for fixed length types:
	 * <ul>
	 * <li>primitive value wrapper types</li>
	 * <li>{@code java.lang.Object}</li>
	 * <li>{@code java.util.Date}</li>
	 * <li>typical entity types (without unshared inlined variable length component instances)</li>
	 * </ul>
	 * 
	 * @return if two instances of the handled type can have different length in persisted form
	 */
	public default boolean hasPersistedVariableLength()
	{
		return this.membersPersistedLengthMinimum() != this.membersPersistedLengthMaximum();
	}

	/**
	 * @return {@code true} if this definition describes a Java primitive type, i.e. its member sequence
	 *         consists of exactly one
	 *         {@linkplain PersistenceTypeDescriptionMember#isPrimitiveDefinition() primitive bit-layout entry}.
	 */
	public boolean isPrimitiveType();

	/**
	 * Provides information if one particular instance can have variing binary length from one store to another.<p>
	 * Examples for variable length instances:
	 * <ul>
	 * <li> variable size collection instances</li>
	 * <li> variable size pseudo collection instances like {@code java.util.StringBuilder}</li>
	 * <li> instances of custom defined types similar to collections</li>
	 * </ul><p>
	 * Examples for fixed length instances:
	 * <ul>
	 * <li>arrays</li>
	 * <li>all immutable type instances (like {@code java.lang.String} )</li>
	 * <li>all fixed length types (see {@link #hasVaryingPersistedLengthInstances()}</li>
	 * </ul>
	 *
	 * @return if one particular instance can have varying binary length from one store to another
	 */
	public boolean hasVaryingPersistedLengthInstances();

	/**
	 * Returns this definition's identifier in textual form, using {@link #runtimeTypeName()} rather than
	 * {@link #typeName()}. Falls back to the placeholder {@code "[no runtime type]"} if the type is
	 * unbound. Used for diagnostic output (notably {@link Default#toString()}).
	 *
	 * @return the runtime-form textual type identifier.
	 */
	public default String toRuntimeTypeIdentifier()
	{
		return PersistenceTypeDescription.buildTypeIdentifier(
			this.typeId(),
			X.coalesce(this.runtimeTypeName(), "[no runtime type]")
		);
	}



	/**
	 * @param members the members to inspect; may be {@code null}.
	 *
	 * @return {@code true} if any member reports {@link PersistenceTypeDescriptionMember#isVariableLength()}.
	 *         Returns {@code false} for {@code null} input.
	 */
	public static boolean determineVariableLength(
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
	)
	{
		if(members == null)
		{
			return false;
		}
		
		for(final PersistenceTypeDescriptionMember member : members)
		{
			if(member.isVariableLength())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Determines whether the passed member sequence describes a primitive type, i.e. consists of exactly
	 * one {@linkplain PersistenceTypeDescriptionMember#isPrimitiveDefinition() primitive bit-layout entry}.
	 *
	 * @param allMembers the members to inspect.
	 *
	 * @return {@code true} if {@code allMembers} describes a primitive type.
	 */
	public static boolean determineIsPrimitive(
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> allMembers
	)
	{
		return allMembers.size() == 1 && allMembers.get().isPrimitiveDefinition();
	}


	/**
	 * Creates a new {@link PersistenceTypeDefinition}. The passed enums must use
	 * {@link PersistenceTypeDescriptionMember#identityHashEqualator()} as their equality &mdash;
	 * an {@link IllegalArgumentException} is thrown otherwise.
	 *
	 * @param typeId          the type id.
	 * @param typeName        the dictionary-stable type name; must not be {@code null}.
	 * @param runtimeTypeName the current runtime type name; may be {@code null} if no runtime counterpart.
	 * @param type            the runtime class; may be {@code null} if explicitly mapped as deleted.
	 * @param allMembers      the full member sequence (instance + non-instance) in dictionary order.
	 * @param instanceMembers the subset of {@code allMembers} that contributes to a persisted instance.
	 *
	 * @return a new {@link PersistenceTypeDefinition}.
	 *
	 * @throws IllegalArgumentException if either passed enum does not use
	 *                                  {@link PersistenceTypeDescriptionMember#identityHashEqualator()}.
	 */
	public static PersistenceTypeDefinition New(
		final long                                                    typeId         ,
		final String                                                  typeName       ,
		final String                                                  runtimeTypeName,
		final Class<?>                                                type           ,
		final XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers     ,
		final XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers
	)
	{
		// as defined by interface contract.
		if(allMembers.equality() != PersistenceTypeDescriptionMember.identityHashEqualator()
		|| instanceMembers.equality() != PersistenceTypeDescriptionMember.identityHashEqualator()
		)
		{
			throw new IllegalArgumentException();
		}
		
		// no-op for already immutable collection type (e.g. PersistenceTypeDescriptionMember#validateAndImmure)
		// type may be null for the sole case of an explicitly mapped to be deleted type.
		final XImmutableEnum<? extends PersistenceTypeDefinitionMember> immutAllMembers = allMembers.immure();
		final XImmutableEnum<? extends PersistenceTypeDefinitionMember> immutInsMembers = instanceMembers.immure();
		return new PersistenceTypeDefinition.Default(
			                                                         typeId          ,
			                                                 notNull(typeName)       ,
			                                                 mayNull(runtimeTypeName),
			                                                 mayNull(type)           ,
			                                                         immutAllMembers ,
			                                                         immutInsMembers ,
			PersistenceTypeDescriptionMember.determineHasReferences (immutInsMembers),
			PersistenceTypeDefinition       .determineIsPrimitive   (immutAllMembers),
			PersistenceTypeDefinition       .determineVariableLength(immutInsMembers)
		);
	}

	

	/**
	 * Default {@link PersistenceTypeDefinition} implementation. Stores all attributes immutably; the
	 * cumulative member length bounds are computed once at construction by summing the per-member
	 * minimum / maximum lengths.
	 */
	public final class Default implements PersistenceTypeDefinition
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final long                                                      typeId          ;
		final String                                                    typeName        ;
		final String                                                    runtimeTypeName ;
		final Class<?>                                                  runtimeType     ;
		final XImmutableEnum<? extends PersistenceTypeDefinitionMember> allMembers      ;
		final XImmutableEnum<? extends PersistenceTypeDefinitionMember> instanceMembers ;
		final long                                                      membersMinLength;
		final long                                                      membersMaxLength;
		final boolean                                                   hasReferences   ;
		final boolean                                                   isPrimitive     ;
		final boolean                                                   variableLength  ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final long                                                      typeId         ,
			final String                                                    typeName       ,
			final String                                                    runtimeTypeName,
			final Class<?>                                                  runtimeType    ,
			final XImmutableEnum<? extends PersistenceTypeDefinitionMember> allMembers     ,
			final XImmutableEnum<? extends PersistenceTypeDefinitionMember> instanceMembers,
			final boolean                                                   hasReferences  ,
			final boolean                                                   isPrimitive    ,
			final boolean                                                   variableLength
		)
		{
			super();
			this.typeId           = typeId         ;
			this.typeName         = typeName       ;
			this.runtimeTypeName  = runtimeTypeName;
			this.runtimeType      = runtimeType    ;
			this.allMembers       = allMembers     ;
			this.instanceMembers  = instanceMembers;
			this.hasReferences    = hasReferences  ;
			this.isPrimitive      = isPrimitive    ;
			this.variableLength   = variableLength ;
			this.membersMinLength = PersistenceTypeDescriptionMember.calculatePersistentMinimumLength(0, instanceMembers);
			this.membersMaxLength = PersistenceTypeDescriptionMember.calculatePersistentMaximumLength(0, instanceMembers);
		}

		

		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

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
		
		@Override
		public final String runtimeTypeName()
		{
			return this.runtimeTypeName;
		}
		
		@Override
		public final Class<?> type()
		{
			return this.runtimeType;
		}
		
		@Override
		public final XImmutableEnum<? extends PersistenceTypeDefinitionMember> allMembers()
		{
			return this.allMembers;
		}
		
		@Override
		public final XImmutableEnum<? extends PersistenceTypeDefinitionMember> instanceMembers()
		{
			return this.instanceMembers;
		}

		@Override
		public final boolean hasPersistedReferences()
		{
			return this.hasReferences;
		}

		@Override
		public final boolean isPrimitiveType()
		{
			return this.isPrimitive;
		}

		@Override
		public final boolean hasPersistedVariableLength()
		{
			return this.variableLength;
		}
		
		@Override
		public final boolean hasVaryingPersistedLengthInstances()
		{
			return this.variableLength;
		}
		
		@Override
		public final String toString()
		{
			return this.toRuntimeTypeIdentifier();
		}

		@Override
		public final long membersPersistedLengthMinimum()
		{
			return this.membersMinLength;
		}

		@Override
		public final long membersPersistedLengthMaximum()
		{
			return this.membersMaxLength;
		}
		

	}
	
}
