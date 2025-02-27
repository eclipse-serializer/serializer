package org.eclipse.serializer.chars;

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

import java.util.function.Consumer;

public interface StringTableProcessor<T>
{
	<C extends Consumer<? super T>> C processStringTable(StringTable sourceData, C collector);



	abstract class Abstract<T> implements StringTableProcessor<T>
	{
		protected abstract void validateColumnNames(StringTable sourceData);

		protected abstract T parseRow(String[] dataRow);

		@Override
		public final <C extends Consumer<? super T>> C processStringTable(
			final StringTable sourceData,
			final C           collector
		)
		{
			this.validateColumnNames(sourceData);
			sourceData.rows().iterate(new Consumer<String[]>()
			{
				@Override
				public void accept(final String[] dataRow)
				{
					collector.accept(Abstract.this.parseRow(dataRow));
				}
			});
			return collector;
		}
	}
}
