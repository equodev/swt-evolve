// import 'package:bitsdojo_window/bitsdojo_window.dart';
import 'dart:async';
import 'dart:io' show Platform;
import 'dart:ui' show Size;

import 'package:flutter/foundation.dart'
    show TargetPlatform, defaultTargetPlatform;
import 'package:flutter/widgets.dart';

import 'src/impl/utils/resize_reports.dart';

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
  return null;
}

int? getBackgroundColor(List<String> args) {
  if (args.length >= 5) {
    return int.parse(args[4]);
  }
  return null;
}

// Desktop renders real native widgets, not a canvas — there's no DOM bridge to switch on.
bool getEnableTestSemantics(List<String> args) => false;

/// Whether to trace how keys are routed while a pop-up menu is up (`MenuKeyTrace`).
bool getTraceMenuKeys(List<String> args) => Platform.environment['EVOLVE_TRACE_MENU_KEYS'] == 'true';

int? getParentBackgroundColor(List<String> args) {
  if (args.length >= 6) {
    return int.parse(args[5]);
  }
  return null;
}

/// Whether the runner gave the UI isolate a thread of its own (the Linux runner does).
bool getUiThreadOwn(List<String> args) => args.length >= 7 && args[6] == 'ui-thread=own';

String? getDeferredFontManifest() => null;

/// Native builds are never embedded in a web iframe. Present to satisfy the
/// shared platform interface with web_platform.dart.
bool isSelfEmbeddedBrowserWidget() => false;

/// Whether the host has a touch-aware pointer device attached, which is what
/// `Display.getTouchEnabled` answers.
///
/// Phones and tablets have a touchscreen. macOS counts because its pointer devices — the built-in
/// trackpad, the Magic Trackpad, the Magic Mouse — are multi-touch, which is why Cocoa's own SWT
/// answers true there. Windows and Linux carry a digitizer only sometimes and Flutter cannot ask,
/// so they answer false rather than guess: a pointer device can still be reported per event.
bool isTouchDeviceAttached() => switch (defaultTargetPlatform) {
      TargetPlatform.android ||
      TargetPlatform.iOS ||
      TargetPlatform.fuchsia ||
      TargetPlatform.macOS =>
        true,
      _ => false,
    };

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

  // A live window drag fires onMetricsChanged every frame; Java hears of it as [ResizeReports]
  // paces it.
  final reports = ResizeReports.pacedByFrames(onChange);
  final previous = dispatcher.onMetricsChanged;
  dispatcher.onMetricsChanged = () {
    previous?.call();
    reports.changed();
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

/// No-op off the web: only a browser has reserved shortcuts to suppress. Present to satisfy the
/// shared platform interface with web_platform.dart.
void suppressBrowserShortcuts() {}

/// No-op off the web: a native engine that crashes takes the process with it. Present to satisfy
/// the shared platform interface with web_platform.dart.
void reloadOnRendererCrash() {}

void close() {}
