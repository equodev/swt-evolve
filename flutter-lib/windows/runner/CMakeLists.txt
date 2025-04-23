cmake_minimum_required(VERSION 3.14)
project(runner LANGUAGES CXX)

# Find Java
find_package(Java REQUIRED)
find_package(JNI REQUIRED)

# If JNI not found automatically, you can set paths manually:
# set(JAVA_HOME "C:/Program Files/Java/jdk-17")
# include_directories(
#     "${JAVA_HOME}/include"
#     "${JAVA_HOME}/include/win32"
# )

set(FLUTTER_LIBRARY_NAME "flutter_library")
add_library(${FLUTTER_LIBRARY_NAME} SHARED
  "flutter_bridge.cpp"
  "flutter_bridge.h"
  "flutter_window.cpp"
  "utils.cpp"
  "win32_window.cpp"
  "${FLUTTER_MANAGED_DIR}/generated_plugin_registrant.cc"
#  "${flutter_library_BINARY_DIR}/flutter_assemble"
)

# Add JNI include directories
target_include_directories(${FLUTTER_LIBRARY_NAME} PRIVATE 
  ${JNI_INCLUDE_DIRS}
)

# Add compiler definitions for DLL export
target_compile_definitions(${FLUTTER_LIBRARY_NAME} PUBLIC FLUTTER_LIBRARY_EXPORTS)

# Configure Flutter dependencies
apply_standard_settings(${FLUTTER_LIBRARY_NAME})
set_target_properties(${FLUTTER_LIBRARY_NAME} PROPERTIES
  CXX_VISIBILITY_PRESET hidden)
#target_compile_features(${FLUTTER_LIBRARY_NAME} PUBLIC cxx_std_17)

# Link Flutter Windows and required libraries
target_link_libraries(${FLUTTER_LIBRARY_NAME} PRIVATE 
  flutter 
  flutter_wrapper_app
  ${JNI_LIBRARIES}
)
target_link_libraries(${FLUTTER_LIBRARY_NAME} PRIVATE "dwmapi.lib")

# Include required directories
target_include_directories(${FLUTTER_LIBRARY_NAME} PUBLIC
#  "${CMAKE_CURRENT_SOURCE_DIR}"
  "${CMAKE_SOURCE_DIR}"
)

# Add dependencies
add_dependencies(${FLUTTER_LIBRARY_NAME} flutter_assemble)

# Installation rules
#install(TARGETS ${FLUTTER_LIBRARY_NAME} DESTINATION "${INSTALL_BUNDLE_LIB_DIR}")
# Modify the install rules to handle the dynamic library
#install(TARGETS ${FLUTTER_LIBRARY_NAME}
#  LIBRARY DESTINATION "${INSTALL_BUNDLE_LIB_DIR}"
#  COMPONENT Runtime)

# Define the application target. To change its name, change BINARY_NAME in the
# top-level CMakeLists.txt, not the value here, or `flutter run` will no longer
# work.
#
# Any new source files that you add to the application should be added here.
add_executable(${BINARY_NAME} WIN32
  "flutter_window.cpp"
  "main.cpp"
  "utils.cpp"
  "win32_window.cpp"
  "${FLUTTER_MANAGED_DIR}/generated_plugin_registrant.cc"
  "Runner.rc"
  "runner.exe.manifest"
)

# Apply the standard set of build settings. This can be removed for applications
# that need different build settings.
apply_standard_settings(${BINARY_NAME})

# Add preprocessor definitions for the build version.
target_compile_definitions(${BINARY_NAME} PRIVATE "FLUTTER_VERSION=\"${FLUTTER_VERSION}\"")
target_compile_definitions(${BINARY_NAME} PRIVATE "FLUTTER_VERSION_MAJOR=${FLUTTER_VERSION_MAJOR}")
target_compile_definitions(${BINARY_NAME} PRIVATE "FLUTTER_VERSION_MINOR=${FLUTTER_VERSION_MINOR}")
target_compile_definitions(${BINARY_NAME} PRIVATE "FLUTTER_VERSION_PATCH=${FLUTTER_VERSION_PATCH}")
target_compile_definitions(${BINARY_NAME} PRIVATE "FLUTTER_VERSION_BUILD=${FLUTTER_VERSION_BUILD}")

# Disable Windows macros that collide with C++ standard library functions.
target_compile_definitions(${BINARY_NAME} PRIVATE "NOMINMAX")

# Add dependency libraries and include directories. Add any application-specific
# dependencies here.
target_link_libraries(${BINARY_NAME} PRIVATE flutter flutter_wrapper_app)
target_link_libraries(${BINARY_NAME} PRIVATE "dwmapi.lib")
target_include_directories(${BINARY_NAME} PRIVATE "${CMAKE_SOURCE_DIR}")

# Run the Flutter tool portions of the build. This must not be removed.
add_dependencies(${BINARY_NAME} flutter_assemble)
