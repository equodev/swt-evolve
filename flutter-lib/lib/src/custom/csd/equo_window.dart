/// Facade over the host Equo Chromium Client-Side-Decorations API (`window.equo.*`).
///
/// On web builds this resolves to [equo_window_web.dart] (real `dart:js_interop`
/// calls); on native/desktop builds it resolves to [equo_window_stub.dart] (no-ops),
/// so the rest of the app can call [EquoWindow] unconditionally and still compile
/// off the web.
library;

export 'equo_window_stub.dart'
    if (dart.library.js_interop) 'equo_window_web.dart';
