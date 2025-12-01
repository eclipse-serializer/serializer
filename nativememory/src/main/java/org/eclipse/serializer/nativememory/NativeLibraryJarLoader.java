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
import java.io.InputStream;
import java.nio.file.Files;

import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

/**
 * Helper class to load the NativeMemoryAccosser native code library.
 * Try to detect the current os and architectuer, extract a matching
 * dynimac linked library from the jar this class is deplyed with to
 * system temp dir and load the lib.
 * 
 * Or try to load a names library from the systems library path.
 * 
 * Please be aware that this depends on the release jar it is deployed
 * with.
 */
public class NativeLibraryJarLoader
{
	private final static Logger logger = Logging.getLogger(NativeMemoryAccessor.class);
	
	private final static String JAR_NATIVEFOLDER = "/native/";
	private final static String LIBRARY_BASE_NAME = "libEclipseStoreNativeMemory";
	private final static String TEMP_DIR_PREFIX = "EclipseStoreNativeMemory";
	
	private final static String PROPERTY_OS_NAME = System.getProperty("os.name").toLowerCase();
	private final static String PROPERTY_OS_ARCH = System.getProperty("os.arch").toLowerCase();
	
	private final static String OS_NAME;
	private final static String OS_ARCH;
	private final static String LIB_FILE_EXTENSION;
	
	private static boolean initalized;
	
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
			case String os_name when os_name.contains("amd64") -> "x86_64";
			default -> "UNKOWN";
		};
		
		LIB_FILE_EXTENSION = switch(PROPERTY_OS_NAME) {
			case String os_name when os_name.startsWith("windows") -> ".dll";
			case String os_name when os_name.startsWith("linux") -> ".so";
			case String os_name when os_name.startsWith("mac") -> ".dylib";
			case String os_name when os_name.startsWith("Darwin") ->".dylib";
			default -> "UNKOWN";
		};
	}
	
	public static synchronized void loadNativeLibrary() {
		
		if(initalized) {
			logger.debug("native library allready initalized");
			return;
		}
		
		String libName = buildLibraryName();
		logger.info("loading native library: {}", libName);
		String library = extractLibrary(JAR_NATIVEFOLDER, libName);
		System.load(library);
		initalized = true;
	};
	
	public static synchronized void loadNativeLibrary(String nativeLibrary) {
		
		if(initalized) {
			logger.debug("native library allready initalized");
			return;
		}
		
		logger.info("loading native library: {}", nativeLibrary);
		System.loadLibrary(nativeLibrary);
		initalized = true;
	}
		
	private static String buildLibraryName() {
		return LIBRARY_BASE_NAME + "-" + OS_NAME + "-" + OS_ARCH + LIB_FILE_EXTENSION;
	}
		
	private static String extractLibrary(String source, String targetFileName) {
		
		try ( InputStream in = NativeLibraryJarLoader.class.getResourceAsStream(source + targetFileName)) {
			
			File tmpDir = Files.createTempDirectory(TEMP_DIR_PREFIX).toFile();
			tmpDir.deleteOnExit();
			File library = new File(tmpDir, targetFileName);
			library.deleteOnExit();
			
			Files.copy(in, library.toPath());
			return library.toString();
			
		} catch (Exception e) {
			throw new RuntimeException("failed to extract native library" + source + targetFileName + " to temp directory!", e);
		}
	}

	private NativeLibraryJarLoader() {
		// do not instantiate...
	}

}
