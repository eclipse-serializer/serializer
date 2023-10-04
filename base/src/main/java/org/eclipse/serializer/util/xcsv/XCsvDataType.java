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

import org.eclipse.serializer.collections.EqConstHashTable;
import org.eclipse.serializer.collections.EqHashTable;
import org.eclipse.serializer.exceptions.XCsvException;
import org.eclipse.serializer.math.XMath;
import org.eclipse.serializer.util.xcsv.XCSV.ValueSeparatorWeight;


public enum XCsvDataType
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	XCSV(
		"xcsv",
		map(
			vc('\t', 1.3),
			vc( ';', 1.2),
			vc( ',', 1.1),
			vc( '|', 1.0),
			vc( ':', 0.9)
		)
	),
	TSV(
		"tsv",
		map(
			vc('\t', 1.3),
			vc( ';', 1.2),
			vc( ',', 1.1),
			vc( '|', 1.0),
			vc( ':', 0.9)
		)
	),
	CSV(
		"csv",
		map(
			vc('\t', 1.1),
			vc( ';', 1.2), // "," is standard, see https://en.wikipedia.org/wiki/Comma-separated_values
			vc( ',', 1.3), // "," is standard, see https://en.wikipedia.org/wiki/Comma-separated_values
			vc( '|', 1.0),
			vc( ':', 0.9)
		)
	);
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	final static ValueSeparatorWeight vc(final char valueSeparator, final double weight)
	{
		return new ValueSeparatorWeight.Default(
                org.eclipse.serializer.util.xcsv.XCSV.validateValueSeparator(valueSeparator),
                XMath.positive((float)weight)
		);
	}
	
	final static EqConstHashTable<Character, ValueSeparatorWeight> map(final ValueSeparatorWeight... weights)
	{
		final EqHashTable<Character, ValueSeparatorWeight> table = EqHashTable.New();
		
		for(final ValueSeparatorWeight weight : weights)
		{
			table.add(Character.valueOf(weight.valueSeparator()), weight);
		}
		
		return table.immure();
	}
	
	final static char determinePreferredValueSeparator(
		final Iterable<? extends ValueSeparatorWeight> weights
	)
	{
		float maxWeight     = -1.0f;
		char  maxWeightChar = '0';
		
		for(final ValueSeparatorWeight weight: weights)
		{
			if(weight.weight() >= maxWeight)
			{
				maxWeight = weight.weight();
				maxWeightChar = weight.valueSeparator();
			}
		}
		
		if(maxWeightChar == '0')
		{
			throw new XCsvException("Invalid value separator weights: " + weights);
		}
		
		return maxWeightChar;
	}
	
	public static XCsvDataType fromIdentifier(final String identifier)
	{
		if(identifier == null)
		{
			return null;
		}
		
		return XCsvDataType.valueOf(identifier.toUpperCase());
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final String identifier;
	private final EqConstHashTable<Character, ValueSeparatorWeight> valueSeparatorWeights;
	private final XCsvConfiguration configuration;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	private XCsvDataType(
		final String                                                 identifier           ,
		final EqConstHashTable<Character, XCSV.ValueSeparatorWeight> valueSeparatorWeights
	)
	{
		this(identifier, valueSeparatorWeights, XCsvConfiguration.Builder());
	}
	
	private XCsvDataType(
		final String                                                 identifier           ,
		final EqConstHashTable<Character, XCSV.ValueSeparatorWeight> valueSeparatorWeights,
		final XCsvConfiguration.Builder                              configurationBuilder
	)
	{
		this.identifier            = identifier           ;
		this.valueSeparatorWeights = valueSeparatorWeights;
		
		final char preferredValueSeparator = determinePreferredValueSeparator(valueSeparatorWeights.values());
		this.configuration = configurationBuilder
			.setValueSeparator(preferredValueSeparator)
			.buildConfiguration()
		;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final String identifier()
	{
		return this.identifier;
	}
	
	public final XCsvConfiguration configuration()
	{
		return this.configuration;
	}
	
	public final EqConstHashTable<Character, ValueSeparatorWeight> valueSeparatorWeights()
	{
		return this.valueSeparatorWeights;
	}
	
	public final boolean isValidValueSeparator(final Character c)
	{
		return this.valueSeparatorWeights.keys().contains(c);
	}
	
	public final boolean isValidValueSeparator(final char c)
	{
		return this.isValidValueSeparator(Character.valueOf(c));
	}
	
	public final XCSV.ValueSeparatorWeight lookupValueSeparator(final Character c)
	{
		return this.valueSeparatorWeights.get(c);
	}
	
	public final XCSV.ValueSeparatorWeight lookupValueSeparator(final char c)
	{
		return this.lookupValueSeparator(Character.valueOf(c));
	}
	
}
