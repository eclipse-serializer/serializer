package test.eclipse.serializer.danglingref;

/*-
 * #%L
 * Eclipse Serializer Integration Tests
 * %%
 * Copyright (C) 2023 - 2026 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.serializer.SerializerFoundation;
import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.binary.types.BinaryStorer;
import org.eclipse.serializer.persistence.exceptions.PersistenceDanglingReferences;
import org.eclipse.serializer.persistence.exceptions.PersistenceException;
import org.eclipse.serializer.persistence.types.PersistenceIdSet;
import org.eclipse.serializer.persistence.types.PersistenceManager;
import org.eclipse.serializer.persistence.types.PersistenceSource;
import org.eclipse.serializer.persistence.types.PersistenceStorer;
import org.eclipse.serializer.persistence.types.PersistenceTarget;
import org.eclipse.serializer.reference.Lazy;
import org.eclipse.serializer.util.X;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Deferred Lazy linking across healing commits (internal#82): the healing storer's compensating
 * commit is durable, but its success does not imply the enclosing store's success — the retried
 * write may still fail terminally. The deferred {@code $link} commit listeners of Lazies the
 * healing storer serialized must therefore fire only with the OUTERMOST commit's success:
 * <ul>
 * <li>retry fails terminally: nothing may have been linked — a linked Lazy over durable-but-
 * unreachable data would be legally clearable, and once the storage GC reclaims the data, the
 * cleared reference's cached id is unhealable: permanent loss.</li>
 * <li>retry succeeds: the transferred listeners fire with the outer commit — the healed subgraph's
 * fresh Lazies end up properly linked, exactly as if the store had never been rejected.</li>
 * </ul>
 * Driven through a scripted {@link PersistenceTarget}, since real storage cannot deterministically
 * produce "healing commit succeeds, retry fails for an unrelated reason".
 */
@Timeout(60)
public class HealingDeferredLinkTest
{
	/** A fabricated object id safely inside the OID range but far above anything assigned here. */
	static final long FAKE_OID = 1_000_000_000_900_000_082L;

	PersistenceManager<Binary> persistenceManager;

	@AfterEach
	public void afterTest()
	{
		if(this.persistenceManager != null)
		{
			this.persistenceManager.close();
		}
	}

	private PersistenceStorer createHealingStorer(final ScriptedTarget target)
	{
		final PersistenceSource<Binary> source = new PersistenceSource<Binary>()
		{
			@Override
			public XGettingCollection<? extends Binary> read()
			{
				return X.empty();
			}

			@Override
			public XGettingCollection<? extends Binary> readByObjectIds(final PersistenceIdSet[] oids)
			{
				return X.empty();
			}
		};

		final SerializerFoundation<?> foundation = SerializerFoundation.New();
		this.persistenceManager = foundation
			.setPersistenceSource(source)
			.setPersistenceTarget(target)
			.createPersistenceManager()
		;

		/*
		 * Bypass PersistenceManager#createStorer: the SerializerFoundation dispatches storers onto
		 * CLONED object managers (LocalObjectRegistration context dispatching), which would hide
		 * the test's ghost registration from the storer. The direct creator call wires the
		 * foundation's shared object manager - the same wiring the embedded storage uses
		 * (pass-through dispatcher). Single channel, capture + healing enabled: heal-mode wiring.
		 */
		return BinaryStorer.Creator(() -> 1, false, true, true).createLazyStorer(
			foundation.getTypeHandlerManager(),
			foundation.getObjectManager()     ,
			this.persistenceManager           ,
			target                            ,
			foundation.getBufferSizeProvider(),
			this.persistenceManager
		);
	}

