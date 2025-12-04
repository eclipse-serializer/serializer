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
import java.net.URL;
import java.nio.file.Files;

import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

/**
 * Helper class to load the NativeMemoryAccessor native code library.
 * Try to detect the current os and architecture, extract a matching
 * dynamic linked library from the jar this class is deployed with to
 * system temp dir and load the lib.
 * <br>
 * Or try to load a names library from the systems library path.
 * <br>
 * Please be aware that this depends on the release jar it is deployed
 * with.
 */
public class NativeLibraryJarLoader
{
	private final static Logger logger = Logging.getLogger(NativeMemoryAccessor.class);
	
	private final static String JAR_LIB_FOLDER = "/native/";
	private final static String LIBRARY_BASE_NAME = "libEclipseStoreNativeMemory";
	private final static String LIBRARY_FALLBACK_BASE_NAME = "EclipseStoreNativeMemory";
	private final static String TEMP_DIR_PREFIX = "EclipseStoreNativeMemory";
	
	private final static String PROPERTY_OS_NAME = System.getProperty("os.name").toLowerCase();
	private final static String PROPERTY_OS_ARCH = System.getProperty("os.arch").toLowerCase();
	
	private final static String OS_NAME;
	private final static String OS_ARCH;
	private final static String LIB_FILE_EXTENSION;

	private static final String NATIVE_LIBRARY_NAME;
	private static final String NATIVE_LIBRARY_FALLBACK_NAME;
	
	private static boolean initialized;
	
	static
	{
		OS_NAME = switch(PROPERTY_OS_NAME)
		{
			case String os_name when os_name.startsWith("windows") -> "windows";
			case String os_name when os_name.startsWith("linux") -> "linux";
			case String os_name when os_name.startsWith("mac") -> "macos";
			case String os_name when os_name.startsWith("Darwin") -> "macos";
			default -> "UNKNOWN";
		};
		
		OS_ARCH = switch(PROPERTY_OS_ARCH)
		{
			case String os_name when os_name.contains("aarch64") -> "arm64";
			case String os_name when os_name.contains("arm64") -> "arm64";
			case String os_name when os_name.contains("amd64") -> "x86_64";
			case String os_name when os_name.contains("x86_64") -> "x86_64";
			default -> "UNKNOWN";
		};
		
		LIB_FILE_EXTENSION = switch(PROPERTY_OS_NAME)
		{
			case String os_name when os_name.startsWith("windows") -> ".dll";
			case String os_name when os_name.startsWith("linux") -> ".so";
			case String os_name when os_name.startsWith("mac") -> ".dylib";
			case String os_name when os_name.startsWith("Darwin") ->".dylib";
			default -> "UNKNOWN";
		};
		
		NATIVE_LIBRARY_NAME = JAR_LIB_FOLDER + LIBRARY_BASE_NAME + "-" + OS_NAME + "-" + OS_ARCH + LIB_FILE_EXTENSION;
		NATIVE_LIBRARY_FALLBACK_NAME = JAR_LIB_FOLDER + LIBRARY_FALLBACK_BASE_NAME + LIB_FILE_EXTENSION;
	}
	
	public static synchronized void loadNativeLibrary()
	{
		if(initialized)
		{
			logger.debug("native library already initialized");
			return;
		}
		
		URL libUrl = getJarLibraryURL();
		String library = extractLibrary(libUrl);
		logger.info("loading native library: {}", library);
		System.load(library);
		initialized = true;
	}

    /**
     * Load user defined library.
     * The library must be named according to the rules of
     * java.lang.System.loadLibrary(String)
     *
     * @param nativeLibrary the name of the library.
     */
	public static synchronized void loadNativeLibrary(String nativeLibrary)
	{
		if(initialized)
		{
			logger.debug("native library already initialized");
			return;
		}
		
		logger.info("loading native library: {}", nativeLibrary);
		System.loadLibrary(nativeLibrary);
		initialized = true;
	}
		
	private static String extractLibrary(URL libUrl)
	{
		if(libUrl == null)
		{
			throw new RuntimeException("No matching native library found in jar!");
		}
		
		try (InputStream in = libUrl.openStream())
		{
			File tmpDir = Files.createTempDirectory(TEMP_DIR_PREFIX).toFile();
			tmpDir.deleteOnExit();
			File library = new File(tmpDir, libUrl.getFile());
			library.deleteOnExit();
			
			Files.copy(in, library.toPath());
			return library.toString();
			
		}
		catch (Exception e)
		{
			throw new RuntimeException("failed to extract native library " + JAR_LIB_FOLDER + libUrl + " to temp directory!", e);
		}
	}

	private static URL getJarLibraryURL()
	{
		URL url = NativeLibraryJarLoader.class.getResource(NATIVE_LIBRARY_NAME);
		if(url == null)
		{
			logger.warn("library {} not found!", NATIVE_LIBRARY_NAME);
			url = NativeLibraryJarLoader.class.getResource(NATIVE_LIBRARY_FALLBACK_NAME);
			if(url == null)
			{
				logger.error("alterate library {} not found!", NATIVE_LIBRARY_FALLBACK_NAME);
			}
		}
		return url;
	}

	private NativeLibraryJarLoader()
	{
		// do not instantiate...
	}

}
