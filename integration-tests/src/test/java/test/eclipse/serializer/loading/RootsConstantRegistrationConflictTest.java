package test.eclipse.serializer.loading;

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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.ByteBuffer;

import org.eclipse.serializer.PassThroughSerializerFoundation;
import org.eclipse.serializer.SerializerFoundation;
import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.collections.types.XGettingCollection;
import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.binary.types.BinaryField;
import org.eclipse.serializer.persistence.binary.types.ChunksWrapper;
import org.eclipse.serializer.persistence.binary.types.CustomBinaryHandler;
import org.eclipse.serializer.persistence.types.PersistenceIdSet;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceManager;
import org.eclipse.serializer.persistence.types.PersistenceObjectRegistry;
import org.eclipse.serializer.persistence.types.PersistenceSource;
import org.eclipse.serializer.persistence.types.PersistenceTarget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Deterministic regression test for internal issue #87 (a regression from serializer#299 /
 * issue #72).
 * <p>
 * #299 deferred the loader's object-registry publication of freshly built instances to a
 * post-{@code complete} phase ({@code BinaryLoader#registerBuiltInstances}) and guarded it with a
 * hard consistency error if a different instance was already registered under an object id. That
 * guard fired on a legitimate same-thread registration during a roots load: the roots/constants
 * resolution ({@code BinaryHandlerPersistenceRootsDefault#updateState} →
 * {@code registerConstant}) registers a canonical constant instance under an object id for which
 * the loader had already created a provisional local copy while building a sibling that references
 * it. The storage then failed to start.
 * <p>
 * This test reproduces that mechanism without a full storage/roots setup: a custom type handler
 * registers a canonical constant under its child's object id from within its {@code complete}
 * phase — exactly the "register during update/complete, after siblings were locally created"
 * shape — and asserts the load completes and adopts the canonical instance. Against the unpatched
 * loader it fails with {@code PersistenceExceptionConsistencyObject} from
 * {@code registerBuiltInstances}.
 */
@Timeout(60)
public class RootsConstantRegistrationConflictTest
{
	PersistenceManager<Binary> persistenceManager;

	final BulkList<Binary> recordedChunks = BulkList.New();

	@AfterEach
	public void afterTest()
	{
		if(this.persistenceManager != null)
		{
			this.persistenceManager.close();
		}
		for(final Binary chunk : this.recordedChunks)
		{
			for(final ByteBuffer buffer : chunk.buffers())
			{
				XMemory.deallocateDirectByteBuffer(buffer);
			}
		}
		this.recordedChunks.clear();
	}

	private PersistenceManager<Binary> createPersistenceManager(final ContainerHandler containerHandler)
	{
		final PersistenceTarget<Binary> target = new PersistenceTarget<Binary>()
		{
			@Override
			public void write(final Binary data)
			{
				final ByteBuffer[] buffers = data.buffers();
				final ByteBuffer[] copies  = new ByteBuffer[buffers.length];
				for(int i = 0; i < buffers.length; i++)
				{
					final ByteBuffer source = buffers[i].duplicate();
					final ByteBuffer copy   = XMemory.allocateDirectNative(source.remaining());
					copy.put(source);
					copy.flip();
					copies[i] = copy;
				}
				RootsConstantRegistrationConflictTest.this.recordedChunks.add(ChunksWrapper.New(copies));
			}

			@Override
			public boolean isWritable()
			{
				return true;
			}
		};

		final PersistenceSource<Binary> source = new PersistenceSource<Binary>()
		{
			@Override
			public XGettingCollection<? extends Binary> read()
			{
				return RootsConstantRegistrationConflictTest.this.recordedChunks;
			}

			@Override
			public XGettingCollection<? extends Binary> readByObjectIds(final PersistenceIdSet[] oids)
			{
				return RootsConstantRegistrationConflictTest.this.recordedChunks;
			}
		};

		final SerializerFoundation<?> foundation = PassThroughSerializerFoundation.New()
			.setPersistenceSource(source)
			.setPersistenceTarget(target)
		;
		foundation.registerCustomTypeHandler(containerHandler);

		return this.persistenceManager = foundation.createPersistenceManager();
	}

	@Test
	void constantRegisteredDuringCompleteMustNotFailThePublication()
	{
		final ContainerHandler           handler  = new ContainerHandler();
		final PersistenceManager<Binary> manager  = this.createPersistenceManager(handler);
		final PersistenceObjectRegistry  registry = manager.objectRegistry();

		final Node      child     = new Node("x");
		final Container container = new Container(child);
		final long containerOid = manager.store(container);
		final long childOid     = registry.lookupObjectId(child);

		// fresh session: the graph exists only in the recorded chunks.
		registry.clear();

		// arm the handler to register a canonical, value-equal-but-distinct child instance under
		// the child's object id during its complete phase (mimics roots/constants registration).
		handler.arm(registry);

		final Object reloaded = assertDoesNotThrow(() -> manager.getObject(containerOid),
			"a same-thread constant registration during the build must not fail the loader's publication");

		assertNotNull(reloaded);
		final Container reloadedContainer = (Container)reloaded;
		assertNotNull(reloadedContainer.child, "the child reference must be wired");
		assertEquals("x", reloadedContainer.child.label, "the child value must be intact");

		assertNotNull(handler.registeredCanonical, "the handler must have registered a canonical child");
		assertSame(handler.registeredCanonical, registry.lookupObject(childOid),
			"the loader must adopt the canonical instance registered during the build");
	}

	///////////////////////////////////////////////////////////////////////////
	// test doubles //
	/////////////////

	/**
	 * A custom handler for {@link Container} that, from within {@link #complete}, registers a
	 * canonical constant instance under the child's object id — reproducing the shape of the
	 * roots/constants resolution registering an instance after sibling build items already exist.
	 */
	static final class ContainerHandler extends CustomBinaryHandler<Container>
	{
		private final BinaryField<Container> child = Field(Node.class, c -> c.child, (c, n) -> c.child = n);

		private PersistenceObjectRegistry registry;

		Node registeredCanonical;

		ContainerHandler()
		{
			super(Container.class);
		}

		void arm(final PersistenceObjectRegistry registry)
		{
			this.registry = registry;
		}

		@Override
		protected Container instantiate(final Binary data)
		{
			return new Container(null);
		}

		@Override
		public void complete(final Binary data, final Container instance, final PersistenceLoadHandler handler)
		{
			super.complete(data, instance, handler);
			if(this.registry != null)
			{
				// the child reference oid is the single reference in the content, at offset 0.
				final long childOid = data.read_long(0);
				this.registeredCanonical = new Node(instance.child.label);
				this.registry.registerConstant(childOid, this.registeredCanonical);
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// data types //
	///////////////

	public static class Container
	{
		Node child;

		public Container(final Node child)
		{
			super();
			this.child = child;
		}
	}

	public static class Node
	{
		String label;

		public Node(final String label)
		{
			super();
			this.label = label;
		}
	}
}
