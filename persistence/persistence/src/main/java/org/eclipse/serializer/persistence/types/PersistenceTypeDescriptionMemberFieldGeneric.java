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



/**
 * Field-style member that is <i>not</i> derived from a Java {@link java.lang.reflect.Field} via reflection
 * but is custom-defined &mdash; typically by a hand-written {@link PersistenceTypeHandlerCustom} for types
 * such as collections, maps, lazy references and other structures whose persistent shape doesn't directly
 * match the Java fields.
 * <p>
 * Generic fields come in three further flavors:
 * <ul>
 * <li>{@link PersistenceTypeDescriptionMemberFieldGenericSimple} &mdash; a single fixed-length value
 *     (primitive or reference).</li>
 * <li>{@link PersistenceTypeDescriptionMemberFieldGenericVariableLength} &mdash; a flat variable-length
 *     entry (e.g. a {@code [byte]} or {@code [char]} chunk).</li>
 * <li>{@link PersistenceTypeDescriptionMemberFieldGenericComplex} &mdash; a variable-length entry whose
 *     elements are themselves described by a nested member sequence (e.g. a list of structured records).</li>
 * </ul>
 * Unlike reflective fields the {@link #qualifier()} is optional; generic fields are usually identified by
 * their simple {@link #name()} alone.
 *
 * @see PersistenceTypeDescriptionMemberFieldReflective
 */
public interface PersistenceTypeDescriptionMemberFieldGeneric extends PersistenceTypeDescriptionMemberField
{
	/**
	 * Abstract base for generic fields, forwarding the persistent-attribute tuple to
	 * {@link PersistenceTypeDescriptionMemberField.Abstract}.
	 */
	public abstract class Abstract
	extends PersistenceTypeDescriptionMemberField.Abstract
	implements PersistenceTypeDescriptionMemberFieldGeneric
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Abstract(
			final String  typeName           ,
			final String  qualifier          ,
			final String  name               ,
			final boolean isReference        ,
			final boolean isPrimitive        ,
			final boolean hasReferences      ,
			final long    persistentMinLength,
			final long    persistentMaxLength
		)
		{
			super(
				typeName           ,
				qualifier          ,
				name               ,
				isReference        ,
				isPrimitive        ,
				hasReferences      ,
				persistentMinLength,
				persistentMaxLength
			);
		}

	}

}
