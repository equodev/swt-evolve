#ifndef RUNNER_ENGINE_VSYNC_H_
#define RUNNER_ENGINE_VSYNC_H_

#include <flutter_linux/flutter_linux.h>
#include <gtk/gtk.h>

// Gives |engine|, which must not have started yet, a vsync source paced by the refresh rate of the
// monitor |window| is on.
//
// The Linux embedder sets no vsync callback, so the engine falls back to a timer fixed at 60 Hz on
// a fixed phase: every frame request waits for that timer's next tick, up to 16.7 ms, and no
// animation runs faster than 60 frames a second whatever the display. With this source a request
// made after an idle frame interval starts its frame at once, and back-to-back frames come one
// refresh interval apart.
//
// The embedder offers no public way to set the callback, so this reaches into FlEngine's instance
// for the proc table it starts the engine through. Returns false, leaving the engine on its timer,
// when that instance does not look like the one this was written against.
bool EngineVsyncInstall(FlEngine *engine, GtkWidget *window);

// A duration event in the engine's timeline (its Embedder stream), on the calling thread, so that the
// runner's own steps of a frame show next to the engine's. Nothing until EngineVsyncInstall has seen
// an engine, or while no timeline is recording.
void EngineTraceBegin(const char *name);
void EngineTraceEnd(const char *name);

#endif // RUNNER_ENGINE_VSYNC_H_
