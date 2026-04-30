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

import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.collections.types.XGettingSequence;

/**
 * One parsed line of the textual type dictionary &mdash; a {@link PersistenceTypeDescription} bound to its
 * biunique {@link #typeId()} and {@link #typeName()} (i.e. a full {@link PersistenceTypeIdentity}) before any
 * runtime resolution or refactoring mapping has been applied.
 * <p>
 * Entries are produced by {@link PersistenceTypeDictionaryParser} and consumed by
 * {@link PersistenceTypeDictionaryBuilder}, which validates them, resolves their runtime types and turns
 * them into the {@link PersistenceTypeDefinition}s held by a {@link PersistenceTypeDictionary}.
 *
 * @see PersistenceTypeDictionaryParser
 * @see PersistenceTypeDictionaryBuilder
 * @see PersistenceTypeIdentity
 */
public interface PersistenceTypeDictionaryEntry extends PersistenceTypeDescription
{
	@Override
	public long   typeId();

	@Override
	public String typeName();

	@Override
	public XGettingSequence<? extends PersistenceTypeDescriptionMember> instanceMembers();



	/**
	 * Skeleton implementation supplying a {@link #toString()} that renders the entry through
	 * {@link PersistenceTypeDictionaryAssembler}, i.e. produces the same textual form an entry has on disk.
	 */
	public abstract class Abstract implements PersistenceTypeDictionaryEntry
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public String toString()
		{
			return PersistenceTypeDictionaryAssembler.New()
				.assembleTypeDescription(VarString.New(), this)
				.toString()
			;
		}

	}
	
}
