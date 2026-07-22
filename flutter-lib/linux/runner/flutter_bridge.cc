#include "flutter_bridge.h"

#include <flutter_linux/flutter_linux.h>
#include <gdk/gdkx.h>
#include <gio/gio.h>
#include <glib-object.h>
#include <gtk/gtk.h>

#include <dlfcn.h>    // dladdr
#include <filesystem> // std::filesystem
#include <stdlib.h>   // setenv
#include <string>     // std::string

#include "flutter/generated_plugin_registrant.h"

// The webview_all Browser backend on Linux is WebKitGTK, which defaults to a
// hardware GL renderer (accelerated compositing / DMABUF). Now that the FlView
// is no longer destructively reparented (see the GtkOverlay note in
// InitializeFlutterWindow), WebKit's GL renderer normally works and composites
// correctly as an overlay child, so it is left enabled by default. Some
// embedded/X11 environments without DRI3 may still need it forced onto the
// software renderer — opt in at runtime with EQUO_WEBKIT_SOFTWARE=1 (no rebuild
// needed). Forced software rendering can itself make the overlaid webview paint
// black, which is why it is NOT the default. Runs at .so load, before any
// GTK/WebKit use.
__attribute__((constructor)) static void EquoConfigureWebKit() {
  const char* software = getenv("EQUO_WEBKIT_SOFTWARE");
  if (software != nullptr && software[0] == '1') {
    setenv("WEBKIT_DISABLE_COMPOSITING_MODE", "1", 1);
    setenv("WEBKIT_DISABLE_DMABUF_RENDERER", "1", 1);
  }
}

struct FlutterWindow {
  GtkWidget *view;
  GtkWidget *headless_window;  // Only used in headless mode
  GtkWidget *top_window;       // Desktop-native (100% Flutter) top-level window
  bool closed;                 // Set when the desktop-native window is closed by the user
};

extern "C" void DummyExportedFunction() {
  // This function exists solely to provide an address within the shared library.
}

// External app-bundle directory set from Java (dev.equo.ewt.bundleDir). When non-empty it
// overrides the dladdr "next to myself" lookup so the bridge can boot a bundle it does not
// sit beside. Empty selects the dladdr lookup.
static std::string g_bundle_override;

std::string GetSharedLibraryPath() {
  if (!g_bundle_override.empty()) {
    return g_bundle_override;
  }
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
  return "";
}

