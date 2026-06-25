package test.eclipse.serializer.sampler.layeredentities;

/*-
 * #%L
 * Eclipse Serializer Integration Tests
 * %%
 * Copyright (C) 2023 - 2026 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */


import org.eclipse.serializer.entity.Entity;
import org.eclipse.serializer.entity.EntityVersionCleaner;
import org.eclipse.serializer.entity.EntityVersionContext;

import test.eclipse.serializer.sampler.layeredentities._Address.AddressCreator;
import test.eclipse.serializer.sampler.layeredentities._Animal.AnimalCreator;
import test.eclipse.serializer.sampler.layeredentities._Human.HumanCreator;
import test.eclipse.serializer.sampler.layeredentities._Pet.PetCreator;


public final class EntityFactory
{
	final static JulLogger                     logger  = new JulLogger();
	final static EntityVersionCleaner<Integer> cleaner = EntityVersionCleaner.AmountPreserving(10);
	
	public static AddressCreator AddressCreator()
	{
		return addLayers(AddressCreator.New());
	}
	
	public static AnimalCreator AnimalCreator()
	{
		return addLayers(AnimalCreator.New());
	}
	
	public static HumanCreator HumanCreator()
	{
		return addLayers(HumanCreator.New());
	}
	
	public static PetCreator PetCreator()
	{
		return addLayers(PetCreator.New());
	}
	
	private static <E extends Entity, C extends Entity.Creator<E, C>> C addLayers(final C creator)
	{
		return creator
			.addLayer(logger)
			.addLayer(EntityVersionContext.AutoIncrementingInt(cleaner))
		;
	}
	
	private EntityFactory()
	{
	}
}
