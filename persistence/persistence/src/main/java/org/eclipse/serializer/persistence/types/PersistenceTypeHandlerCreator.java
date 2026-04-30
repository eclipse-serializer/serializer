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

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import org.eclipse.serializer.collections.HashEnum;
import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import org.eclipse.serializer.reflect.XReflect;

/**
 * Factory for {@link PersistenceTypeHandler}s, dispatched by category by the surrounding
 * {@link PersistenceTypeHandlerEnsurer}. Each {@code createTypeHandlerX} method handles one specific
 * shape of type:
 * <ul>
 * <li>{@link #createTypeHandlerArray} &mdash; reference-typed arrays; primitive-typed arrays must be
 *     handled by special-tailored custom handlers.</li>
 * <li>{@link #createTypeHandlerProxy} / {@link #createTypeHandlerLambda} &mdash; default
 *     implementation rejects both as unsupported.</li>
 * <li>{@link #createTypeHandlerEnum} &mdash; enum types (using the bug-tolerant
 *     {@code XReflect.isEnum} check rather than {@link Class#isEnum()}).</li>
 * <li>{@link #createTypeHandlerEntity} &mdash; types implementing the framework's
 *     {@link org.eclipse.serializer.entity.Entity} interface.</li>
 * <li>{@link #createTypeHandlerAbstract} &mdash; abstract classes (rejected for storing by default).</li>
 * <li>{@link #createTypeHandlerUnpersistable} &mdash; explicit unpersistable handlers (e.g. for
 *     {@link java.lang.Thread}).</li>
 * <li>{@link #createTypeHandlerGeneric} &mdash; the catch-all for ordinary classes; consults
 *     {@link PersistenceTypeAnalyzer} to validate that no field is structurally problematic.</li>
 * </ul>
 *
 * @param <D> the data target type.
 *
 * @see PersistenceTypeHandlerEnsurer
 */
public interface PersistenceTypeHandlerCreator<D>
{
	/**
	 * @param <T>  the array type.
	 * @param type a reference-typed array class.
	 *
	 * @return a handler for the array type.
	 *
	 * @throws PersistenceExceptionTypeNotPersistable if the array's component type is primitive.
	 */
	public <T> PersistenceTypeHandler<D, T> createTypeHandlerArray(Class<T> type) throws PersistenceExceptionTypeNotPersistable;

	/**
	 * Creates a handler for a JDK dynamic proxy class. Default implementation always throws.
	 *
	 * @param <T>  the proxy type.
	 * @param type a JDK proxy class.
	 *
	 * @return a handler for the proxy type.
	 *
	 * @throws PersistenceExceptionTypeNotPersistable always (default behavior).
	 */
	public <T> PersistenceTypeHandler<D, T> createTypeHandlerProxy(Class<T> type) throws PersistenceExceptionTypeNotPersistable;

	/**
	 * Creates a handler for a lambda class. Default implementation always throws because lambdas
	 * cannot be reliably resolved during loading on the current JVM.
	 *
	 * @param <T>  the lambda type.
	 * @param type a class identified as a lambda by {@link org.eclipse.serializer.typing.LambdaTypeRecognizer}.
	 *
	 * @return a handler for the lambda type.
	 *
	 * @throws PersistenceExceptionTypeNotPersistable always (default behavior).
	 */
	public <T> PersistenceTypeHandler<D, T> createTypeHandlerLambda(Class<T> type) throws PersistenceExceptionTypeNotPersistable;

	/**
	 * Creates a handler for an enum type.
	 *
	 * @param <T>  the enum type.
	 * @param type the enum class.
	 *
	 * @return a handler for the enum type.
	 *
	 * @throws PersistenceExceptionTypeNotPersistable if the enum has problematic fields.
	 */
	public <T> PersistenceTypeHandler<D, T> createTypeHandlerEnum(Class<T> type) throws PersistenceExceptionTypeNotPersistable;

	/**
	 * Creates a handler for a framework-{@link org.eclipse.serializer.entity.Entity}-based type.
	 *
	 * @param <T>  the entity type.
	 * @param type the entity class.
	 *
	 * @return a handler for the entity type.
	 *
	 * @throws PersistenceExceptionTypeNotPersistable if the type cannot be persisted.
	 */
	public <T> PersistenceTypeHandler<D, T> createTypeHandlerEntity(Class<T> type) throws PersistenceExceptionTypeNotPersistable;

