package test.eclipse.serializer.reference;

/*-
 * #%L
 * Eclipse Serializer Integration Tests
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.serializer.reference.Lazy;
import org.junit.jupiter.api.Test;

/**
 * Regression test for the usage-mark eviction guard in {@link Lazy.Default#clear(Lazy.ClearingEvaluator)}:
 * a {@link org.eclipse.serializer.reference.UsageMarkable used-marked} lazy reference must never be cleared
 * by evaluator-driven eviction (the {@code LazyReferenceManager} path), because the usage mark signals that
 * the referent carries state that must not be dropped, e.g. changes that have not been persisted yet.
 * <p>
 * Without the guard, a mutated-but-not-yet-stored referent pinned via
 * {@link org.eclipse.serializer.reference.UsageMarkable#markUsedFor(Object)} could be evicted by the
 * periodic lazy clearing, silently losing the unpersisted state (see the GigaMap dirty-segment
 * lost-update scenario, where exactly this caused persisted index/segment divergence).
 */
public class LazyUsedEvictionGuardTest
{
	private static final long FAKE_OBJECT_ID = 1_000_000_000_000_000_001L;

	private static Lazy<String> createStoredLoadedLazy(final String payload)
	{
		// unregistered to keep the global LazyReferenceManager out of the test
		final Lazy<String> lazy = Lazy.UnregisteredReference(payload);

		// same linking the framework's type handler performs when the reference gets persisted
		((Lazy.Default<String>)lazy).$link(FAKE_OBJECT_ID, objectId -> payload);

		return lazy;
	}

	@Test
	void evaluatorDrivenClearingSkipsUsedMarkedReference()
	{
		final Lazy<String> lazy = createStoredLoadedLazy("payload");
		assertTrue(lazy.isStored());

		final Lazy.ClearingEvaluator alwaysClear = ref -> true;

		lazy.markUsed();
		assertFalse(lazy.clear(alwaysClear), "used-marked reference must not be cleared by an evaluator");
		assertEquals("payload", lazy.peek(), "referent must survive evaluator-driven clearing while used-marked");

		lazy.markUnused();
		assertTrue(lazy.clear(alwaysClear), "unmarked reference must be clearable by an evaluator again");
		assertNull(lazy.peek());
	}

	@Test
	void guardTracksMarkUnmarkPerInstance()
	{
		final Lazy<String> lazy = createStoredLoadedLazy("payload");

		final Object markerA = new Object();
		final Object markerB = new Object();
		final Lazy.ClearingEvaluator alwaysClear = ref -> true;

		lazy.markUsedFor(markerA);
		lazy.markUsedFor(markerB);

		lazy.unmarkUsedFor(markerA);
		assertFalse(lazy.clear(alwaysClear), "reference must stay pinned while any usage mark remains");

		lazy.unmarkUsedFor(markerB);
		assertTrue(lazy.clear(alwaysClear), "reference must be clearable once the last usage mark is removed");
		assertNull(lazy.peek());
	}

	@Test
	void explicitClearIgnoresUsageMarks()
	{
		final Lazy<String> lazy = createStoredLoadedLazy("payload");

		lazy.markUsed();
		assertEquals("payload", lazy.clear(), "explicit clear() must stay unconditional");
		assertNull(lazy.peek());
	}
}
