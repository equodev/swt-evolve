# A second build of the webview_all_linux plugin, against the system webkit2gtk-4.0 (libsoup 2.4),
# bundled as libwebview_all_linux_plugin_webkit40.so. The bridge loads it when the regular build,
# linked to webkit2gtk-4.1, does not load: distributions such as RHEL 8 only ship 4.0.
# Built only where the 4.0 development files are installed. Mirrors the build in the plugin's own
# linux/CMakeLists.txt: recheck it when webview_all_linux is upgraded.
find_package(PkgConfig REQUIRED)
pkg_check_modules(WEBKIT40 IMPORTED_TARGET webkit2gtk-4.0 libsoup-2.4)

set(WEBVIEW_PLUGIN_SOURCE_DIR
  "${CMAKE_CURRENT_SOURCE_DIR}/flutter/ephemeral/.plugin_symlinks/webview_all_linux/linux")

if(WEBKIT40_FOUND AND EXISTS "${WEBVIEW_PLUGIN_SOURCE_DIR}/src")
  set(WEBKIT40_PLUGIN "webview_all_linux_plugin_webkit40")
  file(GLOB_RECURSE WEBKIT40_PLUGIN_SOURCES CONFIGURE_DEPENDS "${WEBVIEW_PLUGIN_SOURCE_DIR}/src/*.cc")
  add_library(${WEBKIT40_PLUGIN} SHARED ${WEBKIT40_PLUGIN_SOURCES})
  apply_standard_settings(${WEBKIT40_PLUGIN})
  set_target_properties(${WEBKIT40_PLUGIN} PROPERTIES CXX_VISIBILITY_PRESET hidden)
  target_compile_definitions(${WEBKIT40_PLUGIN} PRIVATE FLUTTER_PLUGIN_IMPL
    # The machines this build is for run glib 2.56 (RHEL 8): newer glib API fails the build.
    GLIB_VERSION_MIN_REQUIRED=GLIB_VERSION_2_56 GLIB_VERSION_MAX_ALLOWED=GLIB_VERSION_2_56)
  # Ahead of any other include path, so the 4.0 and libsoup 2.4 headers win over 4.1 ones that the
  # environment may also list.
  target_include_directories(${WEBKIT40_PLUGIN} BEFORE PRIVATE
    ${WEBKIT40_INCLUDE_DIRS}
    "${WEBVIEW_PLUGIN_SOURCE_DIR}/include"
    "${WEBVIEW_PLUGIN_SOURCE_DIR}/src")
  target_compile_options(${WEBKIT40_PLUGIN} PRIVATE
    -include "${CMAKE_CURRENT_LIST_DIR}/webkit40_compat.h")
  target_link_libraries(${WEBKIT40_PLUGIN} PRIVATE flutter PkgConfig::GTK PkgConfig::WEBKIT40)
  install(TARGETS ${WEBKIT40_PLUGIN} LIBRARY DESTINATION "${INSTALL_BUNDLE_LIB_DIR}"
    COMPONENT Runtime)
else()
  message(STATUS "webkit2gtk-4.0 not found: the WebKitGTK 4.0 webview plugin is not built")
endif()
