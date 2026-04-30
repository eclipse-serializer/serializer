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

import java.util.function.Consumer;

import org.eclipse.serializer.util.X;
import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.collections.EqHashTable;
import org.eclipse.serializer.collections.XSort;
import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.collections.types.XGettingTable;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTypeConsistencyDictionary;
import org.eclipse.serializer.reference.Swizzling;
import org.eclipse.serializer.reflect.XReflect;
import org.eclipse.serializer.typing.KeyValue;


/**
 * Schema registry of a persistence context: holds every {@link PersistenceTypeDefinition} produced from the
 * persisted type dictionary and from runtime type analysis, and lets callers look them up by typeId or by name.
 * <p>
 * <b>Why it exists.</b> Persistent data references types by stable {@link PersistenceTypeIdentity#typeId() typeId}.
 * Runtime classes can be renamed, evolved or removed; the dictionary is what bridges the two, applying any
 * configured refactoring mappings during the {@link PersistenceTypeDictionaryBuilder build} phase. It also
 * keeps deleted types queryable so legacy data can still be read.
 * <p>
 * <b>Indexes.</b> Internally a dictionary maintains two indexes:
 * <ul>
 * <li>typeId &rarr; {@link PersistenceTypeDefinition} via {@link #allTypeDefinitions()} /
 *     {@link #lookupTypeById(long)}</li>
 * <li>typeName &rarr; {@link PersistenceTypeLineage} via {@link #typeLineages()} /
 *     {@link #lookupTypeLineage(Class)}; a lineage groups every historical definition for a given runtime type
 *     so {@link #lookupTypeByName(String)} can return the latest one</li>
 * </ul>
 * <b>Lifecycle.</b> An empty dictionary is created via {@link #New(PersistenceTypeLineageCreator)}; the load
 * pipeline (Loader &rarr; Parser &rarr; Builder &rarr; Compiler) bulk-registers parsed type definitions,
 * after which {@link PersistenceTypeDictionaryManager} validates and registers further runtime definitions.
 * The mutable form is the base interface; {@link PersistenceTypeDictionaryView} is the read-only wrapper
 * obtained via {@link #view()}.
 *
 * @see PersistenceTypeDefinition
 * @see PersistenceTypeLineage
 * @see PersistenceTypeDictionaryView
 * @see PersistenceTypeDictionaryManager
 */
public interface PersistenceTypeDictionary
{
	/**
	 * Returns the latest {@link PersistenceTypeDefinition} of the lineage with the given {@code typeName}, or
	 * {@code null} if no such lineage is registered.
	 *
	 * @param typeName the textual type name to look up.
	 *
	 * @return the latest definition for that name, or {@code null}.
	 */
	public PersistenceTypeDefinition lookupTypeByName(String typeName);

	/**
	 * Returns the {@link PersistenceTypeDefinition} registered under the passed {@code typeId}, or
	 * {@code null} if none is registered.
	 *
	 * @param typeId the type id to look up.
	 *
	 * @return the definition, or {@code null}.
	 */
	public PersistenceTypeDefinition lookupTypeById(long typeId);


	/**
	 * Returns the live, sorted-by-name lineage index. Each entry groups all historical
	 * {@link PersistenceTypeDefinition}s that share a runtime type name.
	 *
	 * @return the lineage table.
	 */
	public XGettingTable<String, ? extends PersistenceTypeLineage> typeLineages();

	/**
	 * Convenience overload of {@link #lookupTypeLineage(String)} that derives the type name from the passed
	 * runtime class.
	 *
	 * @param type the runtime class to look up.
	 *
	 * @return the matching lineage, or {@code null}.
	 */
	public PersistenceTypeLineage lookupTypeLineage(Class<?> type);

	/**
	 * Returns the lineage registered under {@code typeName}, or {@code null} if none is registered.
	 *
	 * @param typeName the textual type name to look up.
	 *
	 * @return the matching lineage, or {@code null}.
	 */
	public PersistenceTypeLineage lookupTypeLineage(String typeName);


	/**
	 * Returns the live, sorted-by-typeId index of every registered {@link PersistenceTypeDefinition}.
	 *
	 * @return the typeId-keyed table.
	 */
	public XGettingTable<Long, PersistenceTypeDefinition> allTypeDefinitions();

