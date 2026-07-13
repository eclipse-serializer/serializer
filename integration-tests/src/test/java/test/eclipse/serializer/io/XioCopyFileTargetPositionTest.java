package test.eclipse.serializer.io;

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

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.serializer.io.XIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

/**
 * Regression guard for the {@code XIO.copyFile} target-position overloads (internal issue #69,
 * originally reproduced in microstream-test):
 * <ul>
 * <li>{@code copyFile(source, target, targetPosition)} sized the transfer with
 * {@code sourceChannel.size()} although {@code transferFrom} consumes from the source's CURRENT
 * position — with a source not positioned at 0 (the AFS invariant: writable channels are
 * positioned at the file end), {@code transferFrom} returned 0 forever and the loop never
 * terminated. Fixed: sized from the current position, zero-progress breaks out.</li>
 * <li>{@code copyFile(source, target, targetPosition, length)} clamped availability with
 * {@code sourceChannel.size() - position}, subtracting the TARGET position from the SOURCE size —
 * mixing up the two cursors. Fixed: clamps with the source's own position.</li>
 * </ul>
 * Plain {@link FileChannel}s in a temp directory — no framework wiring. The formerly-hanging case
 * is guarded by the class-level {@link Timeout}: a regression shows up as a timeout, not a hung build.
 */
@Timeout(60)
public class XioCopyFileTargetPositionTest
{
	@TempDir
	Path tempDir;

	private FileChannel channel(final Path path) throws IOException
	{
		return FileChannel.open(path, CREATE, READ, WRITE);
	}

	@Test
	void copyFileWithTargetPositionTerminatesAndCopiesFromSourcePosition() throws IOException
	{
		final byte[] content = "0123456789".getBytes(StandardCharsets.US_ASCII);
		final Path sourcePath = this.tempDir.resolve("source.bin");
		final Path targetPath = this.tempDir.resolve("target.bin");
		Files.write(sourcePath, content);

		try(FileChannel source = this.channel(sourcePath); FileChannel target = this.channel(targetPath))
		{
			// the AFS invariant: a writable channel is positioned at the file END, not 0.
			source.position(source.size());

			// pre-fix: transferFrom returned 0 forever -> infinite loop (this call never returned).
			final long copied = XIO.copyFile(source, target, 0L);

			assertEquals(0L, copied,
				"a source at its end has zero available bytes - the copy must return the short count 0");
		}

		try(FileChannel source = this.channel(sourcePath); FileChannel target = this.channel(targetPath))
		{
			// mid-file source position: only the remainder from the position may be copied.
			source.position(4);
			final long copied = XIO.copyFile(source, target, 0L);

			assertEquals(content.length - 4, copied,
				"the copy must transfer exactly the bytes available from the source's current position");
			assertArrayEquals("456789".getBytes(StandardCharsets.US_ASCII),
				Files.readAllBytes(targetPath),
				"the copied bytes must be the source's remainder from its position");
		}
	}

	@Test
	void copyFileWithTargetPositionAndLengthClampsAgainstTheSourceCursor() throws IOException
	{
		final byte[] content = "abcdefghij".getBytes(StandardCharsets.US_ASCII); // 10 bytes
		final Path sourcePath = this.tempDir.resolve("source2.bin");
		final Path targetPath = this.tempDir.resolve("target2.bin");
		Files.write(sourcePath, content);
		// pre-existing target content so a large targetPosition is within bounds:
		Files.write(targetPath, new byte[16]);

		try(FileChannel source = this.channel(sourcePath); FileChannel target = this.channel(targetPath))
		{
			/*
			 * targetPosition (12) > source size (10): the pre-fix clamp computed
			 * size() - targetPosition = -2 available bytes although the whole source
			 * is readable from its position 0 - the copy was silently skipped short.
			 */
			final long copied = XIO.copyFile(source, target, 12L, 4L);

			assertEquals(4L, copied,
				"availability must be clamped against the SOURCE cursor, not the target position");
			final byte[] targetBytes = Files.readAllBytes(targetPath);
			assertArrayEquals("abcd".getBytes(StandardCharsets.US_ASCII),
				java.util.Arrays.copyOfRange(targetBytes, 12, 16),
				"the requested range must land at the target position");
		}
	}
}
