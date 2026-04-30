package org.eclipse.serializer.persistence.types;

import org.eclipse.serializer.chars.XChars;

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

import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.persistence.exceptions.PersistenceExceptionTypeConsistencyEnum;
import org.eclipse.serializer.reflect.XReflect;
import org.eclipse.serializer.util.logging.Logging;
import org.eclipse.serializer.util.similarity.Similarity;
import org.slf4j.Logger;

/**
 * Factory that synthesizes a {@link PersistenceLegacyTypeHandler} from a finalized
 * {@link PersistenceLegacyTypeMappingResult}.
 * <p>
 * The {@link Abstract} base implements a fixed dispatch:
 * <ol>
 * <li>If the mapping result represents an unchanged instance structure (only renamings, e.g. of
 *     fields or the type itself), the current handler is wrapped via
 *     {@link PersistenceLegacyTypeHandlerWrapper} (or {@link PersistenceLegacyTypeHandlerWrapperEnum}
 *     for enums whose constants got reordered).</li>
 * <li>If the current handler is reflective, the data-format-specific subclass synthesizes a
 *     reflective translating handler via {@code deriveReflectiveHandler}.</li>
 * <li>Otherwise the subclass synthesizes a custom-wrapping handler via
 *     {@code deriveCustomWrappingHandler}.</li>
 * </ol>
 * <p>
 * <b>Enum-ordinal mapping.</b> When enum constants get reordered (or some get deleted), this class's
 * static {@link Abstract#deriveEnumOrdinalMapping(PersistenceLegacyTypeMappingResult)} computes the
 * legacy-ordinal-to-current-ordinal table used by {@link PersistenceLegacyTypeHandlerWrapperEnum} to
 * remap persisted ordinals at load time. Non-explicit ordinal changes throw
 * {@link PersistenceExceptionTypeConsistencyEnum} &mdash; only explicit refactoring entries can change
 * an ordinal.
 *
 * @param <D> the data target type.
 *
 * @see PersistenceLegacyTypeHandler
 * @see PersistenceLegacyTypeMappingResult
 */
public interface PersistenceLegacyTypeHandlerCreator<D>
{
	/**
	 * Synthesizes a legacy handler for the passed mapping result.
	 *
	 * @param <T>           the runtime type.
	 * @param mappingResult the finalized mapping result.
	 *
	 * @return a new legacy type handler.
	 */
	public <T> PersistenceLegacyTypeHandler<D, T> createLegacyTypeHandler(
		PersistenceLegacyTypeMappingResult<D, T> mappingResult
	);