	/**
	 * Creates a handler for an abstract class. Default behavior is the same as
	 * {@link #createTypeHandlerUnpersistable(Class)}.
	 *
	 * @param <T>  the abstract type.
	 * @param type an abstract class.
	 *
	 * @return a handler for the abstract type.
	 *
	 * @throws PersistenceExceptionTypeNotPersistable if the type cannot be persisted.
	 */
	public <T> PersistenceTypeHandler<D, T> createTypeHandlerAbstract(Class<T> type) throws PersistenceExceptionTypeNotPersistable;

	/**
	 * Creates an explicit unpersistable handler &mdash; used for types that can be referenced but
	 * never persisted (e.g. {@link java.lang.Thread}). The returned handler throws on any actual
	 * storing or loading attempt.
	 *
	 * @param <T>  the type.
	 * @param type the unpersistable class.
	 *
	 * @return an unpersistable handler.
	 */
	public <T> PersistenceTypeHandler<D, T> createTypeHandlerUnpersistable(Class<T> type);

	/**
	 * The catch-all factory for ordinary classes. Consults the {@link PersistenceTypeAnalyzer} to
	 * collect the persistable fields and reject types with structurally problematic fields. Has
	 * special logic for {@link java.util.Collection}/{@link java.util.Map} types to avoid generating
	 * dramatically inefficient generic structures.
	 *
	 * @param <T>  the type.
	 * @param type the runtime class.
	 *
	 * @return a generic handler for {@code type}.
	 *
	 * @throws PersistenceExceptionTypeNotPersistable if {@code type} has problematic fields.
	 */
	public <T> PersistenceTypeHandler<D, T> createTypeHandlerGeneric(Class<T> type) throws PersistenceExceptionTypeNotPersistable;



