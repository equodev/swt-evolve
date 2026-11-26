#include "system_cursor.h"

#include <cstring>

#include <gtk/gtk.h>

#include "engine_vsync.h"

namespace {

// FlMouseCursorHandler's instance, as fl_mouse_cursor_handler.cc declares it in the Flutter release
// whose embedder.h sits next to this runner. EngineMouseCursorHandler checks its type name first.
struct FlMouseCursorHandlerInstance {
  GObject parent_instance;
  gpointer channel;
  GHashTable *system_cursor_table;
  gchar *cursor_name;
};

// Flutter's cursor names (CSS) shown as GTK's SWT shows the SWT cursor they stand for.
const char *SwtCursorName(const char *flutter_name) {
  if (strcmp(flutter_name, "all-scroll") == 0) return "fleur";
  return nullptr;
}

// Runs after FlView's own handler, which has just set the cursor Flutter named on the toplevel.
void OnCursorChanged(GObject *handler, gpointer user_data) {
  const gchar *name = reinterpret_cast<FlMouseCursorHandlerInstance *>(handler)->cursor_name;
  if (name == nullptr) return;
  const char *swt_name = SwtCursorName(name);
  if (swt_name == nullptr) return;
  GtkWidget *view = GTK_WIDGET(user_data);
  GdkWindow *window = gtk_widget_get_window(gtk_widget_get_toplevel(view));
  if (window == nullptr) return;
  g_autoptr(GdkCursor) cursor = gdk_cursor_new_from_name(gdk_window_get_display(window), swt_name);
  if (cursor != nullptr) gdk_window_set_cursor(window, cursor);
}

} // namespace

bool SystemCursorInstall(FlView *view) {
  if (view == nullptr) return false;
  GObject *handler = EngineMouseCursorHandler(fl_view_get_engine(view));
  if (handler == nullptr) {
    g_warning("Mouse cursor handler not recognized; Flutter's cursor names are shown as they are");
    return false;
  }
  g_signal_connect_object(handler, "cursor-changed", G_CALLBACK(OnCursorChanged), view,
                          G_CONNECT_AFTER);
  return true;
}
