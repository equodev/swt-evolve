import 'package:webview_all/webview_all.dart'
    show PlatformWebViewControllerCreationParams;

/// Non-web platforms use the default WebView creation params (no iframe to
/// stamp), so this returns null and the caller falls back to `WebViewController()`.
PlatformWebViewControllerCreationParams? browserWebViewParams(int id) => null;

/// The same-origin proxy and DOM-level eval only apply to the web iframe
/// backend; on native platforms the webview's own controller handles JS.
bool browserProxyEnabled(String url) => false;

String browserProxyRewrite(String url) => url;

String localFileRewrite(String tokenPath) => tokenPath;

String localFileBaseRewrite(String? basePath) => basePath ?? '';

Object? browserEvalInFrame(
        PlatformWebViewControllerCreationParams? params, String script) =>
    null;

/// No iframe to listen to on non-web platforms; desktop webviews get their
/// re-injection from the `onPageFinished` navigation delegate instead.
void browserOnFrameLoad(dynamic params, void Function() onLoad) {}

/// Only the web iframe swallows the keys typed inside it; native webviews route them through
/// Flutter's keyboard.
bool installBrowserFrameKeyHandling(
        PlatformWebViewControllerCreationParams? params,
        void Function(String key, bool ctrl, bool shift, bool alt, bool meta,
                bool down)
            onKey) =>
    false;
