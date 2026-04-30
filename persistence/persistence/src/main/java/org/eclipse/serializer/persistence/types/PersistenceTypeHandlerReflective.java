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

import java.lang.reflect.Field;

import org.eclipse.serializer.collections.types.XGettingEnum;

/**
 * A {@link PersistenceTypeHandlerGeneric} that derives its persistent layout entirely from the declared
 * Java {@link Field}s of the handled class via reflection. All members are
 * {@link PersistenceTypeDefinitionMemberFieldReflective} instances, and the underlying {@link Field}s are
 * additionally exposed in three convenience views &mdash; all, primitive-only, reference-only &mdash;
 * for handlers that need to walk fields by category.
 *
 * @param <D> the data target type.
 * @param <T> the handled Java type.
 *
 * @see PersistenceTypeDefinitionMemberFieldReflective
 */
public interface PersistenceTypeHandlerReflective<D, T> extends PersistenceTypeHandlerGeneric<D, T>
{
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMemberFieldReflective> instanceMembers();

	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMemberFieldReflective> storingMembers();

	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMemberFieldReflective> settingMembers();

	/**
	 * @return the underlying Java {@link Field}s of every instance member, in persisted order.
	 */
	public XGettingEnum<Field> instanceFields();

	/**
	 * @return the underlying Java {@link Field}s of every primitive-typed instance member.
	 */
	public XGettingEnum<Field> instancePrimitiveFields();

	/**
	 * @return the underlying Java {@link Field}s of every reference-typed instance member.
	 */
	public XGettingEnum<Field> instanceReferenceFields();
}
