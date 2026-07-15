// Force the stub (blank box) on every platform so the Flutter iframe browser
// implementation is never used, including on web.
import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/widgets.dart';
import 'package:webview_all/webview_all.dart';

import '../comm/comm.dart';
import '../gen/browser.dart';
import '../gen/event.dart';
import '../gen/widget.dart';
import '../impl/composite_evolve.dart';
import 'browser_frame_params_stub.dart'
    if (dart.library.js_interop) 'browser_frame_params_web.dart';

/// Browser implementation backed by the `webview_all` plugin (the federated
/// `webview_flutter` API), which provides a real embedded webview on every
/// platform:
///   web   -> webview_flutter_web      windows -> webview_windows (WebView2)
///   macos -> WKWebView                linux   -> WebKitGTK (webkit2gtk-4.1)
///
/// Navigation is driven imperatively from the SWT [VBrowser] state, and the
/// webview's navigation callbacks are mapped onto the SWT Browser events.
/// JavaScript run/evaluate and the cancellable locationchanging round-trip are
/// wired through the comm channel. Note that on web most controller APIs
/// (JavaScript mode, navigation delegate) are unavailable, so only URL loading
/// is active there.
class BrowserImpl<T extends BrowserSwt, V extends VBrowser>
    extends CompositeImpl<T, V> {

  late final WebViewController _controller;
  String? _loadedUrl;
  String? _loadedText;
  Completer<bool>? _locationChangingCompleter;
  // Navigation is op-driven once the first "navigate" op arrives. The op carries
  // a monotonic seq so a re-navigation to the *same* URL still loads (the
  // state-driven path dedups same-URL pushes on both sides, which would
  // otherwise drop reloads); the seq also makes the op idempotent.
  int _lastNavSeq = 0;
  bool _opNavigated = false;
  // Web iframe creation params (null on non-web). Kept so execute/evaluate can
  // reach the iframe's contentWindow directly — webview_all_web's runJavaScript
  // is unconditionally unsupported, so DOM-level eval is the only option, and it
  // only works when the content is same-origin (or served through the proxy).
  PlatformWebViewControllerCreationParams? _params;
  // Names of registered BrowserFunctions. Each is exposed in the iframe page as
  // a JS shim that round-trips to Java; they must be re-injected after every
  // navigation (a fresh document loses the previous window's globals).
  final Set<String> _functionNames = <String>{};
  // Last values pushed to Java, to suppress duplicate Title/StatusText events.
  String? _lastTitle;
  String _lastStatus = '';

  @override
  void initState() {
    super.initState();

    // On web, stamp the iframe name so a hosting Chromium standalone window can
    // identify this Browser's sub-frame for cancellable LocationListener.changing.
    _params = browserWebViewParams(state.id);
    final controller = _params != null
        ? WebViewController.fromPlatformCreationParams(_params!)
        : WebViewController();
    // setJavaScriptMode / setNavigationDelegate are not implemented on web.
    // if (!kIsWeb) {
      controller
        ..setJavaScriptMode(
          (state.javascriptEnabled ?? true)
              ? JavaScriptMode.unrestricted
              : JavaScriptMode.disabled,
        )
        ..setNavigationDelegate(
          NavigationDelegate(
            onProgress: (p) =>
                widget.sendProgresschanged(state, VEvent()..x = p..y = 100),
            onPageStarted: (url) {
              widget.sendProgresschanged(state, VEvent()..x = 0..y = 100);
              // New document: clear the dedup state so its title/status fire even
              // if they match the previous page's (SWT fires per navigation).
              _lastTitle = null;
              _lastStatus = '';
            },
            onPageFinished: (url) {
              final real = _resolveReportedUrl(url);
              widget.sendLocationchanged(state, VEvent()..text = real);
              widget.sendProgresscompleted(state, null);
              _emitTitle();
              // A few pages set document.title just after load; re-check once.
              Future.delayed(const Duration(milliseconds: 150), () {
                if (mounted) _emitTitle();
              });
              // Re-expose BrowserFunctions in the freshly-loaded document.
              for (final name in _functionNames) {
                _injectBrowserFunction(name);
              }
              _pushNavState(real);
            },
            onUrlChange: (change) {
              final real = _resolveReportedUrl(change.url);
              if (real != null) {
                widget.sendLocationchanged(state, VEvent()..text = real);
              }
              _pushNavState(real);
            },
            onWebResourceError: (error) =>
                widget.sendProgresscompleted(state, null),
            // Mirror SWT's cancellable LocationListener.changing: ask Java
            // whether to proceed and honour event.doit.
            onNavigationRequest: (request) async {
              _locationChangingCompleter = Completer<bool>();
              widget.sendLocationchanging(
                  state, VEvent()..text = _unproxyUrl(request.url));
              final doit = await _locationChangingCompleter!.future.timeout(
                const Duration(seconds: 2),
                onTimeout: () {
                  _locationChangingCompleter = null;
                  return true;
                },
              );
              return doit
                  ? NavigationDecision.navigate
                  : NavigationDecision.prevent;
            },
          ),
        );
    // }
    _controller = controller;

    _onOp("locationchanging/result", (args) {
      final doit = (args as Map)['doit'] as bool? ?? true;
      final c = _locationChangingCompleter;
      _locationChangingCompleter = null;
      c?.complete(doit);
    });
    // Explicit navigation command (one per setUrl/setText, even same-URL).
    // Bypasses the state-driven dedup so re-navigation actually reloads.
    _onOp("navigate", (args) {
      final m = args as Map;
      final seq = (m["seq"] as num?)?.toInt() ?? 0;
      if (seq <= _lastNavSeq) return;
      _lastNavSeq = seq;
      _opNavigated = true;
      final url = m["url"] as String?;
      final text = m["text"] as String?;
      if (url != null && url.isNotEmpty) {
        final uri = Uri.tryParse(url);
        if (uri != null && uri.hasScheme) {
          _loadedUrl = url;
          _loadedText = null;
          final postData = m["postData"] as String?;
          if (postData != null) {
            // POST bypasses the proxy (which is GET-only); the web backend's
            // own XHR carries the body.
            _controller.loadRequest(
              uri,
              method: LoadRequestMethod.post,
              headers: _parseHeaders(m["headers"]),
              body: Uint8List.fromList(utf8.encode(postData)),
            );
          } else {
            final resolved = _resolveLoadUri(url, uri);
            _controller.loadRequest(resolved);
          }
        }
      } else if (text != null) {
        _loadedText = text;
        _loadedUrl = null;
        _controller.loadHtmlString(text);
      }
    });
    _onOp("back", (_) => _controller.goBack());
    _onOp("forward", (_) => _controller.goForward());
    _onOp("reload", (_) => _controller.reload());
    _onOp("execute", (args) {
      final script = (args as Map)["script"] as String?;
      if (script == null) return;
      if (_params != null) {
        // Web: DOM-level eval (works for same-origin/proxied content).
        try {
          browserEvalInFrame(_params, script);
        } catch (_) {}
        // The script may have set window.status (SWT StatusTextListener).
        _emitStatus();
      } else {
        _controller.runJavaScript(script);
      }
    });
    _onOp("registerFunction", (args) {
      final name = (args as Map)["name"] as String?;
      if (name == null) return;
      _functionNames.add(name);
      _injectBrowserFunction(name);
    });
    _onOp("unregisterFunction", (args) {
      final name = (args as Map)["name"] as String?;
      if (name == null) return;
      _functionNames.remove(name);
      if (_params != null) {
        try {
          browserEvalInFrame(_params, 'try{delete window[${jsonEncode(name)}];}catch(e){}');
        } catch (_) {}
      }
    });
    _onOp("evaluate", (args) async {
      final map = args as Map;
      final script = map["script"] as String?;
      final reqId = map["reqId"] as String?;
      if (script == null || reqId == null) return;
      try {
        final Object? result = _params != null
            ? browserEvalInFrame(_params, script)
            : await _controller.runJavaScriptReturningResult(script);
        // Carry the value as JSON so Java can restore the SWT evaluate() type
        // contract (null/Boolean/Double/String/Object[]); a plain toString()
        // would collapse every result to a String.
        String encoded;
        try {
          encoded = jsonEncode(result);
        } catch (_) {
          // Not JSON-representable (DOM node, function, cyclic): the SWT
          // contract has no mapping for it, so report JS null.
          encoded = 'null';
        }
        EquoCommService.sendPayload(
          "${state.swt}/${state.id}/evaluate/$reqId",
          VEvent()..text = encoded,
        );
      } catch (e) {
        EquoCommService.sendPayload(
          "${state.swt}/${state.id}/evaluate/$reqId",
          VEvent()..text = '__error__:$e',
        );
      }
    });

    _applyContent();
  }

  /// Emits the iframe's current document title (same-origin only) as an SWT
  /// TitleEvent, deduped against the last one pushed. Called on page load (with
  /// one short re-check, since a few pages set the title slightly after load).
  void _emitTitle() {
    if (_params == null) return;
    try {
      final t = browserEvalInFrame(_params, 'document.title');
      if (t is String && t.isNotEmpty && t != _lastTitle) {
        _lastTitle = t;
        widget.sendTitlechanged(state, VEvent()..text = t);
      }
    } catch (_) {}
  }

  /// Reads window.status from the same-origin iframe and emits StatusText on
  /// change. Driven from the execute path (the only place that can mutate
  /// window.status here) rather than a background poll.
  void _emitStatus() {
    if (_params == null) return;
    try {
      final s = browserEvalInFrame(_params, 'window.status');
      final str = s is String ? s : '';
      if (str != _lastStatus) {
        _lastStatus = str;
        widget.sendStatusTextchanged(state, VEvent()..text = str);
      }
    } catch (_) {}
  }

  /// The URL to report in Location/navigation events. With the proxy on, the
  /// iframe's own location is an opaque `/proxy?url=…` wrapper; the proxy injects
  /// a `<base href>` of the *final* fetched URL (after redirects), so the
  /// same-origin document's [baseURI] is the real address. Falls back to
  /// decoding the proxy wrapper, then to the raw URL.
  String? _resolveReportedUrl(String? rawUrl) {
    if (_params != null) {
      try {
        final b = browserEvalInFrame(_params, 'document.baseURI');
        if (b is String &&
            (b.startsWith('http://') || b.startsWith('https://')) &&
            !b.contains('/proxy?url=')) {
          return b;
        }
      } catch (_) {}
    }
    return _unproxyUrl(rawUrl);
  }

  /// Registers an op handler. The generated [onOp] assumes the comm payload is
  /// a JSON string, but the current comm layer delivers already-decoded objects
  /// for Map payloads (`FlutterBridge.send(..., Map.of(...))`). This variant
  /// tolerates either form.
  void _onOp(String op, void Function(dynamic) handler) {
    EquoCommService.onRaw("${state.swt}/${state.id}/$op",
        (raw) => handler(raw is String ? jsonDecode(raw) : raw));
  }

  /// Parses SWT-style request headers (a list of "Name: value" strings, as
  /// passed to Browser.setUrl(url, postData, headers)) into a header map.
  Map<String, String> _parseHeaders(dynamic raw) {
    final headers = <String, String>{};
    if (raw is List) {
      for (final h in raw) {
        final s = h.toString();
        final i = s.indexOf(':');
        if (i > 0) headers[s.substring(0, i).trim()] = s.substring(i + 1).trim();
      }
    }
    return headers;
  }

  /// Resolves the URI to actually load: routes external http(s) URLs through the
  /// same-origin /proxy endpoint when the proxy is enabled (so the iframe
  /// content is scriptable for execute/evaluate/BrowserFunction); otherwise
  /// loads directly. No-op on non-web.
  Uri _resolveLoadUri(String url, Uri uri) {
    if (browserProxyEnabled() && (uri.scheme == 'http' || uri.scheme == 'https')) {
      return Uri.parse(browserProxyRewrite(url));
    }
    return uri;
  }

  /// Reverses [_resolveLoadUri]: if [url] is one of our `/proxy?url=<orig>`
  /// URLs, returns the original target so Location events report the real URL
  /// rather than the proxy wrapper. Pass-through otherwise.
  String? _unproxyUrl(String? url) {
    if (url == null) return null;
    final uri = Uri.tryParse(url);
    final orig = uri?.queryParameters['url'];
    if (uri != null && uri.path.endsWith('/proxy') && orig != null && orig.isNotEmpty) {
      return orig;
    }
    return url;
  }

  /// Installs the JS shim for a registered BrowserFunction into the iframe's
  /// current document. The shim performs a *synchronous* XHR to the hosting
  /// server's /equo-browser-function endpoint, which dispatches to the Java
  /// callback on the SWT thread and returns its (JSON) result — so the page sees
  /// a normal blocking function call. Only meaningful for same-origin (proxied)
  /// content; on a cross-origin iframe the eval throws and is swallowed.
  void _injectBrowserFunction(String name) {
    if (_params == null) return;
    final nameLit = jsonEncode(name);
    final script = '''
      window[$nameLit] = function() {
        var xhr = new XMLHttpRequest();
        xhr.open('POST', location.origin + '/equo-browser-function', false);
        xhr.setRequestHeader('Content-Type', 'application/json');
        xhr.send(JSON.stringify({
          browserId: ${state.id},
          name: $nameLit,
          args: Array.prototype.slice.call(arguments)
        }));
        if (xhr.status !== 200) throw new Error('browser function failed: ' + xhr.status);
        var r = JSON.parse(xhr.responseText);
        if (r && r.error != null) throw new Error(r.error);
        return r ? r.value : undefined;
      };
    ''';
    try {
      browserEvalInFrame(_params, script);
    } catch (_) {}
  }

  /// Pushes the current SWT state (url or html text) into the webview. Driven
  /// from [extraSetState] whenever new state arrives from Java.
  void _applyContent() {
    // Initial render only: once the first navigate op arrives, navigation is
    // op-driven (see _onOp("navigate")) and incidental state pushes (bounds,
    // etc.) must not reload the page.
    if (_opNavigated) return;
    final url = state.url;
    // A freshly-created Browser has no URL yet (the SWT/VBrowser default is an
    // empty string, not null); Uri.parse('') yields a scheme-less uri that
    // loadRequest rejects, so only load once we have a real, absolute URL.
    final uri = (url != null && url.isNotEmpty) ? Uri.tryParse(url) : null;
    if (uri != null && uri.hasScheme && url != _loadedUrl) {
      _loadedUrl = url;
      _loadedText = null;
      final resolved = _resolveLoadUri(url!, uri);
      _controller.loadRequest(resolved);
    } else if (state.text != null && state.text != _loadedText) {
      _loadedText = state.text;
      _loadedUrl = null;
      _controller.loadHtmlString(state.text!);
    }
  }

  /// Pushes the webview's canGoBack/canGoForward to Java (via x/y = 0/1) so
  /// Browser.isBackEnabled()/isForwardEnabled() reflect the history, plus the
  /// resolved current [url] (in `text`) so Browser.getUrl() tracks history
  /// navigation (back/forward) and redirects, not just the last setUrl.
  Future<void> _pushNavState([String? url]) async {
    final back = await _controller.canGoBack();
    final forward = await _controller.canGoForward();
    EquoCommService.sendPayload(
      "${state.swt}/${state.id}/navigationState",
      VEvent()..x = back ? 1 : 0..y = forward ? 1 : 0..text = url,
    );
  }

  @override
  void extraSetState() {
    super.extraSetState();
    _applyContent();
  }

  @override
  Widget build(BuildContext context) {
    return tagSemantics(WebViewWidget(controller: _controller));
  }
}
