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

/**
 * Walks a {@link Class}'s declared instance fields (including inherited ones) and partitions them into
 * persistable, persister-special, and problematic buckets according to a configured pipeline of
 * {@link PersistenceFieldEvaluator}s and a {@link PersistenceTypeEvaluator}. The result drives type-handler
 * generation: only the persistable fields end up in the on-disk member sequence.
 * <p>
 * Three collection variants exist because three categories of types need distinct rules: regular entities,
 * reflectively-handled collections, and enums (which must skip their compiler-synthesized fields).
 *
 * @see PersistenceTypeEvaluator
 * @see PersistenceFieldEvaluator
 */
public interface PersistenceTypeAnalyzer
{
	/**
	 * Whether the passed type is rejected by the configured {@link PersistenceTypeEvaluator} and thus
	 * cannot be persisted.
	 *
	 * @param type the type to check.
	 *
	 * @return {@code true} if the type is not persistable.
	 */
	public boolean isUnpersistable(Class<?> type);

	/**
	 * Walks the instance fields of an entity type and routes each field into one of the three passed
	 * collections: persistable (the default bucket), persister-special (matching the persister field
	 * evaluator), or problematic (matching the problematic-field evaluator). Inherited fields are included.
	 *
	 * @param <C>               the persistable-fields collection type, returned for fluent chaining.
	 * @param type              the entity type to analyze.
	 * @param persistableFields the destination for persistable fields.
	 * @param persisterFields   the destination for persister-special fields.
	 * @param problematicFields the destination for problematic fields.
	 *
	 * @return the {@code persistableFields} collection.
	 */
	public <C extends XEnum<Field>> C collectPersistableFieldsEntity(
		Class<?>               type             ,
		C                      persistableFields,
		XEnum<Field> persisterFields  ,
		XEnum<Field>     problematicFields
	);

	/**
	 * Variant of
	 * {@link #collectPersistableFieldsEntity(Class, XEnum, XEnum, XEnum) collectPersistableFieldsEntity}
	 * for reflectively-handled collection types: applies the additional reflective-collection field selector
	 * to flag fields that violate the collection's expected layout as problematic.
	 *
	 * @param <C>               the persistable-fields collection type, returned for fluent chaining.
	 * @param type              the collection type to analyze.
	 * @param persistableFields the destination for persistable fields.
	 * @param persisterFields   the destination for persister-special fields.
	 * @param problematicFields the destination for problematic fields.
	 *
	 * @return the {@code persistableFields} collection.
	 */
	public <C extends XEnum<Field>> C collectPersistableFieldsCollection(
		Class<?>               type             ,
		C                      persistableFields,
		XEnum<Field> persisterFields  ,
		XEnum<Field>     problematicFields
	);

	/**
	 * Variant of
	 * {@link #collectPersistableFieldsEntity(Class, XEnum, XEnum, XEnum) collectPersistableFieldsEntity}
	 * for enum types: applies the enum field selector to flag fields that should not be part of an enum's
	 * persistent layout (e.g. the compiler-synthesized {@code $VALUES} array) as problematic.
	 *
	 * @param <C>               the persistable-fields collection type, returned for fluent chaining.
	 * @param type              the enum type to analyze.
	 * @param persistableFields the destination for persistable fields.
	 * @param persisterFields   the destination for persister-special fields.
	 * @param problematicFields the destination for problematic fields.
	 *
	 * @return the {@code persistableFields} collection.
	 */
	public <C extends XEnum<Field>> C collectPersistableFieldsEnum(
		Class<?>               type             ,
		C                      persistableFields,
		XEnum<Field> persisterFields  ,
		XEnum<Field>     problematicFields
	);



	/**
	 * Creates a new {@link Default} analyzer wired up with the passed evaluators. None of the arguments may
	 * be {@code null}.
	 *
	 * @param isPersistable                     decides per type whether instances can be persisted.
	 * @param fieldSelectorPersistable          decides per field whether it is persistable.
	 * @param fieldSelectorPersister            decides per field whether it is the special "persister" field.
	 * @param fieldSelectorEnum                 decides per field whether it is part of an enum's persistent
	 *                                          layout.
	 * @param fieldSelectorReflectiveCollection decides per field whether it is part of a reflectively-handled
	 *                                          collection's persistent layout.
	 *
	 * @return the newly created analyzer.
	 */
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

	/**
	 * Default {@link PersistenceTypeAnalyzer}. Iterates declared instance fields up the class hierarchy
	 * via {@link XReflect#iterateDeclaredFieldsUpwards(Class, java.util.function.Consumer)} and applies the
	 * configured evaluator pipeline.
	 */
	public final class Default implements PersistenceTypeAnalyzer
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		/**
		 * Iterates the instance fields of {@code entityType} (including inherited ones) and routes each
		 * field into one of the three passed collections according to the supplied evaluators. Static and
		 * other non-instance fields are skipped silently. Persister fields are only considered when
		 * {@code isPersisterField} is non-{@code null}; problematic fields likewise only when
		 * {@code isProblematic} is non-{@code null}.
		 *
		 * @param entityType        the type whose fields are being iterated.
		 * @param isPersistable     decides per field whether it is persistable.
		 * @param isPersisterField  decides per (rejected) field whether it is a persister field; may be
		 *                          {@code null}.
		 * @param persistableFields destination for persistable fields.
		 * @param persisterFields   destination for persister fields.
		 * @param isProblematic     decides per (persistable) field whether it is problematic; may be
		 *                          {@code null}.
		 * @param problematicFields destination for problematic fields.
		 */
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
