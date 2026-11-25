import 'dart:convert';
import 'dart:io';

import 'package:flutter/foundation.dart'
    show TargetPlatform, debugPrint, defaultTargetPlatform;
import 'package:webview_all/webview_all.dart'
    show
        NavigationDelegate,
        PlatformWebViewControllerCreationParams,
        WebViewController,
        WebViewCookie,
        WebViewCookieManager;
import 'package:webview_all_windows/webview_all_windows.dart';

import 'browser_inline_documents.dart' show injectBaseUrl;

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

/// The largest document WebView2's NavigateToString, behind `loadHtmlString` on Windows, accepts;
/// over it, it loads nothing at all. The string reaches it as UTF-16.
const int _maxStringDocumentBytes = 2 * 1024 * 1024;

int _documentFiles = 0;

Directory get _documentsDirectory => Directory(
    '${Directory.systemTemp.path}${Platform.pathSeparator}evolve-browser-documents-$pid');

/// Native webviews render `loadHtmlString` scriptably on their own, so a `setText` document needs
/// no URL, except one too large for WebView2: that one is written to a file and loaded from it, as
/// SWT's Edge loads every `setText` document.
Future<String?> browserInlineDocumentUrl(String html, String? baseUrl) async {
  if (defaultTargetPlatform != TargetPlatform.windows ||
      html.length * 2 <= _maxStringDocumentBytes) {
    return null;
  }
  final directory = await _documentsDirectory.create(recursive: true);
  final file = File('${directory.path}${Platform.pathSeparator}${++_documentFiles}.html');
  // The BOM makes the webview read the file as UTF-8 whatever the document declares.
  await file.writeAsBytes([0xEF, 0xBB, 0xBF, ...utf8.encode(injectBaseUrl(html, baseUrl))]);
  return file.uri.toString();
}

String browserInlineDocumentEndpoint() {
  if (defaultTargetPlatform != TargetPlatform.windows) return '';
  final directory = Uri.directory(_documentsDirectory.path).toString();
  return directory.substring(0, directory.length - 1);
}

void browserRevokeInlineDocumentUrl(String url) {
  try {
    File.fromUri(Uri.parse(url)).deleteSync();
  } on FileSystemException catch (e) {
    debugPrint('Browser: could not delete a setText document file: $e');
  }
}

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

/// Native webviews report in-frame link navigations to their navigation delegate.
bool installBrowserFrameNavigationHandling(
        PlatformWebViewControllerCreationParams? params,
        void Function(String url, void Function() proceed) onNavigate) =>
    false;

/// Releases the native webview behind [controller]; removing the WebView widget does not. Windows
/// disposes its WebView2. On macOS the webview lives until the controller is collected and keeps
/// loading meanwhile, and a response arriving after its engine is gone aborts the process, so the
/// load is stopped.
Future<void> browserReleaseWebView(WebViewController controller) async {
  final platform = controller.platform;
  if (platform is WindowsWebViewController) {
    await platform.dispose();
    return;
  }
  if (defaultTargetPlatform != TargetPlatform.macOS) return;
  try {
    // A fresh delegate, so the blank load reports nothing for a Browser that is gone.
    await controller.setNavigationDelegate(NavigationDelegate());
    await controller.loadRequest(Uri.parse('about:blank'));
  } catch (e) {
    debugPrint('Browser: could not stop a disposed webview: $e');
  }
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
