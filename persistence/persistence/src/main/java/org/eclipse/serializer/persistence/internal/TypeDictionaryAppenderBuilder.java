package org.eclipse.serializer.persistence.internal;

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
import org.eclipse.serializer.functional.Aggregator;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionMemberFieldGeneric;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionMember;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionMemberAppender;
import org.eclipse.serializer.persistence.types.PersistenceTypeDescriptionMemberFieldReflective;

public final class TypeDictionaryAppenderBuilder
implements Aggregator<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMemberAppender>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final VarString vc   ;
	private final int       level;

	int maxFieldTypeNameLength    ;
	int maxDeclaringTypeNameLength;
	int maxFieldNameLength        ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public TypeDictionaryAppenderBuilder(final VarString vc, final int level)
	{
		super();
		this.vc    = vc   ;
		this.level = level;
	}


	private void measureTypeName(final String typeName)
	{
		if(typeName.length() > this.maxFieldTypeNameLength)
		{
			this.maxFieldTypeNameLength = typeName.length();
		}
	}

	private void measureDeclaringTypeName(final String declaringTypeName)
	{
		if(declaringTypeName.length() > this.maxDeclaringTypeNameLength)
		{
			this.maxDeclaringTypeNameLength = declaringTypeName.length();
		}
	}

	private void measureFieldName(final String fieldName)
	{
		if(fieldName.length() > this.maxFieldNameLength)
		{
			this.maxFieldNameLength = fieldName.length();
		}
	}

	private void measureFieldStrings(final PersistenceTypeDescriptionMemberFieldReflective member)
	{
		this.measureTypeName         (member.typeName());
		this.measureDeclaringTypeName(member.declaringTypeName());
		this.measureFieldName        (member.name());
	}

	private void measureGenericFieldStrings(final PersistenceTypeDescriptionMemberFieldGeneric member)
	{
		this.measureTypeName (member.typeName());
		this.measureFieldName(member.name());
	}

	@Override
	public final void accept(final PersistenceTypeDescriptionMember member)
	{
		// (21.03.2013 TM)XXX: type dictionary member field measurement uses awkward instanceoffing
		if(member instanceof PersistenceTypeDescriptionMemberFieldReflective)
		{
			this.measureFieldStrings((PersistenceTypeDescriptionMemberFieldReflective)member);
		}
		else if(member instanceof PersistenceTypeDescriptionMemberFieldGeneric)
		{
			this.measureGenericFieldStrings((PersistenceTypeDescriptionMemberFieldGeneric)member);
		}
		// otherwise, leave all lengths at 0 (e.g. primitive definition)
	}

	@Override
	public final PersistenceTypeDescriptionMemberAppender yield()
	{
		return new PersistenceTypeDescriptionMemberAppender.Default(
			this.vc,
			this.level,
			this.maxFieldTypeNameLength,
			this.maxDeclaringTypeNameLength,
			this.maxFieldNameLength
		);
	}
}
