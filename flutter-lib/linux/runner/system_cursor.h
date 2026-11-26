#ifndef RUNNER_SYSTEM_CURSOR_H_
#define RUNNER_SYSTEM_CURSOR_H_

#include <flutter_linux/flutter_linux.h>

// Shows GTK's SWT cursors where Flutter's names differ: SWT.CURSOR_SIZEALL is Flutter's all-scroll,
// which a cursor theme such as Adwaita draws as a panning mark, while GTK's SWT shows GDK_FLEUR, the
// four-way arrows. Returns false, leaving Flutter's cursors as they are, when the engine's cursor
// handler can't be reached.
bool SystemCursorInstall(FlView *view);

#endif // RUNNER_SYSTEM_CURSOR_H_
