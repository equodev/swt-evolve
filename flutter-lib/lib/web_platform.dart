import 'dart:async';
import 'dart:js_interop';
import 'dart:ui' show Size;

@JS('window.equoCommPort')
external JSNumber? get _equoCommPort;

@JS('window.evolve.widgetId')
external JSNumber? get _widgetId;

@JS('window.evolve.widgetName')
external JSString? get _widgetName;

@JS('window.evolve.theme')
external JSString? get _theme;

@JS('window.evolve.enableTestSemantics')
external JSBoolean? get _enableTestSemantics;

@JS('window.name')
external JSString? get _windowName;

@JS('window.innerWidth')
external JSNumber? get _innerWidth;

@JS('window.innerHeight')
external JSNumber? get _innerHeight;

@JS('window.addEventListener')
external void _addWindowEventListener(JSString type, JSFunction listener);

@JS('window.requestAnimationFrame')
external JSNumber _requestAnimationFrame(JSFunction callback);

// --dart-define fallbacks for a `flutter run` dev launch (Phase 2 web introspection path): when the
// app is served by `flutter run` instead of WebFlutterServer, there is no runtime placeholder
// injection of window.equoCommPort / window.evolve.*, so these values arrive via
// --dart-define=equo.comm_port / equo.widget_id / equo.widget_name instead. See
// docs/design/flutter-dtd-introspection.md (Phase 2). 0 / "" mean "not provided".
const int _envCommPort = int.fromEnvironment("equo.comm_port", defaultValue: 0);
const int _envWidgetId = int.fromEnvironment("equo.widget_id", defaultValue: 0);
const String _envWidgetName = String.fromEnvironment("equo.widget_name", defaultValue: "");

int? getPort(List<String> _) {
  print("Using web port: $_equoCommPort (env fallback: $_envCommPort)");
  final injected = _equoCommPort?.toDartInt;
  if (injected != null && injected != 0) return injected;
  return _envCommPort != 0 ? _envCommPort : null;
}

int? getWidgetId(List<String> _) {
  final params = Uri.base.queryParameters;
  final value = params['widgetId'];
  if (value != null) return int.tryParse(value);
  final injected = _widgetId?.toDartInt;
  if (injected != null && injected != 0) return injected;
  return _envWidgetId != 0 ? _envWidgetId : null;
}

String? getWidgetName(List<String> _) {
  final params = Uri.base.queryParameters;
  final q = params['widgetName'];
  if (q != null) return q;
  final injected = _widgetName?.toDart;
  if (injected != null && injected.isNotEmpty) return injected;
  return _envWidgetName.isNotEmpty ? _envWidgetName : null;
}

String? getTheme(List<String> args) {
  final params = Uri.base.queryParameters;
  return params['theme'] ?? _theme?.toDart ?? "light";
}

int? getBackgroundColor(List<String> args) {
  return null;
}

bool getEnableTestSemantics(List<String> args) {
  final params = Uri.base.queryParameters;
  return params['enableTestSemantics'] == 'true' ||
      (_enableTestSemantics?.toDart ?? false);
}

int? getParentBackgroundColor(List<String> args) {
  return null;
}

/// Whether this document is running inside an SWT Browser widget's own
/// `<iframe>`. The Browser widget stamps its iframe's browsing-context name as
/// `equo-browser-<id>` (see browser_frame_params_web.dart), and that name
/// persists on `window.name` for whatever document loads in the frame. If the
/// app itself ends up loaded there — because the iframe's `src` resolved back to
/// the app origin (an empty `src` resolves to the parent document URL, and the
/// server's SPA fallback then serves index.html) — booting would paint a second
/// copy of the whole app inside the Browser widget. A legitimate host embed
/// (chromium/remote) does not use this frame name, so it is unaffected.
bool isSelfEmbeddedBrowserWidget() {
  final n = _windowName?.toDart;
  return n != null && n.startsWith('equo-browser-');
}

Size? getViewportSize() {
  final width = _innerWidth?.toDartDouble;
  final height = _innerHeight?.toDartDouble;
  if (width == null || height == null || width <= 0 || height <= 0) {
    return null;
  }
  return Size(width, height);
}

void observeViewportChanges(void Function() onChange) {
  Timer? debounce;

  _addWindowEventListener(
    'resize'.toJS,
    (() {
      debounce?.cancel();
      debounce = Timer(const Duration(milliseconds: 200), onChange);
    }).toJS,
  );

  _requestAnimationFrame(
    (JSNumber _) {
      onChange();
    }.toJS,
  );
}

/// Fires [onClose] when the browser tab/window is being torn down, so Java can run the SWT shell
/// close — mirroring the desktop-native window-close path. We listen on both `pagehide` (the
/// reliable teardown signal: tab close, navigation, bfcache) and `beforeunload`; a socket drop is
/// deliberately NOT used as the signal, since a dropped socket may just be a transient disconnect
/// that reconnects. Double delivery is harmless — the Java side closes the shells idempotently.
///
/// NOTE: this fires for a *refresh* too — the browser exposes no way to tell a reload from a real
/// close at unload time. Disambiguation is deferred to Java: the teardown is sent on `WinUnload`
/// (see main.dart), which Java closes only after a grace period, cancelling it if the reloaded page
/// reconnects and re-sends ClientReady. So a refresh keeps the shells alive; a real close ends them.
void observeWindowClose(void Function() onClose) {
  _addWindowEventListener('pagehide'.toJS, (() { onClose(); }).toJS);
  _addWindowEventListener('beforeunload'.toJS, (() { onClose(); }).toJS);
}

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
