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

/// Whether [url] should be routed through this origin's /proxy endpoint, which makes the iframe
/// content same-origin and so scriptable (eval/execute/BrowserFunction).
///
/// Loopback always is: an app that serves its own UI from a local HTTP server is still a different
/// origin, and a page that waits on a `BrowserFunction` renders nothing without the shim. Anything
/// else needs the opt-in flag the server injects from -Ddev.equo.swt.web.proxy. Both halves of this
/// decision are re-checked server-side by `WebFlutterServer.proxyAllowed`.
bool browserProxyEnabled(String url) {
  if (_isLoopbackUrl(url)) return true;
  final evolve = globalContext.getProperty('evolve'.toJS);
  if (evolve.isUndefinedOrNull) return false;
  final flag = (evolve as JSObject).getProperty('browserProxy'.toJS);
  return flag.isDefinedAndNotNull && (flag as JSBoolean).toDart;
}

/// Literal loopback hosts only — mirrors `WebFlutterServer.isLoopbackHost`.
final RegExp _loopbackV4 =
    RegExp(r'^127(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}$');

bool _isLoopbackUrl(String url) {
  final host = Uri.tryParse(url)?.host;
  if (host == null || host.isEmpty) return false;
  if (host.toLowerCase() == 'localhost') return true;
  if (host == '::1') return true;
  return _loopbackV4.hasMatch(host);
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

/// Base URL for a `setText` document whose `file:` sub-resources Java rewrote to root-absolute
/// `/local-file/...` paths: the document is rendered from a `data:` URL, which has no origin for
/// those to resolve against. [basePath] is the document's own `<base href>` once rewritten, so
/// its relative URLs keep resolving where the application meant them to.
String localFileBaseRewrite(String? basePath) {
  final origin = web.window.location.origin;
  return basePath == null || basePath.isEmpty ? '$origin/' : '$origin$basePath';
}

/// Listens for the iframe's native `load` DOM event, fired every time it lands
/// a new document (navigation, back/forward, reload) -- the real signal that
/// was missing when this was written, versus guessing with a retry loop.
void browserOnFrameLoad(
    PlatformWebViewControllerCreationParams? params, void Function() onLoad) {
  if (params is! WebWebViewControllerCreationParams) return;
  // ignore: invalid_use_of_visible_for_testing_member
  params.iFrame.onLoad.listen((_) => onLoad());
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

/// Reports keys typed inside the iframe to [onKey] and suppresses the same reserved shortcuts the
/// embedding page does — they are delivered to the iframe's document and reach neither Flutter's
/// keyboard nor `suppressBrowserShortcuts()`. Same-origin content only; returns whether the
/// listener is in place. A navigation replaces the document, so the caller re-installs per load.
bool installBrowserFrameKeyHandling(
    PlatformWebViewControllerCreationParams? params,
    void Function(String key, bool ctrl, bool shift, bool alt, bool meta,
            bool down)
        onKey) {
  if (params is! WebWebViewControllerCreationParams) return false;
  try {
    // ignore: invalid_use_of_visible_for_testing_member
    final doc = params.iFrame.contentDocument;
    if (doc == null) return false;
    const marker = '__equoKeyForwarding';
    if (doc.getProperty(marker.toJS).isDefinedAndNotNull) return true;
    doc.setProperty(marker.toJS, true.toJS);
    const reserved = {'s', 'p', 'o'};
    // preventDefault only, so the page's own handlers still see every key.
    doc.addEventListener(
        'keydown',
        ((web.KeyboardEvent e) {
          if ((e.metaKey || e.ctrlKey) &&
              reserved.contains(e.key.toLowerCase())) {
            e.preventDefault();
          }
          onKey(e.key, e.ctrlKey, e.shiftKey, e.altKey, e.metaKey, true);
        }).toJS,
        true.toJS);
    doc.addEventListener(
        'keyup',
        ((web.KeyboardEvent e) {
          onKey(e.key, e.ctrlKey, e.shiftKey, e.altKey, e.metaKey, false);
        }).toJS,
        true.toJS);
    return true;
  } catch (_) {
    // Cross-origin content throws a SecurityError rather than returning a null contentDocument.
    return false;
  }
}
