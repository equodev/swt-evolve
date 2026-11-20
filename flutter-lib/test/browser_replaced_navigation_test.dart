// On Windows a navigation first asks Java's LocationListener.changing and waits for the answer. A
// setText that comes meanwhile replaces it: the page it shows must not be navigated away from once
// the earlier navigation's wait ends.
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/gen/browser.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:webview_platform_interface/webview_platform_interface.dart';

import 'support/channels.dart' as channels;

const _id = 9401;

void _navigate(Map<String, Object?> args) => EquoCommService.commForTesting
    .receiveBinary(channels.frame('Browser/$_id/navigate', TaggedValues.encode(args)));

void main() {
  late _FakeWebViewPlatform platform;

  setUp(() {
    platform = _FakeWebViewPlatform();
    WebViewPlatform.instance = platform;
  });

  testWidgets('a setText made while a setUrl waits on changing is the page that stays',
      (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 200, height: 100, child: BrowserSwt(value: _browser())),
      ),
    ));
    await tester.pump();

    _navigate({'url': 'https://example.com/', 'seq': 1});
    await tester.pump();
    _navigate({'text': '<p>replaces it</p>', 'seq': 2});
    await tester.pump();
    // Java never answers here, so the setUrl's wait ends on its timeout.
    await tester.pump(const Duration(seconds: 1));

    expect(platform.loads, ['html'], reason: 'the replaced setUrl was still loaded');
  }, variant: TargetPlatformVariant.only(TargetPlatform.windows));
}

VBrowser _browser() => VBrowser()
  ..id = _id
  ..style = SWT.NONE
  ..visible = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 100);

class _FakeWebViewPlatform extends WebViewPlatform {
  final List<String> loads = [];

  @override
  PlatformWebViewController createPlatformWebViewController(
          PlatformWebViewControllerCreationParams params) =>
      _FakeController(params, loads);

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
  _FakeController(super.params, this.loads) : super.implementation();

  final List<String> loads;

  @override
  Future<void> setJavaScriptMode(JavaScriptMode javaScriptMode) async {}
  @override
  Future<void> setPlatformNavigationDelegate(PlatformNavigationDelegate handler) async {}
  @override
  Future<void> addJavaScriptChannel(JavaScriptChannelParams javaScriptChannelParams) async {}
  @override
  Future<void> loadRequest(LoadRequestParams params) async => loads.add('${params.uri}');
  @override
  Future<void> loadHtmlString(String html, {String? baseUrl}) async => loads.add('html');
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
  Widget build(BuildContext context) => const SizedBox.expand();
}
