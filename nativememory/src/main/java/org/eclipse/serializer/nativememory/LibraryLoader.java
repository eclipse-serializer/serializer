package org.eclipse.serializer.nativememory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;

public class LibraryLoader
{
	private final static Logger logger = Logging.getLogger(NativeMemoryAccessor.class);
	
	private final static String JAR_NATIVEFOLDER = "/native/";
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
		
		String libName = buildLibraryName();
		logger.info("selected native library: {}", libName);
						
		String library = extractLibrary(JAR_NATIVEFOLDER, libName);
		System.load(library);
		
	}
		
	public LibraryLoader() {
		logger.info("OS Name: {},  arch: {}", PROPERTY_OS_NAME, PROPERTY_OS_ARCH);
		logger.info("OS Name: {},  arch: {}, version {}", OS_NAME, OS_ARCH, RELEASE_VERSION);
		
	}
		
	private static String buildLibraryName() {
		return LIBRARY_BASE_NAME + "-" + OS_NAME + "-" + OS_ARCH + LIB_FILE_EXTENSION;
	}
		
	private static String extractLibrary(String source, String targetFileName) {
		
		try ( InputStream in = LibraryLoader.class.getResourceAsStream(source + targetFileName)) {
			
			File tmpDir = Files.createTempDirectory("EclipseStoreNativeMemory").toFile();
			tmpDir.deleteOnExit();
			File library = new File(tmpDir, targetFileName);
			library.deleteOnExit();
			
			Files.copy(in, library.toPath());
			return library.toString();
			
		} catch (Exception e) {
			throw new RuntimeException("failed to extract native library to temp directory!", e);
		}
	}
}
