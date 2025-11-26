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

import org.eclipse.serializer.collections.types.XEnum;
import org.eclipse.serializer.collections.types.XSequence;
import org.eclipse.serializer.reflect.XReflect;

import java.lang.reflect.Field;

import static org.eclipse.serializer.util.X.notNull;

public interface PersistenceTypeAnalyzer
{
	public boolean isUnpersistable(Class<?> type);
	
	public <C extends XEnum<Field>> C collectPersistableFieldsEntity(
		Class<?>               type             ,
		C                      persistableFields,
		XEnum<Field> persisterFields  ,
		XEnum<Field>     problematicFields
	);
	
	public <C extends XEnum<Field>> C collectPersistableFieldsCollection(
		Class<?>               type             ,
		C                      persistableFields,
		XEnum<Field> persisterFields  ,
		XEnum<Field>     problematicFields
	);
	
	public <C extends XEnum<Field>> C collectPersistableFieldsEnum(
		Class<?>               type             ,
		C                      persistableFields,
		XEnum<Field> persisterFields  ,
		XEnum<Field>     problematicFields
	);


	
	public static PersistenceTypeAnalyzer New(
		final PersistenceTypeEvaluator  isPersistable                    ,
		final PersistenceFieldEvaluator fieldSelectorPersistable         ,
		final PersistenceFieldEvaluator fieldSelectorPersister           ,
		final PersistenceFieldEvaluator fieldSelectorEnum                ,
		final PersistenceFieldEvaluator fieldSelectorReflectiveCollection
	)
	{
		return new PersistenceTypeAnalyzer.Default(
			notNull(isPersistable),
			notNull(fieldSelectorPersistable),
			notNull(fieldSelectorPersister),
			notNull(fieldSelectorEnum),
			notNull(fieldSelectorReflectiveCollection)
		);
	}

	public final class Default implements PersistenceTypeAnalyzer
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		public static void iterateInstanceFields(
			final Class<?>                   entityType       ,
			final PersistenceFieldEvaluator  isPersistable    ,
			final PersistenceFieldEvaluator  isPersisterField ,
			final XSequence<Field>           persistableFields,
			final XSequence<Field>           persisterFields  ,
			final PersistenceFieldEvaluator  isProblematic    ,
			final XSequence<Field>           problematicFields
		)
		{
			XReflect.iterateDeclaredFieldsUpwards(entityType, field ->
			{
				// non-instance fields are always discarded
				if(!XReflect.isInstanceField(field))
				{
					return;
				}
				
				// non-persistable fields are discard
				if(!isPersistable.applies(entityType, field))
				{
					/* Persister field is only considered if a predicate is present
					 * However, the basic check for compatible field type must be done in any case,
					 * otherwise an erroneous predicate could cause inconsistencies.
					 */
					if(isPersisterField != null)
					{
						if(Persistence.isPersisterField(field) && isPersisterField.applies(entityType, field))
						{
							XReflect.setAccessible(field);
							persisterFields.prepend(field);
						}
					}
					
					return;
				}
				
				/*
				 * if there is a "problematic" filter and it applies, the field is registered as such
				 * Note: there is a difference between being not persistable and being problematic.
				 * Not persistable fields are simply ignored, e.g. transient fields.
				 * Problematic fields cannot be ignored but require special behavior as a consequence,
				 * usually an exception about not being generically analyzable.
				 */
				if(isProblematic != null && isProblematic.applies(entityType, field))
				{
					problematicFields.add(field);
					return;
				}
				
				// persistable, non-problematic instance-field
				persistableFields.prepend(field);
			});
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceTypeEvaluator  isPersistable;
		private final PersistenceFieldEvaluator fieldSelectorPersistable;
		private final PersistenceFieldEvaluator fieldSelectorPersister;
		private final PersistenceFieldEvaluator fieldSelectorEnum;
		private final PersistenceFieldEvaluator fieldSelectorReflectiveCollection;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceTypeEvaluator  isPersistable                    ,
			final PersistenceFieldEvaluator fieldSelectorPersistable         ,
			final PersistenceFieldEvaluator fieldSelectorPersister           ,
			final PersistenceFieldEvaluator fieldSelectorEnum                ,
			final PersistenceFieldEvaluator fieldSelectorReflectiveCollection
		)
		{
			super();
			this.isPersistable                     = isPersistable                    ;
			this.fieldSelectorPersistable          = fieldSelectorPersistable         ;
			this.fieldSelectorPersister            = fieldSelectorPersister           ;
			this.fieldSelectorEnum                 = fieldSelectorEnum                ;
			this.fieldSelectorReflectiveCollection = fieldSelectorReflectiveCollection;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public boolean isUnpersistable(final Class<?> type)
		{
			return !this.isPersistable.isPersistableType(type);
		}

		@Override
		public <C extends XEnum<Field>> C collectPersistableFieldsEntity(
			final Class<?>               type             ,
			final C                      persistableFields,
			final XEnum<Field> persisterFields  ,
			final XEnum<Field>     problematicFields
		)
		{
			iterateInstanceFields(
				type,
				this.fieldSelectorPersistable,
				this.fieldSelectorPersister,
				persistableFields,
				persisterFields,
				null,
				problematicFields
			);

			return persistableFields;
		}
		
		@Override
		public <C extends XEnum<Field>> C collectPersistableFieldsEnum(
			final Class<?>     type             ,
			final C            persistableFields,
			final XEnum<Field> persisterFields  ,
			final XEnum<Field> problematicFields
		)
		{
			iterateInstanceFields(
				type,
				this.fieldSelectorPersistable,
				this.fieldSelectorPersister,
				persistableFields,
				persisterFields,
				(t, f) ->
					!this.fieldSelectorEnum.applies(type, f),
				problematicFields
			);
						
			return persistableFields;
		}
		
	

		@Override
		public <C extends XEnum<Field>> C collectPersistableFieldsCollection(
			final Class<?>     type             ,
			final C            persistableFields,
			final XEnum<Field> persisterFields  ,
			final XEnum<Field> problematicFields
		)
		{
			iterateInstanceFields(
				type,
				this.fieldSelectorPersistable,
				this.fieldSelectorPersister,
				persistableFields,
				persisterFields,
				(t, f) ->
					!this.fieldSelectorReflectiveCollection.applies(type, f),
				problematicFields
			);
						
			return persistableFields;
		}

	}

}
