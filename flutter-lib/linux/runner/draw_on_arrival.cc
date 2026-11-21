#include "draw_on_arrival.h"

#include <cstdlib>
#include <cstring>

#include "engine_got.h"
#include "engine_vsync.h"

namespace {

constexpr char kWindowKey[] = "dev.equo.swt.draw-on-arrival";
constexpr char kResizedKey[] = "dev.equo.swt.draw-on-arrival.resized";

// While the window is being resized, GDK's frame clock paints each new size anyway: painting every
// frame on arrival on top of that only adds paints the resize does not show. Arrival waits until the
// size has held this long.
constexpr gint64 kResizeQuietUs = 100000;

bool g_painting = false;

gboolean OnConfigure(GtkWidget *window, GdkEventConfigure *event, gpointer) {
  int width = gtk_widget_get_allocated_width(window), height = gtk_widget_get_allocated_height(window);
  if (event->width != width || event->height != height) {
    g_object_set_data(G_OBJECT(window), kResizedKey, GSIZE_TO_POINTER(static_cast<gsize>(g_get_monotonic_time())));
  }
  return FALSE;
}

bool ResizedLately(GtkWidget *window) {
  auto at = static_cast<gint64>(GPOINTER_TO_SIZE(g_object_get_data(G_OBJECT(window), kResizedKey)));
  return at != 0 && g_get_monotonic_time() - at < kResizeQuietUs;
}

// The engine's gtk_widget_queue_draw: the redraw is queued as before, and a window drawn on arrival
// paints at once. A paint that queues another redraw does not paint again inside itself; GDK takes
// that one at its next tick.
void QueueDraw(GtkWidget *widget) {
  gtk_widget_queue_draw(widget);
  if (g_painting || !g_main_context_is_owner(g_main_context_default())) return;
  GtkWidget *top = gtk_widget_get_toplevel(widget);
  if (top == nullptr || g_object_get_data(G_OBJECT(top), kWindowKey) == nullptr || ResizedLately(top)) return;
  GdkWindow *window = gtk_widget_get_window(top);
  if (window == nullptr || !gdk_window_is_viewable(window)) return;
  g_painting = true;
  EngineTraceBegin("Equo::DrawOnArrival");
  G_GNUC_BEGIN_IGNORE_DEPRECATIONS
  gdk_window_process_updates(window, TRUE);
  G_GNUC_END_IGNORE_DEPRECATIONS
  EngineTraceEnd("Equo::DrawOnArrival");
  g_painting = false;
}

} // namespace

bool DrawOnArrivalInstall(GtkWidget *window) {
  static int installed = -1;
  if (installed < 0) {
    const char *draw = getenv("EQUO_SWT_DRAW");
    bool off = draw != nullptr && strcmp(draw, "clock") == 0;
    installed = !off && EngineGotPatch("gtk_widget_queue_draw", reinterpret_cast<void *>(&QueueDraw)) > 0 ? 1 : 0;
    if (!off && installed == 0) {
      g_warning("gtk_widget_queue_draw not found in the engine's relocations; frames wait for GDK's frame clock");
    }
  }
  if (installed == 0 || window == nullptr) return false;
  if (g_object_get_data(G_OBJECT(window), kWindowKey) == nullptr) {
    g_object_set_data(G_OBJECT(window), kWindowKey, GINT_TO_POINTER(1));
    g_signal_connect(window, "configure-event", G_CALLBACK(OnConfigure), nullptr);
  }
  return true;
}
