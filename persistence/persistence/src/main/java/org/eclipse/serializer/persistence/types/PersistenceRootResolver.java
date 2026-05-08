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
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.serializer.collections.EqConstHashTable;
import org.eclipse.serializer.collections.EqHashEnum;
import org.eclipse.serializer.collections.EqHashTable;
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.collections.types.XGettingTable;
import org.eclipse.serializer.collections.types.XMap;
import org.eclipse.serializer.collections.types.XTable;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.reference.Reference;
import org.eclipse.serializer.reflect.XReflect;
import org.eclipse.serializer.typing.KeyValue;
import org.eclipse.serializer.util.X;

/**
 * Resolves identifier strings (as found in the persistent root set or in refactoring rules) to
 * {@link PersistenceRootEntry}s. Knows the user-defined root reference, the application-supplied named root
 * suppliers, and &mdash; for identifiers it cannot resolve directly &mdash; can fall back to looking up
 * persisted enum constant arrays via the type handler manager.
 * <p>
 * Two implementations are bundled:
 * <ul>
 * <li>{@link Default} &mdash; the regular resolver, looking up entries first in the locally registered set
 * and then in the enum-constants index.</li>
 * <li>{@link MappingWrapper} &mdash; decorates another resolver with a refactoring lookup so identifiers
 * encountered in the persistent dictionary can be rewritten to their current form before resolution.</li>
 * </ul>
 *
 * @see PersistenceRootEntry
 * @see PersistenceRootResolverProvider
 */
public interface PersistenceRootResolver
{
	/**
	 * The identifier used for the user-defined root reference (typically
	 * {@link Persistence#rootIdentifier()}).
	 *
	 * @return the root identifier.
	 */
	public String rootIdentifier();

	/**
	 * The user-defined root reference itself.
	 *
	 * @return the root reference.
	 */
	public PersistenceRootReference root();

	/**
	 * Resolves the passed identifier to a {@link PersistenceRootEntry}, returning {@code null} if no entry
	 * is registered for it (either directly or as an enum-constants identifier).
	 *
	 * @param identifier the identifier to resolve.
	 *
	 * @return the resolved entry, or {@code null} if unknown.
	 */
	public PersistenceRootEntry resolveRootInstance(String identifier);

	/**
	 * The directly registered entries by identifier (excluding entries that this resolver can only
	 * synthesize on demand, such as enum-constants entries).
	 *
	 * @return the defined entries.
	 */
	public XGettingTable<String, PersistenceRootEntry> definedEntries();

	/**
	 * Convenience accessor returning the entry registered under {@link #rootIdentifier()}, or {@code null}
	 * if none.
	 *
	 * @return the root entry.
	 */
	public default PersistenceRootEntry rootEntry()
	{
		return this.definedEntries().get(this.rootIdentifier());
	}

	/**
	 * Resolves every identifier in {@code identifiers}, accumulating successful results into
	 * {@code resolvedEntriesAcceptor}. Identifiers that already exist in the acceptor are not re-resolved;
	 * identifiers that resolve to {@code null} <em>and</em> are not yet in the acceptor are reported as
	 * unresolved.
	 *
	 * @param resolvedEntriesAcceptor the destination map to populate with resolved entries.
	 * @param identifiers             the identifiers to resolve.
	 *
	 * @throws PersistenceException if any identifier remains unresolved after the iteration.
	 */
	public default void resolveRootEntries(
		final XMap<String, PersistenceRootEntry> resolvedEntriesAcceptor,
		final XGettingEnum<String>               identifiers
	)
	{
		final EqHashEnum<String> unresolvedIdentifiers = EqHashEnum.New();
		
		synchronized(this)
		{
			for(final String identifier : identifiers)
			{
				final PersistenceRootEntry resolvedRootEntry = this.resolveRootInstance(identifier);
				if(resolvedRootEntry != null)
				{
					resolvedEntriesAcceptor.add(identifier, resolvedRootEntry);
				}
				else if(!resolvedEntriesAcceptor.keys().contains(identifier))
				{
					unresolvedIdentifiers.add(identifier);
				}
			}
			
			if(!unresolvedIdentifiers.isEmpty())
			{
				throw new PersistenceException(
					"The following root identifiers cannot be resolved: " + unresolvedIdentifiers
				);
			}
		}
	}
	
	/**
	 * Resolves every {@linkplain #definedEntries() defined entry} to its instance and returns the result as
	 * an {@code identifier → instance} table.
	 *
	 * @return the resolved root instances.
	 */
	public default XGettingTable<String, Object> resolveDefinedRootInstances()
	{
		return this.resolveRootInstances(this.definedEntries());
	}

