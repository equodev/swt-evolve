#ifndef RUNNER_MOTION_ON_ARRIVAL_H_
#define RUNNER_MOTION_ON_ARRIVAL_H_

#include <gtk/gtk.h>

// Hands pointer motion to the views as it arrives, instead of at the next tick of GDK's frame clock.
//
// GDK 3 holds a motion event that is alone in its queue until its frame clock flushes events, a clock
// of its own with a 60 Hz rhythm on its own phase (see draw_on_arrival.h). A drag's moves reached
// Flutter 10 ms late on average and up to 25 ms, and at most one per tick: fewer positions than the
// display shows frames, each drawn later than the pointer got there. GDK returns a held motion at once
// when another event is queued behind it, so after each motion GDK queues, a no-op event is queued
// behind it.
//
// Process-wide and idempotent. X11 only.
bool MotionOnArrivalInstall();

// Keeps GDK reading events between the frame clock's flush and its paint.
//
// When anything asks GDK 3's frame clock for a frame, its flush pauses event reading at once and its
// paint, which resumes it, waits for the next frame time, up to a refresh later. This window's
// frames are painted as they arrive (draw_on_arrival.h), so that wait holds back only input: the
// pointer's motion sat unread on the X connection for up to 9 ms, and the GTK thread spun while it
// did, as its poll kept finding the connection readable. The flush and resume handlers of
// |window|'s frame clock are blocked once it is realized.
//
// X11 only.
bool EventsUnpausedInstall(GtkWidget *window);

#endif // RUNNER_MOTION_ON_ARRIVAL_H_