	/**
	 * @return {@code true} if no type definition has been registered.
	 */
	public boolean isEmpty();

	/**
	 * Returns the highest typeId currently registered, or {@link Swizzling#notFoundId()} for an empty
	 * dictionary.
	 *
	 * @return the highest registered typeId.
	 */
	public long determineHighestTypeId();

	/**
	 * Returns an immutable {@linkplain PersistenceTypeDictionaryView snapshot view} of this dictionary.
	 *
	 * @return the view.
	 */
	public PersistenceTypeDictionaryView view();

	/**
	 * Iterates every registered {@link PersistenceTypeDefinition} in typeId order.
	 *
	 * @param logic the consumer to apply to each definition.
	 *
	 * @return {@code logic}.
	 */
	public default <C extends Consumer<? super PersistenceTypeDefinition>> C iterateAllTypeDefinitions(final C logic)
	{
		return this.allTypeDefinitions().values().iterate(logic);
	}

	/**
	 * Iterates the {@linkplain PersistenceTypeLineage#runtimeDefinition() runtime definition} of every
	 * lineage, i.e. the definition currently bound to a runtime class. Lineages without a runtime binding
	 * yield {@code null} entries.
	 *
	 * @param logic the consumer to apply to each runtime definition.
	 *
	 * @return {@code logic}.
	 */
	public default <C extends Consumer<? super PersistenceTypeDefinition>> C iterateRuntimeDefinitions(final C logic)
	{
		this.iterateTypeLineageViews(tl ->
		{
			logic.accept(tl.runtimeDefinition());
		});

		return logic;
	}

	/**
	 * Resolves every passed typeId via {@link #lookupTypeById(long)} and feeds the result into
	 * {@code collector}.
	 *
	 * @param typeIds   the type ids to resolve.
	 * @param collector receives the resolved definitions in iteration order.
	 *
	 * @return {@code collector}.
	 *
	 * @throws PersistenceExceptionTypeConsistencyDictionary if any typeId cannot be resolved.
	 */
	public default <C extends Consumer<? super PersistenceTypeDefinition>> C resolveTypeIds(
		final Iterable<Long> typeIds  ,
		final C              collector
	)
	{
		for(final Long typeId : typeIds)
		{
			final PersistenceTypeDefinition typeDefinition = this.lookupTypeById(typeId);
			if(typeDefinition == null)
			{
				throw new PersistenceExceptionTypeConsistencyDictionary("TypeId cannot be resolved: " + typeId);
			}

			collector.accept(typeDefinition);
		}

		return collector;
	}

	/**
	 * Iterates the {@linkplain PersistenceTypeLineage#latest() latest definition} of every lineage.
	 *
	 * @param logic the consumer to apply to each latest definition.
	 *
	 * @return {@code logic}.
	 */
	public default <C extends Consumer<? super PersistenceTypeDefinition>> C iterateLatestTypes(final C logic)
	{
		this.iterateTypeLineageViews(tl ->
		{
			logic.accept(tl.latest());
		});

		return logic;
	}

	/**
	 * Iterates every registered {@link PersistenceTypeLineage} in name order.
	 *
	 * @param logic the consumer to apply to each lineage.
	 *
	 * @return {@code logic}.
	 */
	public default <C extends Consumer<? super PersistenceTypeLineage>> C iterateTypeLineageViews(final C logic)
	{
		return this.typeLineages().values().iterate(logic);
	}


	// mutating logic //

	/**
	 * Returns the lineage for {@code type}, creating and registering a fresh empty one if none exists yet.
	 *
	 * @param type the runtime class whose lineage shall be ensured.
	 *
	 * @return the existing or newly created lineage.
	 */
	public PersistenceTypeLineage ensureTypeLineage(Class<?> type);

	/**
	 * Validates {@code typeDefinition}'s typeId and registers it in this dictionary, replacing any previously
	 * registered definition with the same typeId. Returns {@code true} if the registration changed dictionary
	 * state.
	 *
	 * @param typeDefinition the definition to register.
	 *
	 * @return {@code true} if the dictionary was modified.
	 *
	 * @throws PersistenceException if {@code typeDefinition.typeId()} is uninitialized.
	 */
	public boolean registerTypeDefinition(PersistenceTypeDefinition typeDefinition);

