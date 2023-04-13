package org.eclipse.serializer.util.xcsv;

/*-
 * #%L
 * Eclipse Serializer Base
 * %%
 * Copyright (C) 2023 Eclipse Foundation
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

import org.eclipse.serializer.chars.StringTable;
import org.eclipse.serializer.collections.LimitList;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.collections.types.XImmutableSequence;
import org.eclipse.serializer.typing.KeyValue;
import org.eclipse.serializer.typing.XTypes;
import org.eclipse.serializer.util.X;


public interface XCsvContent
{
	public String name();

	public XGettingSequence<? extends KeyValue<String, StringTable>> segments();

	public XCsvConfiguration configuration();
	
	
	
	@FunctionalInterface
	public interface Builder<D>
	{
		public XCsvContent build(String name, D data);
	}



	public final class Default implements XCsvContent
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static final Default New(
			final String                                                    name         ,
			final XGettingSequence<? extends KeyValue<String, StringTable>> segments     ,
			final XCsvConfiguration                                          configuration
		)
		{
			return new Default(name, segments, configuration);
		}
		
		public static final Default NewTranslated(
			final String                                  name         ,
			final XGettingSequence<? extends StringTable> segments     ,
			final XCsvConfiguration                        configuration
		)
		{
			final LimitList<KeyValue<String, StringTable>> translated = new LimitList<>(XTypes.to_int(segments.size()));
			for(final StringTable table : segments)
			{
				translated.add(X.KeyValue(table.name(), table));
			}
			return New(name, translated, configuration);
		}
		
		
		
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final String                                                      name         ;
		final XImmutableSequence<? extends KeyValue<String, StringTable>> segments     ;
		final XCsvConfiguration                                            configuration;



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		private Default(
			final String                                                    name         ,
			final XGettingSequence<? extends KeyValue<String, StringTable>> segments     ,
			final XCsvConfiguration                                          configuration
		)
		{
			super();
			this.name          = name             ;
			this.segments      = segments.immure();
			this.configuration = configuration    ;
		}



		////////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final String name()
		{
			return this.name;
		}

		@Override
		public final XGettingSequence<? extends KeyValue<String, StringTable>> segments()
		{
			return this.segments;
		}

		@Override
		public final XCsvConfiguration configuration()
		{
			return this.configuration;
		}

	}

}
