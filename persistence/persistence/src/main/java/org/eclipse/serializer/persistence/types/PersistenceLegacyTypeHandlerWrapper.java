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

import java.util.function.Consumer;

import org.eclipse.serializer.collections.types.XGettingEnum;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTypeNotPersistable;

/**
 * The simplest kind of {@link PersistenceLegacyTypeHandler}: a thin wrapper around a current
 * {@link PersistenceTypeHandler} that forwards every read operation while reporting the <i>legacy</i>
 * type definition through the dictionary-relevant accessors (typeId, members, etc.) inherited from
 * {@link PersistenceLegacyTypeHandler.Abstract}.
 * <p>
 * Used when the legacy and current types are structurally identical and only metadata differs
 * (e.g. a class rename) &mdash; no field translation is needed, the current handler already knows how
 * to read the persisted bytes correctly.
 * <p>
 * <b>Wrapper completeness.</b> Every default method on {@link PersistenceTypeHandler} that is not
 * overridden here would otherwise inherit its parent default and thereby change behavior; the
 * wrapper therefore explicitly forwards <i>every</i> default method (e.g.
 * {@link #membersInDeclaredOrder()}, {@link #storingMembers()}, the viability guards, the enum
 * helpers) to the wrapped handler.
 *
 * @param <D> the data target type.
 * @param <T> the runtime type.
 */
public class PersistenceLegacyTypeHandlerWrapper<D, T> extends PersistenceLegacyTypeHandler.Abstract<D, T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Creates a new {@link PersistenceLegacyTypeHandlerWrapper}.
	 *
	 * @param <D>                  the data target type.
	 * @param <T>                  the runtime type.
	 * @param legacyTypeDefinition the bound legacy type definition.
	 * @param currentTypeHandler   the current handler to forward to.
	 *
	 * @return a new wrapper.
	 */
	public static <D, T> PersistenceLegacyTypeHandler<D, T> New(
		final PersistenceTypeDefinition    legacyTypeDefinition,
		final PersistenceTypeHandler<D, T> currentTypeHandler
	)
	{
		return new PersistenceLegacyTypeHandlerWrapper<>(
			notNull(legacyTypeDefinition),
			notNull(currentTypeHandler)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceTypeHandler<D, T> typeHandler;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	PersistenceLegacyTypeHandlerWrapper(
		final PersistenceTypeDefinition    typeDefinition,
		final PersistenceTypeHandler<D, T> typeHandler
	)
	{
		super(typeDefinition);
		this.typeHandler = typeHandler;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public Class<D> dataType()
	{
		return this.typeHandler.dataType();
	}

	@Override
	public void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
	{
		this.typeHandler.iterateInstanceReferences(instance, iterator);
	}

	@Override
	public void iterateLoadableReferences(final D data, final PersistenceReferenceLoader iterator)
	{
		// current type handler perfectly fits the old types structure, so it can be used here.
		this.typeHandler.iterateLoadableReferences(data, iterator);
	}

	@Override
	public T create(final D data, final PersistenceLoadHandler handler)
	{
		return this.typeHandler.create(data, handler);
	}

	@Override
	public void updateState(final D data, final T instance, final PersistenceLoadHandler handler)
	{
		this.typeHandler.updateState(data, instance, handler);
	}

	@Override
	public void complete(final D data, final T instance, final PersistenceLoadHandler handler)
	{
		this.typeHandler.complete(data, instance, handler);
	}
	
	@Override
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		return this.typeHandler.iterateMemberTypes(logic);
	}
	
	@Override
	public final Class<T> type()
	{
		return this.typeHandler.type();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// default method implementations //
	///////////////////////////////////
	
	/*
	 * Tricky:
	 * Must pass through all default methods to be a correct wrapper.
	 * Otherwise, the wrapper changes the behavior in an unwanted fashion.
	 */
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> membersInDeclaredOrder()
	{
		// Must pass through all default methods to be a correct wrapper.
		return this.typeHandler.membersInDeclaredOrder();
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDescriptionMember> storingMembers()
	{
		return this.typeHandler.storingMembers();
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDescriptionMember> settingMembers()
	{
		return this.typeHandler.settingMembers();
	}
	
	@Override
	public void guaranteeSpecificInstanceViablity() throws PersistenceExceptionTypeNotPersistable
	{
		// Must pass through all default methods to be a correct wrapper.
		this.typeHandler.guaranteeSpecificInstanceViablity();
	}
	
	@Override
	public boolean isSpecificInstanceViable()
	{
		// Must pass through all default methods to be a correct wrapper.
		return this.typeHandler.isSpecificInstanceViable();
	}
	
	@Override
	public void guaranteeSubTypeInstanceViablity() throws PersistenceExceptionTypeNotPersistable
	{
		// Must pass through all default methods to be a correct wrapper.
		this.typeHandler.guaranteeSubTypeInstanceViablity();
	}
	
	@Override
	public boolean isSubTypeInstanceViable()
	{
		// Must pass through all default methods to be a correct wrapper.
		return this.typeHandler.isSubTypeInstanceViable();
	}
	
	@Override
	public Object[] collectEnumConstants()
	{
		// indicate discarding of constants root entry during root resolving
		return null;
	}
	
	@Override
	public int getPersistedEnumOrdinal(final D data)
	{
		// Must pass through all default methods to be a correct wrapper.
		return this.typeHandler.getPersistedEnumOrdinal(data);
	}
	
}
