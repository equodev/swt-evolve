import 'package:webview_all/webview_all.dart'
    show PlatformWebViewControllerCreationParams;

/// Non-web platforms use the default WebView creation params (no iframe to
/// stamp), so this returns null and the caller falls back to `WebViewController()`.
PlatformWebViewControllerCreationParams? browserWebViewParams(int id) => null;

/// The same-origin proxy and DOM-level eval only apply to the web iframe
/// backend; on native platforms the webview's own controller handles JS.
bool browserProxyEnabled() => false;

String browserProxyRewrite(String url) => url;

String localFileRewrite(String tokenPath) => tokenPath;

Object? browserEvalInFrame(
        PlatformWebViewControllerCreationParams? params, String script) =>
    null;
