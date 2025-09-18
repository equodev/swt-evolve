#include "flutter_bridge.h"

#include <flutter_linux/flutter_linux.h>
#include <gdk/gdkx.h>
#include <gio/gio.h>
#include <glib-object.h>
#include <gtk/gtk.h>

#include <dlfcn.h>    // dladdr
#include <filesystem> // std::filesystem
#include <string>     // std::string

#include "flutter/generated_plugin_registrant.h"

struct FlutterWindow {
  GtkWidget *view;
};

extern "C" void DummyExportedFunction() {
  // This function exists solely to provide an address within the shared library.
}

std::string GetSharedLibraryPath() {
  Dl_info dl_info;

  void *address_in_so = reinterpret_cast<void *>(&DummyExportedFunction);

  if (dladdr(address_in_so, &dl_info) != 0) {
    if (dl_info.dli_fname != nullptr) {
      std::filesystem::path so_path(dl_info.dli_fname);
      return so_path.parent_path().parent_path().string();
    } else {
      g_printerr("Error: dli_fname is null\n");
    }
  } else {
    g_printerr("Error getting shared library info with dladdr\n");
  }
  return ""; // Return empty string on failure
}

uintptr_t InitializeFlutterWindow(jint port, void *parentWnd, jlong widget_id,
                                  const char *widget_name, const char *theme, jint background_color, jint parent_background_color) {
  FlDartProject *project = fl_dart_project_new();
  std::string base_path = GetSharedLibraryPath();
  fl_dart_project_set_aot_library_path(
      project, (base_path + "/bundle/lib/libapp.so").c_str());
  fl_dart_project_set_assets_path(
      project, (gchar *)(base_path + "/bundle/data/flutter_assets/").c_str());
  fl_dart_project_set_icu_data_path(
      project, (gchar *)(base_path + "/bundle/data/icudtl.dat").c_str());
  char port_c[256];
  sprintf(port_c, "%d", port);
  char widget_id_c[256];
  sprintf(widget_id_c, "%ld", widget_id);

  char background_color_c[256];
  sprintf(background_color_c, "%d", background_color);

  char parent_background_color_c[256];
  sprintf(parent_background_color_c, "%d", parent_background_color);

  char *args[7];
  args[0] = port_c;
  args[1] = widget_id_c;
  args[2] = (char *)widget_name;
  args[3] = (char *)theme;
  args[4] = background_color_c;
  args[5] = parent_background_color_c;
  args[6] = 0;

  fl_dart_project_set_dart_entrypoint_arguments(project, args);

  GtkWidget *parent_widget = GTK_WIDGET(parentWnd);

  FlView *view = fl_view_new(project);
  GtkWidget *view_widget = GTK_WIDGET(view);

  FlutterWindow *widget_context = new FlutterWindow;
  // Store references - we'll create the SWT container from Java side
  widget_context->view = view_widget;

  g_print("Flutter initialized with parent: %p, view: %p\n", parent_widget, view_widget);
  
  // Register plugins
  fl_register_plugins(FL_PLUGIN_REGISTRY(view));
  
  return (uintptr_t)widget_context;
}

JNIEXPORT jlong JNICALL
Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_InitializeFlutterWindow(
    JNIEnv *env, jclass cls, jint port, jlong parent, jlong widget_id,
    jstring widget_name, jstring theme, jint background_color, jint parent_background_color) {
  const char *widget_name_cstr = env->GetStringUTFChars(widget_name, NULL);
  const char *theme_cstr = env->GetStringUTFChars(theme, NULL);
  jlong result = (jlong)InitializeFlutterWindow(port, (void *)parent, widget_id,
                                                widget_name_cstr, theme_cstr, background_color, parent_background_color);
  env->ReleaseStringUTFChars(widget_name, widget_name_cstr);
  env->ReleaseStringUTFChars(theme, theme_cstr);
  return result;
}

JNIEXPORT jlong JNICALL
Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_GetView(JNIEnv *env,
                                                          jclass cls,
                                                          jlong context) {
  FlutterWindow *window = reinterpret_cast<FlutterWindow *>(context);
  // Return the Flutter view widget, not the parent
  return (jlong)window->view;
}

JNIEXPORT void JNICALL
Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_Dispose(JNIEnv *env,
                                                          jclass cls,
                                                          jlong context) {
  FlutterWindow *window = reinterpret_cast<FlutterWindow *>(context);
  if (window) {
    // Try to gracefully disconnect the Flutter view from its parent BEFORE
    // the SWT container is destroyed, to avoid the "implicit view" removal error
    if (window->view && GTK_IS_WIDGET(window->view)) {
      GtkWidget* parent = gtk_widget_get_parent(window->view);
      if (parent && GTK_IS_CONTAINER(parent)) {
        g_object_ref(window->view); // Add a reference to prevent destruction
        gtk_container_remove(GTK_CONTAINER(parent), window->view);
      }
    }
    delete window;
  }
}

JNIEXPORT void JNICALL
Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_SetBounds(
    JNIEnv *env, jclass cls, jlong context, jint x, jint y, jint width,
    jint height, jint vx, jint vy, jint vwidth, jint vheight) {
  FlutterWindow *window = reinterpret_cast<FlutterWindow *>(context);
  g_print("Setting bounds to flutter view: %d, %d, %d, %d\n", vx, vy, vwidth, vheight);
  
  if (!window->view) {
    return;
  }
  gtk_widget_set_size_request(window->view, vwidth, vheight);
}