	/**
	 * Resolves every entry in {@code entries} to its instance, skipping {@code null} entries (which can
	 * arise from automatic refactoring of removed root types). Removed entries (whose
	 * {@link PersistenceRootEntry#instance()} returns {@code null}) are kept with a {@code null} value so
	 * the caller can distinguish them from never-registered identifiers.
	 *
	 * @param entries the entries to resolve.
	 *
	 * @return a fresh {@code identifier → instance} table.
	 */
	public default XTable<String, Object> resolveRootInstances(
		final XGettingTable<String, PersistenceRootEntry> entries
	)
	{
		final EqHashTable<String, Object> resolvedRoots = EqHashTable.New();
		
		for(final PersistenceRootEntry entry : entries.values())
		{
			if(entry == null)
			{
				// null-entries can (only) happen via automatic refactoring of old root types (custom/default).
				continue;
			}
			
			// may be null if explicitely removed
			final String rootIdentifier = entry.identifier();
			final Object rootInstance   = entry.instance()  ;
			resolvedRoots.add(rootIdentifier, rootInstance);
		}
		
		return resolvedRoots;
	}

	
	
	/**
	 * Derives a set of named root suppliers from the static reference fields of the passed types, using
	 * {@link XReflect#deriveFieldIdentifier(Field)} as the identifier deriver.
	 *
	 * @param types the types whose static reference fields shall be exposed as roots.
	 *
	 * @return the derived {@code identifier → supplier} table.
	 */
	public static XGettingTable<String, Supplier<?>> deriveRoots(final Class<?>... types)
	{
		return deriveRoots(XReflect::deriveFieldIdentifier, types);
	}

	/**
	 * Derives a set of named root suppliers from the static reference fields of the passed types. The
	 * passed deriver function is consulted for each field and may return {@code null} to skip the field.
	 *
	 * @param rootIdentifierDeriver the function turning a {@link Field} into an identifier or {@code null}
	 *                              if the field shall be skipped.
	 * @param types                 the types to scan.
	 *
	 * @return the derived {@code identifier → supplier} table.
	 */
	public static XGettingTable<String, Supplier<?>> deriveRoots(
		final Function<Field, String> rootIdentifierDeriver,
		final Class<?>...             types
	)
	{
		final EqHashTable<String, Supplier<?>> roots = EqHashTable.New();

		addRoots(roots, rootIdentifierDeriver, types);

		return roots;
	}

	/**
	 * Bulk variant of {@link #addRoots(EqHashTable, Function, Class)} that scans every passed type.
	 *
	 * @param roots                 the destination table.
	 * @param rootIdentifierDeriver the identifier deriver.
	 * @param types                 the types to scan.
	 */
	public static void addRoots(
		final EqHashTable<String, Supplier<?>> roots                ,
		final Function<Field, String>          rootIdentifierDeriver,
		final Class<?>...                      types
	)
	{
		for(final Class<?> type : types)
		{
			addRoots(roots, rootIdentifierDeriver, type);
		}
	}

	/**
	 * Scans the static reference fields of {@code type} and adds an {@code identifier → supplier} entry to
	 * {@code roots} for each one whose derived identifier is non-{@code null}. Static primitives,
	 * non-static fields, and the synthetic {@code $SWITCH_TABLE} fields are skipped silently.
	 *
	 * @param roots                 the destination table.
	 * @param rootIdentifierDeriver the identifier deriver; returning {@code null} skips the field.
	 * @param type                  the type to scan.
	 */
	public static void addRoots(
		final EqHashTable<String, Supplier<?>> roots                ,
		final Function<Field, String>          rootIdentifierDeriver,
		final Class<?>                         type
	)
	{
		for(final Field field : type.getDeclaredFields())
		{
			/*
			 * better not trust custom predicates:
			 * - field MUST be static, otherwise no instance can be safely retrieved in a static way.
			 * - field MUST be a reference field, because registering primitives is neither possible nor reasonable.
			 */
			if(!XReflect.isStatic(field) || !XReflect.isReference(field))
			{
				continue;
			}
			
			// (04.05.2018 TM)TODO: proper solution for synthetic hacky fields
			/* (04.05.2018 TM)NOTE:
			 * Quick and dirty hotfix (out of pure anger) for the synthetic switch table thingy
			 * when using enum instances as keys in a switch. Apart from the geniuses not having made
			 * Modifier#isSynthetic public (because hey, why would a user of the JDK want to recognize and filter out
			 * their hacky stuff right?), it wouldn't be wise to simply filter out ALL synthetic fields, since
			 * for example ENUM$VALUES can very well be relevant for persistence.
			 * For now, using the ugly plain string is a quick solution. Let's see when they change something
			 * that breaks this fix.
			 */
			if(field.getName().startsWith("$SWITCH_TABLE"))
			{
				continue;
			}
			
			final String rootIdentifier = rootIdentifierDeriver.apply(field);
			if(rootIdentifier == null)
			{
				// the deriver function also serves as a predicate: if it returns null, the field shall be skipped.
				continue;
			}
			
			field.setAccessible(true);
			
			/*
			 * The static field gets registered for the derived identifier.
			 * The Supplier indirection prevents class initialization loops.
			 * Lambda line break for debuggability.
			 */
			roots.add(rootIdentifier, () ->
				XReflect.getFieldValue(field, null)
			);
		}
	}
	
