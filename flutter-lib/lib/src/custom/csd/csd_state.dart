import 'package:flutter/foundation.dart';

import '../../comm/comm.dart';
import '../../gen/rectangle.dart';
import '../../impl/widget_config.dart';
import 'equo_window.dart';

/// Shared maximize/restore state for the frameless window, so the window controls, the
/// title-bar double-click, and the resize layer all agree on whether the window is
/// maximized. The host owns the real state; this mirrors it for the UI.
final ValueNotifier<bool> csdMaximized = ValueNotifier<bool>(false);

/// The main window's (Display) widget id, set from main() when this client renders the
/// Display. Window-control commands are addressed to `Display/$id/...`. Null on non-main
/// clients, in which case we fall back to the direct `window.equo.*` API.
int? csdMainWindowId;

/// The window title (main shell text), pushed from Java on `Display/$id/WinTitle`. Shown in
/// the overlay title strip.
final ValueNotifier<String> csdWindowTitle = ValueNotifier<String>('');

/// Whether the window is the active/focused window. Driven by JS focus/blur events (see
/// EquoWindow.installWindowStateListeners). macOS controls grey out when inactive.
final ValueNotifier<bool> csdWindowActive = ValueNotifier<bool>(true);

/// Minimize: `window.equo.minimize()` works on all hosts, so use it directly.
void csdMinimize() => EquoWindow.minimize();

/// The window's bounds captured just before maximizing, so restore returns there.
VRectangle? _preMaximizeBounds;

/// Maximize/restore. Routed through the SWT bridge (`Display/$id/WinMaximize|WinRestore`)
/// because `window.equo.maximize()` deadlocks the mac message pump. We compute the target
/// bounds here (the Dart side has the real screen + window geometry; the Java DartDisplay's
/// monitor is zero-sized) and Java just applies them via setWindowBounds. Falls back to the
/// direct API when the main window id is unknown.
void csdSetMaximized(bool maximize) {
  csdMaximized.value = maximize;
  final id = csdMainWindowId;
  final mode = getConfigFlags().csd_maximize ?? 'direct';

  // "direct": call window.equo.maximize()/restore() straight from Dart, like the plain-HTML
  // CSD snippet. Deferred out of the current gesture (mimics the snippet's eq(...) wrapper).
  // Note: the native zoom broke Flutter-web input on mac; "bounds" (default) avoids it.
  if (id == null || mode == 'direct') {
    Future<void>.delayed(Duration.zero, () {
      if (maximize) {
        EquoWindow.maximize();
      } else {
        EquoWindow.restore();
      }
    });
    return;
  }

  // bounds / native / fullscreen: the SWT bridge applies it (rect used only for "bounds").
  if (maximize) {
    _preMaximizeBounds = VRectangle()
      ..x = EquoWindow.screenOriginX.round()
      ..y = EquoWindow.screenOriginY.round()
      ..width = EquoWindow.viewportWidth.round()
      ..height = EquoWindow.viewportHeight.round();
    final target = VRectangle()
      ..x = EquoWindow.screenAvailLeft.round()
      ..y = EquoWindow.screenAvailTop.round()
      ..width = EquoWindow.screenAvailWidth.round()
      ..height = EquoWindow.screenAvailHeight.round();
    EquoCommService.sendPayload("Display/$id/WinMaximize", target);
  } else {
    final restore =
        _preMaximizeBounds ??
        (VRectangle()
          ..x = 100
          ..y = 100
          ..width = 1100
          ..height = 800);
    EquoCommService.sendPayload("Display/$id/WinRestore", restore);
  }
}

/// Toggles between maximized and restored.
void csdToggleMaximize() => csdSetMaximized(!csdMaximized.value);

/// Close. Routed through the SWT bridge (`Display/$id/WinClose` → existing shell teardown)
/// because `window.equo.close()` freezes the mac window; falls back to the direct API.
void csdClose() {
  final id = csdMainWindowId;
  if (id != null) {
    EquoCommService.send("Display/$id/WinClose");
  } else {
    EquoWindow.close();
  }
}
