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

import org.eclipse.serializer.collections.BulkList;

import java.util.function.Consumer;

import static org.eclipse.serializer.util.X.notNull;



/**
 * An instance that collects (buffers) elements and gets notified once the collecting process is completed.
 *
 * @param <E> the collected element's type
 */
public interface BufferingCollector<E>
{
	void accept(E element);

	void resetElements();

	void finalizeElements();

	long size();

	default boolean isEmpty()
	{
		return this.size() == 0;
	}



	static <E> BufferingCollector<E> New(final Consumer<? super E> finalizingLogic)
	{
		return new Default<>(notNull(finalizingLogic), null);
	}

	static <E> BufferingCollector<E> New(
		final Consumer<? super E> finalizingLogic,
		final Consumer<? super E> collectingListener
	)
	{
		return new Default<>(notNull(finalizingLogic), collectingListener);
	}

	final class Default<E> implements BufferingCollector<E>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final BulkList<E>         buffer = BulkList.New();
		private final Consumer<? super E> finalizingLogic   ;
		private final Consumer<? super E> collectingListener;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final Consumer<? super E> finalizingLogic, final Consumer<? super E> collectingListener)
		{
			super();
			this.finalizingLogic    = finalizingLogic   ;
			this.collectingListener = collectingListener;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void accept(final E element)
		{
			if(this.collectingListener != null)
			{
				this.collectingListener.accept(element);
			}
			this.buffer.accept(element);
		}

		@Override
		public void resetElements()
		{
			this.buffer.clear();
		}

		@Override
		public void finalizeElements()
		{
			this.buffer.iterate(this.finalizingLogic);
		}

		@Override
		public long size()
		{
			return this.buffer.size();
		}

	}

}