	/**
	 * Central wrapping method mosty to have a unified and concisely named location for the lambda.
	 * 
	 * @param customRootInstance the instance to be used as the entity graph's root.
	 * 
	 * @return a {@link Supplier} returning the passed {@literal customRootInstance} instance.
	 */
	public static Supplier<?> wrapCustomRoot(final Object customRootInstance)
	{
		notNull(customRootInstance);
		return () ->
			customRootInstance
		;
	}
	
	
	/**
	 * Default {@link PersistenceRootResolver}: resolves identifiers from a fixed set of pre-registered
	 * entries first, then falls back to enum-constants resolution via the {@link PersistenceTypeHandlerManager}.
	 */
	public final class Default implements PersistenceRootResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String                                                rootIdentifier             ;
		private final PersistenceRootReference                              rootReference              ;
		private final EqConstHashTable<String, PersistenceRootEntry>        definedRootEntries         ;
		private final Reference<? extends PersistenceTypeHandlerManager<?>> referenceTypeHandlerManager;
		
		private transient PersistenceTypeHandlerManager<?> cachedTypeHandlerManager;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final String                                                rootIdentifier             ,
			final PersistenceRootReference                              rootReference              ,
			final EqConstHashTable<String, PersistenceRootEntry>        definedRootEntries         ,
			final Reference<? extends PersistenceTypeHandlerManager<?>> referenceTypeHandlerManager
		)
		{
			super();
			this.rootIdentifier              = rootIdentifier             ;
			this.rootReference               = rootReference              ;
			this.definedRootEntries          = definedRootEntries         ;
			this.referenceTypeHandlerManager = referenceTypeHandlerManager;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final String rootIdentifier()
		{
			return this.rootIdentifier;
		}
		
		@Override
		public final PersistenceRootReference root()
		{
			return this.rootReference;
		}

		@Override
		public final XGettingTable<String, PersistenceRootEntry> definedEntries()
		{
			return this.definedRootEntries;
		}
		
		private PersistenceTypeHandlerManager<?> typeHandlerManager()
		{
			if(this.cachedTypeHandlerManager == null)
			{
				this.cachedTypeHandlerManager = this.referenceTypeHandlerManager.get();
			}
			
			return this.cachedTypeHandlerManager;
		}
		
		@Override
		public final PersistenceRootEntry resolveRootInstance(final String identifier)
		{
			final PersistenceRootEntry rootEntry = this.definedRootEntries.get(identifier);
			if(rootEntry != null)
			{
				// directly registered / "normal" root entries are returned right away.
				return rootEntry;
			}
			
			return this.resolveRootEnumConstants(identifier);
		}
		
		private PersistenceRootEntry resolveRootEnumConstants(final String identifier)
		{
			final PersistenceTypeHandlerManager<?> typeHandlerManager = this.typeHandlerManager();
			
			final Long enumTypeId = typeHandlerManager.parseEnumRootIdentifierTypeId(identifier);
			if(enumTypeId == null)
			{
				// neither direct nor enum entry. Unrecognizable, return null.
				return null;
			}
			
			PersistenceTypeHandler<?, ?> enumTypeHandler = typeHandlerManager.lookupTypeHandler(
				enumTypeId.longValue()
			);
			if(enumTypeHandler == null)
			{
				//Under some circumstances the required type-handler may be missing
				//(Issue https://github.com/microstream-one/microstream/issues/209).
				//Try to create one…
				typeHandlerManager.ensureTypeHandlersByTypeIds(X.Enum(enumTypeId));
				enumTypeHandler = typeHandlerManager.lookupTypeHandler(enumTypeId.longValue());
				if(enumTypeHandler == null)
				{
					throw new PersistenceException(
						"No PersistenceTypeHandler found for root enum constant with TypeId: " + enumTypeId);
				}
			}
			
			// Checks for enum type internally. May be null for discarded (i.e. legacy) enums.
			final Object[] enumConstants = typeHandlerManager.collectEnumConstants(enumTypeHandler);
						
			return PersistenceRootEntry.New(identifier, () ->
				enumConstants // debuggability line break, do not remove!
			);
		}
		
	}
	
	
	
	/**
	 * Wraps {@code actualRootResolver} in a {@link MappingWrapper} that consults the refactoring mapping
	 * provided by {@code refactoringMappingProvider} before delegating to the wrapped resolver.
	 *
	 * @param actualRootResolver         the resolver to wrap.
	 * @param refactoringMappingProvider the provider supplying the refactoring mapping.
	 *
	 * @return the wrapping resolver.
	 */
	public static PersistenceRootResolver Wrap(
		final PersistenceRootResolver                    actualRootResolver        ,
		final PersistenceTypeDescriptionResolverProvider refactoringMappingProvider
	)
	{
		return new MappingWrapper(actualRootResolver, refactoringMappingProvider);
	}

	/**
	 * {@link PersistenceRootResolver} that consults a refactoring mapping before delegating to a wrapped
	 * resolver, allowing identifiers from the persistent dictionary to be rewritten to their current form
	 * before resolution. An explicit mapping target of {@code null} marks the source identifier as
	 * deleted; an unresolvable non-{@code null} target raises a {@link PersistenceException}.
	 */
	public final class MappingWrapper implements PersistenceRootResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceRootResolver                    actualRootResolver        ;
		final PersistenceTypeDescriptionResolverProvider refactoringMappingProvider;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		MappingWrapper(
			final PersistenceRootResolver                actualRootResolver        ,
			final PersistenceTypeDescriptionResolverProvider refactoringMappingProvider
		)
		{
			super();
			this.actualRootResolver         = actualRootResolver        ;
			this.refactoringMappingProvider = refactoringMappingProvider;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final String rootIdentifier()
		{
			return this.actualRootResolver.rootIdentifier();
		}
		
		@Override
		public final PersistenceRootReference root()
		{
			return this.actualRootResolver.root();
		}
		
		@Override
		public final XGettingTable<String, PersistenceRootEntry> definedEntries()
		{
			return this.actualRootResolver.definedEntries();
		}

		@Override
		public final PersistenceRootEntry resolveRootInstance(final String identifier)
		{
			/*
			 * Mapping lookups take precedence over the direct resolving attempt.
			 * This is important to enable refactorings that switch names.
			 * E.g.:
			 * A -> B
			 * C -> A
			 * However, this also increases the responsibility of the developer who defines the mapping:
			 * The mapping has to be removed after the first usage, otherwise the new instance under the old name
			 * is mapped to the old name's new name, as well. (In the example: after two executions, both instances
			 * would be mapped to B, which is an error. However, the source of the error is not a bug,
			 * but an outdated mapping rule defined by the using developer).
			 */
			final PersistenceTypeDescriptionResolver resolver = this.refactoringMappingProvider.provideTypeDescriptionResolver();
			
			final String sourceIdentifier = PersistenceMetaIdentifiers.normalizeIdentifier(identifier);
			
			final KeyValue<String, String> mapping = resolver.lookup(sourceIdentifier);
			if(mapping == null)
			{
				// simple case: no mapping found, use (normalized) source identifier directly.
				return this.actualRootResolver.resolveRootInstance(sourceIdentifier);
			}
			
			final String targetIdentifier = PersistenceMetaIdentifiers.normalizeIdentifier(mapping.value());
			
			/*
			 * special case: an explicit mapping entry for the (normalized) sourceIdentifier exists,
			 * but its target is null. This means the sourceIdentifier represents an old root entry
			 * that has been mapped as deleted by the user developer.
			 */
			if(targetIdentifier == null)
			{
				// mark removed entry
				return PersistenceRootEntry.New(sourceIdentifier, null);
			}
			
			// normal case: the sourceIdentifier has been mapped to a non-null targetIdentifier. So resolve it.
			final PersistenceRootEntry mappedEntry = this.actualRootResolver.resolveRootInstance(targetIdentifier);
			
			// but there is a catch: an unresolveable explicitly provided targetIdentifier is an error.
			if(mappedEntry == null)
			{
				throw new PersistenceException(
					"Refactoring mapping target identifier cannot be resolved: " + targetIdentifier
				);
			}
			
			return mappedEntry;
		}
				
	}
	
}
