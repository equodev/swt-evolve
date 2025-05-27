#include "flutter_bridge.h"

#include <flutter_linux/flutter_linux.h>
#include <gdk/gdkx.h>
#include <gio/gio.h>
#include <glib-object.h>
#include <gtk/gtk.h>

#include "flutter/generated_plugin_registrant.h"

struct Context {
  FlDartProject *project;
  FlEngine *engine;
} static context = {0};

struct WidgetContext {
  GtkWidget *widget_container;
  GtkWidget *widget;
  ExpandPolicy policy;
};

JNIEXPORT jlong JNICALL
Java_org_eclipse_swt_widgets_FlutterSwt_InitializeFlutterWindow(
    JNIEnv *env, jclass cls, void *parent, jint port, jlong widget_id,
    jstring widget_name, jint width, jint height, jint policy) {
  if (policy >= EXPAND_POLICY_SIZE || policy < 0) {
    return -1;
  }
  const char *widget_name_cstr = env->GetStringUTFChars(widget_name, NULL);
  jlong result = (jlong)InitializeFlutterWindow(
      parent, port, widget_id, widget_name_cstr, width, height,
      static_cast<ExpandPolicy>(policy));
  env->ReleaseStringUTFChars(widget_name, widget_name_cstr);
  return result;
}

JNIEXPORT void JNICALL
Java_org_eclipse_swt_widgets_FlutterSwt_CloseFlutterWindow(
    JNIEnv *env, jclass cls, void *void_context) {
  CloseFlutterWindow(void_context);
}

JNIEXPORT jboolean JNICALL
Java_org_eclipse_swt_widgets_FlutterSwt_IsFlutterWindowVisible(JNIEnv *env,
                                                               jclass cls) {
  return static_cast<jboolean>(IsFlutterWindowVisible());
}

JNIEXPORT void JNICALL
Java_org_eclipse_swt_widgets_FlutterSwt_ResizeFlutterWindow(
    JNIEnv *env, jclass cls, void *void_context, jint width, jint height) {
  ResizeFlutterWindow(void_context, static_cast<int32_t>(width),
                      static_cast<int32_t>(height));
}

#if 0
static void on_parent_size_allocate(GtkWidget* widget, GtkAllocation* allocation, gpointer user_data) {
    GtkWidget* view_widget = GTK_WIDGET(user_data);
    gtk_widget_set_size_request(view_widget, allocation->width, allocation->height); 
    gtk_fixed_move(GTK_FIXED(widget), view_widget, 0, 0); 
    gtk_widget_queue_draw(view_widget); 
    g_print("Resize event - width: %d, height: %d\n", allocation->width, allocation->height);
}
#endif

void on_parent_size_allocate(GtkWidget *widget, GtkAllocation *allocation,
                             gpointer data) {
  WidgetContext *widget_context = static_cast<WidgetContext *>(data);
  GtkWidget *container = widget_context->widget_container;
  // GtkWidget *child = widget_context->widget;
  switch (widget_context->policy) {
  case FOLLOW_PARENT: {
    gtk_widget_set_size_request(container, allocation->width,
                                allocation->height);
  } break;
  case FOLLOW_W_PARENT: {
    gtk_widget_set_size_request(container, allocation->width, -1);
  } break;
  case FOLLOW_H_PARENT: {
    gtk_widget_set_size_request(container, -1, allocation->height);
  } break;
  default:
    g_print("Unknown policy set: %i\n", widget_context->policy);
  }
  /*
  g_print("on_parent_size_allocate: child=%p\n", child);
  g_print("on_parent_size_allocate: x=%d, y=%d, width=%d, height=%d\n",
          allocation->x, allocation->y, allocation->width,
          allocation->height);
  */
}

