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

import static org.eclipse.serializer.util.X.KeyValue;
import static org.eclipse.serializer.util.X.notNull;

import org.eclipse.serializer.collections.EqHashTable;
import org.eclipse.serializer.collections.types.XGettingTable;
import org.eclipse.serializer.typing.KeyValue;

/**
 * Built-in renaming step that hides simple type path/name changes from the refactoring layer. Applied during
 * type-name resolution so that classes whose fully-qualified name has shifted (e.g. when a type was moved
 * across packages or its enclosing class changed) keep loading without an explicit
 * {@link PersistenceRefactoringMapping} entry.
 * <p>
 * Distinct mappings are kept for classes and interfaces because the distinction matters for the loading
 * logic; both mappings return {@code null} when no entry exists for the passed name &mdash; meaning "no
 * rewrite, use the original".
 *
 * @see PersistenceRefactoringMapping
 * @see Defaults
 */
public interface PersistenceTypeNameMapper
{
	/**
	 * Returns the new fully-qualified class name for {@code oldClassName}, or {@code null} if no mapping
	 * exists.
	 *
	 * @param oldClassName the old class name.
	 *
	 * @return the new class name, or {@code null} if no mapping exists.
	 */
	public String mapClassName(String oldClassName);

	/**
	 * Returns the new fully-qualified interface name for {@code oldInterfaceName}, or {@code null} if no
	 * mapping exists.
	 *
	 * @param oldInterfaceName the old interface name.
	 *
	 * @return the new interface name, or {@code null} if no mapping exists.
	 */
	public String mapInterfaceName(String oldInterfaceName);


	/**
	 * Creates a new mapper preloaded with the default class and interface name mappings (see
	 * {@link Defaults}).
	 *
	 * @return the newly created mapper.
	 */
	public static PersistenceTypeNameMapper New()
	{
		return New(
			Defaults.defaultClassNameMappings(),
			Defaults.defaultInterfaceNameMappings()
		);
	}

	/**
	 * Creates a new mapper backed by the passed mappings.
	 *
	 * @param classNameMapping     the {@code old → new} class name mapping; must not be {@code null}.
	 * @param interfaceNameMapping the {@code old → new} interface name mapping; must not be {@code null}.
	 *
	 * @return the newly created mapper.
	 */
	public static PersistenceTypeNameMapper New(
		final XGettingTable<String, String> classNameMapping    ,
		final XGettingTable<String, String> interfaceNameMapping
	)
	{
		return new PersistenceTypeNameMapper.Default(
			notNull(classNameMapping),
			notNull(interfaceNameMapping)
		);
	}

	/**
	 * Default {@link PersistenceTypeNameMapper}: stateless lookup against two immutable hash tables.
	 */
	public final class Default implements PersistenceTypeNameMapper
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final XGettingTable<String, String> classNameMapping    ;
		private final XGettingTable<String, String> interfaceNameMapping;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final XGettingTable<String, String> classNameMapping    ,
			final XGettingTable<String, String> interfaceNameMapping
		)
		{
			super();
			this.classNameMapping     = classNameMapping    ;
			this.interfaceNameMapping = interfaceNameMapping;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final String mapClassName(final String oldClassName)
		{
			return this.classNameMapping.get(oldClassName);
		}
		
		@Override
		public final String mapInterfaceName(final String oldInterfaceName)
		{
			return this.interfaceNameMapping.get(oldInterfaceName);
		}
		
	}
	
	/**
	 * Built-in default class and interface name mappings. Currently rewrites the legacy
	 * {@code one.microstream.persistence.lazy.Lazy} types to their {@code one.microstream.reference.Lazy}
	 * counterparts so that data persisted before that refactoring still loads.
	 */
	public interface Defaults
	{
		/*
		 * Note on Lazy type history:
		 * 
		 * 1.)
		 * one.microstream.persistence.lazy.Lazy was a class
		 * 
		 * 2.)
		 * one.microstream.persistence.lazy.Lazy became an interface
		 * one.microstream.persistence.lazy.Lazy$Default was the class
		 * 
		 * 3.)
		 * one.microstream.persistence.lazy.Lazy         refactored to one.microstream.reference.Lazy
		 * one.microstream.persistence.lazy.Lazy$Default refactored to one.microstream.reference.Lazy$Default
		 * 
		 * Don't create cycles!
		 */
		
		/**
		 * The default class name mapping table. See {@link Defaults} for the rewrites it contains.
		 *
		 * @return the default class name mappings.
		 */
		public static XGettingTable<String, String> defaultClassNameMappings()
		{
			return EqHashTable.New(
				microstreamMapping("persistence.lazy.Lazy"        , "reference.Lazy$Default"),
				microstreamMapping("persistence.lazy.Lazy$Default", "reference.Lazy$Default")
			);
		}

		/**
		 * The default interface name mapping table. See {@link Defaults} for the rewrites it contains.
		 *
		 * @return the default interface name mappings.
		 */
		public static XGettingTable<String, String> defaultInterfaceNameMappings()
		{
			return EqHashTable.New(
				microstreamMapping("persistence.lazy.Lazy", "reference.Lazy")
			);
		}

		@Deprecated
		// FIXME Remove as this was to support a breaking change in one of the older MicroStream versions.
		public static KeyValue<String, String> microstreamMapping(final String s1, final String s2)
		{
			return KeyValue("one.microstream." + s1, "one.microstream." + s2);
		}
		
	}
	
}