	/**
	 * Abstract base implementation of {@link PersistenceTypeHandlerCreator} that pre-implements the
	 * dispatch logic common to all data target formats and defers the actual handler construction to
	 * {@code internalCreateTypeHandler*} hooks the concrete subclass implements.
	 *
	 * @param <D> the data target type.
	 */
	public abstract class Abstract<D> implements PersistenceTypeHandlerCreator<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final PersistenceTypeAnalyzer               typeAnalyzer              ;
		final PersistenceTypeResolver               typeResolver              ;
		final PersistenceFieldLengthResolver        lengthResolver            ;
		final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Abstract(
			final PersistenceTypeAnalyzer               typeAnalyzer              ,
			final PersistenceTypeResolver               typeResolver              ,
			final PersistenceFieldLengthResolver        lengthResolver            ,
			final PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator
		)
		{
			super();
			this.typeAnalyzer               = notNull(typeAnalyzer)              ;
			this.typeResolver               = notNull(typeResolver)              ;
			this.lengthResolver             = notNull(lengthResolver)            ;
			this.eagerStoringFieldEvaluator = notNull(eagerStoringFieldEvaluator);
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		public String deriveTypeName(final Class<?> type)
		{
			return this.typeResolver.deriveTypeName(type);
		}
		
		public PersistenceFieldLengthResolver lengthResolver()
		{
			return this.lengthResolver;
		}
		
		public PersistenceEagerStoringFieldEvaluator eagerStoringFieldEvaluator()
		{
			return this.eagerStoringFieldEvaluator;
		}

		@Override
		public <T> PersistenceTypeHandler<D, T> createTypeHandlerArray(final Class<T> type)
			throws PersistenceExceptionTypeNotPersistable
		{
			if(type.getComponentType().isPrimitive())
			{
				throw new PersistenceException(
					"Persisting primitive component type arrays requires a special-tailored "
					+ PersistenceTypeHandler.class.getSimpleName()
					+ " and cannot be done in a generic way."
				);
			}
			
			// array types can never change and therefore can never have obsolete types.
			return this.internalCreateTypeHandlerArray(type);
		}
		
		@Override
		public <T> PersistenceTypeHandler<D, T> createTypeHandlerProxy(final Class<T> type)
			throws PersistenceExceptionTypeNotPersistable
		{
			throw new PersistenceException(
				"Proxy classes (subclasses of " + Proxy.class.getName() + ") are not supported."
			);
		}
		
		@Override
		public <T> PersistenceTypeHandler<D, T> createTypeHandlerLambda(final Class<T> type)
			throws PersistenceExceptionTypeNotPersistable
		{
			throw new PersistenceException(
				"Lambdas are not supported as they cannot be resolved during loading"
				+ " due to insufficient reflection mechanisms provided by the (current) JVM."
			);
		}
		
		@Override
		public <T> PersistenceTypeHandler<D, T> createTypeHandlerEnum(final Class<T> type)
			throws PersistenceExceptionTypeNotPersistable
		{
			return this.internalCreateTypeHandlerEnum(type);
		}
		
		@Override
		public <T> PersistenceTypeHandler<D, T> createTypeHandlerEntity(final Class<T> type)
			throws PersistenceExceptionTypeNotPersistable
		{
			return this.internalCreateTypeHandlerEntity(type);
		}

		@Override
		public <T> PersistenceTypeHandler<D, T> createTypeHandlerAbstract(final Class<T> type)
			throws PersistenceExceptionTypeNotPersistable
		{
			return this.internalCreateTypeHandlerAbstractType(type);
		}

		@Override
		public <T> PersistenceTypeHandler<D, T> createTypeHandlerUnpersistable(final Class<T> type)
		{
			return this.internalCreateTypeHandlerUnpersistable(type);
		}
		
		@Override
		public <T> PersistenceTypeHandler<D, T> createTypeHandlerGeneric(final Class<T> type)
			throws PersistenceExceptionTypeNotPersistable
		{
			// collections need special handling to avoid dramatically inefficient generic structures
			if(XReflect.isJavaUtilCollectionType(type))
			{
				return this.internalCreateTypeHandlerJavaUtilCollection(type);
			}
			
			final HashEnum<Field> persistableFields = HashEnum.New();
			final HashEnum<Field> persisterFields   = HashEnum.New();
			final HashEnum<Field> problematicFields = HashEnum.New();
			this.typeAnalyzer.collectPersistableFieldsEntity(type, persistableFields, persisterFields, problematicFields);
			checkNoProblematicFields(type, problematicFields);

			return this.internalCreateTypeHandlerGeneric(type, persistableFields, persisterFields);
		}
		
		private static void checkNoProblematicFields(final Class<?> type, final XGettingEnum<Field> problematicFields)
		{
			if(problematicFields.isEmpty())
			{
				return;
			}
			
			throw new PersistenceException(
				"Type \"" + type.getName() +
				"\" not persistable due to problematic fields "
				+ problematicFields.toString()
			);
		}
		
		protected <T> PersistenceTypeHandler<D, T> internalCreateTypeHandlerEnum(final Class<T> type)
		{
			final HashEnum<Field> persistableFields = HashEnum.New();
			final HashEnum<Field> persisterFields   = HashEnum.New();
			final HashEnum<Field> problematicFields = HashEnum.New();
			this.typeAnalyzer.collectPersistableFieldsEnum(type, persistableFields, persisterFields, problematicFields);
			checkNoProblematicFields(type, problematicFields);

			return this.internalCreateTypeHandlerEnum(type, persistableFields, persisterFields);
		}
		
		protected <T> PersistenceTypeHandler<D, T> internalCreateTypeHandlerJavaUtilCollection(final Class<T> type)
		{
			final HashEnum<Field> persistableFields = HashEnum.New();
			final HashEnum<Field> persisterFields   = HashEnum.New();
			final HashEnum<Field> problematicFields = HashEnum.New();
			this.typeAnalyzer.collectPersistableFieldsCollection(type, persistableFields, persisterFields, problematicFields);
			
			if(!problematicFields.isEmpty())
			{
				this.internalCreateTypeHandlerGenericJavaUtilCollection(type);
			}

			return this.internalCreateTypeHandlerGeneric(type, persistableFields, persisterFields);
		}
		
		protected abstract <T> PersistenceTypeHandler<D, T> internalCreateTypeHandlerEnum(
			Class<T>            type             ,
			XGettingEnum<Field> persistableFields,
			XGettingEnum<Field> persisterFields
		);
		
		protected abstract <T> PersistenceTypeHandler<D, T> internalCreateTypeHandlerEntity(
			Class<T>            type
		);

		protected abstract <T> PersistenceTypeHandler<D, T> internalCreateTypeHandlerAbstractType(
			Class<T> type
		);
		
		protected abstract <T> PersistenceTypeHandler<D, T> internalCreateTypeHandlerUnpersistable(
			Class<T> type
		);
		
		protected abstract <T> PersistenceTypeHandler<D, T> internalCreateTypeHandlerArray(
			Class<T> type
		);
		
		protected abstract <T> PersistenceTypeHandler<D, T> internalCreateTypeHandlerGeneric(
			Class<T>            type             ,
			XGettingEnum<Field> persistableFields,
			XGettingEnum<Field> persisterFields
		);
		
		protected abstract <T> PersistenceTypeHandler<D, T> internalCreateTypeHandlerGenericStateless(
			Class<T> type
		);
		
		protected abstract <T> PersistenceTypeHandler<D, T> internalCreateTypeHandlerGenericJavaUtilCollection(
			Class<T> type
		);
		
	}
	
}