	/**
	 * Bulk variant of {@link #registerTypeDefinition(PersistenceTypeDefinition)}: validates every passed
	 * definition first, then registers them, sorting only once at the end.
	 *
	 * @param typeDefinitions the definitions to register.
	 *
	 * @return {@code true} if the dictionary was modified.
	 *
	 * @throws PersistenceException if any typeId is uninitialized.
	 */
	public boolean registerTypeDefinitions(Iterable<? extends PersistenceTypeDefinition> typeDefinitions);

	/**
	 * Like {@link #registerTypeDefinition(PersistenceTypeDefinition)} but additionally promotes the registered
	 * definition to the {@linkplain PersistenceTypeLineage#runtimeDefinition() runtime definition} of its
	 * lineage. Used when registering a definition that was derived from the current runtime class.
	 *
	 * @param typeDefinition the definition to register and promote.
	 *
	 * @return {@code true} if the dictionary was modified.
	 */
	public boolean registerRuntimeTypeDefinition(PersistenceTypeDefinition typeDefinition);

	/**
	 * Bulk variant of {@link #registerRuntimeTypeDefinition(PersistenceTypeDefinition)}.
	 *
	 * @param typeDefinitions the definitions to register and promote.
	 *
	 * @return {@code true} if the dictionary was modified.
	 */
	public boolean registerRuntimeTypeDefinitions(Iterable<? extends PersistenceTypeDefinition> typeDefinitions);

	/**
	 * Installs a callback notified after every successful type-definition registration. Used by the
	 * surrounding persistence machinery to keep the on-disk dictionary form in sync.
	 *
	 * @param observer the observer, or {@code null} to clear the existing one.
	 *
	 * @return this dictionary, for fluent chaining.
	 */
	public PersistenceTypeDictionary setTypeDescriptionRegistrationObserver(PersistenceTypeDefinitionRegistrationObserver observer);

	/**
	 * Returns the currently installed registration observer, or {@code null} if none.
	 *
	 * @return the observer, or {@code null}.
	 */
	public PersistenceTypeDefinitionRegistrationObserver getTypeDescriptionRegistrationObserver();
	

	
	/**
	 * Throws {@link PersistenceException} if {@code typeDefinition} carries an uninitialized typeId, i.e. one
	 * that {@link Swizzling#isFoundId(long)} rejects.
	 *
	 * @param typeDefinition the definition to validate.
	 *
	 * @throws PersistenceException if the typeId is uninitialized.
	 */
	public static void validateTypeId(final PersistenceTypeDefinition typeDefinition)
	{
		if(Swizzling.isFoundId(typeDefinition.typeId()))
		{
			return;
		}

		throw new PersistenceException("Uninitialized TypeId for type definition " + typeDefinition.typeName());
	}
	
	/**
	 * Bulk variant of {@link #validateTypeId(PersistenceTypeDefinition)}.
	 *
	 * @param typeDefinitions the definitions to validate.
	 *
	 * @throws PersistenceException if any typeId is uninitialized.
	 */
	public static void validateTypeIds(final Iterable<? extends PersistenceTypeDefinition> typeDefinitions)
	{
		typeDefinitions.forEach(PersistenceTypeDictionary::validateTypeId);
	}


	/**
	 * Iterates every registered {@link PersistenceTypeLineage}.
	 *
	 * @param logic the consumer to apply to each lineage.
	 *
	 * @return {@code logic}.
	 */
	public default <C extends Consumer<? super PersistenceTypeLineage>> C iterateTypeLineages(final C logic)
	{
		return this.typeLineages().values().iterate(logic);
	}

	/**
	 * Convenience wrapper around {@link #registerTypeDefinitions(Iterable)} that returns the dictionary for
	 * fluent chaining.
	 *
	 * @param typeDictionary  the target dictionary.
	 * @param typeDefinitions the definitions to register.
	 *
	 * @return {@code typeDictionary}.
	 */
	public static <D extends PersistenceTypeDictionary> D registerTypes(
		final D                                                          typeDictionary  ,
		final XGettingCollection<? extends PersistenceTypeDefinition> typeDefinitions
	)
	{
		typeDictionary.registerTypeDefinitions(typeDefinitions);
		return typeDictionary;
	}

