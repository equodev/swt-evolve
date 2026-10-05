import 'package:webview_all/webview_all.dart'
    show
        PlatformWebViewControllerCreationParams,
        WebViewController,
        WebViewCookie,
        WebViewCookieManager;
import 'package:webview_all_windows/webview_all_windows.dart';

/// Non-web platforms use the default WebView creation params (no iframe to
/// stamp), so this returns null and the caller falls back to `WebViewController()`.
PlatformWebViewControllerCreationParams? browserWebViewParams(int id) => null;

/// The same-origin proxy and DOM-level eval only apply to the web iframe
/// backend; on native platforms the webview's own controller handles JS.
bool browserProxyEnabled(String url) => false;

String browserAppBasePath() => '/';

String browserAppBaseUrl() => '';

String browserProxyRewrite(String url, [Map<String, String>? headers]) => url;

String localFileRewrite(String tokenPath) => tokenPath;

String localFileBaseRewrite(String? basePath) => basePath ?? '';

Object? browserEvalInFrame(
        PlatformWebViewControllerCreationParams? params, String script) =>
    null;

/// Only the web iframe needs a same-origin URL for inline content; native webviews render
/// `loadHtmlString` scriptably on their own.
String? browserInlineDocumentUrl(String html, String? baseUrl) => null;

void browserRevokeInlineDocumentUrl(String url) {}

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

/// Releases the WebView2 behind [controller]. Removing the WebView widget does not, so without
/// this every disposed Browser keeps its WebView2 until the engine shuts down.
Future<void> browserReleaseWebView(WebViewController controller) async {
  final platform = controller.platform;
  if (platform is WindowsWebViewController) await platform.dispose();
}

/// The value of cookie [name] for [url], HttpOnly cookies included; null when there is none or
/// the platform cannot read cookies.
Future<String?> browserGetCookie(String name, String url) async {
  final uri = Uri.tryParse(url);
  if (uri == null) return null;
  try {
    final cookies = await WebViewCookieManager().platform.getCookies(uri);
    for (final cookie in cookies) {
      if (cookie.name == name) return cookie.value;
    }
  } catch (_) {
    // Not implemented on this platform.
  }
  return null;
}

/// Sets a cookie; [expires] is in seconds since the epoch, null for a session cookie. Only
/// Windows honours [secure] and [httpOnly]. Returns whether the cookie was set.
Future<bool> browserSetCookie(String name, String value, String domain, String path,
    {double? expires, bool secure = false, bool httpOnly = false}) async {
  try {
    final platform = WebViewCookieManager().platform;
    if (platform is WindowsWebViewCookieManager) {
      await platform.setWindowsCookie(WindowsWebViewCookie(
          name: name,
          value: value,
          domain: domain,
          path: path,
          expires: expires == null
              ? null
              : DateTime.fromMillisecondsSinceEpoch((expires * 1000).round()),
          isSecure: secure,
          isHttpOnly: httpOnly));
    } else {
      await platform.setCookie(
          WebViewCookie(name: name, value: value, domain: domain, path: path));
    }
    return true;
  } catch (_) {
    return false;
  }
}
