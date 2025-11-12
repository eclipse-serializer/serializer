
find_package(JNI)

if (JNI_FOUND)
    message (STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
    message (STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")

    include_directories(${JNI_INCLUDE_DIRS})
else()
    message (STATUS "JNI NOT FOUND")
endif()

add_library (EclipseStoreNativeMemory SHARED 
	 
	"org_eclipse_serializer_nativememory_NativeMemoryAccessor.cpp" "org_eclipse_serializer_nativememory_NativeMemoryAccessor.h")

if (CMAKE_VERSION VERSION_GREATER 3.12)
  set_property(TARGET EclipseStoreNativeMemory PROPERTY CXX_STANDARD 11)
  #set_target_properties(EclipseStoreNativeMemory PROPERTIES LINKER_LANGUAGE CXX)
endif()