uintptr_t InitializeFlutterWindow(jint port, void *parentWnd, jlong widget_id,
                                  const char *widget_name, const char *theme, jint background_color, jint parent_background_color) {
  FlDartProject *project = fl_dart_project_new();
  std::string base_path = GetSharedLibraryPath();
  // AOT (release) ships libapp.so; a --debug (JIT) build has no libapp.so and runs the kernel from
  // flutter_assets instead — which also brings up the Dart VM Service for DTD/MCP introspection.
  // Only point the engine at an AOT library when one is actually present.
  std::string aot_path = base_path + "/bundle/lib/libapp.so";
  if (g_file_test(aot_path.c_str(), G_FILE_TEST_EXISTS))
    fl_dart_project_set_aot_library_path(project, aot_path.c_str());
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

  FlutterWindow *widget_context = new FlutterWindow;
  widget_context->headless_window = nullptr;
  widget_context->top_window = nullptr;
  widget_context->closed = false;

  // Headless mode: create a hidden window to host Flutter
  if (!parentWnd) {
    if (!gtk_init_check(nullptr, nullptr)) {
      g_printerr("Failed to initialize GTK\n");
      return 0;
    }

    GtkWidget *window = gtk_window_new(GTK_WINDOW_TOPLEVEL);
    gtk_window_set_default_size(GTK_WINDOW(window), 1280, 720);
    gtk_widget_realize(window);

    FlView *view = fl_view_new(project);
    GtkWidget *view_widget = GTK_WIDGET(view);

    gtk_container_add(GTK_CONTAINER(window), view_widget);
    gtk_widget_show(view_widget);

    // Directly allocate size to the view (triggers size-allocate signal to Flutter engine)
    GtkAllocation allocation = {0, 0, 1280, 720};
    gtk_widget_size_allocate(view_widget, &allocation);

    widget_context->view = view_widget;
    widget_context->headless_window = window;

    g_print("Flutter headless initialized with window: %p, view: %p\n", window, view_widget);

    fl_register_plugins(FL_PLUGIN_REGISTRY(view));

    // Pump events to propagate size to Flutter engine
//    while (gtk_events_pending()) {
//      gtk_main_iteration();
//    }

    return (uintptr_t)widget_context;
  }

  // Embedded mode: use provided parent
  GtkWidget *parent_widget = GTK_WIDGET(parentWnd);

  FlView *view = fl_view_new(project);
  GtkWidget *view_widget = GTK_WIDGET(view);

  // Pre-wrap the FlView in a GtkOverlay and embed THAT into the SWT composite.
  // The webview_all (WebKitGTK) plugin's ensure_overlay() needs the FlView to
  // live inside a GtkOverlay; when it doesn't, the plugin destructively
  // reparents the already-realized FlView (gtk_container_remove + re-add),
  // which recreates its GdkWindow and invalidates Flutter's live GL context —
  // the X server then aborts the process with GLX BadAccess on
  // glXMakeContextCurrent (request_code 151, minor_code 26). By providing the
  // overlay up front, the plugin takes its non-destructive "parent is already
  // an overlay" path and the context survives.
  GtkWidget *overlay = gtk_overlay_new();
  gtk_widget_set_hexpand(view_widget, TRUE);
  gtk_widget_set_vexpand(view_widget, TRUE);
  gtk_container_add(GTK_CONTAINER(overlay), view_widget);
  gtk_widget_show(view_widget);
  gtk_widget_show(overlay);

  // The overlay is the widget the Java side reparents into the SWT container.
  widget_context->view = overlay;

  g_print("Flutter initialized with parent: %p, overlay: %p, view: %p\n",
          parent_widget, overlay, view_widget);

  // Register plugins
  fl_register_plugins(FL_PLUGIN_REGISTRY(view));

  return (uintptr_t)widget_context;
}

// =================================================================================================
// Desktop-native (100% Flutter) display window + the unified FlutterNative JNI surface.
//
// Hosts the ENTIRE Dart-backed SWT tree in one native top-level GTK window: a single FlView fills a
// GtkWindow and connects back over the comm port, rendering the whole Display like the web client.
// The same JNI entry points serve both an embedded view and this top-level window; the context is a
// FlutterWindow* either way, and top_window != null selects the window behaviour.
// =================================================================================================

// "destroy" handler: the user closed the top-level window. Flag it so pump() reports back.
static void on_display_window_destroy(GtkWidget * /*widget*/, gpointer data) {
  FlutterWindow *ctx = static_cast<FlutterWindow *>(data);
  if (ctx) {
    ctx->closed = true;
    // The GTK widgets are gone now; avoid dangling pointers from later calls.
    ctx->top_window = nullptr;
    ctx->view = nullptr;
  }
}

