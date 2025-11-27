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
	
	private final static String JAR_NATIVEFOLDER = "native";
	private final static String LIBRARY_BASE_NAME = "libEclipseStoreNativeMemory";
	
	private final static String PROPERTY_OS_NAME = System.getProperty("os.name").toLowerCase();
	private final static String PROPERTY_OS_ARCH = System.getProperty("os.arch").toLowerCase();
	
	private final static String OS_NAME;
	private final static String OS_ARCH;
	private final static String RELEASE_VERSION;
	private final static String LIB_FILE_EXTENSION;
	
	static {
		OS_NAME = switch(PROPERTY_OS_NAME) {
			case String os_name when os_name.startsWith("windows") -> "windows";
			case String os_name when os_name.startsWith("linux") -> "ubuntu";
			case String os_name when os_name.startsWith("mac") -> "macos";
			case String os_name when os_name.startsWith("Darwin") -> "macos";
			default -> "UNKOWN";
		};
		
		OS_ARCH = switch(PROPERTY_OS_ARCH) {
			case String os_name when os_name.contains("aarch64") -> "arm";
			case String os_name when os_name.contains("arm64") -> "arm";
			case String os_name when os_name.contains("amd64") -> "x86_amd64";
			default -> "UNKOWN";
		};
		
		LIB_FILE_EXTENSION = switch(PROPERTY_OS_NAME) {
			case String os_name when os_name.startsWith("windows") -> ".dll";
			case String os_name when os_name.startsWith("linux") -> ".so";
			case String os_name when os_name.startsWith("mac") -> ".dylib";
			case String os_name when os_name.startsWith("Darwin") ->".dylib";
		default -> "UNKOWN";
	};
		
		RELEASE_VERSION = getReleaseVersion();
		
		String libPath = buildInternalPath(buildLibraryName());
		logger.info("selected native library: {}", libPath);
		
		//String library = extractLibrary("natives/windows/libEclipseStoreNativeMemory-windows-x86_64.dll");
		
//		String library = extractLibrary(buildInternalPath(buildLibraryName()));
//		System.load(library);
		
	}
		
	public LibraryLoader() {
		logger.info("OS Name: {},  arch: {}", PROPERTY_OS_NAME, PROPERTY_OS_ARCH);
		
		logger.info("OS Name: {},  arch: {}, version {}", OS_NAME, OS_ARCH, RELEASE_VERSION);
		
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
	
	private static String buildLibraryName() {
		return LIBRARY_BASE_NAME + "-" + OS_NAME + "-" + OS_ARCH + LIB_FILE_EXTENSION;
	}
	
	private static String buildInternalPath(String libName) {
		return JAR_NATIVEFOLDER + "/" + OS_NAME + "/" + libName;
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
