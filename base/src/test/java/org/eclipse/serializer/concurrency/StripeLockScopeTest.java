package org.eclipse.serializer.concurrency;

/*-
 * #%L
 * Eclipse Serializer Base
 * %%
 * Copyright (C) 2026 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;


class StripeLockScopeTest
{
	@Test
	void createExecutor_notOverridden_producesNonFairExecutor() throws Exception
	{
		final DefaultScope scope = new DefaultScope();
		scope.touch();

		assertFalse(((StripeLockedExecutor.Default)executorOf(scope)).isFair());
	}

	@Test
	void createExecutor_overridden_isUsed() throws Exception
	{
		final FairScope scope = new FairScope();
		scope.touch();

		assertTrue(((StripeLockedExecutor.Default)executorOf(scope)).isFair());
	}

	@Test
	void executor_afterTransientFieldCleared_reinitializes() throws Exception
	{
		final FairScope scope = new FairScope();
		scope.touch();

		final Field field = StripeLockScope.class.getDeclaredField("executor");
		field.setAccessible(true);
		field.set(scope, null);

		assertTrue(scope.touch(), "the scope did not re-create its executor");
		assertNotNull(executorOf(scope));
	}

	@Test
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	void write_producer_returnsProducersResult()
	{
		assertEquals("write", new DefaultScope().writeResult());
	}

	private static StripeLockedExecutor executorOf(final StripeLockScope scope) throws Exception
	{
		final Field field = StripeLockScope.class.getDeclaredField("executor");
		field.setAccessible(true);
		return (StripeLockedExecutor)field.get(scope);
	}


	private static class DefaultScope extends StripeLockScope
	{
		private final Object mutex = new Object();

		DefaultScope()
		{
			super();
		}

		/**
		 * Runs a trivial read to trigger the lazy executor initialization.
		 *
		 * @return whether the action was executed
		 */
		boolean touch()
		{
			final AtomicBoolean executed = new AtomicBoolean();
			this.read(this.mutex, () -> executed.set(true));
			return executed.get();
		}

		String writeResult()
		{
			return this.write(this.mutex, () -> "write");
		}

	}


	private static final class FairScope extends DefaultScope
	{
		FairScope()
		{
			super();
		}

		@Override
		protected StripeLockedExecutor createExecutor()
		{
			return StripeLockedExecutor.New(this.stripeCount(), true);
		}

	}

}
