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

import org.eclipse.serializer.typing.TypeMapping;


/**
 * Since the value translator lookup might potentially get rather giant in the future, it is wrapped in a trivial
 * on-demand provider to ensure it is really only created (and held in memory forever) if necessary.
 *
 */
public interface BinaryValueTranslatorMappingProvider extends BinaryValueTranslatorLookupProvider
{
	@Override
	public TypeMapping<BinaryValueSetter> mapping(boolean switchByteOrder);
	
	
	
	public static BinaryValueTranslatorMappingProvider New()
	{
		return new BinaryValueTranslatorMappingProvider.Default();
	}
	
	public final class Default implements BinaryValueTranslatorMappingProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private transient TypeMapping<BinaryValueSetter> typeMapping;

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public synchronized TypeMapping<BinaryValueSetter> mapping(final boolean switchByteOrder)
		{
			if(this.typeMapping == null)
			{
				this.typeMapping = BinaryValueTranslators.createDefaultValueTranslators(switchByteOrder);
			}

			return this.typeMapping;
		}
		
	}
	
}
