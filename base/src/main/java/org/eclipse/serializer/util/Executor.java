package org.eclipse.serializer.util;

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
import java.util.function.Function;
import java.util.function.Supplier;

import static org.eclipse.serializer.util.X.notNull;


public interface Executor<EX extends Throwable>
{
	Executor<EX> reset();

	boolean handle(Throwable t);

	void complete(Runnable onSuccessLogics);



	default void execute(final Runnable logic)
	{
		try
		{
			logic.run();
		}
		catch(final Throwable e)
		{
			if(!this.handle(e))
			{
				throw e;
			}
		}
	}

	default <E> void execute(final E element, final Consumer<? super E> logic)
	{
		try
		{
			logic.accept(element);
		}
		catch(final Throwable e)
		{
			if(!this.handle(e))
			{
				throw e;
			}
		}
	}

	default <E> void executeNullIgnoring(final E element, final Consumer<? super E> logic)
	{
		if(element == null)
		{
			return;
		}
		try
		{
			logic.accept(element);
		}
		catch(final Throwable e)
		{
			if(!this.handle(e))
			{
				throw e;
			}
		}
	}

	default <E> void executeNullHandling(
		final E element,
		final Consumer<? super E> logic,
		final Runnable nullCaseLogic
	)
	{
		try
		{
			if(element != null)
			{
				logic.accept(element);
			}
			else
			{
				nullCaseLogic.run();
			}
		}
		catch(final Throwable e)
		{
			if(!this.handle(e))
			{
				throw e;
			}
		}
	}

	default <R> R executeR(final Supplier<? extends R> logic)
	{
		try
		{
			return logic.get();
		}
		catch(final Throwable e)
		{
			if(this.handle(e))
			{
				return null;
			}
			throw e;
		}
	}

	default <E, R> R executeR(final E element, final Function<? super E, R> logic)
	{
		try
		{
			return logic.apply(element);
		}
		catch(final Throwable e)
		{
			if(this.handle(e))
			{
				return null;
			}
			throw e;
		}
	}

	default <E, R> R executeRNullIgnoring(final E element, final Function<? super E, R> logic)
	{
		if(element == null)
		{
			return null;
		}
		try
		{
			return logic.apply(element);
		}
		catch(final Throwable e)
		{
			if(this.handle(e))
			{
				return null;
			}
			throw e;
		}
	}

	default <E, R> R executeRNullHandling(
		final E element,
		final Function<? super E, R> logic,
		final Supplier<? extends R> nullCaseLogic
	)
	{
		try
		{
			if(element == null)
			{
				return nullCaseLogic.get();
			}
			return logic.apply(element);
		}
		catch(final Throwable e)
		{
			if(this.handle(e))
			{
				return null;
			}
			throw e;
		}
	}

	static Executor<Exception> New(final BufferingCollector<? super Exception> collector)
	{
		return New(Exception.class, collector);
	}

	static Executor<Exception> New(final Consumer<? super Exception> exceptionFinalizer)
	{
		return New(Exception.class, BufferingCollector.New(notNull(exceptionFinalizer)));
	}

	static <EX extends Throwable> Executor<EX> New(
		final Class<EX> exceptionType,
		final BufferingCollector<? super EX> collector
	)
	{
		return new Default<>(notNull(exceptionType), notNull(collector));
	}

	static <EX extends Throwable> Executor<EX> New(
		final Class<EX> exceptionType,
		final Consumer<? super EX> exceptionFinalizer
	)
	{
		return new Default<>(
			notNull(exceptionType),
			BufferingCollector.New(notNull(exceptionFinalizer))
		);
	}

	final class Default<EX extends Throwable> implements Executor<EX>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final Class<EX>                      type     ;
		final BufferingCollector<? super EX> collector;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final Class<EX> type, final BufferingCollector<? super EX> collector)
		{
			super();
			this.type      = type     ;
			this.collector = collector;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public Default<EX> reset()
		{
			this.collector.resetElements();
			return this;
		}

		@Override
		public boolean handle(final Throwable t)
		{
			if(this.type.isAssignableFrom(t.getClass()))
			{
				this.collector.accept(this.type.cast(t));
				return true;
			}
			return false;
		}

		@Override
		public void complete(final Runnable onSuccessLogics)
		{
			if(this.collector.isEmpty())
			{
				onSuccessLogics.run();
			}
			else
			{
				this.collector.finalizeElements();
			}
		}

	}

}
