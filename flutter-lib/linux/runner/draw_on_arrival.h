#ifndef RUNNER_DRAW_ON_ARRIVAL_H_
#define RUNNER_DRAW_ON_ARRIVAL_H_

#include <gtk/gtk.h>

// Draws the engine's frames into |window| as they arrive, instead of at the next tick of GDK's frame
// clock.
//
// The engine hands a frame to GTK by queueing a redraw of its view; GDK paints the window at its frame
// clock's next tick, a clock of its own with a 60 Hz rhythm on its own phase. A frame that is ready
// waited for it, about 13 ms on average, after already waiting for the engine's own frame start.
// Here the engine's call to gtk_widget_queue_draw is followed by painting the window at once.
//
// Only |window| and other windows passed here are drawn on arrival; any other view the engine draws
// keeps GDK's clock. EQUO_SWT_DRAW=clock leaves every window on GDK's clock. Returns false when the
// engine's call could not be reached, which leaves every window on GDK's clock.
bool DrawOnArrivalInstall(GtkWidget *window);

#endif // RUNNER_DRAW_ON_ARRIVAL_H_
