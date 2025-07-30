cmake_minimum_required(VERSION 3.13)
project(runner LANGUAGES CXX)

# Find Java
find_package(Java REQUIRED)
find_package(JNI REQUIRED)

set(FLUTTER_LIBRARY_NAME "flutter_library")
add_library(${FLUTTER_LIBRARY_NAME} SHARED
  "flutter_bridge.cc"
  "${FLUTTER_MANAGED_DIR}/generated_plugin_registrant.cc"
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

# Link Flutter Linux and required libraries
target_link_libraries(${FLUTTER_LIBRARY_NAME} PRIVATE 
  flutter 
  ${JNI_LIBRARIES}
)

# Include required directories
target_include_directories(${FLUTTER_LIBRARY_NAME} PUBLIC
#  "${CMAKE_CURRENT_SOURCE_DIR}"
  "${CMAKE_SOURCE_DIR}"
)

# Add dependencies
add_dependencies(${FLUTTER_LIBRARY_NAME} flutter_assemble)

# Define the application target. To change its name, change BINARY_NAME in the
# top-level CMakeLists.txt, not the value here, or `flutter run` will no longer
# work.
#
# Any new source files that you add to the application should be added here.
add_executable(${BINARY_NAME}
  "main.cc"
  "${FLUTTER_MANAGED_DIR}/generated_plugin_registrant.cc"
)

# Apply the standard set of build settings. This can be removed for applications
# that need different build settings.
apply_standard_settings(${BINARY_NAME})

# Add preprocessor definitions for the application ID.
add_definitions(-DAPPLICATION_ID="${APPLICATION_ID}")

# Add dependency libraries. Add any application-specific dependencies here.
target_link_libraries(${BINARY_NAME} PRIVATE flutter)
target_link_libraries(${BINARY_NAME} PRIVATE PkgConfig::GTK)

target_include_directories(${BINARY_NAME} PRIVATE "${CMAKE_SOURCE_DIR}")
