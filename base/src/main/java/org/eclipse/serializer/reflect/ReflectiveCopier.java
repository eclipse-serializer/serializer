package org.eclipse.serializer.reflect;

/*-
 * #%L
 * Eclipse Serializer Base
 * %%
 * Copyright (C) 2023 MicroStream Software
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

import org.eclipse.serializer.collections.XArrays;
import org.eclipse.serializer.functional.XFunc;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Predicate;


/**
 * 
 * 
 *
 * @param <S> Does not necessarily have to be a class.
 */
public interface ReflectiveCopier<S>
{
	default <T extends S> T copyTo(final T targetInstance)
	{
		return this.copy(this.sourceInstance(), targetInstance);
	}
	
	<T extends S> T copy(S sourceInstance, T targetInstance);
	
	/**
	 * Does not necessarily have to be S. S could be an interface.
	 * @return the source class
	 */
	Class<?> sourceClass();
	
	S sourceInstance();
	
	Predicate<? super Field> fieldSelector();
	
	CopyPredicate copySelector();
	
	<I extends Consumer<? super Field>> I iterateFields(I iterator);
	
	
	
	static <S> ReflectiveCopier<S> New(final S sourceInstance)
	{
		return New(XReflect.getClass(sourceInstance), sourceInstance);
	}
	
	static <S, C extends S> ReflectiveCopier<S> New(final Class<C> sourceClass)
	{
		return New(sourceClass, null);
	}
	
	static <S, C extends S> ReflectiveCopier<S> New(
		final Class<C> sourceClass,
		final S sourceInstance
	)
	{
		return New(sourceClass, sourceInstance, XFunc.all());
	}
	
	static <S, C extends S> ReflectiveCopier<S> New(
		final Class<C> sourceClass,
		final S sourceInstance,
		final Predicate<? super Field> fieldSelector
	)
	{
		return New(sourceClass, sourceInstance, fieldSelector, CopyPredicate::all);
	}
	
	static <S, C extends S> ReflectiveCopier<S> New(
		final Class<C> sourceClass,
		final S sourceInstance,
		final Predicate<? super Field> fieldSelector,
		final CopyPredicate copySelector
	)
	{
		final Field[] copyFields = XReflect.collectInstanceFields(sourceClass, fieldSelector);
		
		return new Default<>(
			sourceClass,
			sourceInstance,
			fieldSelector,
			copyFields,
			copySelector
		);
	}
	
	final class Default<S> implements ReflectiveCopier<S>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Class<?>                 sourceClass   ;
		private final S                        sourceInstance;
		private final Predicate<? super Field> fieldSelector ;
		private final Field[]                  fields        ;
		private final CopyPredicate            copySelector  ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final Class<?>                 sourceClass   ,
			final S                        sourceInstance,
			final Predicate<? super Field> fieldSelector ,
			final Field[]                  fields        ,
			final CopyPredicate            copySelector
		)
		{
			super();
			this.sourceClass    = sourceClass   ;
			this.sourceInstance = sourceInstance;
			this.fieldSelector  = fieldSelector ;
			this.fields         = fields        ;
			this.copySelector   = copySelector  ;
		}
		

		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public Class<?> sourceClass()
		{
			return this.sourceClass;
		}
		
		@Override
		public S sourceInstance()
		{
			return this.sourceInstance;
		}

		@Override
		public Predicate<? super Field> fieldSelector()
		{
			return this.fieldSelector;
		}

		@Override
		public CopyPredicate copySelector()
		{
			return this.copySelector;
		}

		@Override
		public <I extends Consumer<? super Field>> I iterateFields(final I iterator)
		{
			return XArrays.iterate(this.fields, iterator);
		}

		@Override
		public <T extends S> T copy(final S sourceInstance, final T targetInstance)
		{
			return XReflect.copyFields(sourceInstance, targetInstance, this.fields, this.copySelector);
		}
		
	}
	
}
