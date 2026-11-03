import 'package:flutter/foundation.dart';

import '../../comm/comm.dart';
import '../../gen/rectangle.dart';
import '../../impl/widget_config.dart';
import 'equo_window.dart';
import '../../gen/widgets.dart' show WireChannel, WireType;

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

/// Whether [csdMaximized] currently stands for full screen rather than a maximized window, so a
/// restore from either control leaves the state the window is actually in.
bool _fullScreen = false;

/// Full screen: the window takes a Space of its own and the Dock hides, which is what the macOS
/// green traffic light does to a native window. Java applies it to the main Shell
/// (`Shell.setFullScreen`), so SWT's own full-screen state follows the window.
void csdSetFullScreen(bool fullScreen) {
  csdMaximized.value = fullScreen;
  _fullScreen = fullScreen;
  final id = csdMainWindowId;
  if (id == null) return;
  EquoCommService.sendTo(
    WireType.display,
    id,
    fullScreen ? WireChannel.winFullScreen : WireChannel.winExitFullScreen,
  );
}

/// The host window entered or left full screen on its own (the OS, accessibility tools). Java
/// updates the main Shell and answers with [csdApplyFullScreenState].
void csdReportWindowFullScreen(bool fullScreen) {
  final id = csdMainWindowId;
  if (id == null) return;
  EquoCommService.sendTo(
    WireType.display,
    id,
    fullScreen ? WireChannel.winDidEnterFullScreen : WireChannel.winDidExitFullScreen,
  );
}

/// The main Shell's full-screen state as Java holds it, whatever changed it, so the controls show
/// what the window really is.
void csdApplyFullScreenState(bool fullScreen) {
  if (fullScreen == _fullScreen && csdMaximized.value == fullScreen) return;
  _fullScreen = fullScreen;
  csdMaximized.value = fullScreen;
}

/// Listens for [csdApplyFullScreenState] updates addressed to the window [id].
void csdListenFullScreenState(int id) {
  EquoCommService.on(WireType.display, id, WireChannel.winFullScreenState, (dynamic args) {
    if (args is Map && args['fullScreen'] is bool) {
      csdApplyFullScreenState(args['fullScreen'] as bool);
    }
  });
}

/// Maximize/restore. Routed through the SWT bridge (`Display/$id/WinMaximize|WinRestore`)
/// because `window.equo.maximize()` deadlocks the mac message pump. We compute the target
/// bounds here (the Dart side has the real screen + window geometry; the Java DartDisplay's
/// monitor is zero-sized) and Java just applies them via setWindowBounds. Falls back to the
/// direct API when the main window id is unknown.
void csdSetMaximized(bool maximize) {
  if (!maximize && _fullScreen) {
    csdSetFullScreen(false);
    return;
  }
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
    EquoCommService.sendTo(WireType.display, id, WireChannel.winMaximize, target);
  } else {
    final restore =
        _preMaximizeBounds ??
        (VRectangle()
          ..x = 100
          ..y = 100
          ..width = 1100
          ..height = 800);
    EquoCommService.sendTo(WireType.display, id, WireChannel.winRestore, restore);
  }
}

/// Toggles between maximized and restored.
void csdToggleMaximize() => csdSetMaximized(!csdMaximized.value);

/// Close. On desktop the native embedder owns the top-level window, so close it directly
/// (the runner vetoes that and reports a close *request*, so SWT runs Shell.close() and a
/// listener can still refuse — the same path as the OS window-manager close). On the web it is
/// routed through the SWT bridge
/// (`Display/$id/WinClose` → existing shell teardown) because `window.equo.close()` freezes
/// the mac window; falls back to the direct API when the main window id is unknown.
void csdClose() {
  if (!kIsWeb) {
    EquoWindow.close();
    return;
  }
  final id = csdMainWindowId;
  if (id != null) {
    EquoCommService.sendTo(WireType.display, id, WireChannel.winClose);
  } else {
    EquoWindow.close();
  }
}
