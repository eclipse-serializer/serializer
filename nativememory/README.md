# Native Memory Accessor Module
The Native Memory Accessor module provides a Java 25 upwards compatible implementation of the default Eclipse Serializers MemoryAccessor. This implementation used the new Java Foreign Memory API (java.lang.foreign) or custom native implementations to replace the obsolete sun.misc.Unsafe APIs.

Please note that we only support a limited set of operating systems and architectures.
If your system is not supported please try to build the required native library by yourself.
See [Building locally](#Building-locally).


## Supported systems
| Operating system | Architecture |
|:-----------------|:-------------|
| Linux            | 64Bit x86    |
| Linux            | 64Bit arm    |
| Windows          | 64Bit x86    |
| Mac OS           | 64Bit x86    |
| Mac OS           | 64Bit arm    |


## Usage
### Maven
```Maven
<dependency>
	<groupId>org.eclipse.serializer</groupId>
	<artifactId>nativememory</artifactId>
	<version>4.0.0-SNAPSHOT</version>
</dependency>
```
### Automatic setup
By default, the required library will be loaded automatically if
- Java version 25 or greater is used
- the required module 'nativememory' module is on the class path.

### Manual setup
- ensure that the 'nativememory' module is on the class path.
- build or extract the native library from the jar to a custom folder.
- set up the java library path to include the native libraries' directory.

- initialize the NativeMemoryAccessor in code before it is used,
the library must be named according to the rules defined by java.lang.System.loadLibrary(String).
```java
NativeMemoryAccessor.New("libEclipseStoreNativeMemory");
```

## Building locally

### Prerequisites:
- C++ 11 compatible compiler
- CMake version 3.12 or greater
- configure JAVA_HOME

### building with maven
If running a local maven build the created jar only contains the native libraries
build for the current system, this library is named 'libEclipseStoreNativeMemory' without
os or architecture postfixes.

### building the native library using Cmake
switch to a projects '\target' sub folder

configure the local build
```bash
user@hostname:~/eclipse-serializer/nativememory/target$cmake ..
```
build the library
```bash
cmake --build . --config Release
```
The locally build library can be found in 'target\libs' folder.


## Building the released jar
The released jar contains some native libraries for different operating systems and architectures. As the build and packaging is quite complex and requires c++ builds for several os we currently don't support that kind of build locally. If interested how it is done have a look at the projects GitHub build scripts.  