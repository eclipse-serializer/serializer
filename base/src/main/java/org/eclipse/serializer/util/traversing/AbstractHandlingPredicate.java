package org.eclipse.serializer.util.traversing;

/*-
 * #%L
 * Eclipse Serializer Base
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

import java.util.function.Predicate;

import org.eclipse.serializer.collections.HashEnum;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.collections.types.XGettingSet;

public abstract class AbstractHandlingPredicate implements Predicate<Object>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Predicate<Object>          customPredicate ;
	private final HashEnum<Class<?>>         positiveTypes   ;
	private final HashEnum<Class<?>>         negativeTypes   ;
	private final XGettingSequence<Class<?>> typesPolymorphic;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected AbstractHandlingPredicate(
		final Predicate<Object>          customPredicate ,
		final XGettingSet<Class<?>>      positiveTypes   ,
		final XGettingSequence<Class<?>> typesPolymorphic
	)
	{
		super();
		this.customPredicate  = customPredicate ;
		this.positiveTypes    = HashEnum.New(positiveTypes);
		this.negativeTypes    = HashEnum.New()  ;
		this.typesPolymorphic = typesPolymorphic;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final boolean test(final Object instance)
	{
		if(this.customPredicate != null && this.customPredicate.test(instance))
		{
			return true;
		}
		
		final Class<?> type = instance.getClass();
		if(this.positiveTypes.contains(type))
		{
			return true;
		}
		if(this.negativeTypes.contains(type))
		{
			return false;
		}
		for(final Class<?> tp : this.typesPolymorphic)
		{
			if(tp.isAssignableFrom(type))
			{
				this.positiveTypes.add(type);
				return true;
			}
		}
		this.negativeTypes.add(type);
		return false;
	}
	
}
