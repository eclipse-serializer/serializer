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

import org.eclipse.serializer.collections.types.XGettingEnum;

/**
 * Provider that constructs {@link PersistenceTypeDescriptionResolver} instances from a
 * {@link PersistenceTypeResolver}, a {@link PersistenceRefactoringMappingProvider} and the configured
 * identifier-builder lists.
 * <p>
 * This indirection exists because the underlying {@link PersistenceRefactoringMapping} may need to be
 * (re-)materialized lazily &mdash; for example loaded from a refactoring file on first use &mdash; and
 * because some setups want a single resolver instance shared across a foundation while others prefer a
 * fresh resolver per call.
 * <p>
 * Two flavors of provider are offered via the static factories:
 * <ul>
 * <li>{@link #New} &mdash; produces a new resolver on every call to
 *     {@link #provideTypeDescriptionResolver()}, picking up changes to the underlying mapping.</li>
 * <li>{@link #Caching} &mdash; produces a resolver on first call and caches it; subsequent calls return
 *     the same instance until {@link Caching#clear()} is invoked.</li>
 * </ul>
 *
 * @see PersistenceTypeDescriptionResolver
 * @see PersistenceRefactoringMappingProvider
 */
public interface PersistenceTypeDescriptionResolverProvider
{
	/**
	 * Returns a {@link PersistenceTypeDescriptionResolver}. May either build a fresh resolver each call
	 * (default behavior) or return a cached instance, depending on the concrete provider.
	 *
	 * @return a resolver.
	 */
	public PersistenceTypeDescriptionResolver provideTypeDescriptionResolver();



	/**
	 * Creates a non-caching provider that uses the default identifier-builder lists for legacy and current
	 * member identifiers. The simplest entry point for typical setups.
	 *
	 * @param typeResolver               the {@link PersistenceTypeResolver} used by the produced resolver.
	 * @param refactoringMappingProvider the provider of the underlying refactoring mapping.
	 *
	 * @return a new {@link PersistenceTypeDescriptionResolverProvider}.
	 */
	public static PersistenceTypeDescriptionResolverProvider New(
		final PersistenceTypeResolver               typeResolver              ,
		final PersistenceRefactoringMappingProvider refactoringMappingProvider
	)
	{
		return new PersistenceTypeDescriptionResolverProvider.Default(
			notNull(typeResolver),
			notNull(refactoringMappingProvider),
			PersistenceRefactoringTypeIdentifierBuilder.createDefaultRefactoringLegacyTypeIdentifierBuilders(),
			PersistenceRefactoringMemberIdentifierBuilder.createDefaultRefactoringLegacyMemberIdentifierBuilders(),
			PersistenceRefactoringMemberIdentifierBuilder.createDefaultRefactoringCurrentMemberIdentifierBuilders()
		);
	}
	
	/**
	 * Creates a non-caching provider with custom identifier-builder lists. Use the simpler
	 * {@link #New(PersistenceTypeResolver, PersistenceRefactoringMappingProvider)} unless you need to
	 * override the default identifier construction.
	 *
	 * @param typeResolver                   the type resolver.
	 * @param refactoringMappingProvider     the refactoring mapping provider.
	 * @param sourceTypeIdentifierBuilders   source-type identifier builders.
	 * @param sourceMemberIdentifierBuilders source-member identifier builders.
	 * @param targetMemberIdentifierBuilders target-member identifier builders.
	 *
	 * @return a new {@link PersistenceTypeDescriptionResolverProvider}.
	 */
	public static PersistenceTypeDescriptionResolverProvider New(
		final PersistenceTypeResolver                                               typeResolver                  ,
		final PersistenceRefactoringMappingProvider                                 refactoringMappingProvider    ,
		final XGettingEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ,
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders,
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders
	)
	{
		return new PersistenceTypeDescriptionResolverProvider.Default(
			notNull(typeResolver)                  ,
			notNull(refactoringMappingProvider)    ,
			notNull(sourceTypeIdentifierBuilders)  ,
			notNull(sourceMemberIdentifierBuilders),
			notNull(targetMemberIdentifierBuilders)
		);
	}