	/**
	 * Appends every passed definition's {@code toString()} form to the buffer, separated by newlines.
	 * Used internally by {@link Default#toString()} and by {@link PersistenceTypeDictionaryView}.
	 *
	 * @param vs                the buffer to append into.
	 * @param allTypesPerTypeId the typeId-keyed table of definitions.
	 *
	 * @return {@code vs}, for fluent chaining.
	 */
	public static VarString assembleTypesPerTypeId(
		final VarString                                      vs               ,
		final XGettingTable<Long, PersistenceTypeDefinition> allTypesPerTypeId
	)
	{
		for(final PersistenceTypeDefinition type : allTypesPerTypeId.values())
		{
			vs.add(type).lf();
		}
		
		return vs;
	}
	
	/**
	 * Determines the highest typeId present in the passed table, or {@link Swizzling#notFoundId()} for an
	 * empty table.
	 *
	 * @param allTypesPerTypeId the typeId-keyed table.
	 *
	 * @return the highest typeId, or {@link Swizzling#notFoundId()}.
	 */
	public static long determineHighestTypeId(final XGettingTable<Long, PersistenceTypeDefinition> allTypesPerTypeId)
	{
		long maxTypeId = Swizzling.notFoundId();

		for(final Long typeId : allTypesPerTypeId.keys())
		{
			if(typeId >= maxTypeId)
			{
				maxTypeId = typeId;
			}
		}

		return maxTypeId;
	}

	
	
	/**
	 * Creates an empty dictionary backed by the passed lineage creator.
	 *
	 * @param typeLineageCreator the lineage creator to use; must not be {@code null}.
	 *
	 * @return the new dictionary.
	 */
	public static PersistenceTypeDictionary New(final PersistenceTypeLineageCreator typeLineageCreator)
	{
		return new PersistenceTypeDictionary.Default(
			notNull(typeLineageCreator)
		);
	}

	/**
	 * Creates a dictionary backed by the passed lineage creator and pre-populated with {@code typeDefinitions}.
	 *
	 * @param typeLineageCreator the lineage creator to use; must not be {@code null}.
	 * @param typeDefinitions    the definitions to register up-front.
	 *
	 * @return the new dictionary.
	 */
	public static PersistenceTypeDictionary New(
		final PersistenceTypeLineageCreator                           typeLineageCreator,
		final XGettingCollection<? extends PersistenceTypeDefinition> typeDefinitions
	)
	{
		return PersistenceTypeDictionary.registerTypes(
			New(typeLineageCreator),
			typeDefinitions
		);
	}

	/**
	 * Default {@link PersistenceTypeDictionary} implementation. Holds the typeId-keyed and name-keyed indexes
	 * and serializes mutating operations through {@code synchronized} on the dictionary instance. Lineage
	 * creation goes through the {@link PersistenceTypeLineageCreator} supplied at construction time.
	 */
	public final class Default implements PersistenceTypeDictionary
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// the dictionary must be enhanceable at runtime, hence it must know a type lineage provider
		private final PersistenceTypeLineageCreator                  typeLineageCreator;
		private final EqHashTable<String, PersistenceTypeLineage>    typeLineages       = EqHashTable.New();
		
