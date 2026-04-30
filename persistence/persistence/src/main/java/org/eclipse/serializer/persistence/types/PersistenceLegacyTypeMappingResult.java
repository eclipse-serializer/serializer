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

import java.util.Iterator;

import org.eclipse.serializer.collections.EqHashEnum;
import org.eclipse.serializer.collections.XUtilsCollection;
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.collections.types.XGettingTable;
import org.eclipse.serializer.util.similarity.Similarity;


/**
 * The finalized result of mapping the members of a legacy {@link PersistenceTypeDefinition} onto the
 * members of a current {@link PersistenceTypeHandler}.
 * <p>
 * Carries four orthogonal pieces of information:
 * <ul>
 * <li>{@link #legacyToCurrentMembers()} &mdash; for each mapped legacy member, the current member it
 *     translates to (and the similarity of that mapping).</li>
 * <li>{@link #currentToLegacyMembers()} &mdash; the inverse for round-trip diagnostics.</li>
 * <li>{@link #discardedLegacyMembers()} &mdash; legacy members with no current counterpart (their
 *     persisted bytes are skipped on load).</li>
 * <li>{@link #newCurrentMembers()} &mdash; current members that have no legacy counterpart (default
 *     values are used on load).</li>
 * </ul>
 * <p>
 * Three convenience predicates let downstream code recognize special cases that allow simpler legacy
 * handlers: {@link #isUnchangedInstanceStructure}, {@link #isUnchangedFullStructure} and
 * {@link #isUnchangedStaticStructure}.
 *
 * @param <D> the data target type.
 * @param <T> the runtime type.
 */
public interface PersistenceLegacyTypeMappingResult<D, T>
{
	/**
	 * @return the legacy type definition. May describe a type whose runtime counterpart has been
	 *         renamed or removed.
	 */
	// the legacy type might potentially or usually be another type, maybe one that no more has a runtime type.
	public PersistenceTypeDefinition legacyTypeDefinition();

	/**
	 * @return the current handler the legacy data should be re-bound to.
	 */
	public PersistenceTypeHandler<D, T> currentTypeHandler();

	/**
	 * @return for every mapped legacy member, the current member it maps to plus the similarity score.
	 */
	public XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> legacyToCurrentMembers();

	/**
	 * @return the inverse of {@link #legacyToCurrentMembers()}.
	 */
	public XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> currentToLegacyMembers();

	/**
	 * @return legacy members with no current counterpart; their bytes are skipped on load.
	 */
	public XGettingEnum<PersistenceTypeDefinitionMember> discardedLegacyMembers();

	/**
	 * @return current members with no legacy counterpart; default values are used on load.
	 */
	public XGettingEnum<PersistenceTypeDefinitionMember> newCurrentMembers();



	/**
	 * Tests whether the mapping describes an unchanged <i>instance</i>-member structure: equal counts,
	 * order-aligned mappings, equal type names per pair. Allows callers to skip member-translating
	 * handlers and just wrap the current handler verbatim.
	 *
	 * @param mappingResult the mapping result.
	 *
	 * @return {@code true} if instance members are structurally unchanged.
	 */
	public static boolean isUnchangedInstanceStructure(
		final PersistenceLegacyTypeMappingResult<?, ?> mappingResult
	)
	{
		return isUnchangedStructure(
			mappingResult.legacyTypeDefinition().instanceMembers(),
			mappingResult.currentTypeHandler().instanceMembers(),
			mappingResult
		);
	}
	
	/**
	 * Like {@link #isUnchangedInstanceStructure} but compares all members (instance + non-instance).
	 *
	 * @param mappingResult the mapping result.
	 *
	 * @return {@code true} if all members are structurally unchanged.
	 */
	public static boolean isUnchangedFullStructure(
		final PersistenceLegacyTypeMappingResult<?, ?> mappingResult
	)
	{
		return isUnchangedStructure(
			mappingResult.legacyTypeDefinition().allMembers(),
			mappingResult.currentTypeHandler().allMembers(),
			mappingResult
		);
	}

	/**
	 * Tests whether only the <i>non-instance</i> members (e.g. enum constants) are structurally
	 * unchanged. Used by enum legacy-handler creation to decide whether ordinal remapping is needed.
	 *
	 * @param mappingResult the mapping result.
	 *
	 * @return {@code true} if non-instance members are structurally unchanged.
	 */
	public static boolean isUnchangedStaticStructure(
		final PersistenceLegacyTypeMappingResult<?, ?> mappingResult
	)
	{
		final EqHashEnum<PersistenceTypeDefinitionMember> legacyEnumMembers = XUtilsCollection.subtract(
			EqHashEnum.<PersistenceTypeDefinitionMember>New(mappingResult.legacyTypeDefinition().allMembers()),
			mappingResult.legacyTypeDefinition().instanceMembers()
		);
		
		final EqHashEnum<PersistenceTypeDefinitionMember> currentEnumMembers = XUtilsCollection.subtract(
			EqHashEnum.<PersistenceTypeDefinitionMember>New(mappingResult.currentTypeHandler().allMembers()),
			mappingResult.currentTypeHandler().instanceMembers()
		);
		
		return PersistenceLegacyTypeMappingResult.isUnchangedStructure(
			legacyEnumMembers,
			currentEnumMembers,
			mappingResult
		);
	}
	
