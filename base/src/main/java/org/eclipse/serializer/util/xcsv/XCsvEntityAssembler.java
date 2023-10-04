package org.eclipse.serializer.util.xcsv;

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

import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.collections.ConstList;
import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.collections.types.XImmutableSequence;
import org.eclipse.serializer.typing.XTypes;


public interface XCsvEntityAssembler<T> extends XCsvRowAssembler<T>
{
	XImmutableSequence<String> columnHeader();

	VarString createCollector(final int entityCount);

	default VarString assemble(final XGettingCollection<T> entities)
	{
		return this.assembleInto(this.createCollector(XTypes.to_int(entities.size())), this.columnHeader(), entities);
	}

	VarString assembleInto(
		VarString vs,
		XGettingSequence<String> columnHeader,
		XGettingCollection<T> entities
	);

	default VarString assembleInto(final VarString vs, final XGettingCollection<T> entities)
	{
		this.assembleInto(vs, this.columnHeader(), entities);
		return vs;
	}

	abstract class Abstract<T> implements XCsvEntityAssembler<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		private static final int DEFAULT_ROW_COUNT_ESTIAMTE = 100;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final XImmutableSequence<String> columnHeader        ;
		private final int                        rowCharCountEstimate;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(final XGettingSequence<String> columnHeader)
		{
			this(columnHeader, DEFAULT_ROW_COUNT_ESTIAMTE);
		}

		protected Abstract(final XGettingSequence<String> columnHeader, final int rowCharCountEstimate)
		{
			super();
			this.columnHeader         = columnHeader.immure();
			this.rowCharCountEstimate = rowCharCountEstimate ;
		}

		protected Abstract(final String... columnHeader)
		{
			this(ConstList.New(columnHeader));
		}

		protected Abstract(final int rowCharCountEstimate, final String... columnHeader)
		{
			this(ConstList.New(columnHeader), rowCharCountEstimate);
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		@Override
		public final XImmutableSequence<String> columnHeader()
		{
			return this.columnHeader;
		}

		@Override
		public VarString createCollector(final int entityCount)
		{
			return VarString.New(entityCount * this.rowCharCountEstimate);
		}

		@Override
		public abstract VarString assembleInto(
			VarString                vs          ,
			XGettingSequence<String> columnHeader,
			XGettingCollection<T>    entities
		);

	}

}
