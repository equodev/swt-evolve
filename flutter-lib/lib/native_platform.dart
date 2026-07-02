// import 'package:bitsdojo_window/bitsdojo_window.dart';
import 'dart:async';
import 'dart:ui' show Size;

import 'package:flutter/widgets.dart';

//late final ShellListener? listener;

int? getPort(List<String> args) {
  if (args.isNotEmpty) {
    print("Using desk port ${args.first}");
    return int.parse(args.first);
  }
  return null;
}

int? getWidgetId(List<String> args) {
  if (args.isNotEmpty) {
    return int.parse(args[1]);
  }
  return null;
}

String? getWidgetName(List<String> args) {
  if (args.length >= 2) {
    return args[2];
  }
  return null;
}

String? getTheme(List<String> args) {
  if (args.length >= 4) {
    return args[3];
  }
  return "light"; // default fallback
}

int? getBackgroundColor(List<String> args) {
  if (args.length >= 5) {
    return int.parse(args[4]);
  }
  return null;
}

int? getParentBackgroundColor(List<String> args) {
  if (args.length >= 6) {
    return int.parse(args[5]);
  }
  return null;
}

/// Native builds are never embedded in a web iframe. Present to satisfy the
/// shared platform interface with web_platform.dart.
bool isSelfEmbeddedBrowserWidget() => false;

Size? getViewportSize() {
  // No window.innerWidth on native; main.dart falls back to the Flutter view's
  // physicalSize / devicePixelRatio when this returns null.
  return null;
}

/// Drives the desktop-native Display's ClientReady handshake. The web build wires
/// this to the browser's resize/animation-frame events; the native top-level
/// window has none of those, so observe Flutter's own view metrics instead.
///
/// We listen on [PlatformDispatcher.onMetricsChanged], which fires when the native
/// window delivers new view metrics (initial size and every resize) — on the
/// platform thread, BEFORE/independent of any frame being rasterized. That matters
/// under Flutter 3.35's merged platform/UI thread, where a post-frame callback
/// only runs once a frame is actually presented (which an SWT-driven pump may
/// never get to), so a postFrameCallback-based trigger would stall. The metrics
/// callback has no such dependency, so the Display reaches ClientReady on both
/// 3.32 and 3.35.
void observeViewportChanges(void Function() onChange) {
  final binding = WidgetsBinding.instance;
  final dispatcher = binding.platformDispatcher;

  // A live window drag fires onMetricsChanged every frame. Reporting each one floods Java with a
  // full-tree relayout + Display update over the comm (each via a blocking syncExec), which on a
  // non-trivial UI backs up for seconds before the new layout appears. Coalesce the burst into a
  // single report after the resize settles — the same debounce the web build applies to the browser
  // 'resize' event. The very first metrics delivery (the ClientReady handshake / initial size) is
  // reported immediately so it never waits on the debounce; only subsequent resizes are coalesced.
  // (Maximize/restore is a single metrics change, so it already stays instant.)
  Timer? debounce;
  var reportedOnce = false;
  final previous = dispatcher.onMetricsChanged;
  dispatcher.onMetricsChanged = () {
    previous?.call();
    if (!reportedOnce) {
      reportedOnce = true;
      onChange();
      return;
    }
    debounce?.cancel();
    debounce = Timer(const Duration(milliseconds: 120), onChange);
  };

  // The window may already have a valid size by the time the Display reporter is
  // wired (we registered after the first metrics delivery), so try immediately;
  // also retry after the first frame as a belt-and-suspenders fallback. onChange
  // is a no-op until a non-zero view size is available, so early calls are safe.
  onChange();
  binding.addPostFrameCallback((_) => onChange());
}

/// Desktop-native: window close is detected on the native side (FlutterNative.pump returns -1 once
/// the OS window is closed) and handled by DeskDisplayBridge.onWindowClosed(), so there is nothing
/// for Dart to observe here. Present to satisfy the shared platform interface with web_platform.dart.
void observeWindowClose(void Function() onClose) {}

void close() {
  //if (listener != null) {
  //  windowManager.removeListener(listener!);
  //}
  //windowManager.close();
}

//class ShellListener extends WindowListener {
//  @override
//  void onWindowClose() {
//    print("onWindowClose");
//    EquoCommService.send("close");
//  }
//}