	@Test
	void lazyStaysUnlinkedWhenRetryFailsAfterSuccessfulHealing()
	{
		// write 1 (original store): rejected for the ghost's oid; write 2 (healing commit):
		// accepted; write 3 (retry): fails for an unrelated reason - terminal.
		final RuntimeException retryFailure = new RuntimeException("unrelated retry failure (e.g. IO)");
		final ScriptedTarget   target       = new ScriptedTarget(retryFailure);
		final PersistenceStorer storer      = this.createHealingStorer(target);

		final Lazy<Payload> freshLazy = Lazy.Reference(new Payload("only in-memory copy"));
		final Child         ghost     = new Child(freshLazy);
		this.persistenceManager.objectRegistry().registerObject(FAKE_OID, ghost);

		final Parent parent = new Parent(ghost);

		final RuntimeException thrown = assertThrows(RuntimeException.class, () ->
		{
			storer.store(parent);
			storer.commit();
		});
		// precondition: the lazy storer must have skipped the registry-known ghost as a trusted id.
		assertArrayEquals(new long[]{FAKE_OID}, target.firstWriteTrustedIds,
			"the original store must reference the ghost as a trusted (skipped) id");
		assertSame(retryFailure, thrown, "the terminal failure must be the retry's own exception");
		assertEquals(3, target.writeCount, "expected reject, healing write, failed retry");

		/*
		 * THE regression pin: the healing commit serialized the ghost's subgraph including the
		 * fresh Lazy, but its deferred $link must not have fired - the store as a whole failed.
		 * A linked Lazy here would be clearable while its data is durable-but-unreachable garbage,
		 * which is the permanent-loss path of internal#82.
		 */
		assertFalse(freshLazy.isStored(), "a failed store must leave the healed subgraph's Lazy unlinked");
		assertThrows(IllegalStateException.class, freshLazy::clear, "an unlinked Lazy may not be clearable");
	}

	@Test
	void lazyIsLinkedWhenRetrySucceedsAfterHealing()
	{
		// write 1: rejected; write 2 (healing commit): accepted; write 3 (retry): accepted.
		final ScriptedTarget    target = new ScriptedTarget(null);
		final PersistenceStorer storer = this.createHealingStorer(target);

		final Lazy<Payload> freshLazy = Lazy.Reference(new Payload("payload"));
		final Child         ghost     = new Child(freshLazy);
		this.persistenceManager.objectRegistry().registerObject(FAKE_OID, ghost);

		final Parent parent = new Parent(ghost);

		assertDoesNotThrow(() ->
		{
			storer.store(parent);
			storer.commit();
		});
		assertEquals(3, target.writeCount, "expected reject, healing write, successful retry");

		// the transferred listener must have fired with the outer commit's success.
		assertTrue(freshLazy.isStored(), "a successful store must link the healed subgraph's Lazy");
	}


	///////////////////////////////////////////////////////////////////////////
	// test doubles //
	/////////////////

	/**
	 * Rejects the first write reporting {@link #FAKE_OID} missing, accepts the second (the healing
	 * commit), and fails or accepts the third (the retry) depending on {@code retryFailure}.
	 */
	static final class ScriptedTarget implements PersistenceTarget<Binary>
	{
		final RuntimeException retryFailure;

		int    writeCount;
		long[] firstWriteTrustedIds;

		ScriptedTarget(final RuntimeException retryFailure)
		{
			super();
			this.retryFailure = retryFailure;
		}

		@Override
		public void write(final Binary data)
		{
			if(this.writeCount == 0)
			{
				this.firstWriteTrustedIds = data.trustedObjectIds();
			}
			switch(++this.writeCount)
			{
				case 1:
					throw new DanglingRejection(FAKE_OID);
				case 3:
					if(this.retryFailure != null)
					{
						throw this.retryFailure;
					}
					// fall through: accepted
				default:
					// accepted
			}
		}

		@Override
		public boolean isWritable()
		{
			return true;
		}
	}

	static final class DanglingRejection extends PersistenceException implements PersistenceDanglingReferences
	{
		final long[] missingObjectIds;

		DanglingRejection(final long... missingObjectIds)
		{
			super("store references non-existing entities");
			this.missingObjectIds = missingObjectIds;
		}

		@Override
		public long[] missingObjectIds()
		{
			return this.missingObjectIds.clone();
		}
	}


	///////////////////////////////////////////////////////////////////////////
	// data types //
	///////////////

	public static class Parent
	{
		public Child child;

		public Parent(final Child child)
		{
			super();
			this.child = child;
		}
	}

	public static class Child
	{
		public Lazy<Payload> lazy;

		public Child(final Lazy<Payload> lazy)
		{
			super();
			this.lazy = lazy;
		}
	}

	public static class Payload
	{
		public String data;

		public Payload(final String data)
		{
			super();
			this.data = data;
		}
	}
}