// Creates a visible top-level GtkWindow hosting one FlView for the whole Display.
FlutterWindow *createDisplayWindow(int port, int64_t displayId, const char *widget_name,
                                   const char *theme, int backgroundColor, int width, int height) {
  FlDartProject *project = fl_dart_project_new();
  std::string base_path = GetSharedLibraryPath();
  // See note above: skip the AOT library in --debug (JIT) builds so the engine runs the kernel
  // from flutter_assets and exposes the Dart VM Service.
  std::string aot_path = base_path + "/bundle/lib/libapp.so";
  if (g_file_test(aot_path.c_str(), G_FILE_TEST_EXISTS))
    fl_dart_project_set_aot_library_path(project, aot_path.c_str());
  fl_dart_project_set_assets_path(project, (gchar *)(base_path + "/bundle/data/flutter_assets/").c_str());
  fl_dart_project_set_icu_data_path(project, (gchar *)(base_path + "/bundle/data/icudtl.dat").c_str());

  char port_c[256];
  sprintf(port_c, "%d", port);
  char id_c[256];
  sprintf(id_c, "%ld", (long)displayId);
  char bg_c[256];
  sprintf(bg_c, "%d", backgroundColor);

  // Same arg layout as the embedded path: [port, id, name, theme, bg, parentBg].
  char *args[7];
  args[0] = port_c;
  args[1] = id_c;
  args[2] = (char *)widget_name;
  args[3] = (char *)theme;
  args[4] = bg_c;
  args[5] = bg_c;
  args[6] = 0;
  fl_dart_project_set_dart_entrypoint_arguments(project, args);

  if (!gtk_init_check(nullptr, nullptr)) {
    g_printerr("Failed to initialize GTK\n");
    return nullptr;
  }

  FlutterWindow *ctx = new FlutterWindow;
  ctx->headless_window = nullptr;
  ctx->top_window = nullptr;
  ctx->view = nullptr;
  ctx->closed = false;

  GtkWidget *window = gtk_window_new(GTK_WINDOW_TOPLEVEL);
  gtk_window_set_title(GTK_WINDOW(window), widget_name);
  gtk_window_set_default_size(GTK_WINDOW(window), width, height);
  g_signal_connect(window, "destroy", G_CALLBACK(on_display_window_destroy), ctx);

  FlView *view = fl_view_new(project);
  GtkWidget *view_widget = GTK_WIDGET(view);
  gtk_container_add(GTK_CONTAINER(window), view_widget);
  gtk_widget_show_all(window);

  ctx->view = view_widget;
  ctx->top_window = window;

  fl_register_plugins(FL_PLUGIN_REGISTRY(view));
  g_print("FlutterNative.createDisplayWindow port:%d id:%ld name:%s %dx%d\n",
          port, (long)displayId, widget_name, width, height);
  return ctx;
}

// =================================================================================================
// JNI entry points (dev.equo.swt.FlutterNative). One set of functions for both surface kinds.
// =================================================================================================

extern "C" JNIEXPORT void JNICALL
Java_dev_equo_swt_FlutterNative_SetBundleDir(JNIEnv *env, jclass cls, jstring dir) {
  if (dir == nullptr) {
    g_bundle_override.clear();
    return;
  }
  const char *c = env->GetStringUTFChars(dir, nullptr);
  g_bundle_override = (c != nullptr) ? std::string(c) : std::string();
  if (c != nullptr) env->ReleaseStringUTFChars(dir, c);
}

JNIEXPORT jlong JNICALL
Java_dev_equo_swt_FlutterNative_Initialize(JNIEnv *env, jclass cls, jint port, jlong parent,
                                           jlong widget_id, jstring widget_name, jstring theme,
                                           jint background_color, jint parent_background_color,
                                           jint width, jint height) {
  const char *name = env->GetStringUTFChars(widget_name, NULL);
  const char *th = env->GetStringUTFChars(theme, NULL);
  FlutterWindow *w;
  if (width > 0 && height > 0) {
    w = createDisplayWindow(port, widget_id, name, th, background_color, width, height);
  } else {
    w = reinterpret_cast<FlutterWindow *>(
        InitializeFlutterWindow(port, (void *)parent, widget_id, name, th, background_color, parent_background_color));
  }
  env->ReleaseStringUTFChars(widget_name, name);
  env->ReleaseStringUTFChars(theme, th);
  return (jlong)w;
}

JNIEXPORT jlong JNICALL
Java_dev_equo_swt_FlutterNative_GetView(JNIEnv *env, jclass cls, jlong context) {
  FlutterWindow *w = reinterpret_cast<FlutterWindow *>(context);
  return (jlong)(w ? w->view : nullptr);
}

JNIEXPORT void JNICALL
Java_dev_equo_swt_FlutterNative_Dispose(JNIEnv *env, jclass cls, jlong context) {
  FlutterWindow *w = reinterpret_cast<FlutterWindow *>(context);
  if (!w) return;
  if (w->top_window) {
    // window surface: destroying the toplevel destroys the FlView and shuts the engine down. Our
    // "destroy" handler nulls top_window/view, so the drain below won't touch freed widgets.
    if (GTK_IS_WIDGET(w->top_window)) {
      gtk_widget_destroy(w->top_window);
      while (gtk_events_pending()) {
        gtk_main_iteration_do(FALSE);
      }
    }
  } else {
    // embedded surface: detach the view from its SWT parent (keeping it alive).
    if (w->headless_window && GTK_IS_WIDGET(w->headless_window)) {
    } else if (w->view && GTK_IS_WIDGET(w->view)) {
      GtkWidget *parent = gtk_widget_get_parent(w->view);
      if (parent && GTK_IS_CONTAINER(parent)) {
        g_object_ref(w->view);
        gtk_container_remove(GTK_CONTAINER(parent), w->view);
      }
    }
  }
  delete w;
}

