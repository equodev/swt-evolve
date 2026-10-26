import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/browser.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:webview_platform_interface/webview_platform_interface.dart';

/// A disposed Browser must not leave its op handlers registered: the comm layer would keep the
/// state, its WebViewController and so the native webview alive for the rest of the session. On
/// macOS it also stops the page the webview was loading.
void main() {
  setUp(() {
    _FakeController.calls.clear();
    WebViewPlatform.instance = _FakeWebViewPlatform();
  });

  testWidgets('a disposed Browser leaves no op handler behind and stops its load', (tester) async {
    debugDefaultTargetPlatformOverride = TargetPlatform.macOS;
    final browser = VBrowser()
      ..id = 77
      ..style = SWT.NONE
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 200
        ..height = 100);
    bool ownChannel(String c) => c.startsWith('Browser/77/');

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 200, height: 100, child: BrowserSwt(value: browser)),
    ));
    await tester.pump();
    expect(EquoCommService.commForTesting.handledChannels.where(ownChannel), isNotEmpty);

    _FakeController.calls.clear();
    await tester.pumpWidget(EvolveApp(theme: ThemeMode.light, contentWidget: const SizedBox()));
    await tester.pump();
    expect(EquoCommService.commForTesting.handledChannels.where(ownChannel), isEmpty);
    await tester.idle();
    expect(_FakeController.calls, ['setPlatformNavigationDelegate', 'loadRequest about:blank']);
    debugDefaultTargetPlatformOverride = null;
  });
}

class _FakeWebViewPlatform extends WebViewPlatform {
  @override
  PlatformWebViewController createPlatformWebViewController(
          PlatformWebViewControllerCreationParams params) =>
      _FakeController(params);

  @override
  PlatformNavigationDelegate createPlatformNavigationDelegate(
          PlatformNavigationDelegateCreationParams params) =>
      _FakeNavigationDelegate(params);

  @override
  PlatformWebViewWidget createPlatformWebViewWidget(
          PlatformWebViewWidgetCreationParams params) =>
      _FakeWebViewWidget(params);
}

class _FakeController extends PlatformWebViewController {
  _FakeController(super.params) : super.implementation();

  static final List<String> calls = [];

  @override
  Future<void> setJavaScriptMode(JavaScriptMode javaScriptMode) async {}
  @override
  Future<void> setPlatformNavigationDelegate(PlatformNavigationDelegate handler) async =>
      calls.add('setPlatformNavigationDelegate');
  @override
  Future<void> addJavaScriptChannel(JavaScriptChannelParams javaScriptChannelParams) async {}
  @override
  Future<void> loadRequest(LoadRequestParams params) async => calls.add('loadRequest ${params.uri}');
  @override
  Future<void> loadHtmlString(String html, {String? baseUrl}) async => calls.add('loadHtmlString');
  @override
  Future<String?> currentUrl() async => null;
  @override
  Future<String?> getTitle() async => null;
  @override
  Future<void> runJavaScript(String javaScript) async {}
  @override
  Future<Object> runJavaScriptReturningResult(String javaScript) async => '';
}

class _FakeNavigationDelegate extends PlatformNavigationDelegate {
  _FakeNavigationDelegate(super.params) : super.implementation();

  @override
  Future<void> setOnNavigationRequest(NavigationRequestCallback onNavigationRequest) async {}
  @override
  Future<void> setOnPageStarted(PageEventCallback onPageStarted) async {}
  @override
  Future<void> setOnPageFinished(PageEventCallback onPageFinished) async {}
  @override
  Future<void> setOnProgress(ProgressCallback onProgress) async {}
  @override
  Future<void> setOnWebResourceError(WebResourceErrorCallback onWebResourceError) async {}
  @override
  Future<void> setOnUrlChange(UrlChangeCallback onUrlChange) async {}
  @override
  Future<void> setOnHttpAuthRequest(HttpAuthRequestCallback onHttpAuthRequest) async {}
  @override
  Future<void> setOnHttpError(HttpResponseErrorCallback onHttpError) async {}
}

class _FakeWebViewWidget extends PlatformWebViewWidget {
  _FakeWebViewWidget(super.params) : super.implementation();

  @override
  Widget build(BuildContext context) => const SizedBox();
}
