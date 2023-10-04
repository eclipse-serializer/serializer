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

import java.util.function.BiConsumer;

public interface XCsvRowAssembler<R> extends BiConsumer<R, XCsvAssembler>
{
	static void addNonNullDelimited(final CharSequence s, final XCsvAssembler assembler)
	{
		if(s == null)
		{
			return;
		}
		assembler.addRowValueDelimited(s);
	}

	static void addNonNullSimple(final CharSequence s, final XCsvAssembler assembler)
	{
		if(s == null)
		{
			return;
		}
		assembler.addRowValueSimple(s);
	}



	@Override
	void accept(R row, XCsvAssembler rowAssembler);

}
