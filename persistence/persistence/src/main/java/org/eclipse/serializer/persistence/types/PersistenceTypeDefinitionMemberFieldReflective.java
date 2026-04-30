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

import static org.eclipse.serializer.util.X.mayNull;
import static org.eclipse.serializer.util.X.notNull;
import static org.eclipse.serializer.math.XMath.positive;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import org.eclipse.serializer.collections.types.XGettingCollection;

/**
 * Runtime-bound counterpart to {@link PersistenceTypeDescriptionMemberFieldReflective}. Adds the
 * resolved runtime declaring-class name, declaring {@link Class}, and the underlying {@link Field} to
 * the dictionary attributes inherited from the description.
 * <p>
 * Any of {@link #runtimeDeclaringClassName()}, {@link #declaringClass()}, {@link #field()} and
 * {@link #type()} can be {@code null} when the dictionary entry no longer matches the runtime &mdash;
 * for example when the declaring class has been removed or renamed and the new declaring-class name
 * conflicts with an unrelated current class. In that case the legacy member is kept for serialization
 * purposes (length range etc.) but cannot be reflectively bound.
 */
public interface PersistenceTypeDefinitionMemberFieldReflective
extends PersistenceTypeDefinitionMemberField, PersistenceTypeDescriptionMemberFieldReflective
{
	/**
	 * The current runtime name of the class this field is declared on, after refactoring renames.
	 * May differ from the dictionary {@link #declaringTypeName()}, which preserves the original
	 * declaring-class name. May be {@code null} if no runtime equivalent exists.
	 *
	 * @return the runtime declaring-class name, or {@code null}.
	 */
	public String runtimeDeclaringClassName();

	@Override
	public default String runtimeQualifier()
	{
		return this.runtimeDeclaringClassName();
	}

	/**
	 * The runtime declaring {@link Class} of this field, or {@code null} if the field cannot be
	 * resolved on the current runtime.
	 *
	 * @return the declaring class, or {@code null}.
	 */
	public Class<?> declaringClass();

	/**
	 * The underlying Java {@link Field} bound to this member, or {@code null} if it could not be
	 * resolved (declaring class missing, field name no longer present, or declaring-class identity
	 * mismatch).
	 *
	 * @return the underlying {@link Field}, or {@code null}.
	 */
	@Override
	public Field field();



	/**
	 * Iterates the passed members and forwards each non-{@code null} {@link #field()} to the collector.
	 * Useful for handing a list of reflective members to a Field-consuming API in one step.
	 *
	 * @param <C>       the collector type.
	 * @param members   the members to unbox.
	 * @param collector the collector receiving each member's underlying field.
	 *
	 * @return the same collector, for fluent chaining.
	 */
	public static <C extends Consumer<? super Field>> C unbox(
		final XGettingCollection<? extends PersistenceTypeDefinitionMemberFieldReflective> members,
		final C collector
	)
	{
		for(final PersistenceTypeDefinitionMemberFieldReflective member : members)
		{
			collector.accept(member.field());
		}
		
		return collector;
	}
	
		
	/**
	 * Full-control factory for a reflective field definition. Use the {@link Field}-only overload
	 * {@link #New(Field, long, long)} for the common case where dictionary and runtime names match.
	 *
	 * @param runtimeDeclaringClass   the runtime declaring-class name; may be {@code null}.
	 * @param declaringClass          the runtime declaring class; may be {@code null}.
	 * @param field                   the runtime {@link Field}; may be {@code null}.
	 * @param type                    the runtime field type; may be {@code null}.
	 * @param typeName                the dictionary type name; must not be {@code null}.
	 * @param name                    the simple field name; must not be {@code null}.
	 * @param declaringTypeName       the dictionary declaring-type name; must not be {@code null}.
	 * @param isReference             whether the field is a reference (otherwise primitive).
	 * @param persistentMinimumLength the persistent length lower bound; must be positive.
	 * @param persistentMaximumLength the persistent length upper bound; must be positive.
	 *
	 * @return a new reflective field definition.
	 */
	public static PersistenceTypeDefinitionMemberFieldReflective New(
		final String   runtimeDeclaringClass  ,
		final Class<?> declaringClass         ,
		final Field    field                  ,
		final Class<?> type                   ,
		final String   typeName               ,
		final String   name                   ,
		final String   declaringTypeName      ,
		final boolean  isReference            ,
		final long     persistentMinimumLength,
		final long     persistentMaximumLength
	)
	{
		return new PersistenceTypeDefinitionMemberFieldReflective.Default(
			 mayNull(runtimeDeclaringClass)  ,
			 mayNull(declaringClass)         ,
			 mayNull(field)                  ,
			 mayNull(type)                   ,
			 notNull(typeName)               ,
			 notNull(name)                   ,
			 notNull(declaringTypeName)      ,
			         isReference             ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}

	/**
	 * Convenience factory that derives every name and class binding from the passed {@link Field}: the
	 * dictionary {@code typeName} and {@code declaringTypeName} are taken from {@code field}'s runtime
	 * type and declaring class, and {@code isReference} is inferred from
	 * {@link Class#isPrimitive() field.getType().isPrimitive()}.
	 *
	 * @param field                   the runtime field.
	 * @param persistentMinimumLength the persistent length lower bound; must be positive.
	 * @param persistentMaximumLength the persistent length upper bound; must be positive.
	 *
	 * @return a new reflective field definition.
	 */
	public static PersistenceTypeDefinitionMemberFieldReflective New(
		final Field field                  ,
		final long  persistentMinimumLength,
		final long  persistentMaximumLength
	)
	{
		return New(
			field.getDeclaringClass().getName(),
			field.getDeclaringClass()          ,
			field                              ,
			field.getType()                    ,
			field.getType().getName()          ,
			field.getName()                    ,
			field.getDeclaringClass().getName(),
			!field.getType().isPrimitive()     ,
			positive(persistentMinimumLength)  ,
			positive(persistentMaximumLength)
		);
	}

	public final class Default
	extends PersistenceTypeDescriptionMemberFieldReflective.Default
	implements PersistenceTypeDefinitionMemberFieldReflective
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		// owner type must be initialized effectively final to prevent circular constructor dependencies
		private final Class<?> type                     ;
		private final String   runtimeDeclaringClassName;
		private final Class<?> declaringClass           ;
		private final Field    field                    ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final String   runtimeDeclClassName,
			final Class<?> declaringClass      ,
			final Field    field               ,
			final Class<?> type                ,
			final String   typeName            ,
			final String   name                ,
			final String   declaringTypeName   ,
			final boolean  isReference         ,
			final long     persistentMinLength ,
			final long     persistentMaxLength
		)
		{
			super(
				typeName           ,
				declaringTypeName  ,
				name               ,
				isReference        ,
				persistentMinLength,
				persistentMaxLength
			);

			this.runtimeDeclaringClassName = runtimeDeclClassName;
			this.declaringClass            = declaringClass      ;
			this.field                     = field               ;
			this.type                      = type                ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public String runtimeDeclaringClassName()
		{
			return this.runtimeDeclaringClassName;
		}
				
		@Override
		public final Class<?> declaringClass()
		{
			return this.declaringClass;
		}
		
		@Override
		public final Field field()
		{
			return this.field;
		}
		
		@Override
		public final Class<?> type()
		{
			return this.type;
		}

	}

}
