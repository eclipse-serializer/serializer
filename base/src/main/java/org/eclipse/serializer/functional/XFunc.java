package org.eclipse.serializer.functional;

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

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.serializer.util.X;
import org.eclipse.serializer.equality.Equalator;


/**
 * Normally, writing "Func" instead of "Functional" is a capital sin of writing clean code.
 * However, in the sake of shortness for static util method class names AND in light of
 * "Mathematics", "Sorting" and "Characters" already being shortened to the (albeit more common)
 * names "Math", "Sort", "Chars" PLUS the unique recognizable of "Func", the shortness trumped
 * the clarity of completeness here (as well).
 *
 */
public final class XFunc
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	/**
	 * Functional alias for{@code return true;}.
	 * @param <T> the type of the input to the predicate
	 * @return the all predicate
	 */
	public static final <T> Predicate<T> all()
	{
		// note on performance: equal to caching an instance in a constant field (lambdas are cached internally)
		return e -> true;
	}

	/**
	 * Functional alias for {@code return e != null;}.
	 * @param <T> the type of the input to the predicate
	 * @return the not null predicate
	 */
	public static <T> Predicate<T> notNull()
	{
		// note on performance: equal to caching an instance in a constant field (lambdas are cached internally)
		return e -> e != null;
	}

	public static final <T, R> Function<T, R> toNull()
	{
		// note on performance: equal to caching an instance in a constant field (lambdas are cached internally)
		return t -> null;
	}

	public static final <T> Function<T, T> passThrough()
	{
		// note on performance: equal to caching an instance in a constant field (lambdas are cached internally)
		return t -> t;
	}

	public static final <T> Predicate<T> isEqualTo(final T subject)
	{
		return new Predicate<T>()
		{
			@Override
			public boolean test(final T o)
			{
				return subject == null ? o == null : subject.equals(o);
			}
		};
	}

	public static final <T, E extends T> Predicate<T> predicate(final E subject, final Equalator<T> equalator)
	{
		return new Predicate<T>()
		{
			@Override
			public boolean test(final T t)
			{
				return equalator.equal(subject, t);
			}
		};
	}


	public static <E> Consumer<E> wrapWithSkip(final Consumer<? super E> target, final long skip)
	{
		return new AbstractProcedureSkip<E>(skip)
		{
			@Override
			public void accept(final E e)
			{
				if(--this.skip >= 0)
				{
					return;
				}
				target.accept(e);
			}
		};
	}

	public static <E> Consumer<E> wrapWithLimit(final Consumer<? super E> target, final long limit)
	{
		return new AbstractProcedureLimit<E>(limit)
		{
			@Override
			public void accept(final E e)
			{
				target.accept(e);
				if(--this.limit == 0)
				{
					throw X.BREAK();
				}
			}
		};
	}

	public static <E> Consumer<E> wrapWithSkipLimit(
		final Consumer<? super E> target,
		final long                skip  ,
		final long                limit
	)
	{
		return new AbstractProcedureSkipLimit<E>(skip, limit)
		{
			@Override
			public void accept(final E e)
			{
				if(--this.skip >= 0)
				{
					return;
				}
				target.accept(e);
				if(--this.limit == 0)
				{
					throw X.BREAK();
				}
			}
		};
	}

	public static final <E> Consumer<E> wrapWithPredicate(
		final Consumer<? super E>  target   ,
		final Predicate<? super E> predicate
	)
	{
		return e ->
		{
			if(!predicate.test(e))
			{
				return; // debug hook
			}
			target.accept(e);
		};
	}

	public static <E> Consumer<E> wrapWithPredicateSkip(
		final Consumer<? super E>  target   ,
		final Predicate<? super E> predicate,
		final long                 skip
	)
	{
		return new AbstractProcedureSkip<E>(skip)
		{
			@Override
			public void accept(final E e)
			{
				if(!predicate.test(e))
				{
					return; // debug hook
				}
				if(--this.skip >= 0)
				{
					return;
				}
				target.accept(e);
			}
		};
	}

	public static <E> Consumer<E> wrapWithPredicateLimit(
		final Consumer<? super E>  target   ,
		final Predicate<? super E> predicate,
		final long                 limit
	)
	{
		return new AbstractProcedureLimit<E>(limit)
		{
			@Override
			public void accept(final E e)
			{
				if(!predicate.test(e))
				{
					return; // debug hook
				}
				target.accept(e);
				if(--this.limit == 0)
				{
					throw X.BREAK();
				}
			}
		};
	}

	public static <E> Consumer<E> wrapWithPredicateSkipLimit(
		final Consumer<? super E>  target   ,
		final Predicate<? super E> predicate,
		final long                 skip     ,
		final long                 limit
	)
	{
		return new AbstractProcedureSkipLimit<E>(skip, limit)
		{
			@Override
			public void accept(final E e)
			{
				if(!predicate.test(e))
				{
					return; // debug hook
				}
				if(--this.skip >= 0)
				{
					return;
				}
				target.accept(e);
				if(--this.limit == 0)
				{
					throw X.BREAK();
				}
			}
		};
	}

	public static final <I, O> Consumer<I> wrapWithFunction(
		final Consumer<? super O>    target  ,
		final Function<? super I, O> function
	)
	{
		return e ->
		{
			target.accept(function.apply(e));
		};
	}

	public static <I, O> Consumer<I> wrapWithFunctionSkip(
		final Consumer<? super O>    target  ,
		final Function<? super I, O> function,
		final long                   skip
	)
	{
		return new AbstractProcedureSkip<I>(skip)
		{
			@Override
			public void accept(final I e)
			{
				if(--this.skip >= 0)
				{
					return;
				}
				target.accept(function.apply(e));
			}
		};
	}

	public static <I, O> Consumer<I> wrapWithFunctionLimit(
		final Consumer<? super O>    target  ,
		final Function<? super I, O> function,
		final long                   limit
	)
	{
		return new AbstractProcedureLimit<I>(limit)
		{
			@Override
			public void accept(final I e)
			{
				target.accept(function.apply(e));
				if(--this.limit == 0)
				{
					throw X.BREAK();
				}
			}
		};
	}

	public static <I, O> Consumer<I> wrapWithFunctionSkipLimit(
		final Consumer<? super O>    target  ,
		final Function<? super I, O> function,
		final long                   skip    ,
		final long                   limit
	)
	{
		return new AbstractProcedureSkipLimit<I>(skip, limit)
		{
			@Override
			public void accept(final I e)
			{
				if(--this.skip >= 0)
				{
					return;
				}
				target.accept(function.apply(e));
				if(--this.limit == 0)
				{
					throw X.BREAK();
				}
			}
		};
	}

	public static final <I, O> Consumer<I> wrapWithPredicateFunction(
		final Consumer<? super O>    target   ,
		final Predicate<? super I>   predicate,
		final Function<? super I, O> function
	)
	{
		return e ->
		{
			if(!predicate.test(e))
			{
				return; // debug hook
			}
			target.accept(function.apply(e));
		};
	}

	public static <I, O> Consumer<I> wrapWithPredicateFunctionSkip(
		final Consumer<? super O>    target   ,
		final Predicate<? super I>   predicate,
		final Function<? super I, O> function ,
		final long                   skip
	)
	{
		return new AbstractProcedureSkip<I>(skip)
		{
			@Override
			public void accept(final I e)
			{
				if(!predicate.test(e))
				{
					return; // debug hook
				}
				if(--this.skip >= 0)
				{
					return;
				}
				target.accept(function.apply(e));
			}
		};
	}

	public static <I, O> Consumer<I> wrapWithPredicateFunctionLimit(
		final Consumer<? super O>    target   ,
		final Predicate<? super I>   predicate,
		final Function<? super I, O> function ,
		final long                   limit
	)
	{
		return new AbstractProcedureLimit<I>(limit)
		{
			@Override
			public void accept(final I e)
			{
				if(!predicate.test(e))
				{
					return; // debug hook
				}
				target.accept(function.apply(e));
				if(--this.limit == 0)
				{
					throw X.BREAK();
				}
			}
		};
	}

	public static <I, O> Consumer<I> wrapWithPredicateFunctionSkipLimit(
		final Consumer<? super O>    target   ,
		final Predicate<? super I>   predicate,
		final Function<? super I, O> function ,
		final long                   skip     ,
		final long                   limit
	)
	{
		return new AbstractProcedureSkipLimit<I>(skip, limit)
		{
			@Override
			public void accept(final I e)
			{
				if (!predicate.test(e))
				{
					return; // debug hook
				}
				if (--this.skip >= 0)
				{
					return;
				}
				target.accept(function.apply(e));
				if (--this.limit == 0)
				{
					throw X.BREAK();
				}
			}
		};
	}

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private XFunc()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