	/**
	 * Creates a caching provider with the default identifier-builder lists. The first call to
	 * {@link #provideTypeDescriptionResolver()} materializes the resolver; subsequent calls return the
	 * cached instance until {@link Caching#clear()} is invoked.
	 *
	 * @param typeResolver               the type resolver.
	 * @param refactoringMappingProvider the refactoring mapping provider.
	 *
	 * @return a new caching {@link PersistenceTypeDescriptionResolverProvider}.
	 */
	public static PersistenceTypeDescriptionResolverProvider Caching(
		final PersistenceTypeResolver               typeResolver              ,
		final PersistenceRefactoringMappingProvider refactoringMappingProvider
	)
	{
		return new PersistenceTypeDescriptionResolverProvider.Caching(
			notNull(typeResolver),
			notNull(refactoringMappingProvider),
			PersistenceRefactoringTypeIdentifierBuilder.createDefaultRefactoringLegacyTypeIdentifierBuilders(),
			PersistenceRefactoringMemberIdentifierBuilder.createDefaultRefactoringLegacyMemberIdentifierBuilders(),
			PersistenceRefactoringMemberIdentifierBuilder.createDefaultRefactoringCurrentMemberIdentifierBuilders()
		);
	}
	
	/**
	 * Creates a caching provider with custom identifier-builder lists.
	 *
	 * @param typeResolver                   the type resolver.
	 * @param refactoringMappingProvider     the refactoring mapping provider.
	 * @param sourceTypeIdentifierBuilders   source-type identifier builders.
	 * @param sourceMemberIdentifierBuilders source-member identifier builders.
	 * @param targetMemberIdentifierBuilders target-member identifier builders.
	 *
	 * @return a new caching {@link PersistenceTypeDescriptionResolverProvider}.
	 */
	public static PersistenceTypeDescriptionResolverProvider Caching(
		final PersistenceTypeResolver                                               typeResolver                  ,
		final PersistenceRefactoringMappingProvider                                 refactoringMappingProvider    ,
		final XGettingEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ,
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders,
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders
	)
	{
		return new PersistenceTypeDescriptionResolverProvider.Caching(
			notNull(typeResolver)                  ,
			notNull(refactoringMappingProvider)    ,
			notNull(sourceTypeIdentifierBuilders)  ,
			notNull(sourceMemberIdentifierBuilders),
			notNull(targetMemberIdentifierBuilders)
		);
	}
	
	public class Default implements PersistenceTypeDescriptionResolverProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final PersistenceTypeResolver                                               typeResolver                  ;
		final PersistenceRefactoringMappingProvider                                 refactoringMappingProvider    ;
		final XGettingEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ;
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders;
		final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders;
		
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default(
			final PersistenceTypeResolver                                               typeResolver                  ,
			final PersistenceRefactoringMappingProvider                                 refactoringMappingProvider    ,
			final XGettingEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ,
			final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders,
			final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders
		)
		{
			super();
			this.typeResolver                   = typeResolver                  ;
			this.refactoringMappingProvider     = refactoringMappingProvider    ;
			this.sourceTypeIdentifierBuilders   = sourceTypeIdentifierBuilders  ;
			this.sourceMemberIdentifierBuilders = sourceMemberIdentifierBuilders;
			this.targetMemberIdentifierBuilders = targetMemberIdentifierBuilders;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public PersistenceTypeDescriptionResolver provideTypeDescriptionResolver()
		{
			// nifty: immure at creation time, not before.
			return new PersistenceTypeDescriptionResolver.Default(
				this.typeResolver                                          ,
				this.refactoringMappingProvider.provideRefactoringMapping(),
				this.sourceTypeIdentifierBuilders  .immure(),
				this.sourceMemberIdentifierBuilders.immure(),
				this.targetMemberIdentifierBuilders.immure()
			);
		}
		
	}
	
	public class Caching extends Default implements org.eclipse.serializer.typing.Caching
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		transient PersistenceTypeDescriptionResolver cachedResolver;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Caching(
			final PersistenceTypeResolver                                               typeResolver                  ,
			final PersistenceRefactoringMappingProvider                                 refactoringMappingProvider    ,
			final XGettingEnum<? extends PersistenceRefactoringTypeIdentifierBuilder>   sourceTypeIdentifierBuilders  ,
			final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> sourceMemberIdentifierBuilders,
			final XGettingEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> targetMemberIdentifierBuilders
		)
		{
			super(
				typeResolver                  ,
				refactoringMappingProvider    ,
				sourceTypeIdentifierBuilders  ,
				sourceMemberIdentifierBuilders,
				targetMemberIdentifierBuilders
			);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public synchronized PersistenceTypeDescriptionResolver provideTypeDescriptionResolver()
		{
			if(this.cachedResolver == null)
			{
				this.cachedResolver = super.provideTypeDescriptionResolver();
			}
			
			return this.cachedResolver;
		}

		@Override
		public synchronized boolean hasFilledCache()
		{
			return this.cachedResolver != null;
		}

		@Override
		public synchronized boolean ensureFilledCache()
		{
			if(this.hasFilledCache())
			{
				return false;
			}
			
			this.provideTypeDescriptionResolver();
			
			return true;
		}

		@Override
		public synchronized void clear()
		{
			this.cachedResolver = null;
		}
	}
	
}
