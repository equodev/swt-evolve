#include "motion_on_arrival.h"

#include <cstdlib>
#include <cstring>
#include <initializer_list>

#include <gdk/gdkx.h>
#include <gtk/gtk.h>

namespace {

// XInput 2's motion, which GDK 3 selects in place of the core MotionNotify (XI2.h's XI_Motion).
constexpr int kXiMotion = 6;

bool g_release_pending = false;

// Runs once GDK has queued what it read from the server: the motion is in its queue then, held for the
// frame clock. A no-op event behind it makes GDK return it now (_gdk_event_queue_find_first returns a
// held motion as soon as another event follows it). Without a window, GTK drops the no-op unhandled.
gboolean ReleaseMotion(gpointer) {
  g_release_pending = false;
  GdkEvent *nothing = gdk_event_new(GDK_NOTHING);
  gdk_event_put(nothing);
  gdk_event_free(nothing);
  return G_SOURCE_REMOVE;
}

// Sees every X event before GDK translates it; only notes that a motion came.
GdkFilterReturn OnXEvent(GdkXEvent *xevent, GdkEvent *, gpointer) {
  auto *ev = static_cast<XEvent *>(xevent);
  bool motion = ev->type == MotionNotify || (ev->type == GenericEvent && ev->xgeneric.evtype == kXiMotion);
  if (motion && !g_release_pending) {
    g_release_pending = true;
    g_idle_add_full(G_PRIORITY_HIGH, ReleaseMotion, nullptr, nullptr);
  }
  return GDK_FILTER_CONTINUE;
}

} // namespace

bool MotionOnArrivalInstall() {
  static int installed = -1;
  if (installed >= 0) return installed == 1;
  GdkDisplay *display = gdk_display_get_default();
  if (display == nullptr || !GDK_IS_X11_DISPLAY(display)) {
    installed = 0;
    return false;
  }
  gdk_window_add_filter(nullptr, OnXEvent, nullptr);
  installed = 1;
  return true;
}

namespace {

// Blocks GDK's flush-events and resume-events handlers on the frame clock of |window|'s GdkWindow.
void Unpause(GtkWidget *window) {
  GdkWindow *gdk = gtk_widget_get_window(window);
  GdkFrameClock *clock = gdk != nullptr ? gdk_window_get_frame_clock(gdk) : nullptr;
  if (clock == nullptr || g_object_get_data(G_OBJECT(clock), "dev.equo.swt.unpaused") != nullptr) return;
  for (const char *name : {"flush-events", "resume-events"}) {
    guint id = g_signal_lookup(name, GDK_TYPE_FRAME_CLOCK);
    if (id != 0) g_signal_handlers_block_matched(clock, G_SIGNAL_MATCH_ID, id, 0, nullptr, nullptr, nullptr);
  }
  g_object_set_data(G_OBJECT(clock), "dev.equo.swt.unpaused", GINT_TO_POINTER(1));
}

} // namespace

bool EventsUnpausedInstall(GtkWidget *window) {
  if (window == nullptr || !MotionOnArrivalInstall()) return false;
  if (gtk_widget_get_realized(window)) {
    Unpause(window);
  } else {
    g_signal_connect_after(window, "realize", G_CALLBACK(+[](GtkWidget *w, gpointer) { Unpause(w); }), nullptr);
  }
  return true;
}