	/**
	 * Abstract base implementing the unchanged-structure / reflective / custom dispatch and the static
	 * enum-ordinal mapping derivation. Concrete subclasses (e.g. binary) implement the
	 * {@code deriveReflectiveHandler} and {@code deriveCustomWrappingHandler} hooks.
	 *
	 * @param <D> the data target type.
	 */
	public abstract class Abstract<D> implements PersistenceLegacyTypeHandlerCreator<D>
	{
		private final static Logger logger = Logging.getLogger(PersistenceLegacyTypeHandlerCreator.class);

		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		/**
		 * Builds the legacy-ordinal-to-current-ordinal map used by
		 * {@link PersistenceLegacyTypeHandlerWrapperEnum} from the enum-constant entries in the legacy
		 * and current type definitions.
		 * <p>
		 * For each legacy constant the mapping result is consulted: an explicit / similar match
		 * yields the current ordinal; an explicitly discarded legacy constant produces a {@code null}
		 * entry (signalling deletion); an unmapped legacy constant or an inconsistent target throws.
		 * Implicit (non-explicit) ordinal changes are also rejected via
		 * {@link PersistenceExceptionTypeConsistencyEnum}, since silently shifting ordinals would
		 * corrupt persisted data.
		 *
		 * @param result the mapping result.
		 *
		 * @return an array indexed by legacy ordinal, valued by mapped current ordinal (or
		 *         {@code null} for deleted constants).
		 *
		 * @throws PersistenceException                  if a legacy constant is unmapped or maps to an
		 *                                               unknown target.
		 * @throws PersistenceExceptionTypeConsistencyEnum if an ordinal change was not explicitly
		 *                                               authorized via the refactoring mapping.
		 */
		public static Integer[] deriveEnumOrdinalMapping(final PersistenceLegacyTypeMappingResult<?, ?> result)
		{
			final PersistenceTypeDefinition legacyTypeDef = result.legacyTypeDefinition();
			final BulkList<PersistenceTypeDefinitionMember> legacyConstantMembers = legacyTypeDef.allMembers()
				.filterTo(BulkList.New(), PersistenceTypeDefinitionMember::isEnumConstant)
			;
			
			final PersistenceTypeDefinition currentTypeDef = result.currentTypeHandler();
			final BulkList<PersistenceTypeDefinitionMember> currentConstantMembers = currentTypeDef.allMembers()
				.filterTo(BulkList.New(), PersistenceTypeDefinitionMember::isEnumConstant)
			;
			
			final Integer[] ordinalMap = new Integer[legacyConstantMembers.intSize()];
			
			int ordinal = 0;
			for(final PersistenceTypeDefinitionMember legacyMember : legacyConstantMembers)
			{
				final Similarity<PersistenceTypeDefinitionMember> match =
					result.legacyToCurrentMembers().get(legacyMember)
				;
				if(match == null)
				{
					if(result.discardedLegacyMembers().contains(legacyMember))
					{
						ordinalMap[ordinal] = null;
					}
					else
					{
						throw new PersistenceException(
							"Unmapped legacy enum constant: " + legacyTypeDef + "#" + legacyMember.name()
						);
					}
				}
				else
				{
					final PersistenceTypeDefinitionMember targetCurrentConstant = match.targetElement();
					final long targetOrdinal = currentConstantMembers.indexOf(targetCurrentConstant);
					
					if(targetOrdinal >= 0)
					{
						//allow ordinal changes only by explicit manual mappings
						if(targetOrdinal != ordinal)
						{
							if(match.similarity() != PersistenceLegacyTypeMapper.Defaults.defaultExplicitMappingSimilarity())
							{
								throw new PersistenceExceptionTypeConsistencyEnum(
									targetCurrentConstant.identifier(),
									result.currentTypeHandler().typeName(),
									ordinal,
									targetOrdinal
								);
							}
						}
						
						ordinalMap[ordinal] = Integer.valueOf((int)targetOrdinal);
					}
					else
					{
						throw new PersistenceException(
							"Inconsistent target enum constant: " + currentTypeDef + "#" + targetCurrentConstant.name()
						);
					}
				}
				ordinal++;
			}
			
			return ordinalMap;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public <T> PersistenceLegacyTypeHandler<D, T> createLegacyTypeHandler(
			final PersistenceLegacyTypeMappingResult<D, T> result
		)
		{
			if(PersistenceLegacyTypeMappingResult.isUnchangedInstanceStructure(result))
			{
				/*
				 * special case: structure didn't change, only namings, so the current type handler can be used.
				 * Note that this applies to custom handlers, too. Even ones with variable length instances.
				 */
				return this.createTypeHandlerUnchangedInstanceStructure(result);
			}
			
			if(result.currentTypeHandler() instanceof PersistenceTypeHandlerReflective<?, ?>)
			{
				final PersistenceLegacyTypeHandler<D, T> reflectiveHandler = this.deriveReflectiveHandler(
					result,
					(PersistenceTypeHandlerReflective<D, T>)result.currentTypeHandler()
				);
				
				this.logHandlerCreation("reflective", reflectiveHandler);
				
				return reflectiveHandler;
			}

			final PersistenceLegacyTypeHandler<D, T> customWrappingHandler = this.deriveCustomWrappingHandler(result);
			
			this.logHandlerCreation("custom wrapping", customWrappingHandler);
			
			return customWrappingHandler;
		}
		
		private void logHandlerCreation(final String handlerType, final PersistenceLegacyTypeHandler<?, ?> handler)
		{
			logger.debug(
				"Create {} legacy type handler for {}: {}",
				handlerType,
				handler.type().getName(),
				XChars.systemString(handler)
			);
		}
		
		protected <T> PersistenceLegacyTypeHandler<D, T> createTypeHandlerUnchangedInstanceStructure(
			final PersistenceLegacyTypeMappingResult<D, T> result
		)
		{
			if(XReflect.isEnum(result.currentTypeHandler().type()))
			{
				return this.createTypeHandlerUnchangedInstanceStructureGenericEnum(result);
			}

			return this.createTypeHandlerUnchangedInstanceStructureGenericType(result);
		}
				
		protected <T> PersistenceLegacyTypeHandler<D, T> createTypeHandlerUnchangedInstanceStructureGenericEnum(
			final PersistenceLegacyTypeMappingResult<D, T> result
		)
		{
			return this.createTypeHandlerEnumWrapping(result, result.currentTypeHandler());
		}
		
		protected <T> PersistenceLegacyTypeHandler<D, T> createTypeHandlerEnumWrapping(
			final PersistenceLegacyTypeMappingResult<D, T> result     ,
			final PersistenceTypeHandler<D, T>             typeHandler
		)
		{
			if(PersistenceLegacyTypeMappingResult.isUnchangedStaticStructure(result))
			{
				// current enum type handler is generically wrapped
				return PersistenceLegacyTypeHandlerWrapper.New(
					result.legacyTypeDefinition(),
					typeHandler
				);
			}
			
			final Integer[] ordinalMapping = deriveEnumOrdinalMapping(result);
			
			return PersistenceLegacyTypeHandlerWrapperEnum.New(
				result.legacyTypeDefinition(),
				typeHandler,
				ordinalMapping
			);
		}
				
		protected <T> PersistenceLegacyTypeHandler<D, T> createTypeHandlerUnchangedInstanceStructureGenericType(
			final PersistenceLegacyTypeMappingResult<D, T> result
		)
		{
			return PersistenceLegacyTypeHandlerWrapper.New(
				result.legacyTypeDefinition(),
				result.currentTypeHandler()
			);
		}
							
		/**
		 * Subclass hook: synthesize a legacy handler for a non-reflective ("custom") current handler.
		 *
		 * @param <T>           the runtime type.
		 * @param mappingResult the finalized mapping result.
		 *
		 * @return the legacy handler.
		 */
		protected abstract <T> PersistenceLegacyTypeHandler<D, T> deriveCustomWrappingHandler(
			PersistenceLegacyTypeMappingResult<D, T> mappingResult
		);

		/**
		 * Subclass hook: synthesize a translating legacy handler around a reflective current handler.
		 *
		 * @param <T>                the runtime type.
		 * @param mappingResult      the finalized mapping result.
		 * @param currentTypeHandler the reflective current handler.
		 *
		 * @return the legacy handler.
		 */
		protected abstract <T> PersistenceLegacyTypeHandler<D, T> deriveReflectiveHandler(
			PersistenceLegacyTypeMappingResult<D, T> mappingResult,
			PersistenceTypeHandlerReflective<D, T>   currentTypeHandler
		);
	}
	
}