		private final EqHashTable<Long  , PersistenceTypeDefinition> allTypesPerTypeId  = EqHashTable.New();
		private       PersistenceTypeDefinitionRegistrationObserver  registrationObserver;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final PersistenceTypeLineageCreator typeLineageCreator)
		{
			super();
			this.typeLineageCreator = typeLineageCreator;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final XGettingTable<String, PersistenceTypeLineage> typeLineages()
		{
			return this.typeLineages;
		}
		
		@Override
		public synchronized PersistenceTypeLineage lookupTypeLineage(final Class<?> type)
		{
			return this.synchLookupTypeLineage(type.getName());
		}
		
		@Override
		public synchronized PersistenceTypeLineage lookupTypeLineage(final String typeName)
		{
			return this.synchLookupTypeLineage(typeName);
		}
		
		private <T> PersistenceTypeLineage synchLookupTypeLineage(final String typeName)
		{
			final PersistenceTypeLineage lineage = this.typeLineages.get(typeName);
			return lineage;
		}
		
		
		@Override
		public synchronized PersistenceTypeLineage ensureTypeLineage(final Class<?> type)
		{
			final PersistenceTypeLineage lineage = this.lookupTypeLineage(type);
			if(lineage != null)
			{
				return lineage;
			}
			
			return this.synchRegisterTypeLineage(this.typeLineageCreator.createTypeLineage(type));
		}
				
		private <T> PersistenceTypeLineage synchRegisterTypeLineage(final PersistenceTypeLineage lineage)
		{
			this.typeLineages.add(lineage.typeName(), lineage);
			this.synchSortTypeLineages();

			return lineage;
		}
		
		public synchronized PersistenceTypeLineage ensureTypeLineage(
			final PersistenceTypeDefinition typeDefinition
		)
		{
			PersistenceTypeLineage typeLineage = this.lookupTypeLineage(
				typeDefinition.runtimeTypeName()
			);
			
			if(typeLineage == null)
			{
				typeLineage = this.typeLineageCreator.createTypeLineage(
					typeDefinition.runtimeTypeName(),
					typeDefinition.type()
				);
				this.synchRegisterTypeLineage(typeLineage);
			}

			return typeLineage;
		}

		final <T> boolean synchRegisterType(final PersistenceTypeDefinition typeDefinition)
		{
			final PersistenceTypeLineage lineage = this.ensureTypeLineage(typeDefinition);
				
			// may not abort here in order to consistently use TypeHandlers over dictionary-loaded definitions
			final boolean hasChanged = lineage.registerTypeDefinition(typeDefinition);
//			if(!lineage.registerTypeDefinition(typeDefinition))
//			{
//				// type definition already contained, abort.
//				return false;
//			}
			
			// definitions can be replaced by another instance (e.g. a plain instance by a handler instance)
			this.allTypesPerTypeId.put(typeDefinition.typeId(), typeDefinition);

			// callback gets set externally, can/may be null, so check for it.
			if(this.registrationObserver != null)
			{
				this.registrationObserver.observeTypeDefinitionRegistration(typeDefinition);
			}
			
			// the proper state feedback is important for avoiding redundant updats to the dictionary persistent form.
			return hasChanged;
//			return true;
		}
		
		private void synchSortTypeLineages()
		{
			this.typeLineages.keys().sort(XSort::compare);
		}

		private void internalSort()
		{
			this.allTypesPerTypeId.keys().sort(XSort::compare);
		}
		
		@Override
		public final synchronized PersistenceTypeDictionary setTypeDescriptionRegistrationObserver(
			final PersistenceTypeDefinitionRegistrationObserver registrationObserver
		)
		{
			this.registrationObserver = registrationObserver;
			
			return this;
		}

		@Override
		public final synchronized PersistenceTypeDefinitionRegistrationObserver getTypeDescriptionRegistrationObserver()
		{
			return this.registrationObserver;
		}

		@Override
		public final synchronized XGettingTable<Long, PersistenceTypeDefinition> allTypeDefinitions()
		{
			return this.allTypesPerTypeId;
		}
		
		@Override
		public final synchronized boolean isEmpty()
		{
			return this.allTypesPerTypeId.isEmpty();
		}
		
		@Override
		public final synchronized boolean registerTypeDefinition(
			final PersistenceTypeDefinition typeDefinition
		)
		{
			PersistenceTypeDictionary.validateTypeId(typeDefinition);
			
			if(this.synchRegisterType(typeDefinition))
			{
				this.internalSort();
				return true;
			}
			return false;
		}

		@Override
		public final synchronized boolean registerTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			// first validate all before changing any state
			PersistenceTypeDictionary.validateTypeIds(typeDefinitions);
			
			final long oldSize = this.allTypesPerTypeId.size();

			for(final PersistenceTypeDefinition td : typeDefinitions)
			{
				this.synchRegisterType(td);
			}

			if(this.allTypesPerTypeId.size() != oldSize)
			{
				this.internalSort();
				return true;
			}
			
			return false;
		}
		
		@Override
		public final synchronized boolean registerRuntimeTypeDefinition(
			final PersistenceTypeDefinition typeDefinition
		)
		{
			final boolean returnValue = this.registerTypeDefinition(typeDefinition);
			this.synchSetRuntimeTypeDefinition(typeDefinition);
			
			return returnValue;
		}
		
		@Override
		public final synchronized boolean registerRuntimeTypeDefinitions(
			final Iterable<? extends PersistenceTypeDefinition> typeDefinitions
		)
		{
			final boolean returnValue = this.registerTypeDefinitions(typeDefinitions);
			
			for(final PersistenceTypeDefinition td : typeDefinitions)
			{
				this.synchSetRuntimeTypeDefinition(td);
			}
			
			return returnValue;
		}
		
		private <T> void synchSetRuntimeTypeDefinition(final PersistenceTypeDefinition td)
		{
			final PersistenceTypeLineage lineage = this.lookupTypeLineage(td.runtimeTypeName());
			lineage.setRuntimeTypeDefinition(td);
		}

		@Override
		public final synchronized PersistenceTypeDefinition lookupTypeByName(final String typeName)
		{
			final PersistenceTypeLineage lineage = this.lookupTypeLineage(typeName);
			
			return lineage == null
				? null
				: lineage.latest()
			;
		}

		@Override
		public final synchronized PersistenceTypeDefinition lookupTypeById(final long typeId)
		{
			return this.allTypesPerTypeId.get(typeId);
		}

		@Override
		public final synchronized long determineHighestTypeId()
		{
			return PersistenceTypeDictionary.determineHighestTypeId(this.allTypesPerTypeId);
		}
		
		@Override
		public synchronized PersistenceTypeDictionaryView view()
		{
			// wrap in an instance of an immutable view implementation
			return PersistenceTypeDictionaryView.New(this);
		}

		@Override
		public final synchronized String toString()
		{
			return PersistenceTypeDictionary.assembleTypesPerTypeId(VarString.New(), this.allTypesPerTypeId).toString();
		}

	}
	


	/**
	 * Reports whether {@code typeName} is one of the variable-length textual type keywords used in the
	 * dictionary form ({@code [byte]}, {@code [char]}, {@code [list]}).
	 *
	 * @param typeName the textual type name from the dictionary.
	 *
	 * @return {@code true} if the keyword denotes a variable-length type.
	 */
	public static boolean isVariableLength(final String typeName)
	{
		switch(typeName)
		{
			case Symbols.TYPE_BYTES:
			case Symbols.TYPE_CHARS:
			case Symbols.TYPE_COMPLEX:
			{
					return true;
			}
			default:
			{
				return false;
			}
		}
	}

	/**
	 * Builds the qualified field name {@code qualifier + separator + fieldName}, or just {@code fieldName} if
	 * {@code qualifier} is {@code null}. Used to derive
	 * {@link PersistenceTypeDescriptionMember#identifier() member identifiers}.
	 *
	 * @param qualifier the qualifier (e.g. declaring class name), or {@code null}.
	 * @param fieldName the simple field name.
	 *
	 * @return the qualified field name.
	 */
	public static String fullQualifiedFieldName(
		final String qualifier,
		final String fieldName
	)
	{
		if(qualifier == null)
		{
			return fieldName;
		}

		return fullQualifiedFieldName(VarString.New(), qualifier, fieldName).toString();
	}

	/**
	 * @return the separator character placed between qualifier and field name in
	 *         {@link #fullQualifiedFieldName(String, String)}.
	 */
	public static char fullQualifiedFieldNameSeparator()
	{
		return Symbols.MEMBER_FIELD_QUALIFIER_SEPERATOR;
	}

	/**
	 * Buffer-based variant of {@link #fullQualifiedFieldName(String, String)} that appends into {@code vc}.
	 *
	 * @param vc        the buffer to append into.
	 * @param qualifier the qualifier (e.g. declaring class name), or {@code null}.
	 * @param fieldName the simple field name.
	 *
	 * @return {@code vc}, for fluent chaining.
	 */
	public static VarString fullQualifiedFieldName(
		final VarString vc       ,
		final String    qualifier,
		final String    fieldName
	)
	{
		if(qualifier != null)
		{
			vc.add(qualifier).add(fullQualifiedFieldNameSeparator());
		}

		return vc.add(fieldName);
	}

	/**
	 * Splits a qualified identifier produced by {@link #fullQualifiedFieldName(String, String)} back into
	 * {@code (qualifier, fieldName)}; returns {@code (null, identifier)} if no separator is present.
	 *
	 * @param identifier the qualified identifier.
	 *
	 * @return a key-value pair of qualifier (or {@code null}) and field name.
	 */
	public static KeyValue<String, String> splitFullQualifiedFieldName(
		final String identifier
	)
	{
		final int index = identifier.lastIndexOf(fullQualifiedFieldNameSeparator());
		
		return index < 0
			? X.KeyValue(null, identifier)
			: X.KeyValue(identifier.substring(0, index).trim(), identifier.substring(index + 1).trim())
		;
	}

	// type is primarily defined by the dictionary string. Parser must guarantee to create the appropriate member types
	/**
	 * @param typeName the textual type name from the dictionary.
	 *
	 * @return {@code true} if {@code typeName} is the inlined complex (nested-list) type keyword.
	 */
	public static boolean isInlinedComplexType(final String typeName)
	{
		return Symbols.TYPE_COMPLEX.equals(typeName);
	}

	// type is primarily defined by the dictionary string. Parser must guarantee to create the appropriate member types
	/**
	 * @param typeName the textual type name from the dictionary.
	 *
	 * @return {@code true} if {@code typeName} is one of the inlined variable-length type keywords
	 *         ({@code [byte]}, {@code [char]}, {@code [list]}).
	 */
	public static boolean isInlinedVariableLengthType(final String typeName)
	{
		return Symbols.TYPE_BYTES.equals(typeName)
			|| Symbols.TYPE_CHARS.equals(typeName)
			|| isInlinedComplexType(typeName)
		;
	}



	/**
	 * Shared character and keyword constants used by the textual on-disk dictionary form &mdash; consumed by
	 * both {@link PersistenceTypeDictionaryAssembler} (writing) and {@link PersistenceTypeDictionaryParser}
	 * (reading). Subclassed rather than imported so that subtypes can access the {@code protected} fields
	 * directly.
	 */
	public class Symbols
	{
		protected static final transient char   TYPE_START                       = '{';
		protected static final transient char   TYPE_END                         = '}';
		protected static final transient char   MEMBER_FIELD_QUALIFIER_SEPERATOR = XReflect.fieldIdentifierDelimiter();
		protected static final transient char   MEMBER_TERMINATOR                = ','; // cannot be ";" as array names are terminated by it
		protected static final transient char   MEMBER_COMPLEX_DEF_START         = '(';
		protected static final transient char   MEMBER_COMPLEX_DEF_END           = ')';
		
		// (30.07.2019 TM)NOTE: literal parsing implemented but then not needed. Kept around a while.
//		protected static final transient char   LITERAL_DELIMITER                = '"';
//		protected static final transient char   LITERAL_ESCAPER                  = '\\';

		protected static final transient String KEYWORD_PRIMITIVE                = "primitive";
		protected static final transient String KEYWORD_ENUM                     = XReflect.typename_enum();
		protected static final transient String TYPE_CHARS                       = "[char]"   ;
		protected static final transient String TYPE_BYTES                       = "[byte]"   ;
		protected static final transient String TYPE_COMPLEX                     = "[list]"   ;

		protected static final transient char[] ARRAY_KEYWORD_PRIMITIVE          = KEYWORD_PRIMITIVE.toCharArray();
		protected static final transient char[] ARRAY_KEYWORD_ENUM               = KEYWORD_ENUM     .toCharArray();
		protected static final transient char[] ARRAY_TYPE_CHARS                 = TYPE_CHARS       .toCharArray();
		protected static final transient char[] ARRAY_TYPE_BYTES                 = TYPE_BYTES       .toCharArray();
		protected static final transient char[] ARRAY_TYPE_COMPLEX               = TYPE_COMPLEX     .toCharArray();

		public static final String typeChars()
		{
			return TYPE_CHARS;
		}

		public static final String typeBytes()
		{
			return TYPE_BYTES;
		}

		public static final String typeComplex()
		{
			return TYPE_COMPLEX;
		}



		protected Symbols()
		{
			super();
			// can be extended to access the symbols
		}

	}
	
}