JNIEXPORT void JNICALL
Java_dev_equo_swt_FlutterNative_SetBounds(JNIEnv *env, jclass cls, jlong context, jint x, jint y,
                                          jint width, jint height, jint vx, jint vy, jint vwidth,
                                          jint vheight) {
  FlutterWindow *w = reinterpret_cast<FlutterWindow *>(context);
  if (!w) return;
  if (w->top_window && GTK_IS_WINDOW(w->top_window)) {
    gtk_window_move(GTK_WINDOW(w->top_window), x, y);
    gtk_window_resize(GTK_WINDOW(w->top_window), width, height);
  } else if (w->view) {
    gtk_widget_set_size_request(w->view, vwidth, vheight);
  }
}

// Headless measurement pump (kept as a no-op, matching prior behaviour).
JNIEXPORT jint JNICALL
Java_dev_equo_swt_FlutterNative_PumpMessages(JNIEnv *env, jclass cls, jint maxMessages) {
  return 0;
}

// Pumps a window surface's event loop; returns -1 once the window has been closed by the user.
JNIEXPORT jint JNICALL
Java_dev_equo_swt_FlutterNative_Pump(JNIEnv *env, jclass cls, jlong context) {
  FlutterWindow *ctx = reinterpret_cast<FlutterWindow *>(context);
  int count = 0;
  while (gtk_events_pending()) {
    gtk_main_iteration_do(FALSE);
    count++;
    if (ctx && ctx->closed) {
      break;
    }
  }
  if (ctx && ctx->closed) {
    delete ctx;  // safe: Java zeroes windowContext after a -1 and makes no further calls with it
    return -1;
  }
  return count;
}

// Blocks until an event is available or up to |millis| ms, then dispatches what's ready (idle sleep).
JNIEXPORT void JNICALL
Java_dev_equo_swt_FlutterNative_WaitEvents(JNIEnv *env, jclass cls, jlong context, jint millis) {
  FlutterWindow *ctx = reinterpret_cast<FlutterWindow *>(context);
  if (ctx && ctx->closed) {
    return;
  }
  GMainContext *mc = g_main_context_default();
  GSource *timeout = g_timeout_source_new(millis < 0 ? 0 : (guint)millis);
  g_source_set_callback(
      timeout, [](gpointer) -> gboolean { return G_SOURCE_REMOVE; }, nullptr, nullptr);
  g_source_attach(timeout, mc);
  g_main_context_iteration(mc, TRUE);
  g_source_destroy(timeout);
  g_source_unref(timeout);
}

JNIEXPORT void JNICALL
Java_dev_equo_swt_FlutterNative_SetTitle(JNIEnv *env, jclass cls, jlong context, jstring title) {
  FlutterWindow *w = reinterpret_cast<FlutterWindow *>(context);
  const char *t = env->GetStringUTFChars(title, NULL);
  if (w && w->top_window && GTK_IS_WINDOW(w->top_window)) {
    gtk_window_set_title(GTK_WINDOW(w->top_window), t ? t : "");
  }
  env->ReleaseStringUTFChars(title, t);
}

// state: 0 = restore/normal, 1 = maximized, 2 = minimized, 3 = fullscreen.
JNIEXPORT void JNICALL
Java_dev_equo_swt_FlutterNative_SetState(JNIEnv *env, jclass cls, jlong context, jint state) {
  FlutterWindow *w = reinterpret_cast<FlutterWindow *>(context);
  if (!w || !w->top_window || !GTK_IS_WINDOW(w->top_window)) return;
  GtkWindow *win = GTK_WINDOW(w->top_window);
  switch (state) {
    case 1:
      gtk_window_maximize(win);
      break;
    case 2:
      gtk_window_iconify(win);
      break;
    case 3:
      gtk_window_fullscreen(win);
      break;
    default:
      gtk_window_unfullscreen(win);
      gtk_window_unmaximize(win);
      gtk_window_deiconify(win);
      break;
  }
}
