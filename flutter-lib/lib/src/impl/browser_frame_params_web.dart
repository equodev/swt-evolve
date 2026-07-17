import 'dart:js_interop';
import 'dart:js_interop_unsafe';

import 'package:web/web.dart' as web;
import 'package:webview_all/webview_all.dart'
    show PlatformWebViewControllerCreationParams;
import 'package:webview_all_web/webview_all_web.dart';

/// On web, stamps the WebView's `<iframe>` with a stable `name`
/// (`equo-browser-<id>`) so a hosting Equo Chromium standalone window can
/// identify this Browser's sub-frame in CEF's `onBeforeBrowse` (whose only
/// frame key is `getFrameName()`) and route the cancellable
/// `LocationListener.changing` to the right Browser. The id matches the Java
/// side's `FlutterBridge.id(widget)` (== this widget's `state.id`).
PlatformWebViewControllerCreationParams? browserWebViewParams(int id) {
  final params = WebWebViewControllerCreationParams();
  // ignore: invalid_use_of_visible_for_testing_member
  params.iFrame.name = 'equo-browser-$id';
  // Explicit initial src so that if the webview controller is attached before
  // the first navigation applies its URL, an empty `src` never resolves to the
  // parent document URL (the app origin) — which the server would answer with
  // index.html (SPA fallback), self-embedding a second app instance.
  // ignore: invalid_use_of_visible_for_testing_member
  params.iFrame.src = 'about:blank';
  return params;
}

/// Whether the same-origin Browser proxy is enabled (server injects
/// `window.evolve.browserProxy` from -Ddev.equo.swt.web.proxy). When on,
/// external URLs are routed through this origin's /proxy endpoint so the iframe
/// content is same-origin (enabling eval/execute/BrowserFunction).
bool browserProxyEnabled() {
  final evolve = globalContext.getProperty('evolve'.toJS);
  if (evolve.isUndefinedOrNull) return false;
  final flag = (evolve as JSObject).getProperty('browserProxy'.toJS);
  return flag.isDefinedAndNotNull && (flag as JSBoolean).toDart;
}

/// Rewrites an absolute http(s) URL to go through this origin's /proxy endpoint.
String browserProxyRewrite(String url) {
  final origin = web.window.location.origin;
  return '$origin/proxy?url=${Uri.encodeComponent(url)}';
}

String localFileRewrite(String tokenPath) {
  final origin = web.window.location.origin;
  return '$origin/local-file/$tokenPath';
}

/// Evaluates [script] directly in the iframe's content window and returns the
/// dartified result (`null`/`bool`/`double`/`String`/`List`/`Map`, recursively).
/// Works only when the content is same-origin (e.g. served via the proxy);
/// cross-origin access throws a SecurityError, which propagates to the caller.
/// webview_all_web's own runJavaScript throws UnsupportedError unconditionally,
/// so we go straight to the DOM. The caller JSON-encodes the value; the Java
/// side restores the SWT evaluate() type contract (JS number -> Double etc.).
Object? browserEvalInFrame(
    PlatformWebViewControllerCreationParams? params, String script) {
  if (params is! WebWebViewControllerCreationParams) return null;
  // ignore: invalid_use_of_visible_for_testing_member
  final win = params.iFrame.contentWindow;
  if (win == null) throw StateError('iframe has no content window');
  final result = (win as JSObject).callMethod<JSAny?>('eval'.toJS, script.toJS);
  return result.dartify();
}
