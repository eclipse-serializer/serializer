package org.eclipse.serializer.nativememory;

/*-
 * #%L
 * Eclipse Serializer NativeMemory
 * %%
 * Copyright (C) 2023 - 2025 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.jar.Manifest;

import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

public class LibraryLoader
{
	private final static Logger logger = Logging.getLogger(NativeMemoryAccessor.class);
	
	private final static String PROPERTY_OS_NAME = System.getProperty("os.name").toLowerCase();
	private final static String PROPERTY_OS_ARCH = System.getProperty("os.arch").toLowerCase();
	
	private final static String OS_NAME;
	private final static String AS_ARCH;
	private final static String RELEASE_VERSION;
	
	static {
		OS_NAME = switch(PROPERTY_OS_NAME) {
			case String os_name when os_name.startsWith("windows") -> "WIN";
			case String os_name when os_name.startsWith("linux") -> "LINUX";
			case String os_name when os_name.startsWith("mac") -> "MACOS";
			case String os_name when os_name.startsWith("Darwin") -> "MACOS";
			default -> "UNKOWN";
		};
		
		AS_ARCH = switch(PROPERTY_OS_ARCH) {
			case String os_name when os_name.contains("64") -> "64";
			default -> "UNKOWN";
		};
		
		RELEASE_VERSION = getReleaseVersion();
		
		
		String library = extractLibrary("natives/windows/libEclipseStoreNativeMemory-windows-x86_64.dll");
		
		System.load(library);
		
	}
		
	public LibraryLoader() {
		logger.info("OS Name: {},  arch: {}", PROPERTY_OS_NAME, PROPERTY_OS_ARCH);
		
		logger.info("OS Name: {},  arch: {}, version {}", OS_NAME, AS_ARCH, RELEASE_VERSION);
		
	}
			
	private static String getReleaseVersion() {
		
		final String implVersion = "Implementation-Version";
		
		try(var in =  NativeMemoryAccessor.class.getResource("/META-INF/MANIFEST.MF").openStream()) {
			Manifest manifest = new Manifest(in);
			var entries = manifest.getEntries();
			
			logger.info("Manifest Entries: {}", entries);
			
			return NativeMemoryAccessor.class.getPackage().getImplementationVersion();
			
		} catch(IOException e) {
			throw new RuntimeException("Failed to read implementation version from manifest!");
		}
	}
	
	private static String extractLibrary(String name) {

		URL url = LibraryLoader.class.getResource("/" + name);
		File tmpDir;
		File library;
		
		try {
			tmpDir = Files.createTempDirectory("EclipseStoreNativeMemory").toFile();
			tmpDir.deleteOnExit();
			library = new File(tmpDir, "library.dll");
			library.deleteOnExit();
			
			try (InputStream in = url.openStream()) {
			    Files.copy(in, library.toPath());
			}
			
			return library.toString();
			
		} catch(IOException e) {
			throw new RuntimeException("failed to extract native library to temp directory!", e);
		}
		
		
		
	}
}
