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

import java.nio.file.Path;
import java.util.Arrays;

import org.eclipse.serializer.chars.EscapeHandler;
import org.eclipse.serializer.chars.StringTable;
import org.eclipse.serializer.chars.XChars;
import org.eclipse.serializer.chars._charArrayRange;
import org.eclipse.serializer.exceptions.XCsvException;
import org.eclipse.serializer.io.XIO;

/**
 * An extended CSV format ("XCSV") with the following traits:
 * <ul>
 * <li>Allows an arbitrary separator value (interpreting "CSV" as "character separated values" instead of
 * "comma separated values"), with a TAB ascii character as the default separator since that character
 * has been designed exactly for that purpose and is superior to any other character for that task.</li>
 * <li>Optionally contains a header line defining all control characters</li>
 * <li>Contains an optional second header line defining/hinting the data type of the column</li>
 * <li>Allows single line and multi line comments</li>
 * <li>Allows multiple tables of different structure ("segments") in one file</li>
 * </ul>
 * In short: this is the ultimate textual data format for tabular data regarding efficiency and readability.
 */
public final class XCSV
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final char          DEFAULT_LINE_SEPERATOR              = '\n';
	// the most reasonable control character for anyone who actually understands how it really works
	static final char          DEFAULT_SEPERATOR                   = '\t';
	static final char          DEFAULT_DELIMITER                   = '"' ;
	static final char          DEFAULT_ESCAPER                     = '\\';
	static final char          DEFAULT_SEGMENT_STARTER             = '{' ;
	static final char          DEFAULT_SEGMENT_TERMINATOR          = '}' ;
	static final char          DEFAULT_HEADER_STARTER              = '(' ;
	static final char          DEFAULT_HEADER_TERMINATOR           = ')' ;
	static final char          DEFAULT_COMMENT_SIGNAL              = '/' ;
	static final char          DEFAULT_COMMENT_SIMPLE_STARTER      = '/' ;
	static final char          DEFAULT_COMMENT_FULL_STARTER        = '*' ;
	static final String        DEFAULT_COMMENT_FULL_TERMINATOR     = "*/";
	static final char          DEFAULT_TERMINATOR                  = 0   ; // null character by default
	static final int           DEFAULT_SKIP_LINE_COUNT             = 0   ;
	static final int           DEFAULT_SKIP_LINE_COUNT_POST_HEADER = 0   ;
	static final int           DEFAULT_TRAILING_LINE_COUNT         = 0   ;
	static final Boolean       DEFAULT_HAS_COLUMN_NAMES_HEADER     = null;
	static final Boolean       DEFAULT_HAS_COLUMN_TYPES_HEADER     = null;
	static final Boolean       DEFAULT_HAS_CTRLCHAR_DEF_HEADER     = null;
	
	static final EscapeHandler DEFAULT_ESCAPE_HANDLER = new EscapeHandler.Default();
			
	
	// only the common ones. Crazy special needs must be handled explicitely
	static final char[] VALID_VALUE_SEPARATORS = {'\t', ';', ',', '|', ':'};
	
	public interface ValueSeparatorWeight
	{
		public char valueSeparator();
		
		public float weight();
		
		
		
		public final class Default implements ValueSeparatorWeight
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final char  valueSeparator;
			private final float weight;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Default(final char valueSeparator, final float weight)
			{
				super();
				this.valueSeparator = valueSeparator;
				this.weight         = weight        ;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public final char valueSeparator()
			{
				return this.valueSeparator;
			}
			
			@Override
			public final float weight()
			{
				return this.weight;
			}
			
		}
		
	}

	

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
				
	public static final XCsvConfiguration configurationDefault()
	{
		return XCsvDataType.XCSV.configuration();
	}

	public static boolean isValidValueSeparator(final char c)
	{
		return XChars.contains(VALID_VALUE_SEPARATORS, c);
	}
	
	public static char validateValueSeparator(final char c)
	{
		if(isValidValueSeparator(c))
		{
			return c;
		}
		
		throw new XCsvException(
			"Invalid " + XCSV.class.getSimpleName()
			+ " value separator '" + c + "'. Valid separators are "
			+ Arrays.toString(VALID_VALUE_SEPARATORS)
		);
	}

	public static StringTable parse(final String rawData)
	{
		return parse(rawData, null, null);
	}
	
	public static StringTable parse(final String rawData, final char valueSeparator)
	{
		return parse(rawData, XCsvConfiguration.New(valueSeparator), null);
	}
	
	public static StringTable parse(final String rawData, final XCsvDataType dataType)
	{
		return parse(rawData, null, dataType);
	}
	
	public static StringTable parse(final String rawData, final XCsvConfiguration configuration)
	{
		return parse(rawData, configuration, null);
	}
	
	public static StringTable parse(
		final String            rawData      ,
		final XCsvConfiguration configuration,
		final XCsvDataType      dataType
	)
	{
		return parse(_charArrayRange.New(XChars.readChars(rawData)), configuration, dataType);
	}
	
	public static StringTable parse(
		final _charArrayRange   rawData         ,
		final XCsvConfiguration csvConfiguration,
		final XCsvDataType      dataType
	)
	{
		final XCsvContentBuilderCharArray parser = XCsvContentBuilderCharArray.New(
			csvConfiguration, dataType
		);
		
		final XCsvContent content = parser.build(null, rawData);
		final StringTable data    = content.segments().first().value();

		return data;
	}
	
	public static StringTable readFromFile(final Path file)
	{
		final String       fileSuffix = XIO.getFileSuffix(file);
		final String       normalized = fileSuffix == null ? null : fileSuffix.trim().toLowerCase();
		final XCsvDataType dataType   = XCsvDataType.fromIdentifier(normalized);
		
		return readFromFile(file, dataType);
	}
	
	public static StringTable readFromFile(final Path file, final XCsvDataType dataType)
	{
		final String fileContent = XIO.unchecked(() ->
			XIO.readString(file)
		);
		
		return parse(fileContent, dataType);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private XCSV()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}