	/**
	 * Generic variant: tests whether the two passed member sequences are structurally unchanged
	 * given the mapping in {@code mappingResult}. Same size, same order, each legacy member is
	 * mapped to the order-corresponding current member, and the type names match.
	 *
	 * @param legacyMembers  the legacy member sequence.
	 * @param currentMembers the current member sequence.
	 * @param mappingResult  the mapping result providing legacy-to-current lookups.
	 *
	 * @return {@code true} if the two sequences are structurally unchanged.
	 */
	public static boolean isUnchangedStructure(
		final XGettingEnum<? extends PersistenceTypeDefinitionMember> legacyMembers ,
		final XGettingEnum<? extends PersistenceTypeDefinitionMember> currentMembers,
		final PersistenceLegacyTypeMappingResult<?, ?>                mappingResult
	)
	{
		if(legacyMembers.size() != currentMembers.size())
		{
			// if there are differing members counts, the structure cannot be unchanged.
			return false;
		}

		final Iterator<? extends PersistenceTypeDefinitionMember> legacy  = legacyMembers.iterator();
		final Iterator<? extends PersistenceTypeDefinitionMember> current = currentMembers.iterator();
		
		final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> mapping =
			mappingResult.legacyToCurrentMembers()
		;
		
		// check as long as both collections have order-wise corresponding entries (ensured by size check above)
		while(legacy.hasNext())
		{
			final PersistenceTypeDefinitionMember legacyMember  = legacy.next() ;
			final PersistenceTypeDefinitionMember currentMember = current.next();

			// all legacy members must be directly mapped to their order-wise corresponding current member.
			final Similarity<PersistenceTypeDefinitionMember> match = mapping.get(legacyMember);
			if(match == null || match.targetElement() != currentMember)
			{
				return false;
			}

			// the persistent layout of the two members must match. equalsLayout handles every
			// member kind (regular fields, primitive definitions, enum constants, complex generic
			// fields) via polymorphic dispatch and intentionally ignores member names: the
			// mapping/order check above already establishes identity, so a renamed-but-otherwise-
			// unchanged member still qualifies for the unchanged-structure fast path.
			if(!legacyMember.equalsLayout(currentMember))
			{
				return false;
			}
		}
		
		// no need to check for remaining elements since size was checked above
		return true;
	}
	
	
	
	/**
	 * Creates a new {@link PersistenceLegacyTypeMappingResult}. All arguments are required.
	 *
	 * @param <D>                    the data target type.
	 * @param <T>                    the runtime type.
	 * @param legacyTypeDefinition   the legacy type definition.
	 * @param currentTypeHandler     the current handler the data should be re-bound to.
	 * @param legacyToCurrentMembers legacy-to-current member mapping with similarities.
	 * @param currentToLegacyMembers the inverse mapping.
	 * @param discardedLegacyMembers legacy members with no current counterpart.
	 * @param newCurrentMembers      current members with no legacy counterpart.
	 *
	 * @return a new mapping result.
	 */
	public static <D, T> PersistenceLegacyTypeMappingResult<D, T> New(
		final PersistenceTypeDefinition                                                                   legacyTypeDefinition  ,
		final PersistenceTypeHandler<D, T>                                                                currentTypeHandler    ,
		final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> legacyToCurrentMembers,
		final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> currentToLegacyMembers,
		final XGettingEnum<PersistenceTypeDefinitionMember>                                               discardedLegacyMembers,
		final XGettingEnum<PersistenceTypeDefinitionMember>                                               newCurrentMembers
	)
	{
		return new PersistenceLegacyTypeMappingResult.Default<>(
			notNull(legacyTypeDefinition)  ,
			notNull(currentTypeHandler)    ,
			notNull(legacyToCurrentMembers),
			notNull(currentToLegacyMembers),
			notNull(discardedLegacyMembers),
			notNull(newCurrentMembers)
		);
	}
	
	public final class Default<D, T> implements PersistenceLegacyTypeMappingResult<D, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceTypeDefinition                     legacyTypeDefinition  ;
		final PersistenceTypeHandler<D, T>                  currentTypeHandler    ;
		final XGettingEnum<PersistenceTypeDefinitionMember> discardedLegacyMembers;
		final XGettingEnum<PersistenceTypeDefinitionMember> newCurrentMembers     ;
		
		final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>>
			legacyToCurrentMembers,
			currentToLegacyMembers
		;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final PersistenceTypeDefinition                                                                   legacyTypeDefinition  ,
			final PersistenceTypeHandler<D, T>                                                                currentTypeHandler    ,
			final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> legacyToCurrentMembers,
			final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> currentToLegacyMembers,
			final XGettingEnum<PersistenceTypeDefinitionMember>                                               discardedLegacyMembers,
			final XGettingEnum<PersistenceTypeDefinitionMember>                                               newCurrentMembers
		)
		{
			super();
			this.legacyTypeDefinition   = legacyTypeDefinition  ;
			this.currentTypeHandler     = currentTypeHandler    ;
			this.legacyToCurrentMembers = legacyToCurrentMembers;
			this.currentToLegacyMembers = currentToLegacyMembers;
			this.discardedLegacyMembers = discardedLegacyMembers;
			this.newCurrentMembers      = newCurrentMembers     ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public PersistenceTypeDefinition legacyTypeDefinition()
		{
			return this.legacyTypeDefinition;
		}

		@Override
		public PersistenceTypeHandler<D, T> currentTypeHandler()
		{
			return this.currentTypeHandler;
		}

		@Override
		public XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> legacyToCurrentMembers()
		{
			return this.legacyToCurrentMembers;
		}
		
		@Override
		public XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> currentToLegacyMembers()
		{
			return this.currentToLegacyMembers;
		}
		
		@Override
		public XGettingEnum<PersistenceTypeDefinitionMember> discardedLegacyMembers()
		{
			return this.discardedLegacyMembers;
		}

		@Override
		public XGettingEnum<PersistenceTypeDefinitionMember> newCurrentMembers()
		{
			return this.newCurrentMembers;
		}
		
	}
	
}
