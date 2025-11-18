package org.eclipse.serializer.util.cql;

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

import org.eclipse.serializer.collections.sorting.SortableProcedure;
import org.eclipse.serializer.collections.types.XIterable;
import org.eclipse.serializer.collections.types.XSequence;
import org.eclipse.serializer.functional.Aggregator;

import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.eclipse.serializer.util.X.notNull;

@FunctionalInterface
public interface CqlResultor<O, R>
{
	public Aggregator<O, R> prepareCollector(XIterable<?> source);



	public static <O> CqlResultor<O, XSequence<O>> New()
	{
		return e ->
			new CqlWrapperCollectorProcedure<>(
				CQL.prepareTargetCollection(e)
			)
		;
	}

	public static <O, T extends Consumer<O> & XIterable<O>> CqlResultor<O, T> New(final T target)
	{
		notNull(target);
		return e ->
			new CqlWrapperCollectorProcedure<>(target)
		;
	}

	// (06.07.2016 TM)NOTE: javac reports a false ambiguity here.
	public static <O, R> CqlResultor<O, R> NewFromAggregator(final Aggregator<O, R> collector)
	{
		notNull(collector);
		return e -> collector;
	}

	// (06.07.2016 TM)NOTE: javac reports a false ambiguity here.
	public static <O, T extends Consumer<O>> CqlResultor<O, T> NewFromSupplier(final Supplier<T> supplier)
	{
		return e -> new CqlWrapperCollectorProcedure<>(supplier.get());
	}

	public static <O, T extends SortableProcedure<O> & XIterable<O>> CqlResultor<O, T> NewFromSupplier(
		final Supplier<T>           supplier,
		final Comparator<? super O> order
	)
	{
		notNull(supplier);
		return order == null
			? CqlResultor.NewFromSupplier(supplier)
			: e -> new CqlWrapperCollectorSequenceSorting<>(supplier.get(), order)
		;
	}

	public static <O, T> CqlResultor<O, T> NewFromSupplier(final Supplier<T> supplier, final BiConsumer<O, T> linker)
	{
		final T target = supplier.get();

		return new CqlResultor<>()
		{
			@Override
			public Aggregator<O, T> prepareCollector(final XIterable<?> source)
			{
				return new Aggregator<>()
				{
					@Override
					public void accept(final O element)
					{
						linker.accept(element, target);
					}

					@Override
					public T yield()
					{
						return target;
					}
				};
			}
		};
	}

	public static <O, T extends XSequence<O>> CqlResultor<O, T> NewFromSupplier(
		final Supplier<T>           supplier,
		final BiConsumer<O, T>      linker  ,
		final Comparator<? super O> order
	)
	{
		notNull(supplier);
		return order == null
			? CqlResultor.NewFromSupplier(supplier, linker)
			: e -> new CqlWrapperCollectorLinkingSorting<>(supplier.get(), linker, order)
		;
	}

	public static <O, T> CqlResultor<O, T> NewFromSupplier(
		final Supplier<T>         supplier ,
		final BiConsumer<O, T>    linker   ,
		final Consumer<? super T> finalizer
	)
	{
		notNull(supplier);
		return finalizer == null
			? CqlResultor.NewFromSupplier(supplier, linker)
			: e -> new CqlWrapperCollectorLinkingFinalizing<>(supplier.get(), linker, finalizer)
		;
	}

}
