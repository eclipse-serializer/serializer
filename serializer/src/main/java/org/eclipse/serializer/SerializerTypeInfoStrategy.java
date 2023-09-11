package org.eclipse.serializer;

/*-
 * #%L
 * Eclipse Serializer
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

import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.collections.types.XList;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceManager;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionRegistrationObserver;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionary;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionaryAssembler;
import org.eclipse.serializer.util.logging.Logging;

import org.slf4j.Logger;

/**
 * 
 * Implementations supply the type information that is included into the serializers output.
 *
 */
public interface SerializerTypeInfoStrategy extends PersistenceTypeDefinitionRegistrationObserver
{
	/**
	 * returns the current type information available
	 * 
	 * @return the actual type information
	 */
	public SerializerTypeInfo get();
	
	/**
	 * indicates that new types had been added since the last get call
	 * 
	 * @return true if there are updated type info
	 */
	public boolean hasUpdate();
	
	/**
	 * Returns true if the type information should be included only once after it has been updated.
	 * 
	 * 
	 * @return true if type info has not to be included repeatedly
	 */
	public boolean includeOnce();
	
	/**
	 * 
	 * This implementation includes only type information for types added to the
	 * serializers type registry in the current serialization.
	 *
	 * Types that are registered during the serializers setup are never included.
	 */
	public static class IncrementalDiff implements SerializerTypeInfoStrategy
	{
		protected final static Logger logger = Logging.getLogger(SerializerTypeInfoStrategy.class);
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		protected final PersistenceTypeDictionaryAssembler typeAssembler      ;
		protected final XList<String>                      newTypes           ;
		protected       boolean                            updateAvailable    ;
		protected final boolean                            includeTypeInfoOnce;
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public IncrementalDiff(final PersistenceManager<Binary> persistenceManager, final boolean includeTypeInfoOnce)
		{
			super();
			this.typeAssembler       = PersistenceTypeDictionaryAssembler.New();
			this.newTypes            = BulkList.New();
			this.includeTypeInfoOnce = includeTypeInfoOnce;
			
			persistenceManager.typeDictionary().setTypeDescriptionRegistrationObserver(this);
		}

		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void observeTypeDefinitionRegistration(final PersistenceTypeDefinition typeDefinition)
		{
			logger.debug("new type registered: {}", typeDefinition);
			
			final VarString vc = VarString.New();
			this.typeAssembler.assembleTypeDescription(vc, typeDefinition);
			this.newTypes.add(vc.toString());
			this.updateAvailable = true;
		}

		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public SerializerTypeInfo get()
		{
			final SerializerTypeInfo typeInfo = new SerializerTypeInfo(this.newTypes.immure());
			this.newTypes.clear();
			this.updateAvailable = false;
			return typeInfo;
		}

		@Override
		public boolean hasUpdate()
		{
			return this.updateAvailable;
		}

		@Override
		public boolean includeOnce()
		{
			return this.includeTypeInfoOnce;
		}
	}
	
	/**
	 * 
	 * This implementation includes type information for types added to the
	 * serializers type registry in the current serialization and all previous
	 * serializations.
	 * Types that are registered during the serializers setup are never included.
	 *
	 */
	public static class Diff extends IncrementalDiff
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Diff(final PersistenceManager<Binary> persistenceManager, final boolean includeTypeInfoOnce)
		{
			super(persistenceManager, includeTypeInfoOnce);
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public SerializerTypeInfo get()
		{
			final SerializerTypeInfo typeInfo = new SerializerTypeInfo(this.newTypes.immure());
			this.updateAvailable = false;
			return typeInfo;
		}
	}
	
	/**
	 * 
	 * This implementation includes type information for all types currently known
	 * to the serializer including those registered during the setup.
	 *
	 */
	public static class TypeDictionary implements SerializerTypeInfoStrategy
	{
		protected final static Logger logger = Logging.getLogger(SerializerTypeInfoStrategy.class);
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		protected final PersistenceTypeDictionary          persistenceTypeDictionary;
		protected final PersistenceTypeDictionaryAssembler typeAssembler            ;
		protected       boolean                            updateAvailable          ;
		protected final boolean                            includeTypeInfoOnce      ;
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public TypeDictionary(final PersistenceManager<Binary> persistenceManager, final boolean includeTypeInfoOnce)
		{
			super();
			this.persistenceTypeDictionary = persistenceManager.typeDictionary();
			this.typeAssembler             = PersistenceTypeDictionaryAssembler.New();
			this.includeTypeInfoOnce       = includeTypeInfoOnce;
			
			persistenceManager.typeDictionary().setTypeDescriptionRegistrationObserver(this);
		}

		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void observeTypeDefinitionRegistration(final PersistenceTypeDefinition typeDefinition)
		{
			logger.debug("New type registered: {}.", typeDefinition);
			this.updateAvailable = true;
		}

		@Override
		public SerializerTypeInfo get()
		{
			final String assembledTypeDictionary = this.typeAssembler.assemble(this.persistenceTypeDictionary);
			final SerializerTypeInfo typeInfo    = new SerializerTypeInfo(new BulkList<>(assembledTypeDictionary).immure());
			this.updateAvailable                 = false;
			return typeInfo;
		}

		@Override
		public boolean hasUpdate()
		{
			return this.updateAvailable;
		}
		
		@Override
		public boolean includeOnce()
		{
			return this.includeTypeInfoOnce;
		}
	}
	
}