uintptr_t InitializeFlutterWindow(void *parentWnd, jint port, jlong widget_id,
                                  const char *widget_name, int width,
                                  int height, ExpandPolicy policy) {
  /*
   * NOTE(elias): SWT initializes GTK using gtk_init_check() and that doesn't
   * create a default instance of GApplication or GtkApplication. So just create
   * a flutter view with parentWnd as its parent widget
   */
#if 0
  GApplication *application = g_application_get_default();
  if (!GTK_IS_APPLICATION(application)) {
      fprintf(stderr, "WTF\n");
      return;
  }
#endif
  g_print("Received width: %i, height: %i \n", width, height);

  g_print("Creating project!\n");
  // g_autoptr(FlDartProject) project = fl_dart_project_new();
  FlDartProject *project = fl_dart_project_new();
  fl_dart_project_set_aot_library_path(
      project, "/home/elias/Documents/Equo/swt-flutter/flutter-lib/build/linux/"
               "x64/debug/bundle/lib/libapp.so");
  fl_dart_project_set_assets_path(
      project, (gchar *)"/home/elias/Documents/Equo/swt-flutter/flutter-lib/"
                        "build/linux/x64/debug/bundle/data/flutter_assets/");
  fl_dart_project_set_icu_data_path(
      project, (gchar *)"/home/elias/Documents/Equo/swt-flutter/flutter-lib/"
                        "build/linux/x64/debug/bundle/data/icudtl.dat");
  char port_c[256];
  sprintf(port_c, "%d", port);
  char widget_id_c[256];
  sprintf(widget_id_c, "%ld", widget_id);

  char *args[4];
  args[0] = port_c;
  args[1] = widget_id_c;
  args[2] = (char *)widget_name;
  args[3] = 0;

  fl_dart_project_set_dart_entrypoint_arguments(project, args);
  // context.project = project;
  g_print("Project created! %p\n", project);
#if 0
  if (!context.engine) {
    context.engine = fl_engine_new(project);
    if (!FL_IS_ENGINE(context.engine)) {
      g_printerr("Created engine is not valid\n");
      g_object_unref(context.engine);
      context.engine = NULL;
      return 0;
    }
    g_print("Initialized Flutter engine: %p\n", context.engine);
  }
#endif

  /**/
  fprintf(stderr, "parentWnd native: %p \n", parentWnd);
  fprintf(stderr, "IS_GTK_CONTAINER: %b \n", GTK_IS_CONTAINER(parentWnd));

  GtkWidget *parent_widget = GTK_WIDGET(parentWnd);

  FlView *view = 0;
  if (context.engine) {
    g_print("We have an engine bois let's go: %p\n", context.engine);
    view = fl_view_new_for_engine(context.engine);
  } else {
    g_print("We don't have an engine...\n");
    g_print("Using project: %p\n", project);
    view = fl_view_new(project);
  }
  g_print("Result view: %p\n", view);
  GtkWidget *view_widget = GTK_WIDGET(view);

  // gtk_widget_set_size_request(view_widget, parent_allocation.width,
  //                             parent_allocation.height);

  GtkWidget *fl_container = gtk_box_new(GTK_ORIENTATION_VERTICAL, 0);
  /*
  gtk_widget_set_hexpand(view_widget, TRUE);
  gtk_widget_set_vexpand(view_widget, TRUE);
  */
  gtk_container_add(GTK_CONTAINER(fl_container), view_widget);

  gtk_container_add(GTK_CONTAINER(parent_widget), fl_container);

  // Connect the signal
  WidgetContext *widget_context = new WidgetContext;
  widget_context->widget_container = fl_container;
  widget_context->widget = view_widget;
  widget_context->policy = policy;
  g_signal_connect(parent_widget, "size-allocate",
                   G_CALLBACK(on_parent_size_allocate), widget_context);

  // gtk_fixed_put(GTK_FIXED(parent_widget), view_widget, 0, 0);

  //gtk_widget_set_size_request(fl_container, width, height);
  gtk_widget_set_size_request(view_widget, width, height);

  GtkAllocation parent_allocation;
  gtk_widget_get_allocation(parent_widget, &parent_allocation);

  // Connect size-allocate signal to parent
  // g_signal_connect(parent_widget, "size-allocate",
  // G_CALLBACK(on_parent_size_allocate), view_widget);

  gtk_widget_show_all(fl_container);

  // gtk_widget_show(view_widget);

  FlEngine *engine = fl_view_get_engine(view);
  if (!engine) {
    fprintf(stderr, "Wtf, no engine\n");
  } else {
    g_assert(FL_IS_ENGINE(engine));
    fprintf(stderr, "Engine yay: %p\n", engine);
  }

  GtkRequisition min;
  GtkRequisition nat;
  gtk_widget_get_preferred_size(view_widget, &min, &nat);

  GtkAllocation allocation_flutter;
  gtk_widget_get_allocation(view_widget, &allocation_flutter);
  g_print("Param proportions: width=%d, height=%d\n", width, height);
  g_print("Parent widget type: %s\n", G_OBJECT_TYPE_NAME(parent_widget));
  g_print("Parent widget handle: %p\n", parent_widget);
  g_print("Parent widget allocation: x=%d, y=%d, width=%d, height=%d\n",
          parent_allocation.x, parent_allocation.y, parent_allocation.width,
          parent_allocation.height);
  g_print("Flutter view widget type: %s\n", G_OBJECT_TYPE_NAME(view_widget));
  g_print("View widget is visible: %d\n", gtk_widget_get_visible(view_widget));
  g_print("View widget is mapped: %d\n", gtk_widget_get_mapped(view_widget));
  g_print("View widget allocation: x=%d, y=%d, width=%d, height=%d\n",
          allocation_flutter.x, allocation_flutter.y, allocation_flutter.width,
          allocation_flutter.height);
  g_print("View widget requests: min_width=%d, min_height=%d, nat_width=%d, "
          "nat_height=%d\n",
          min.width, min.height, nat.width, nat.height);

  fl_register_plugins(FL_PLUGIN_REGISTRY(view));

  // gtk_widget_grab_focus(view_widget);

  return (uintptr_t)widget_context;
}

void CloseFlutterWindow(void *void_context) {
  WidgetContext *widget_context = static_cast<WidgetContext *>(void_context);
  //g_object_unref(widget_context->widget_container);
  //g_object_unref(widget_context->widget);
  delete widget_context;
#if 0 
    GtkWindow *window = gtk_application_get_active_window(context.gtk_application);
    gtk_window_close(window);
#endif
}

bool IsFlutterWindowVisible() { return FALSE; }

void ResizeFlutterWindow(void *void_context, int32_t width, int32_t height) {
  WidgetContext *widget_context = static_cast<WidgetContext *>(void_context);
  gtk_widget_set_size_request(widget_context->widget, width, height);
}

/* vim: set ts=2 sw=2 tw=0 et :*/
